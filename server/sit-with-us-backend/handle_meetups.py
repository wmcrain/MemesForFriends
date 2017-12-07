from handle import ApiHandler
from handle import Keys
from models import User
from models import Meetup
from models import SearchEntity

from google.appengine.ext import ndb

class StartMeetupSearchHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        latitude = self.getParam(Keys.LATITUDE)
        longitude = self.getParam(Keys.LONGITUDE)
        user = ndb.Key(urlsafe=user_key).get()

        entity = None
        # Return the existing search entity for the meetup if one exists
        search_entities = SearchEntity.query(SearchEntity.meetup == user.current_meetup).fetch()
        if len(search_entities) > 0:
            entity = search_entities[0]

        # Create a new search entity for the meetup
        else:
            entity = SearchEntity(meetup=user.current_meetup, latitude=float(latitude), 
                longitude=float(longitude))
            entity.put()

        if not user.key in entity.searching_users:
            entity.searching_users.append(user.key)
            entity.put()

        return { Keys.SUCCESS : 1, Keys.SEARCH_KEY : entity.key.urlsafe() }

def join_meetup(meetup, user_key):
    if user_key in meetup.previous_users:
        meetup.previous_users.remove(user_key)

    if user_key not in meetup.current_users:
        meetup.previous_users.append(user_key)

    meetup.put()

    user = user_key.get()
    user.current_meetup = meetup.key
    user.put()

def leave_meetup(user_key):
    user = user_key.get()

    if not user.current_meetup is None and not user.current_meetup.get() is None:
        meetup = user.current_meetup.get()

        # Move the user to the previous users in the meetup
        if user_key in meetup.current_users:
            meetup.current_users.remove(user_key)
            meetup.previous_users.append(user_key)
            meetup.put()

        if len(meetup.current_users) == 0:
            entities = SearchEntity.query(SearchEntity.meetup == meetup.key).fetch()
            for entity in entities:
                entity.key.delete()

        # Add the meetup to the user's meetup history if there was another user in it besides the 
        # current user
        if len(meetup.current_users) + len(meetup.previous_users) > 1:
            user.previous_meetups.append(meetup.key)
        else:
            meetup.key.delete()

        # 
        user.current_meetup = None
        user.put()


def remove_searching_user(search_entity, user_key):
    # 
    if user_key in search_entity.searching_users:
        search_entity.searching_users.remove(user_key)
        search_entity.put()

    # Remove the meetup from matching if all users have stopped searching
    if len(search_entity.searching_users) == 0:
        search_entity.key.delete()


class StopMeetupSearchHandler(ApiHandler):
    def handle(self):
        user_key = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY))
        search_entity = ndb.Key(urlsafe=self.getParam(Keys.SEARCH_KEY)).get()

        remove_searching_user(search_entity, user_key)
        return { Keys.SUCCESS : 1 }

class UpdateMeetupSearchHandler(ApiHandler):
    """
    """
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        search_key = self.getParam(Keys.SEARCH_KEY)
        latitude = self.getParam(Keys.LATITUDE)
        longitude = self.getParam(Keys.LONGITUDE)
        willing_matches = self.getParam(Keys.WILLING_MATCHES)

        # Retrieve the meetup of the user
        search_entity = ndb.Key(urlsafe=search_key).get()
        search_entity.latitude = latitude
        search_entity.longitude = longitude

        # Return the new meetup key if a match was successful
        if search_entity.pending_match_status == SearchEntity.Status.CONFIRMED:
            remove_searching_user(search_entity, user_key)
            return { 
                Keys.SUCCESS : 1, 
                Keys.CONFIRMED : 1,
            }

        # TODO: Block list
        meetup = search_entity.meetup.get()

        # Set the people 
        search_entity.willing_matches = [ndb.Key(urlsafe=x) for x in willing_matches]
        search_entity.put()

        MAX_MATCH_DISTANCE = 5 #km

        # Retrieve all the people searching in the area
        potential_matches = [x for x in SearchEntity.query(SearchEntity.key != search_entity.key)
            if x.distance(search_entity) < MAX_MATCH_DISTANCE]

        # Retrieve a list of all the nearby meetups that are also searching
        formatted_matches = []
        for match in potential_matches:
            formatted_matches.append({
                Keys.SEARCH_KEY : match.key.urlsafe(),
                Keys.DISTANCE : match.distance(search_entity),
                Keys.USERNAME : [x.get().username for x in match.meetup.get().current_users],
            })

            # If both entities are willing to match with each other, then set them as each other's 
            # pending match
            if (match.pending_match is None and search_entity.pending_match is None
                and match.key in search_entity.willing_matches 
                and search_entity.key in match.willing_matches):

                match.pending_match = search_entity.key
                match.pending_match_status = SearchEntity.Status.PENDING
                match.put()

                search_entity.pending_match = match.key
                search_entity.pending_match_status = SearchEntity.Status.PENDING
                search_entity.put()

        result = { 
            Keys.SUCCESS : 1,
            Keys.MATCHES : formatted_matches,
        }

        # Add the pending match search entity key to the result if a match is currently pending
        if search_entity.pending_match is not None:
            result.update({ Keys.PENDING_MATCH : search_entity.pending_match.urlsafe()})

        return result


