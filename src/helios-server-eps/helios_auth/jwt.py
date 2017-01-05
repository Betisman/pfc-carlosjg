from jose import jwt
token = jwt.encode({'key': 'value'}, 'Turing', algorithm='HS256')
print token
d = jwt.decode(token, 'Turing', algorithms=['HS256'])
print d