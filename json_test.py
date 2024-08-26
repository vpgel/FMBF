import json
data = json.load(open('data.json'))
string = json.dumps(data)
print(f'gaming{string}')