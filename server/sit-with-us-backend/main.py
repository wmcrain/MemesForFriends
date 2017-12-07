#!/usr/bin/env python
import webapp2

from handle import ApiHandler
import handle_user_account
import handle_user_profile
import handle_meetups

class MainHandler(ApiHandler):
    def handle(self):
        return {"response" : "Hello world!"}

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/create', handle_user_account.CreateUserHandler),
    ('/create/verify', handle_user_account.CreateVerifyUserHandler),
    ('/login', handle_user_account.LoginUserHandler),
    ('/login/verify', handle_user_account.LoginEmailHandler),
    ('/login/ping', handle_user_account.LoginPingHandler),
    ('/profile/get', handle_user_profile.GetProfileHandler),
    ('/profile/set', handle_user_profile.SetProfileHandler),
    ('/profile/block/get', handle_user_profile.GetBlockListHandler),
    ('/profile/block/add', handle_user_profile.AddBlockHandler),
    ('/profile/block/remove', handle_user_profile.RemoveBlockHandler),
    ('/meetup/history', handle_meetups.MeetupHistoryHandler),
    ('/meetup/search/start', handle_meetups.StartMeetupSearchHandler),
    ('/meetup/search/stop', handle_meetups.StopMeetupSearchHandler),
    ('/meetup/search/update', handle_meetups.UpdateMeetupSearchHandler),
    ('/meetup/search/confirm', handle_meetups.ConfirmMeetupMatchHandler),
    ('/meetup/start', handle_meetups.StartMeetupHandler),
    ('/meetup/leave', handle_meetups.LeaveMeetupHandler),
    ('/meetup/update', handle_meetups.UpdateMeetupHandler),
    ('/friends/get', handle_user_profile.GetFriendsHandler),
    ('/friends/toggle', handle_user_profile.ToggleFriendHandler),
    ('/friends/remove', handle_user_profile.RemoveFriendHandler),
    ('/friends/remove/all/danger', handle_user_profile.RemoveAllFriends),
], debug=True)
