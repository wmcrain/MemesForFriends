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
    def handle(self):
        search_key = self.getParam(Keys.SEARCH_KEY)
        latitude = self.getParam(Keys.LATITUDE)
        longitude = self.getParam(Keys.LONGITUDE)

        willing_matches = self.getParam(Keys.WILLING_MATCHES)

        # Retrieve the meetup of the user
        search_entity = ndb.Key(urlsafe=search_key).get()
        meetup = search_entity.meetup.get()

        # Set the people 
        search_entity.willing_matches = [ndb.Key(urlsafe=x) for x in willing_matches]

        MAX_MATCH_DISTANCE = 5 #km

        # Retrieve all the people searching in the area
        potential_matches = [x for x in SearchEntity.query(SearchEntity.key != search_entity.key)
            if x.distance(search_entity) < MAX_MATCH_DISTANCE]

        # 
        formatted_matches = []
        for match in potential_matches:
            formatted_matches.append({
                Keys.SEARCH_KEY : match.key.urlsafe(),
                Keys.DISTANCE : match.distance(search_entity),
                Keys.USERNAME : [x.get().username for x in match.meetup.get().current_users],
                Keys.WILLING_MATCHES : search_entity.key in match.willing_matches
            })

        return { 
            Keys.SUCCESS : 1,
            Keys.MATCHES : formatted_matches
        }

class LeaveMeetup(ApiHandler):
    def handle(self):
        pass