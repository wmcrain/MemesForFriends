from handle import ApiHandler
from handle import Keys
from models import User

from google.appengine.ext import ndb


class GetProfileHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        username = self.getParam(Keys.USERNAME)

        # 
        user_entries = User.query(User.username == username).fetch()
        if len(user_entries) == 0:
            return { Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "Could not find user" }

        user = user_entries[0]

        # Do not show the user's profile if the other user has blocked them

        properties = { 
            Keys.SUCCESS : 1,
            Keys.USERNAME : user.username,
            Keys.FIRST_NAME : user.first_name,
            Keys.LAST_NAME : user.last_name,
            Keys.BIO : user.bio,
            Keys.PICTURE : user.picture,
        }

        # Add the phone number if the users are friends through the app

        return properties

class SetProfileHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        user = ndb.Key(urlsafe=user_key).get()

        if self.hasParam(Keys.BIO):
            user.bio = self.getParam(Keys.BIO)

        if self.hasParam(Keys.PICTURE):
            user.picture = self.getParam(Keys.PICTURE)

        user.put()

        return { Keys.SUCCESS : 1 }

class ToggleFriendHandler(ApiHandler):
    def handle(self):
        
        return { Keys.SUCCESS : 1 }
