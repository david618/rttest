import json
import uuid


def main():


    #print("Start")

    fin = open("planes00001")
    fout = open("planes00001g", "w")

    i = 0
    for line in fin:
        i += 1
        guid = str(uuid.uuid4())
        fout.write(guid + "," + line);
        

        #if i > 10: break

    fin.close()
    fout.close()
    print("Processed " + str(i) + " lines.")
        
main()

