# -*- coding: utf-8 -*-
"""
Forms for Helios
"""

from django import forms
from models import Election
from widgets import *
from fields import *
from django.conf import settings


class ElectionForm(forms.Form):
  short_name = forms.SlugField(max_length=25, help_text='Sin espacios. Es parte de la URL de la elección. Ej: mi-club-2017.')
  name = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':60}), help_text='El nombre de la elcción. Ej: Eleccions Mi Club 2017.')
  description = forms.CharField(max_length=4000, widget=forms.Textarea(attrs={'cols': 70, 'wrap': 'soft'}), required=False)
  election_type = forms.ChoiceField(label="type", choices = Election.ELECTION_TYPES)
  use_voter_aliases = forms.BooleanField(required=False, initial=False, help_text='Si está seleccionado, las identidades de los votantes se reemplazarán por alias (ej: "V12") en el centro de seguimiento de votos.')
  #use_advanced_audit_features = forms.BooleanField(required=False, initial=True, help_text='disable this only if you want a simple election with reduced security but a simpler user interface')
  randomize_answer_order = forms.BooleanField(required=False, initial=False, help_text='Si está seleccionado, las opciones de cada votación aparecerán en orden aleatorio para cad votante.')
  private_p = forms.BooleanField(required=False, initial=False, label="¿Privada?", help_text='Una elección privada sólo es visible para votantes registrados.')
  help_email = forms.CharField(required=False, initial="", label="Email de ayuda", help_text='Un email donde los votantes pueden consultar en caso de necesitar ayuda.')
  
  if settings.ALLOW_ELECTION_INFO_URL:
    election_info_url = forms.CharField(required=False, initial="", label="Election Info Download URL", help_text="the URL of a PDF document that contains extra election information, e.g. candidate bios and statements")
  

class ElectionTimesForm(forms.Form):
  # times
  voting_starts_at = SplitDateTimeField(help_text = 'Fecha y hora UTC de comienzo de la votación.',
                                   widget=SplitSelectDateTimeWidget)
  voting_ends_at = SplitDateTimeField(help_text = 'Fecha y hora UTC de finalización de la votación.',
                                   widget=SplitSelectDateTimeWidget)

  
class EmailVotersForm(forms.Form):
  subject = forms.CharField(max_length=80)
  body = forms.CharField(max_length=4000, widget=forms.Textarea)
  send_to = forms.ChoiceField(label="Send To", initial="all", choices= [('all', 'all voters'), ('voted', 'voters who have cast a ballot'), ('not-voted', 'voters who have not yet cast a ballot')])

class TallyNotificationEmailForm(forms.Form):
  subject = forms.CharField(max_length=80)
  body = forms.CharField(max_length=2000, widget=forms.Textarea, required=False)
  send_to = forms.ChoiceField(label="Send To", choices= [('all', 'all voters'), ('voted', 'only voters who cast a ballot'), ('none', 'no one -- are you sure about this?')])

class VoterPasswordForm(forms.Form):
  voter_id = forms.CharField(max_length=50, label="Voter ID")
  password = forms.CharField(widget=forms.PasswordInput(), max_length=100)

