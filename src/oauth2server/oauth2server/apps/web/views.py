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
    def __init__(self, commonName, givenName, surname, serialNumber, c, certStart=None, certEnd=None):
        self.commonName = commonName
        self.givenName = givenName
        self.surname = surname
        self.serialNumber = serialNumber
        self.c = c
        self.certStart = certStart
        self.certEnd = certEnd

    def __str__(self):
        # logger.debug('CN: %s' %(self.commonName))
        # logger.debug('GN: %s' %(self.givenName))
        # logger.debug('SN: %s' %(self.surname))
        # logger.debug('serial: %s' %(self.serialNumber)) 
        # logger.debug('C: %s' %(self.c))
        # logger.debug('certStart: %s' %(self.certStart))
        # logger.debug('certEnd: %s' %(self.certEnd))
        logger.debug('|-- CN: %s --- GN: %s --- SN: %s --- serialNumber: %s --- C: %s -- certStart: %s -- certEnd: %s --|' % (self.commonName, self.givenName, self.surname, self.serialNumber, self.c, self.certStart, self.certEnd))
        return '|-- CN: %s --- GN: %s --- SN: %s --- serialNumber: %s --- C: %s -- certStart: %s -- certEnd: %s --|' % (self.commonName, self.givenName, self.surname, self.serialNumber, self.c, self.certStart, self.certEnd)


