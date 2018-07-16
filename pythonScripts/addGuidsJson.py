import json
import uuid


def main():


    #print("Start")

    fin = open("planes00001.1M.json")
    fout = open("planes00001guid.1M.json", "w")

    i = 0
    for line in fin:
        i += 1
        jsonLine = json.loads(line)
        #print jsonLine["id"]

        jsonLine["pid"] = jsonLine["id"]

        jsonLine["id"] = str(uuid.uuid4())


        json.dump(jsonLine, fout)
        fout.write("\n")
        

        #if i > 10: break

    fin.close()
    fout.close()
    print("Processed " + str(i) + " lines.")
        

    


main()

