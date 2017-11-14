import jinja2
import json
import os
import webapp2

# The email from which emails will be sent to users
AUTHORIZED_EMAIL = 'Sit With Me <ryanmitch16@gmail.com>'

# 
DOMAIN = 'sit-with-us-backend.appspot.com'

STRING_FORMAT = 'utf-8'

class Keys():
    """ The enumeration of keys that map to values in JSON request and responses. """
    DEVICE_KEY = "device_key"
    EMAIL = "email"
    ERROR_MESSAGE = "error"
    PASSWORD = "password"
    PHONE_NUMBER = "phone"
    SUCCESS = "success"
    USERNAME = "username"
    USER_KEY = "user_key"
    LINK_PARAM = "p"
    FIRST_NAME = "first_name"
    LAST_NAME = "last_name"
    BIO = "bio"
    PICTURE = "picture"
    PROFILE = "profile"
    SEARCH_ENTITY_KEY = "seach_entity_key"
    MEETUP_KEY = "meetup_key"
    LATITUDE = "latitude"
    LONGITUDE = "longitude"


class Links():
    """ """
    LINK_CREATE = "create/verify"
    LINK_LOGIN = "login/verify"

class MissingParameter(Exception):
    """ Thrown when a parameter is missing from the request JSON. """
    pass


class ApiHandler(webapp2.RequestHandler):
    def post(self):
        """ Handles when an HTTP POST call is made on the path the handler is mapped to. """
        try:
            # Extract a json object containing the provided parameters
            self.request.json = json.loads(self.request.body.decode(STRING_FORMAT))

            # Call the method used to handle the api call
            self.response.set_status(200)
            self.response.write(json.dumps(self.handle()) + '\n')

        except MissingParameter as e:

            # Report that a error happened server size and return the error message
            self.response.set_status(400)
            self.response.write(json.dumps({ Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : str(e) }))

    def handle(self):
        """ """
        self.response.set_status(400)
        self.response.write(json.dumps({ 
            Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "No handle implementation found"
        }))

    def getParam(self, param):
        """ Retrieves the parameter data from the JSON data of the request. """
        if param in self.request.json:
            return self.request.json[param]
        
        raise MissingParameter('Parameter \"' + param + '\" not provided')

    def hasParam(self, param):
        return param in self.request.json


# The environment for making templated html
_JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

def get_template(file):
    """ """
    return _JINJA_ENVIRONMENT.get_template(file)



