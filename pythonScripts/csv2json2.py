import json
import csv

if __name__ == '__main__':
    print "Start"

    fin = open("planes.csv")
    fout = open("planes.json", "w")

    csvFin = csv.reader(fin)

    n = 0

    for field in csvFin:

        row = {}

        row['id'] = int(field[0])
        row['ts'] = long(field[1])
        row['spd'] = float(field[2])
        row['dis'] = float(field[3])
        row['brg'] = float(field[4])
        row['rid'] = int(field[5])
        row['org'] = field[6]
        row['dst'] = field[7]
        row['s2d'] = int(field[8])
        row['lon'] = float(field[9])
        row['lat'] = float(field[10])
            
        jsonStr = json.dumps(row)
    
        fout.write(jsonStr + "\n")


    fin.close()
    fout.close()