def get_dni_info_from_ssl(request):
    ssl_client_s_dn = request.META['SSL_CLIENT_S_DN']
    ssl_client_s_dn = ssl_client_s_dn.replace('\,', 'XXXCOMAXXX')
    params = dict(u.split("=") for u in ssl_client_s_dn.split(","))
    for param in params:
        params[param] = params[param].replace('XXXCOMAXXX', ',')
    certStart = request.META['SSL_CLIENT_V_START']
    certEnd = request.META['SSL_CLIENT_V_END']
    strlogger = '*%s, %s*' %(certStart, certEnd)
    logger.warn(strlogger)
    return DNIe(params['CN'], params['GN'], params['SN'], params['serialNumber'], params['C'], certStart, certEnd)


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
        logger.debug('en get_AuthorizeView')
        form = self.form_class(initial=self.initial)
        try:
            logger.debug('en get')
            prueba = request.META['SSL_CLIENT_S_DN']
            logger.debug('prueba: *' + prueba + '*')
            claves = [
                    #'HTTPS'
                    'SSL_PROTOCOL'
                    # ,'SSL_SESSION_ID'
                    ,'SSL_SESSION_RESUMED'
                    ,'SSL_SECURE_RENEG'
                    ,'SSL_CIPHER'
                    ,'SSL_CIPHER_EXPORT'
                    ,'SSL_CIPHER_USEKEYSIZE'
                    ,'SSL_CIPHER_ALGKEYSIZE'
                    ,'SSL_COMPRESS_METHOD'
                    ,'SSL_VERSION_INTERFACE'
                    ,'SSL_VERSION_LIBRARY'
                    ,'SSL_CLIENT_M_VERSION'
                    ,'SSL_CLIENT_M_SERIAL'
                    ,'SSL_CLIENT_S_DN'
                    # ,'SSL_CLIENT_S_DN_x509'
                    # ,'SSL_CLIENT_SAN_Email_n'
                    # ,'SSL_CLIENT_SAN_DNS_n'
                    # ,'SSL_CLIENT_SAN_OTHER_msUPN_n'
                    ,'SSL_CLIENT_I_DN'
                    # ,'SSL_CLIENT_I_DN_x509'
                    ,'SSL_CLIENT_V_START'
                    ,'SSL_CLIENT_V_END'
                    ,'SSL_CLIENT_V_REMAIN'
                    ,'SSL_CLIENT_A_SIG'
                    ,'SSL_CLIENT_A_KEY'
                    ,'SSL_CLIENT_CERT'
                    # ,'SSL_CLIENT_CERT_CHAIN_n'
                    # ,'SSL_CLIENT_CERT_RFC4523_CEA'
                    ,'SSL_CLIENT_VERIFY'
                    ,'SSL_SERVER_M_VERSION'
                    ,'SSL_SERVER_M_SERIAL'
                    ,'SSL_SERVER_S_DN'
                    # ,'SSL_SERVER_SAN_Email_n'
                    # ,'SSL_SERVER_SAN_DNS_n'
                    # ,'SSL_SERVER_SAN_OTHER_dnsSRV_n'
                    # ,'SSL_SERVER_S_DN_x509'
                    ,'SSL_SERVER_I_DN'
                    # ,'SSL_SERVER_I_DN_x509'
                    ,'SSL_SERVER_V_START'
                    ,'SSL_SERVER_V_END'
                    ,'SSL_SERVER_A_SIG'
                    ,'SSL_SERVER_A_KEY'
                    ,'SSL_SERVER_CERT'
                    # ,'SSL_SRP_USER'
                    # ,'SSL_SRP_USERINFO'
                    # ,'SSL_TLS_SNI']
                    ]
            for k in claves:
                try:
                    logger.warn(k+': '+str(request.META[k]))
                except Exception as ex:
                    logger.warn(k+': no funciona esto de recorrer el meta')
                    logger.warn(ex)


            dnie = get_dni_info_from_ssl(request)
        except Exception as e:
            logger.warn('get_No vienen las credenciales del DNIe')

        return self._render(request=request, form=form, dnie=dnie)

    def post(self, request, *args, **kwargs):
        logger.debug('en post_AuthorizeView')
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

        claves = [
            #'HTTPS'
                'SSL_PROTOCOL'
                # ,'SSL_SESSION_ID'
                ,'SSL_SESSION_RESUMED'
                ,'SSL_SECURE_RENEG'
                ,'SSL_CIPHER'
                ,'SSL_CIPHER_EXPORT'
                ,'SSL_CIPHER_USEKEYSIZE'
                ,'SSL_CIPHER_ALGKEYSIZE'
                ,'SSL_COMPRESS_METHOD'
                ,'SSL_VERSION_INTERFACE'
                ,'SSL_VERSION_LIBRARY'
                ,'SSL_CLIENT_M_VERSION'
                ,'SSL_CLIENT_M_SERIAL'
                ,'SSL_CLIENT_S_DN'
                # ,'SSL_CLIENT_S_DN_x509'
                # ,'SSL_CLIENT_SAN_Email_n'
                # ,'SSL_CLIENT_SAN_DNS_n'
                # ,'SSL_CLIENT_SAN_OTHER_msUPN_n'
                ,'SSL_CLIENT_I_DN'
                # ,'SSL_CLIENT_I_DN_x509'
                ,'SSL_CLIENT_V_START'
                ,'SSL_CLIENT_V_END'
                ,'SSL_CLIENT_V_REMAIN'
                ,'SSL_CLIENT_A_SIG'
                ,'SSL_CLIENT_A_KEY'
                ,'SSL_CLIENT_CERT'
                # ,'SSL_CLIENT_CERT_CHAIN_n'
                # ,'SSL_CLIENT_CERT_RFC4523_CEA'
                ,'SSL_CLIENT_VERIFY'
                ,'SSL_SERVER_M_VERSION'
                ,'SSL_SERVER_M_SERIAL'
                ,'SSL_SERVER_S_DN'
                # ,'SSL_SERVER_SAN_Email_n'
                # ,'SSL_SERVER_SAN_DNS_n'
                # ,'SSL_SERVER_SAN_OTHER_dnsSRV_n'
                # ,'SSL_SERVER_S_DN_x509'
                ,'SSL_SERVER_I_DN'
                # ,'SSL_SERVER_I_DN_x509'
                ,'SSL_SERVER_V_START'
                ,'SSL_SERVER_V_END'
                ,'SSL_SERVER_A_SIG'
                ,'SSL_SERVER_A_KEY'
                ,'SSL_SERVER_CERT'
                # ,'SSL_SRP_USER'
                # ,'SSL_SRP_USERINFO'
                # ,'SSL_TLS_SNI']
                ]
        for k in claves:
            try:
                logger.warn(k+': '+str(request.META[k]))
            except Exception as ex:
                logger.warn(k+': no funciona esto de recorrer el meta')
                logger.warn(ex)
        try:
            logger.debug('enn post')
            prueba = request.META['SSL_CLIENT_S_DN']
            logger.debug('prueba: *' + prueba + '*')
            dnie = get_dni_info_from_ssl(request)
            logger.debug('dnie: *' + str(dnie) + '*')
        except Exception as e:
            logger.warn('post_No vienen las credenciales del DNIe')
            logger.warn(e)
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
                logger.debug('request.POST["step"]: ' + request.POST['step'])
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

        logger.debug('redirect_uri: ' + request.redirect_uri)
        logger.debug('request.response_type('+request.response_type+')')
        logger.debug('request.client('+str(request.client)+')')
        logger.debug('authorized('+str(authorized)+')')
        logger.debug('scopes('+str(scopes)+')')
        logger.debug('request.redirect_uri('+str(request.redirect_uri)+')')
        logger.debug('state='+str(request.state)+'')
        logger.debug('dnie='+str(dnie)+'')
        logger.debug('client_type='+str(request.POST["client_type"])+'')
        try:
            return factory(response_type=request.response_type).process(
                client=request.client,
                authorized=authorized,
                scopes=scopes,
                redirect_uri=request.redirect_uri,
                state=request.state,
                dnie=dnie,
                client_type=request.POST['client_type']
            )
        except Exception as e:
            logger.error(e.message)
            logger.error(e)

    def _render(self, request, form, dnie=None, cadena=None, prueba=None):
        return HttpResponse(render(request, self.template_name, {
            'title': 'Authorize', 'client': request.client,
            'form': form, 'scopes': OAuthScope.objects.all(), 'prueba': prueba, 'dnie': dnie, 'prueba': prueba}))

    # def _render(self, request, form):
        # return HttpResponse(render(request, self.template_name, {
            # 'title': 'Authorize', 'client': request.client,
            # 'form': form, 'scopes': OAuthScope.objects.all()}))


from apps.tokens.decorators import authentication_required
import json


def me_view(request, *args, **kwargs):
    logger.debug('me_view <--------------------------')
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
