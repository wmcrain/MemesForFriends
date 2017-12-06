#!/usr/bin/env python
import datetime
import math
from random import randint
import urllib

from handle import DOMAIN
from handle import Keys
from handle import Links

from google.appengine.ext import ndb

class User(ndb.Model):
    """The database entry that represents a user account. """
    username = ndb.StringProperty(required=True, indexed=True)
    email = ndb.StringProperty(required=True, indexed=True)
    phone = ndb.StringProperty(required=True, indexed=True)

    first_name = ndb.StringProperty(indexed=False)
    last_name = ndb.StringProperty(indexed=False)

    bio = ndb.StringProperty(indexed=False, default="Empty bio...")
    picture = ndb.StringProperty(indexed=False, default=None)

    # Whether the user has verified their email or not
    verified = ndb.BooleanProperty(required=True, indexed=False)

    # 
    friends = ndb.KeyProperty(kind='User', indexed=False, repeated=True)
    pending_friends = ndb.KeyProperty(kind='User', indexed=False, repeated=True)
    
    blocked = ndb.KeyProperty(kind='User', indexed=False, repeated=True)

    current_meetup = ndb.KeyProperty(kind='Meetup', indexed=False, default=None)
    previous_meetups = ndb.KeyProperty(kind='Meetup', indexed=False, repeated=True)

    def get_create_link(self, active_time):
        """ Retrieves a link used to verify the account of the user. 

        The user can only receive one email to verify their account so only one account creation
        verification link can exist for a particular user. 
            
        Args:
            active_time (int): the amount of time in minutes before the link expires

        Returns:
            VerficationLink: The link for the user
        """

        # 
        links = VerficationLink.query(ndb.AND(VerficationLink.user_key == self.key,
            VerficationLink.op == Links.LINK_CREATE)).fetch()

        if len(links) != 0:
            raise Exception('Account create link already created for this user.')

        # Put an user create link in the database for this user
        link = VerficationLink.create_link(self.key, Links.LINK_CREATE, active_time)
        link.put()
        return link

    def get_login_link(self, active_time):
        """ Creates a new link that the user can use to login to their account.

        For a user, each login link will have a unique device code so that an email login link will
        only allow the device associated with the device code to log in.

        Args:
            active_time (int): the amount of time in minutes before the link expires

        Returns:
            VerficationLink: The link for the user
        """
        link = VerficationLink.create_link(self.key, Links.LINK_LOGIN, active_time)
        link.put()

        return link


    def expired(self):
        return len(ndb.AND(ndb.AND(
                VerficationLink.query(VerficationLink.user_key == self.key,
                VerficationLink.op == Links.LINK_CREATE),
                VerficationLink.expiration_time < datetime.datetime.now()).fetch())) == 0


# The ranges for the salt and device code
MIN_DEVICE_CODE = 0
MAX_DEVICE_CODE = 999999
MIN_SALT_CODE = 33333333
MAX_SALT_CODE = 99999999

class VerficationLink(ndb.Model):
    """ """
    user_key = ndb.KeyProperty(kind='User', required=True, indexed=True)

    # The operation the link performs
    op = ndb.StringProperty(required=True)

    # An unique identifier for the device requesting the link
    device_code = ndb.StringProperty(required=True, indexed=True)

    # The time when this link will expire and will no longer work
    expiration_time = ndb.DateTimeProperty(auto_now_add=True)

    param = ndb.StringProperty(required=True, indexed=True)

    #
    link = ndb.StringProperty(required=True)

    used = ndb.BooleanProperty(required=True, default=False)

    @staticmethod
    def create_link(user_key, op, active_time):

        link = VerficationLink(user_key=user_key, op=op, used=False)

        # Calculate the expiration time of the link
        link.expiration_time = datetime.datetime.now() + datetime.timedelta(minutes=active_time)

        # Generate a random code for the device issuing the request
        while True:
            link.device_code = (str(randint(MIN_DEVICE_CODE, MAX_DEVICE_CODE))
                .zfill(int(math.log(MAX_DEVICE_CODE, 10))))

            # Continue generating links until a link with a unique device code for the user has
            # been generated
            if len(VerficationLink.query(VerficationLink.user_key == link.user_key
                and VerficationLink.device_code == link.device_code).fetch()) == 0:
                break;

        # Generate a random salt to be appended onto the link parameter 
        salt = randint(MIN_SALT_CODE, MAX_SALT_CODE)

        # Generate the parameter that will be used in the link
        link.param = 'u{}d{}s{}t{}'.format(user_key.urlsafe(), link.device_code, 
            salt, str(link.expiration_time)).replace(' ', '')

        # Generate the link using the operation and parameter
        link.link = '{}/{}?{}={}'.format(DOMAIN, op, Keys.LINK_PARAM, link.param)
    
        return link

class Meetup(ndb.Model):
    time_formed = ndb.DateTimeProperty(auto_now_add=True, indexed=False)
    current_users = ndb.KeyProperty(kind='User', indexed=False, repeated=True)
    previous_users = ndb.KeyProperty(kind='User', indexed=False, repeated=True)

class SearchEntity(ndb.Model):
    meetup = ndb.KeyProperty(kind='Meetup', indexed=True, default=None)

    latitude = ndb.FloatProperty(required=True)
    longitude = ndb.FloatProperty(required=True)

    willing_matches = ndb.KeyProperty(kind='SearchEntity', repeated=True)

    pending_match = ndb.KeyProperty(kind='SearchEntity')
    pending_match_status = ndb.StringProperty()

    searching_users = ndb.KeyProperty(kind='User', indexed=False, repeated=True)

    class Status():
        """ The enumeration of keys that map to values in JSON request and responses. """
        APPROVED = "approved"
        CONFIRMED = "confirmed"
        DENIED = "denied"
        PENDING = "pending"

    def distance(self, other):
        earth_radius = 6371 # The radius of earth in kilometers

        #Calculate Distance based in Haversine Formula
        dlat = math.radians(other.latitude - self.latitude)
        dlon = math.radians(other.longitude - self.longitude)

        a = (math.sin(dlat / 2) * math.sin(dlat / 2) 
            + math.cos(math.radians(self.latitude)) 
            * math.cos(math.radians(other.latitude)) 
            * math.sin(dlon / 2) * math.sin(dlon / 2))

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        d = earth_radius * c
        return d


