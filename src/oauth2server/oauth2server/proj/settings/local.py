import sys

from proj.settings.default import *


# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'tbd(pv7679n_w-t++*s_*oon&#v0ubhkxhzvlq51ko2+=dt*z#'

# Database
# https://docs.djangoproject.com/en/1.6/ref/settings/#databases
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2',
        'NAME': 'oauth2server',
        'USER': 'carlos',
        'PASSWORD': 'carlos',
        'HOST': 'localhost',
    },
}

DEBUG = True

OAUTH2_SERVER = {
    'ACCESS_TOKEN_LIFETIME': 3600,
    'AUTH_CODE_LIFETIME': 3600,
    'REFRESH_TOKEN_LIFETIME': 3600,
    'IGNORE_CLIENT_REQUESTED_SCOPE': False,
}


# set up logging
import logging
logging.basicConfig(
    filename='/home/carlos/pfc/oauth.log',
    level = logging.DEBUG,
    format = '%(asctime)s %(levelname)s %(message)s'
)


ROOT_PATH = os.path.dirname(__file__) + '/../../'