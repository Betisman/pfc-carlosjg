from django.conf.urls import patterns, url

from apps.web.views import AuthorizeView
from apps.web.views import *


urlpatterns = patterns(
    '',
    url('^authorize/?', AuthorizeView.as_view(), name='authorize'),
    (r'^me/?', me_view)
)