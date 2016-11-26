
import logging

from django.conf import settings
from django.core.mail import send_mail
import urllib, urllib2, cgi

"""
Control de autenticacion basada en el DNIe espanol
"""
STATUS_UPDATES = False

def dnie_url(url, params):
    #http://localhost:9011/web/authorize/?response_type=code&client_id=testclient&redirect_uri=https://www.example.com&state=somestate
  logger = logging.getLogger('dnie')
  ipport = 'localhost:9011'
  ipport = '192.168.1.153:8001'
  ipport = '192.168.1.153:8443'
  ipport = '192.168.1.144'
  logger.info('IPPORT: %s' %(ipport))
  logger.info('De settings: %s' %(settings.SECURE_URL_HOST))
  protocolipport = settings.DNIE_OAUTH_SECURE_HOST
  protocolipport = protocolipport if protocolipport.find('http') == 0 else 'https://'+protocolipport
  if params:
    # logger.info("http://%s%s?%s" % (ipport, url, urllib.urlencode(params)))
    # #return "http://%s%s?%s" % (ipport, url, urllib.urlencode(params))
    # return "https://%s%s?%s" % (ipport, url, urllib.urlencode(params))    
    logger.info("%s%s?%s" % (protocolipport, url, urllib.urlencode(params)))
    return "%s%s?%s" % (protocolipport, url, urllib.urlencode(params))
  else:
    # logger.info("http://%s%s" % (ipport, url))
    # #return "http://%s%s" % (ipport, url)
    # return "https://%s%s" % (ipport, url)
    logger.info("%s%s" % (protocolipport, url))
    return "%s%s" % (protocolipport, url)

def dnie_url_step2(url, params):
  logger = logging.getLogger('dnie')
  ipport = '192.168.1.153:8553'
  protocolipport = settings.OAUTH_SECURE_HOST
  protocolipport = protocolipport if protocolipport.find('http') == 0 else 'https://'+protocolipport
  if params:
    # logger.info("http://%s%s?%s" % (ipport, url, urllib.urlencode(params)))
    # #return "http://%s%s?%s" % (ipport, url, urllib.urlencode(params))
    # return "https://%s%s?%s" % (ipport, url, urllib.urlencode(params))    
    logger.info("%s%s?%s" % (protocolipport, url, urllib.urlencode(params)))
    return "%s%s?%s" % (protocolipport, url, urllib.urlencode(params))
  else:
    # logger.info("http://%s%s" % (ipport, url))
    # #return "http://%s%s" % (ipport, url)
    # return "https://%s%s" % (ipport, url)
    logger.info("%s%s" % (protocolipport, url))
    return "%s%s" % (protocolipport, url)

def dnie_get(url, params):
  full_url = dnie_url(url,params)
  try:
    return urllib2.urlopen(full_url).read()
  except urllib2.HTTPError:
    from helios_auth.models import AuthenticationExpired
    raise AuthenticationExpired()

def dnie_post(url, params):
  full_url = dnie_url(url, None)
  logger = logging.getLogger('dnie')
  logger.info("full_url(%s), params(%s)" % (full_url, urllib.urlencode(params)))
  return urllib2.urlopen(full_url, urllib.urlencode(params)).read()

def dnie_post_step2(url, params):
  full_url = dnie_url_step2(url, None)
  logger = logging.getLogger('dnie')
  logger.info("full_url(%s), params(%s)" % (full_url, urllib.urlencode(params)))
  # -------------------------------------------------------------------------
  import ssl
  gcontext = ssl.SSLContext(ssl.PROTOCOL_TLSv1)  # Only for gangstars
  # info = urllib2.urlopen(req, context=gcontext).read()
  return urllib2.urlopen(full_url, urllib.urlencode(params), context=gcontext).read()
  # -------------------------------------------------------------------------
  # return urllib2.urlopen(full_url, urllib.urlencode(params)).read()

def can_create_election(user_id, user_info):
  return True

def send_message(user_id, user_name, user_info, subject, body):
  pass

def get_info_from_certificate():
    pass

def get_auth_url(request, redirect_url = None):
  request.session['dnie_redirect_uri'] = redirect_url
  request.session['dnie_redirect_uri'] = 'http://localhost:8005/auth/after/'
  request.session['dnie_redirect_uri'] = 'https://192.168.1.153:8442/auth/after/'
  request.session['dnie_redirect_uri'] = settings.SECURE_URL_HOST + '/auth/after/'
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
  logger = logging.getLogger('dnie')
  logger.debug('get_user_info_after_auth')
  mstring = []
  for key in request.GET.iterkeys():
    valuelist = request.GET.getlist(key)
    mstring.extend(['%s=%s' % (key, val) for val in valuelist])
    logger.debug('*'+key+'*=*'+val+'*')
  msg = ','.join(mstring)
  logger.debug(msg)
  try:
    logger.debug('clienttype: ' + request.GET['client_type'])
    if request.GET['client_type'] == 'androidnfcapp':
      return get_user_info_after_auth_androidClient(request)
  except Exception:
    pass
  logger.info('ahora el dni_post')
  args = dnie_post_step2('/api/v1/tokens/', {
      'grant_type': 'authorization_code',
      'code': request.GET['code'],
      'client_id': 'testclient',
      'client_secret': 'testpassword'
  })
  logger.info(args)
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

  info = utils.from_json(dnie_post_step2('/web/me', {'access_token':access_token}))
  #info = {'user_id': '53159931P'}

  #return {'type': 'facebook', 'user_id' : info['id'], 'name': info.get('name'), 'email': info.get('email'), 'info': info, 'token': {'access_token': access_token}}
  return {'type': 'dnie', 'user_id' : info['id'], 'name': info.get('name'), 'email': info.get('email'), 'info': info, 'token': {'access_token': access_token}}


def get_user_info_after_auth_androidClient(request):
    logger = logging.getLogger('dnie')
    logger.debug('get_user_info_after_auth_androidClient')
    logger.debug('info: ' + request.GET['info'])
    logger.debug('access_token: ' + request.GET['access_token'])
    import json
    info = json.loads(request.GET['info'])
    access_token = request.GET['access_token']
    return {'type': 'dnie', 'user_id' : info['id'], 'name': info.get('name'), 'email': info.get('email'), 'info': info, 'token': {'access_token': access_token}}


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

def do_logout(request):
    # import logging
    # logger = logging.getLogger('dnie')
    # logger.debug('metaaaaaaaa')
    # logger.debug(request.META['SSL_CLIENT_S_DN'])
    return None

