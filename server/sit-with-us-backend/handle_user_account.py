import re
import webapp2

from handle import ApiHandler
from handle import AUTHORIZED_EMAIL
from handle import get_template
from handle import Keys
from handle import Links
from models import User
from models import VerficationLink

from google.appengine.api import mail
from google.appengine.ext import ndb

# The amount of time in minutes that the user has to verify their email
VERIFICATION_TIME_MINUTES = 30

class CreateUserHandler(ApiHandler):
    def handle(self):
        """Handles the creation of user accounts.

        Attempts to create a new user account with the specified username, password, email, and 
        phone number. Fails creating a new user if the username or email is taken. If user creation 
        does not fail, the user will be sent a verification email to confirm their account.
        """
        username = self.getParam(Keys.USERNAME)
        email = self.getParam(Keys.EMAIL)
        phone = self.getParam(Keys.PHONE_NUMBER)
        first_name = self.getParam(Keys.FIRST_NAME)
        last_name = self.getParam(Keys.LAST_NAME)

        # Check if the username or email has been taken by a verified user or a user that still is 
        # attempting to verify their email.
        user_entries = User.query(User.username == username).fetch()
        if len(user_entries) != 0:
            return { Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "Username taken" }

        user_entries = User.query(User.email == email).fetch()
        if len(user_entries) != 0:
            return { Keys.SUCCESS : 0, Keys.ERROR_MESSAGE : "Email already in use" }

        # Remove all non-numbers from the phone number
        phone = re.sub("[^0-9]", "", phone)

        # Create a new unverified user and put the user in the database
        user = User(username=username, email=email, phone=phone, verified=False, 
            first_name=first_name, last_name=last_name, current_meetup=None)
       
        user.friends = [x.key for x in User.query()]
        user.put()

        # 
        link = user.get_create_link(VERIFICATION_TIME_MINUTES)

        # Generate a verification email for the new user
        email_body_template = get_template('res/user_verification_email.html')
        email_body = email_body_template.render({
            'confirmation_link' : link.link, 
            'username' : user.username,
            'minutes' : VERIFICATION_TIME_MINUTES,
        })

        # Send the verification email to the user
        mail.EmailMessage(sender=AUTHORIZED_EMAIL, to=email,
            subject='Confirm Your Registration', html=email_body).send()

        return { Keys.SUCCESS : 1, Keys.DEVICE_KEY : link.device_code }

class CreateVerifyUserHandler(webapp2.RequestHandler):
    def get(self):
        # Generate the verification page the user will see after attempting to create their account
        template = get_template('res/user_verification.html')
        template_params = { 'message' : '', 'username' : ''}

        # Retrieve the link model associated with this link
        links = VerficationLink.query(ndb.AND(
            VerficationLink.param == self.request.get(Keys.LINK_PARAM),
            VerficationLink.op == Links.LINK_CREATE)).fetch()

        if len(links) == 0:
            template_params['message'] = 'This link is invalid.'

        else:
            # Retrieve the user that requested the link
            user = links[0].user_key.get()

            # Determine if the verification is successful or not
            if user is not None:
                template_params['username'] = user.username
                
                if not user.verified:
                    #if not user.expired():

                        # Update the user's verification status
                    user.verified = True
                    user.put()
                    template_params['message'] = 'This account has been successfully verified.'

                    #
                    links[0].used = True
                    links[0].put()

                    #else:
                    #    template_params['message'] = ('The verification link has expired.'
                    #        ' Please try creating your account again.')
                else:
                    template_params['message'] = 'This account has already been verified'
            else:
                # The model for the user has already been deleted because the link expired
                template_params['message'] = ('The verification link has expired.' 
                    ' Please try creating your account again.')

        self.response.set_status(200)
        self.response.write(template.render(template_params))


class LoginUserHandler(ApiHandler):
    def handle(self):
        email = self.getParam(Keys.EMAIL)

        user_entries = [u for u in User.query(User.email == email).fetch() if u.verified]
        if len(user_entries) == 0:
            return { Keys.SUCCESS : 1, Keys.DEVICE_KEY : 999999 }

        user = user_entries[0]
        link = user.get_login_link(VERIFICATION_TIME_MINUTES)

        # Generate a verification email for the new user
        email_body_template = get_template('res/user_login_email.html')
        email_body = email_body_template.render({
            'confirmation_link' : link.link, 
            'device_code' : link.device_code,
            'username' : user.username,
        })

        print(link.link)

        # Send the verification email to the user
        mail.EmailMessage(sender=AUTHORIZED_EMAIL, to=email,
            subject='Log In To Your Account', html=email_body).send()

        return { 
            Keys.SUCCESS : 1, 
            Keys.DEVICE_KEY : link.device_code, 
        }


class LoginEmailHandler(webapp2.RequestHandler):
    def get(self):
        # Generate the verification page the user will see after attempting to create their account
        template = get_template('res/user_verification.html')
        template_params = { 'message' : ''}

        # Retrieve the link model associated with this link
        links = [x for x in VerficationLink.query(VerficationLink.param == self.request.get(
            Keys.LINK_PARAM)).fetch()]

        if len(links) == 0:
            template_params['message'] = ('This link has either expired or is invalid.'
                ' Try logging in again.')

        else:
            links[0].used = True
            links[0].put()
            template_params['message'] = 'This link has been activated.'

        self.response.set_status(200)
        self.response.write(template.render(template_params))


class LoginPingHandler(ApiHandler):
    def handle(self):
        email = self.getParam(Keys.EMAIL)
        device_code = self.getParam(Keys.DEVICE_KEY)

        # If a user with the email does not exist, then just return a failure without giving detail
        users = [u for u in User.query(User.email == email).fetch() if u.verified]
        if len(users) == 0:
            return { Keys.SUCCESS : 0 }

        # Find the link that will verify the log in
        links = [x for x in VerficationLink.query(ndb.AND(VerficationLink.user_key == users[0].key,
            VerficationLink.device_code == device_code)).fetch()]

        # If no link exists, then someone is sending formatted requests outside the app 
        if len(links) == 0:
            return { Keys.SUCCESS : 0 }

        # Login will succeed if the user clicked the link
        if links[0].used:
            links[0].key.delete()
            return { 
                Keys.SUCCESS : 1, 
                Keys.USER_KEY : users[0].key.urlsafe(),
                Keys.USERNAME : users[0].username,
            }
        else:
            return { Keys.SUCCESS : 0 }
