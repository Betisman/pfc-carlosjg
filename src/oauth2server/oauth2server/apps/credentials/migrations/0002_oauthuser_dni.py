# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('credentials', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='oauthuser',
            name='dni',
            field=models.CharField(default='9', unique=True, max_length=9),
            preserve_default=False,
        ),
    ]