class ConfirmMeetupMatchHandler(ApiHandler):
    def handle(self):
        search_key = self.getParam(Keys.SEARCH_KEY)
        confirmed = int(self.getParam(Keys.CONFIRMED)) == 1

        # 
        search_entity = ndb.Key(urlsafe=search_key).get()

        # Attempt to retrieve the key of the other user
        other_entity = None
        try:
            other_entity = search_entity.pending_match.get()
        except e:
            return { Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "Does not exist" }

        # Set that this user has denied matching with the other user if the other user 
        # has not declined yet
        if not confirmed and other_entity.pending_match == search_entity.key:
            search_entity.pending_match = None
            other_entity.pending_match = None

            search_entity.pending_match_status = None
            other_entity.pending_match_status = None

            # Remove the users from each other's willing to match with lists
            search_entity.willing_matches.remove(other_entity.key)
            other_entity.willing_matches.remove(search_entity.key)

            search_entity.put()
            other_entity.put()

        
        # Set that this user has approved matching with the other user
        # Do not set this if the other user has already denied this user because the denial
        # must still be relayed to the user
        if confirmed and other_entity.pending_match == search_entity.key: 
            search_entity.pending_match_status = SearchEntity.Status.APPROVED
            search_entity.put()

        # Match the two meetups into a new meetup if they both approve each other
        if other_entity.pending_match_status == SearchEntity.Status.APPROVED:

            # Retrieve all the users from both meetups
            users = []
            for user_key in search_entity.meetup.get().current_users:
                users.append(user_key)
            for user_key in other_entity.meetup.get().current_users:
                users.append(user_key)

            # 
            meetup = Meetup(current_users=users)
            meetup.put()

            # Set the current meetup of the users to the new meetup
            for user_key in users:
                leave_meetup(user_key)
                join_meetup(meetup, user_key)

            # 
            search_entity.pending_match_status = SearchEntity.Status.CONFIRMED
            other_entity.pending_match_status = SearchEntity.Status.CONFIRMED

            #
            search_entity.meetup = meetup.key
            other_entity.meetup = meetup.key

            search_entity.put()
            other_entity.put()

        return { 
            Keys.SUCCESS : 1
        }


class MeetupHistoryHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()

        history = []
        for meetup_key in user.previous_meetups:

            if not meetup_key is None and not meetup_key.get() is None:
                meetup = meetup_key.get()
                history.append({
                    Keys.USERNAME : [x.get().username for x in meetup.previous_users],
                    Keys.TIME_FORMED : meetup.time_formed.strftime("%Y-%m-%d %H:%M:%S"),
                })

        return { 
            Keys.SUCCESS : 1,
            Keys.HISTORY : history
        }  

class StartMeetupHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()

        # Retrieve the meetup the user is currently in or create a new meetup if they are not
        # currently in a meetup
        meetup = None
        if user.current_meetup is None or user.current_meetup.get() is None:

            # Add the user as a current user in the meetup
            meetup = Meetup(current_users=[user.key])
            meetup.put()

            # Specify the current meetup the user is in
            user.current_meetup = meetup.key
            user.put()
        else: 
            meetup = user.current_meetup.get()

        # Return the meetup key to the user
        return {
            Keys.SUCCESS : 1
        }

class UpdateMeetupHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()

        usernames = []
        for members in user.current_meetup.get().current_users:
            usernames.append(members.get().username)

        return {
            Keys.SUCCESS : 1,
            Keys.USERNAME : usernames,
        }

class LeaveMeetupHandler(ApiHandler):
    def handle(self):
        user_key = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY))

        leave_meetup(user_key)

        # Return the meetup key to the user
        return {
            Keys.SUCCESS : 1
        }