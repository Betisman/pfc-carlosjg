from django.views.generic import View
from django.shortcuts import render
from django.http import HttpResponse
from django.utils.decorators import method_decorator
from apps.web.decorators import validate_request
from apps.web.responsetypes import factory
from apps.web.forms import AuthorizeForm
from apps.tokens.models import OAuthScope
import logging
logger = logging.getLogger('views')


class DNIe:
    def __init__(self, commonName, givenName, surname, serialNumber, c):
        self.commonName = commonName
        self.givenName = givenName
        self.surname = surname
        self.serialNumber = serialNumber
        self.c = c

    def __str__(self):
        return '|-- CN: %s --- GN: %s --- SN: %s --- serialNumber: %s --- C: %s --|' % (
        self.commonName, self.givenName, self.surname, self.serialNumber, self.c)


def get_dni_info_from_ssl(request):
    ssl_client_s_dn = request.META['SSL_CLIENT_S_DN']
    ssl_client_s_dn = ssl_client_s_dn.replace('\,', 'XXXCOMAXXX')
    params = dict(u.split("=") for u in ssl_client_s_dn.split(","))
    for param in params:
        params[param] = params[param].replace('XXXCOMAXXX', ',')
    return DNIe(params['CN'], params['GN'], params['SN'], params['serialNumber'], params['C'])


class AuthorizeView(View):
    form_class = AuthorizeForm
    initial = {}
    template_name = 'web/authorize.html'

    @method_decorator(validate_request)
    def dispatch(self, *args, **kwargs):
        """
        Decorating the dispatch method decorates all methods.
        So both get and post will have the decorator applied.
        """
        logger.debug('dispatch')
        return super(AuthorizeView, self).dispatch(*args, **kwargs)

    def get(self, request, *args, **kwargs):
        form = self.form_class(initial=self.initial)
        try:

            logger.debug('en get')
            prueba = request.META['SSL_CLIENT_S_DN']
            logger.debug('prueba: *' + prueba + '*')
            dnie = get_dni_info_from_ssl(request)
        except Exception as e:
            logger.warn('No vienen las credenciales del DNIe')

        return self._render(request=request, form=form)

    def post(self, request, *args, **kwargs):
        form = self.form_class(request.POST)

        mstring = []
        for key in request.POST.iterkeys():
            valuelist = request.POST.getlist(key)
            mstring.extend(['%s=%s' % (key, val) for val in valuelist])
        msg = ','.join(mstring)
        logger.debug(msg)
        try:
            logger.debug(request.POST['client_type'])
            #logger.debug(request.client_type)
            # Si la peticion viene desde la app Android, vamos por otro camino
            #if request.client_type == 'androidnfcapp':
            if request.POST['client_type'] == 'androidnfcapp':
                return self.post_android_app(request, *args, **kwargs)

        except Exception as e:
            logger.warn('Excepcion: ' + e.message)

        try:
            logger.debug('enn post')
            prueba = request.META['SSL_CLIENT_S_DN']
            logger.debug('prueba: *' + prueba + '*')
            dnie = get_dni_info_from_ssl(request)
            logger.debug('dnie: *' + str(dnie) + '*')
        except Exception as e:
            logger.warn('No vienen las credenciales del DNIe')
            dnie = None

        logger.debug('form.is_valid' + str(form.is_valid()))
        if not form.is_valid():
            return self._render(request=request, form=form)

        logger.debug('hhhhhhhhhhhhhhhhhhhhhhh: ' + request.redirect_uri)
        return factory(response_type=request.response_type).process(
            client=request.client,
            authorized=form.cleaned_data['authorize'],
            scopes=form.cleaned_data['scopes'],
            redirect_uri=request.redirect_uri,
            state=request.state,
            dnie=dnie
        )

    def post_android_app(self, request, *args, **kwargs):
        # Si la peticion viene desde la App de Android, tenemos que tratar la respuesta de forma diferente.
        try:
            logger.debug('enn post_android_app')
            prueba = request.META['SSL_CLIENT_S_DN']
            logger.debug('prueba: *' + prueba + '*')
            dnie = get_dni_info_from_ssl(request)
            logger.debug('dnie: *' + str(dnie) + '*')
        except Exception as e:
            logger.warn('No vienen las credenciales del DNIe')
            dnie = None

        authorized = True
        try:
            # Si la peticion viene desde la app Android, vamos por otro camino
            #if request.step == '1':
            if request.POST['step'] == '1':
                permisosObj = OAuthScope.objects.all()
                from django.core import serializers
                data = serializers.serialize('json', permisosObj)
                logger.debug(data)
                from django.http import HttpResponse
                return HttpResponse(data, content_type='application/json')

        except Exception as e:
            logger.error('Exception: ' + e.message)
            return None

        # Los scopes deben venir de la peticion del cliente Android, pero por ahora simulamos que se acepta todos
        permisosObj = OAuthScope.objects.all()
        scopes = [1,2]


        logger.debug('hhhhhhhhhhhhhhhhhhhhhhh: ' + request.redirect_uri)
        return factory(response_type=request.response_type).process(
            client=request.client,
            authorized=authorized,
            scopes=scopes,
            redirect_uri=request.redirect_uri,
            state=request.state,
            dnie=dnie,
            client_type=request.client_type
        )

    def _render(self, request, form, dnie, cadena, prueba):
        return HttpResponse(render(request, self.template_name, {
            'title': 'Authorize', 'client': request.client,
            'form': form, 'scopes': OAuthScope.objects.all(), 'prueba': prueba, 'dnie': dnie, 'prueba': prueba}))

    def _render(self, request, form):
        return HttpResponse(render(request, self.template_name, {
            'title': 'Authorize', 'client': request.client,
            'form': form, 'scopes': OAuthScope.objects.all()}))


from apps.tokens.decorators import authentication_required
import json


def me_view(request, *args, **kwargs):
    from rest_framework.response import Response
    data = {
        'id': '53159931P',
        'name': 'Carlos Jimenez',
        'email': 'mi@email.com',
    }
    return HttpResponse(
        json.dumps(data),
        content_type='application/json',
    )
