#!/usr/bin/env python


from random import random


fout = open("verizon_sample.json", "w")

JSON_TEMPLATE = '{"SequenceID": REPLACE_WITH_ID,"UpdateUTC": "1588957835000","DeviceTimeZoneOffset": 2,"DeviceTimeZoneUseDST": true,"DisplayState": "Idle","IsPrivate": false,"SpeedKmph": 85,"DirectionDegrees": 280,"Heading": "North","DeltaDistanceKm": 0.354,"OdometerKm": 120045,"TotalEngineMinutes": 1380,"IdleTimeMinutes": 4,"DeltaTimeInSec": 251,"Latitude": REPLACE_WITH_LAT,"Longitude": REPLACE_WITH_LON,"SensorBits": 65,"SensorValues": ["Boom Stow 1-ON", "Boom Stow 2-OFF", "Boom Stow 3-OFF", "Boom Stow 4-OFF"],"Vehicle": {"Number": "V242342","Name": "Truck2324","VIN": "Z232SD43FAS","ESN": 342434234},"Address": {"AddressLine1": "Atrium Building, Blackthorn Road","AddressLine2": "Sandyford Business Park","Locality": "Dublin","PostalCode": "D18 F5X2","AdministrativeArea": "Dublin","Country": "IRL"},"Driver": {"DriverKeyFobID": 234423678,"DriverNumber": "D234","DriverFirstName": "Tim","DriverLastName": "Daruch"}}'
# ID: 234446
# Latitude: -6.3752626
# Longitude": 53.2979679

id = 234446
lat = -6.3752626
lon = 53.2979679

for _ in range(10000):
    val1 = (random() - 0.5)/10
    val2 = (random() - 0.5)/10
    lat += val1
    lon += val2
    
    JSON = JSON_TEMPLATE.replace("REPLACE_WITH_ID",str(id))
    JSON = JSON.replace("REPLACE_WITH_LAT",str(lat))
    JSON = JSON.replace("REPLACE_WITH_LON",str(lon))


    fout.write(JSON + "\n")

    id += 1

fout.close()