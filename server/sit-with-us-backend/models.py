#!/usr/bin/env python
import datetime
import jinja2
import os
import re

import keys

from google.appengine.api import mail
from google.appengine.ext import ndb

# The email from which emails will be sent to users
AUTHORIZED_EMAIL = 'Sit With Me <ryanmitch16@gmail.com>'

# The amount of time in minutes that the user has to verify their email
VERIFICATION_TIME_MINUTES = 30

# The environment for making templated html
JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

class User(ndb.Model):
    """The database entry that represents a user account. """
    username = ndb.StringProperty(required=True, indexed=True)
    password = ndb.StringProperty(required=True, indexed=False)
    email = ndb.StringProperty(required=True, indexed=True)
    phone = ndb.StringProperty(required=True, indexed=True)

    # Whether the user has verified their email or not
    verified = ndb.BooleanProperty(required=True, indexed=False)

    # The time at which the verification email was sent
    verfication_sent = ndb.DateTimeProperty(auto_now_add=True)

    @staticmethod
    def handle_create(username, password, email, phone):
        """Handles the creation of user accounts.

        Attempts to create a new user account with the specified username, password, email, and 
        phone number. Fails creating a new user if the username or email is taken. If user creation 
        does not fail, the user will be sent a verification email to confirm their account.

        Args:
            username (str): the username of the account
            password (str): the password of the account
            email (str): the email of the account
            phone (str): the phone number of the account
                        All non-numbers will be removed when stored in the database

        Returns:
            dict: The api response to the app
        """
        # Check if the username or email has been taken by a verified user or a user that still is 
        # attempting to verify their email.
        user_entries = [x for x in User.query(User.username == username).fetch() if not x.expired()]
        if len(user_entries) != 0:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Username taken" }

        user_entries = [x for x in User.query(User.email == email).fetch() if not x.expired()]
        if len(user_entries) != 0:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Email already in use" }

        # Remove all non-numbers from the phone number
        phone = re.sub("[^0-9]", "", phone)

        # Create a new unverified user and put the user in the database
        user = User(username=username, password=password, email=email, phone=phone,
            verified=False)
        user.put()

        # Generate a verification email for the new user
        email_body_template = JINJA_ENVIRONMENT.get_template('res/user_verification_email.html')
        email_body = email_body_template.render({
            'confirmation_link' : 'http://sit-with-us-backend.appspot.com/createconfirm?{}={}'
                .format(keys.USER_KEY, user.key.urlsafe()),
            'minutes' : VERIFICATION_TIME_MINUTES
        })

        # Send the verification email to the user
        mail.EmailMessage(sender=AUTHORIZED_EMAIL, to=email,
            subject='Confirm Your Registration', html=email_body).send()

        return { keys.SUCCESS : 1 }

    @staticmethod
    def handle_verify(user_key):
        """Handles when a user clicks the link to verify their email address.

        Verifies the account if the verification link has not expired yet. The generated html page
        displays a message regarding if the user was verified, if the user was previously verified,
        if the verification link has expired, or if something went wrong with verification.

        Args:
            user_key (string): The keys.USER_KEY query parameter in the confirmation link

        Returns:
            The html to be displayed on the verfication page
        """
        user = None:
        try:
            user = ndb.Key(urlsafe=user_key).get()
        except:
            # Catch if the user key is not formatted correctly
            user = None

        # Generate the verification page the user will see after attempting to create their account
        page_template = JINJA_ENVIRONMENT.get_template('res/user_verification.html')

        message = ''
        if user is not None:
            # Check if the account has already been verified 
            if user.verified == True:
                message = 'This account has already been verified'
            else:
                # Check if the verification has expired
                if user.expired():
                    message = ('The verification link has expired. Please try creating your'
                            ' account again.')
                else:
                    message = 'This account has been successfully verified.'

                    # Update the user's verification status
                    user.verified = True
                    user.put()
        else:
            # Check if the user key exists at all
            message = 'Something went wrong.'

        return page_template.render({ 'message' : message })

    @staticmethod
    def handle_login(username, password):
        """Handles when a user attempts to login.

        Checks to see if there exists a user with the specified username and password combination
        who has verified the account in the database.

        Args:
            username (str): The username of the account
            password (str): The password of the account

        Returns:
            dict: The api response to the app
        """
        # Retrieve the user with the specified username and password combination who has verified 
        # their email address
        user = [x for x in User.query(User.username == username).fetch() 
            if x.verified and x.password == password]

        # Return if no verified user with the username and password combination was found
        if len(user) == 0:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Wrong username or password" } 

        # Return that the user was able to be logged in
        return { keys.SUCCESS : 1, keys.USER_KEY : user[0].key.urlsafe() }

    def expired(self):
        """ Checks whether the verification link for this user entry has expired or not. """
        return not self.verified and ((datetime.datetime.now() - self.verfication_sent)
            > datetime.timedelta(minutes=VERIFICATION_TIME_MINUTES))

