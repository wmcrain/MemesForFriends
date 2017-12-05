from handle import ApiHandler
from handle import Keys
from models import User

from google.appengine.ext import ndb


class GetProfileHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        usernames = self.getParam(Keys.USERNAME)

        profiles = []

        # 
        for username in usernames:
            user_entries = User.query(User.username == username).fetch()
            if len(user_entries) == 0:
                return { Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "Could not find user " + username }

            user = user_entries[0]

            # Do not show the user's profile if the other user has blocked them

            properties = { 
                Keys.USER_KEY : user.key.urlsafe(),
                Keys.USERNAME : user.username,
                Keys.FIRST_NAME : user.first_name,
                Keys.LAST_NAME : user.last_name,
                Keys.BIO : user.bio,
            }

            if user.picture is not None:
                properties.update({ Keys.PICTURE : user.picture })

            profiles.append(properties)

        # Add the phone number if the users are friends through the app

        return { Keys.SUCCESS : 1, Keys.PROFILE : profiles }

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

class GetFriendsHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        user = ndb.Key(urlsafe=user_key).get()

        return { 
            Keys.SUCCESS : 1, 
            Keys.USERNAME : [x.username for x in user.query().fetch()]
        }

class ToggleFriendHandler(ApiHandler):
    def handle(self):
        
        return { Keys.SUCCESS : 1 }


class GetBlockListHandler(ApiHandler):
    def handle(self):
        user_key = self.getParam(Keys.USER_KEY)
        user = ndb.Key(urlsafe=user_key).get()

        # Add the the usernames and keys of the blocked users to the results
        blocked_users = []
        for blocked_user_key in user.blocked:
            blocked_user = blocked_user_key.get()
            blocked_users.append({
                Keys.USER_KEY : blocked_user_key.urlsafe(),
                Keys.USERNAME : blocked_user.username,
                Keys.FIRST_NAME : blocked_user.first_name,
                Keys.LAST_NAME : blocked_user.last_name
            })

        return {
            Keys.SUCCESS : 1, 
            Keys.BLOCKED : blocked_users,
        }
        

class AddBlockHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()
        block_user_key = ndb.Key(urlsafe=self.getParam(Keys.BLOCKED))

        # Add the other user to this user's block list if the other user exists
        if block_user_key is not None and not block_user_key in user.blocked:
            user.blocked.append(block_user_key)
            user.put()
        
        return { Keys.SUCCESS : 1 }


class RemoveBlockHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()
        block_user_key = ndb.Key(urlsafe=self.getParam(Keys.BLOCKED))

        # Add the other user to this user's block list if the other user exists
        if block_user_key is not None and block_user_key in user.blocked:
            user.blocked.remove(block_user_key)
            user.put()
        
        return { Keys.SUCCESS : 1 }

