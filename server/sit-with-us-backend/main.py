#!/usr/bin/env python

import json
import keys
import urllib
import webapp2

from models import User

from google.appengine.api import app_identity
from google.appengine.api import mail
from google.appengine.ext import ndb
from webapp2_extras import json as JSON

# The encoding format of the json requests
STRING_FORMAT = 'utf-8'

class MissingParameter(Exception):
    """ Thrown when a parameter is missing from the JSON data of the request. """
    pass

class ApiHandler(webapp2.RequestHandler):
    def post(self):
        """ Handles when an HTTP POST call is made on the path the handler is mapped to. """
        try:
            # Extract a json object containing the provided parameters
            self.request.json = json.loads(self.request.body.decode(STRING_FORMAT))

            # Call the method used to handle the api call
            self.handle()
        except MissingParameter as e:

            # Report that a error happened server size and return the error message
            self.response.set_status(400)
            self.response.write(json.dumps({ keys.SUCCESS : 0, keys.ERROR_MESSAGE : str(e) }))

    def getParam(self, param):
        """ Retrieves the parameter data from the JSON data of the request. """
        if not param in self.request.json:
            raise MissingParameter('Paramater \"' + param + '\" not provided')
        return self.request.json[param]


class MainHandler(ApiHandler):
    def handle(self):
        self.response.set_status(200)
        self.response.write('{"response" : "Hello world!"}')


class CreateUserHandler(ApiHandler):
    def handle(self):
        self.response.set_status(200)
        self.response.write(json.dumps(
            User.handle_create(
                self.getParam(keys.USERNAME), 
                self.getParam(keys.PASSWORD), 
                self.getParam(keys.EMAIL), 
                self.getParam(keys.PHONE_NUMBER))))

class CreateConfirmUserHandler(webapp2.RequestHandler):
    def get(self):
        self.response.set_status(200)
        self.response.write(User.handle_verify(self.request.get(keys.USER_KEY)))


class LoginUserHandler(ApiHandler):
    def handle(self):
        self.response.set_status(200)
        self.response.write(json.dumps(User.handle_login(
            self.getParam(keys.USERNAME), 
            self.getParam(keys.PASSWORD))))

app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/create', CreateUserHandler),
    ('/createconfirm', CreateConfirmUserHandler),
    ('/login', LoginUserHandler)
], debug=True)
