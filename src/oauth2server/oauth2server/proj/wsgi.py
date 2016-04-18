import os
from django.core.wsgi import get_wsgi_application
import sys
sys.path.append('/home/carlos/pfc/pfc-carlosjg/src/oauth2server/oauth2server')
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'proj.settings.local')
application = get_wsgi_application()