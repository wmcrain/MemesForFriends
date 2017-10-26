#!/usr/bin/env python
import keys
from google.appengine.ext import ndb

class User(ndb.Model):
    """The database entry that represents a user account. """
    username = ndb.StringProperty(required=True, indexed=True)
    password = ndb.StringProperty(required=True, indexed=False)
    email = ndb.StringProperty(required=True, indexed=True)
    phone = ndb.StringProperty(required=True, indexed=True)

    @staticmethod
    def create(username, password, email, phone):
        # Check if the username has been taken
        if len(User.query(User.username == username).fetch()) != 0:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Username taken" }

        # Check if the email has already been used 
        if len(User.query(User.email == email).fetch()) != 0:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Email already in use" }

        user = User()
        user.username = username
        user.password = password
        user.email = email
        user.phone = phone
        print(">>>>>>>")
        print(user)
        user.put()

        # Put the user in the database and return the database key of the user
        return { keys.SUCCESS : 1, keys.USER_KEY : user.put().urlsafe() }

    @staticmethod
    def login(username, password):
        # Attempt to find a user with the specified username and password
        user = User.find_by_username(username)
        if user is None or user.password != password:
            return { keys.SUCCESS : 0, keys.ERROR_MESSAGE : "Wrong username or password" } 

        # There should only be one user with the specified username
        return { keys.SUCCESS : 1, keys.USER_KEY : user.key.urlsafe() }

    @staticmethod   
    def find_by_username(username):
        existing_user = User.query(User.username == username).fetch()
        if len(existing_user) == 0:
            return None
        return existing_user[0]

