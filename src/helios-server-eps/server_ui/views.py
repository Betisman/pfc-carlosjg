"""
server_ui specific views
"""

from helios.models import *
from helios_auth.security import *
from view_utils import *

import helios.views
import helios
from helios.crypto import utils as cryptoutils
from helios_auth.security import *
from helios.security import can_create_election

from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpResponseRedirect, Http404, HttpResponseNotAllowed

from django.conf import settings

import copy
import helios_auth.views as auth_views

def get_election():
  return None
  
def home(request):
  # load the featured elections
  featured_elections = Election.get_featured()
  
  user = get_user(request)
  create_p = can_create_election(request)

  if create_p:
    elections_administered = Election.get_by_user_as_admin(user, archived_p=False, limit=5)
  else:
    elections_administered = None

  if user:
    elections_voted = Election.get_by_user_as_voter(user, limit=5)
  else:
    elections_voted = None

  auth_systems = copy.copy(settings.AUTH_ENABLED_AUTH_SYSTEMS)
  try:
    #auth_systems.remove('password')
    pass
  except: pass

  """
  ssl_client_s_dn = request.META['SSL_CLIENT_S_DN']
  ssl_client_s_dn = ssl_client_s_dn.replace('\,', 'XXXCOMAXXX')
  params = dict(u.split("=") for u in ssl_client_s_dn.split(","))
  for param in params:
    params[param] = params[param].replace('XXXCOMAXXX', ',')
  return DNIe(params['CN'], params['GN'], params['SN'], params['serialNumber'], params['C'])
  """
  import logging
  logger = logging.getLogger("home")
  try:
    for meta in request.META:
      if (str(meta).find('SSL') > -1):
        logger.debug(meta + ': ' + request.META[meta])
      else:
        logger.debug(meta + ': --')
  except Exception:
    logger.debug("Pues no")

  try:
    ssl_client_s_dn = request.META['SSL_CLIENT_S_DN']
    logger.debug('1: ' + str(ssl_client_s_dn))
    ssl_client_s_dn = ssl_client_s_dn.replace('\,', 'XXXCOMAXXX')
    logger.debug('2: ' + str(ssl_client_s_dn))
    sd = dict(u.split("=") for u in ssl_client_s_dn.split(","))
    logger.debug('3: ' + str(sd))
    for fff in sd:
      # logger.debug('4: ' + str(fff))
      sd[fff] = sd[fff].replace('XXXCOMAXXX', ',')
      # logger.debug('5: ' + str(sd[fff])
    dni = sd['serialNumber']
    # logger.debug('6: ' + str(dni))
  except KeyError:
    dni = None
  """
  if dni and not user:
    logger.debug("dni and not user")
    if len(settings.AUTH_ENABLED_AUTH_SYSTEMS) == 1:
      logger.debug('redirect desde home ----------------- ' + settings.AUTH_ENABLED_AUTH_SYSTEMS[0])
      return HttpResponseRedirect(reverse(auth_views.start, args=[settings.AUTH_ENABLED_AUTH_SYSTEMS[0]])+ '?return_url=' + request.GET.get('return_url', ''))

  """
  login_box = auth_views.login_box_raw(request, return_url="/", auth_systems=auth_systems)

  return render_template(request, "index", {'elections': featured_elections,
                                            'elections_administered' : elections_administered,
                                            'elections_voted' : elections_voted,
                                            'create_p':create_p,
                                            'login_box' : login_box,
                                            'dni': dni
                                            })

def about(request):
  return render_template(request, "about")

def docs(request):
  return render_template(request, "docs")

def faq(request):
  return render_template(request, "faq")

def privacy(request):
  return render_template(request, "privacy")

