
import logging

from django.conf import settings
from django.core.mail import send_mail
import urllib, urllib2, cgi

"""
Control de autenticacion basada en el DNIe espanol
"""

def dnie_url(url, params):
    #http://localhost:9011/web/authorize/?response_type=code&client_id=testclient&redirect_uri=https://www.example.com&state=somestate
  if params:
    return "http://localhost:9011%s?%s" % (url, urllib.urlencode(params))
  else:
    return "http://localhost:9011%s" % url

def dnie_get(url, params):
  full_url = dnie_url(url,params)
  try:
    return urllib2.urlopen(full_url).read()
  except urllib2.HTTPError:
    from helios_auth.models import AuthenticationExpired
    raise AuthenticationExpired()

def dnie_post(url, params):
  full_url = dnie_url(url, None)
  return urllib2.urlopen(full_url, urllib.urlencode(params)).read()

def can_create_election(user_id, user_info):
  return False

def send_message(user_id, user_name, user_info, subject, body):
  pass

def get_info_from_certificate():
    pass

def get_auth_url(request, redirect_url = None):
  request.session['dnie_redirect_uri'] = redirect_url
  request.session['dnie_redirect_uri'] = 'http://localhost:8005/auth/after/'
  """return facebook_url('/oauth/authorize', {
      'client_id': APP_ID,
      'redirect_uri': redirect_url,
      'scope': 'publish_stream,email,user_groups'})
  """
  #get_user_info_after_auth(request)
  return dnie_url('/web/authorize', {
      'response_type': 'code',
      'client_id': 'testclient',
      'redirect_uri': request.session['dnie_redirect_uri'],
      'state': 'somestate'
  })

def get_user_info_after_auth(request):
  """
  args = facebook_get('/oauth/access_token', {
      'client_id' : APP_ID,
      'redirect_uri' : request.session['fb_redirect_uri'],
      'client_secret' : API_SECRET,
      'code' : request.GET['code']
      })
  """
  args = dnie_post('/api/v1/tokens/', {
      'grant_type': 'authorization_code',
      'code': request.GET['code'],
      'client_id': 'testclient',
      'client_secret': 'testpassword'
  })
  #     'client_id' : '1',
  #     #'client_id' : APP_ID,
  #     'redirect_uri' : request.session['dnie_redirect_uri'],
  #     #'client_secret' : API_SECRET,
  #     'client_secret' : 'ddd',
  #     'code' : request.GET['code']
  #     })

  #access_token = cgi.parse_qs(args)['access_token'][0]

  from helios_auth import utils
  access_token_req = utils.from_json(args)
  access_token = access_token_req['access_token']

  info = utils.from_json(dnie_post('/web/me', {'access_token':access_token}))
  #info = {'user_id': '53159931P'}

  #return {'type': 'facebook', 'user_id' : info['id'], 'name': info.get('name'), 'email': info.get('email'), 'info': info, 'token': {'access_token': access_token}}
  return {'type': 'dnie', 'user_id' : info['id'], 'name': info.get('name'), 'email': info.get('email'), 'info': info, 'token': {'access_token': access_token}}
  return {}


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
    import logging
    logger = logging.getLogger('dnie')
    logger.debug('metaaaaaaaa')
    logger.debug(request.META['SSL_CLIENT_S_DN'])
    return None
