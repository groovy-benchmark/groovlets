#!/usr/bin/env bash

file="src/test/resources/karen.lif"

type="Content-type: application/json"
url="LAPPS-PROXY-URL: http://vassar.lappsgrid.org/invoker/anc:stanford.tokenizer_2.0.0"
username="LAPPS-PROXY-USERNAME: weblicht" 
password="LAPPS-PROXY-PASSWORD: weblicht"

server=http://localhost:8888/soap-proxy
if [ "$1" = "remote" ] ; then
    server=http://api.lappsgrid.org/soap-proxy
    shift
else
    # Ensure the local server is running.
    if (ps aux | grep server.groovy | grep java) ; then
        echo "Using the local Groovy server."
    else
        echo "Please start the server.groovy script first."
        exit 1
    fi
fi

if [ -z "$1" ] ; then
    echo "URL via headers to $server"
    curl -i -X POST -H "$type" -H "$url" -d @src/test/resources/karen.lif $server
    exit 0
fi

echo "Testing $1 on $server"
case $1 in
    headers)
        curl -i -X POST -H "$type" -H "$url" -H "$username" -H "$password" --data-binary @src/test/resources/karen.lif $server
        ;;
    url)
        curl -i -X POST -H "$type" -H "$url" -d @src/test/resources/karen.lif $server
        ;;
    params)
        curl -i -X POST -H "$type" -d @src/test/resources/karen-parameters.lif $server
        ;;
    baduser)
        curl -i -X POST -H "$type" -H "$url" -H "LAPPS-PROXY-USERNAME: unknown" -H "$password" --data-binary @src/test/resources/karen.lif $server
        ;;
    badpass)
        curl -i -X POST -H "$type" -H "$url" -H "$username" -H "LAPPS-PROXY-PASSWORD: n0s3cr37" --data-binary @src/test/resources/karen.lif $server
        ;;
    text)
        curl -i -X POST -H "Content-type: text/plain" -H "$url" -d "Karen flew to New York." $server
        ;;
    *)
        echo "Invalid option: one of headers, url, params, baduser, badpass, or text"
        exit 1
        ;;
esac