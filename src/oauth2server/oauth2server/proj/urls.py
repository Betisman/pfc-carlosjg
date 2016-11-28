from django.conf.urls import patterns, include, url
from django.conf import settings

urlpatterns = patterns(
    '',
    url(r'^api/v1/', include('apps.tokens.urls', namespace='api_v1')),
    url(r'^web/', include('apps.web.urls', namespace='web')),
    url(r'static/(?P<path>.*)$', 'django.views.static.serve', {'document_root' : settings.ROOT_PATH + '/proj/static'}),
)