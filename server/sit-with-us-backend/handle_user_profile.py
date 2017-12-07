from handle import ApiHandler
from handle import Keys
from models import User
import webapp2

from google.appengine.ext import ndb


class GetProfileHandler(ApiHandler):
    def handle(self):
        usernames = self.getParam(Keys.USERNAME)
        self_user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()

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

            if user.key in self_user.friends: 
                properties.update({ Keys.PHONE_NUMBER : user.phone })

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
            Keys.USERNAME : list(set([x.get().username for x in user.friends if not x.get() is None]))
        }

def remove_friend(user, other):
    if user.key in other.pending_friends:
        other.pending_friends.remove(user.key)
        other.put()
    elif other.key in user.friends:
        user.friends.remove(other.key)
        if user.key in other.friends:
            other.friends.remove(user.key)
        other.put()
        user.put()


class RemoveFriendHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()
        other = ndb.Key(urlsafe=self.getParam(Keys.PENDING_MATCH)).get()
        remove_friend(user, other)

        return { Keys.SUCCESS : 1 }

class RemoveAllFriends(webapp2.RequestHandler):
    def get(self):
        users = User.query()
        for user in users:
            user.friends = []
            user.pending_friends = []
            user.blocked = []
            user.put()

        self.response.set_status(200)
        self.response.write("Good")


class ToggleFriendHandler(ApiHandler):
    def handle(self):
        user = ndb.Key(urlsafe=self.getParam(Keys.USER_KEY)).get()
        other = ndb.Key(urlsafe=self.getParam(Keys.PENDING_MATCH)).get()
        confirmed = int(self.getParam(Keys.CONFIRMED)) == 1

        if confirmed:
            if not user.key in other.pending_friends:
                other.pending_friends.append(user.key)
                other.put()
        elif user.key in other.pending_friends:
            other.pending_friends.remove(user.key)
            other.put()

        if user.key in other.pending_friends and other.key in user.pending_friends:
            user.pending_friends.remove(other.key)
            other.pending_friends.remove(user.key)

            user.friends.append(other.key)
            other.friends.append(user.key)

            user.put()
            other.put()
        
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

            remove_friend(user, block_user_key.get())
        
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

