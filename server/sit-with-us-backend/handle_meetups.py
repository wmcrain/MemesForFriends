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

        if meetup.search_entity is None:
            entity = SearchEntity(latitude=float(latitude), longitude=float(longitude))

            meetup.search_entity = entity.key
            meetup.put()
            entity.put()

        return { Keys.SUCCESS : 1, Keys.MEETUP_KEY : meetup.key.urlsafe() }


class StopMeetupSearchHandler(ApiHandler):
    def handle(self):
        meetup_key = self.getParam(Keys.MEETUP_KEY)
        meetup = ndb.Key(urlsafe=meetup_key).get()
        meetup.search_entity.delete()

        return { Keys.SUCCESS : 1 }


class UpdateMeetupSearchHandler(ApiHandler):
    def handle(self):
        meetup_key = self.getParam(Keys.MEETUP_KEY)

        # Retrieve the current members of the meetup

        return { Keys.SUCCESS : 1 }

class LeaveMeetup(ApiHandler):
    def handle(self):
        pass