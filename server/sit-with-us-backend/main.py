#!/usr/bin/env python
import webapp2

from handle import ApiHandler
import handle_user_account

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
], debug=True)
