from handle import ApiHandler
from handle import Keys
from models import User
from models import Meetup
from models import SearchEntity

from google.appengine.ext import ndb

class StartMeetupSearchHandler(ApiHandler):
    def handle(self):
        # 
        user_key = self.getParam(Keys.USER_KEY)
        latitude = self.getParam(Keys.LATITUDE)
        longitude = self.getParam(Keys.LONGITUDE)

        user = ndb.Key(urlsafe=user_key).get()

        # If the user is not in a meetup, place them in one
        meetup = None
        if user.current_meetup is None:
            # Add the user as a current user in the meetup
            meetup = Meetup(current_users=[user.key])
            meetup.put()

            # Specify the current meetup the user is in
            user.current_meetup = meetup.key
            user.put()
        else: 
            meetup = user.current_meetup.get()

        entity = SearchEntity(meetup=meetup.key, latitude=float(latitude), longitude=float(longitude))
        entity.put()

        return { Keys.SUCCESS : 1, Keys.SEARCH_KEY : entity.key.urlsafe() }


class StopMeetupSearchHandler(ApiHandler):
    def handle(self):
        search_entity_key = self.getParam(Keys.SEARCH_KEY)
        search_entity = ndb.Key(urlsafe=search_entity_key).delete()

        return { Keys.SUCCESS : 1 }

class UpdateMeetupSearchHandler(ApiHandler):
    """
    """
    def handle(self):
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
            return { 
                Keys.SUCCESS : 1, 
                Keys.CONFIRMED : 1, 
                Keys.MEETUP_KEY : search_entity.meetup.urlsafe()
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
            result.update({ Keys.PENDING_MATCH : search_entity.pending_match.key.urlsafe()})

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
            search_entity.willing_matches = search_entity.willing_matches.remove(other_entity.key)
            other_entity.willing_matches = other_entity.willing_matches.remove(search_entity.key)

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
                user = user_key.get()
                current_meetup = user.current_meetup.get()

                # Add the meetup to the meetup history if there was another user besides the 
                # current user in it
                if len(current_meetup.current_users) + len(current_meetup.previous_users) > 1:
                    user.previous_meetups.append(current_meetup.key)

                # Set the current meetup of the user to the new meetup
                user.current_meetup = meetup.key
                user.put()

            # 
            search_entity.confirmed_status = SearchEntity.Status.CONFIRMED
            other_entity.confirmed_status = SearchEntity.Status.CONFIRMED

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
        user_key = self.getParam(Keys.USER_KEY)
        user = ndb.Key(urlsafe=search_key).get()

        history = []
        for meetup_key in user.previous_meetups:
            meetup = meetup_key.get()
            history.append({
                Keys.USERNAME : [x.get().username for x in meetup.previous_users],
                Keys.TIME_FORMED : meetup.time_formed
            })

        return { 
            Keys.SUCCESS : 1,
            Keys.HISTORY : history
        }  

class UpdateMeetupHandler(ApiHandler):
    def handle(self):
        meetup_key = self.getParam(Keys.MEETUP_KEY)

        meetup = ndb.Key(urlsafe=meetup_key).get()

        usernames = []
        for user in meetup.current_meetup:
            usernames.append(user.get().username)

        return {
            Keys.SUCCESS : 1,
            Keys.USERNAME : usernames,
        }

class LeaveMeetup(ApiHandler):
    def handle(self):
        pass