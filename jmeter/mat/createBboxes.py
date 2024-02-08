#!/usr/bin/env python3
import random
n = 14
i = 0

while i < n:
    i += 1
    filename = r"bbox_samples" + str(i) + "0.txt"
    fout = open(filename, "w")
    num_samples = 10000
    cnt = 0
    size = 10*i

    while cnt < num_samples:
        lon = random.uniform(-180,179-size)
        lat = random.uniform(-80,80-size)

        lllon = str(lon)
        lllat = str(lat)
        urlon = str(lon + size)
        urlat = str(lat + size)

        bbox = lllon + "," + lllat + "," + urlon + "," + urlat + "\n"

        fout.write(bbox)
        cnt += 1

    fout.close()
