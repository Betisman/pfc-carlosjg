
import logging

from django.conf import settings
from django.core.mail import send_mail

"""
Control de autenticacion basada en el DNIe espanol
"""

def can_create_election(user_id, user_info):
  return False

def send_message(user_id, user_name, user_info, subject, body):
  pass

def get_info_from_certificate():
    pass

def get_auth_url(request, redirect_url = None):
  return redirect_url

def get_user_info_after_auth(request):
    from helios_auth.models import User
    import logging
    logger = logging.getLogger('helios_auth.dnie')
    # user = User.get_by_dni('password', request.session['password_user_id'])
    dnie = get_dni_info_from_ssl(request)
    logger.debug('------------------------------------' + dnie.serialNumber)
    try:
        user = User.get_by_type_and_id('dnie', dnie.serialNumber)
    except Exception as dne:
        logger.error(dne)
        user = None

    #del request.session['password_user_id']
    if user is None:
        return user

    return {'type': 'password', 'user_id' : user.user_id, 'name': user.name, 'info': user.info, 'token': None}

class DNIe:
    def __init__(self, commonName, givenName, surname, serialNumber, c):
        self.commonName = commonName
        self.givenName = givenName
        self.surname = surname
        self.serialNumber = serialNumber
        self.c = c

    def __str__(self):
        return '|-- CN: %s --- GN: %s --- SN: %s --- serialNumber: %s --- C: %s --|' %(self.commonName, self.givenName, self.surname, self.serialNumber, self.c)

def get_dni_info_from_ssl(request):
  ssl_client_s_dn = request.META['SSL_CLIENT_S_DN']
  ssl_client_s_dn = ssl_client_s_dn.replace('\,', 'XXXCOMAXXX')
  params = dict(u.split("=") for u in ssl_client_s_dn.split(","))
  for param in params:
    params[param] = params[param].replace('XXXCOMAXXX', ',')
  return DNIe(params['CN'], params['GN'], params['SN'], params['serialNumber'], params['C'])


def do_auth(request):
    import logging
    logger = logging.getLogger('dnie')
    logger.error(get_dni_info_from_ssl(request))
    return settings.SECURE_URL_HOST

def do_logout(user_for_remote, request):
    logger.debug('metaaaaaaaa')
    logger.debug(request.META['SSL_CLIENT_S_DN'])
    return None

