#!/usr/bin/env python3

import requests
import getopt
import sys
from getpass import getpass
import json
import urllib.parse
import os
from os.path import expanduser
import csv

debug = False


def usage(arg0):
    print(arg0 + " -h or --help displays this message")
    print(arg0 + " -c clusterUrl -u username -p password")
    print("")
    print("Example: " + arg0 + " -c ${CLUSTER_URL} -u ${USERNAME} -p ${PASSWORD}")
    print("")


def getToken(cargs):
    try:
        opts, args = getopt.getopt(
            cargs, "vhc:u:p:", ["help", "cluster=", "username=", "password="]
        )
    except getopt.GetoptError as err:
        # print help information and exit:
        print(str(err))  # will print something like "option -a not recognized"
        usage(sys.argv[0])
        sys.exit(2)
    cluster = None
    username = None
    password = None
    verbose = False
    for o, a in opts:
        if o == "-v":
            verbose = True
        elif o in ("-h", "--help"):
            usage(sys.argv[0])
            sys.exit()
        elif o in ("-c", "--cluster"):
            cluster = a
        elif o in ("-u", "--username"):
            username = a
        elif o in ("-p", "--password"):
            password = a
        else:
            assert False, "unhandled option"

    if username == None:
        print("username must be specified")
        usage(sys.argv[0])
        sys.exit(3)

    if cluster == None:
        print("cluster must be specified")
        usage(sys.argv[0])
        sys.exit(3)

    home = expanduser("~")

    if password == None:
        # Try to load from file
        try:
            passwords_file = os.path.join(home, ".itemctl", "passwords.txt")
            fin = open(passwords_file)
            for l in fin:
                if l.find("|" + username + "|") > -1:
                    break
            if debug:
                print(l)
            fin.close()
            password = l.split("|")[2].strip()  # Password should be third field on line
        except:
            print("Could not find password")

    if password == None:
        password = getpass()

    print("cluster: {}".format(cluster))
    print("username: {}".format(username))
    if debug:
        print("password: {}".format(password))
    else:
        print("password: {}".format("*********"))

    print("verbose: {}".format(verbose))

    AGOLURL = urllib.parse.urlparse("https://" + cluster)
    AGOL_NETLOC = AGOLURL.netloc
    if AGOL_NETLOC == "us-iotqa.arcgis.com":
        AGOL = "https://qaext.arcgis.com"
    elif AGOL_NETLOC == "us-iotdev.arcgis.com":
        AGOL = "https://devext.arcgis.com"
    elif AGOL_NETLOC == "us2-iotdev.arcgis.com":
        AGOL = "https://devext.arcgis.com"
    elif AGOL_NETLOC == "us-iotdevops.arcgis.com":
        AGOL = "https://devext.arcgis.com"
    elif AGOL_NETLOC == "eu-iotdev.arcgis.com":
        AGOL = "https://devext.arcgis.com"
    elif AGOL_NETLOC == "eu-iotdevops.arcgis.com":
        AGOL = "https://devext.arcgis.com"
    elif AGOL_NETLOC == "eu-iotqa.arcgis.com":
        AGOL = "https://qaext.arcgis.com"
    else:
        print("Unrecognized Cluster Prefix: {}".format(AGOL_NETLOC))
        usage(sys.argv[0])
        sys.exit(4)

    payload = {}
    payload["client_id"] = "analytics4IoT"
    payload["response_type"] = "token"
    payload["expiration"] = "20160"
    payload["redirect_uri"] = "https://" + cluster + "/oauth"
    payload["state"] = "analytics4IoT"
    payload["locale"] = ""

    headers = {}
    headers["Connection"] = "keep-alive"
    headers["Referer"] = "https://" + cluster
    headers["Upgrade-Insecure-Requests"] = "1"
    headers["TE"] = "Trailers"

    auth_url = AGOL + "/sharing/rest/oauth2/authorize"
    print(auth_url)

    resp = requests.post(auth_url, headers=headers, data=payload)

    print(resp.status_code)
    if debug:
        print(resp.headers)
    if debug:
        print(resp.text)

    lines = resp.text.split("\n")

    for line in lines:
        if debug:
            print(line)
        if line.find("var oAuthInfo =") > -1:
            oauthLine = line
            break

    if debug:
        print(oauthLine)
    jsonLine = json.loads(oauthLine.split("=")[1])

    oauth_state = jsonLine["oauth_state"]

    if debug:
        print(oauth_state)

    payload = {}
    payload["oauth_state"] = oauth_state
    payload["authoricp ze"] = "true"
    payload["username"] = username
    payload["password"] = password

    headers = {}
    headers["Content-Type"] = "application/x-www-form-urlencoded"
    headers["Origin"] = AGOL
    headers["Connection"] = "keep-alive"
    headers["Referer"] = AGOL + "/sharing/rest/oauth2/authorize"
    headers["Upgrade-Insecure-Requests"] = "1"
    headers["TE"] = "Trailers"

    signin_url = AGOL + "/sharing/oauth2/signin"
    resp = requests.post(
        signin_url, headers=headers, data=payload, allow_redirects=True
    )

    print(signin_url)
    print(resp.status_code)
    if debug:
        print(resp.headers)
    if debug:
        print(resp.text)
    if debug:
        print("")
    redir_resp = resp.history[0]
    redir_resp_headers = redir_resp.headers
    location = redir_resp_headers["Location"].replace("#", "?", 1)
    params = dict(urllib.parse.parse_qsl(urllib.parse.urlsplit(location).query))
    if debug:
        print(params)

    print("token: " + params["access_token"])

    config_folder = os.path.join(home, ".itemctl")

    if not os.path.isdir(config_folder):
        os.makedirs(config_folder)

    fout = open(os.path.join(config_folder, username + ".token"), "w")
    fout.write(params["access_token"])
    fout.close()

    fout = open(os.path.join(config_folder, "username"), "w")
    fout.write(username)
    fout.close()


def main():
    getToken(sys.argv[1:])


if __name__ == "__main__":
    main()
