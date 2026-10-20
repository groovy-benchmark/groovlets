# LAPPS Grid API Services

The services available at `http://api.lappsgrid.org` are typically proof-of-concept prototype services implemented as [Groovlets](http://docs.groovy-lang.org/latest/html/documentation/servlet-userguide.html).  A Groovlet is simply a Groovy script that is run inside a Java Servlet container.  Currently calls to `http://api.lappsgrid.org` will forward to a Jetty server (see server.groovy) that serves these scripts as web services.

**Note** The Groovlets repository has a GitHub webhook enabled so pushing code to the *master* branch automatically deploys these services to api.lappsgrid.org. 

### Service Index
- [pull](#pull) - webhook used by GitHub
- [password](#password) - generates random strings for use as passwords or secret keys
- [uuid](#uuid) - generates a type 4 UUID
- [lookup](#lookup) - lookup persistent identifiers for LAPPS services
- [producers](#producers) - find all LAPPS services that produce a given annotation type
- [services](#services) - list all services installed on a particular node
- [metadata](#metadata) - fetch the metadata for a given service
- [soap-proxy](#soap-proxy) - RESTful proxy for LAPPS SOAP services
- [json-compiler](#json-compiler) - Compiles the LAPPS Alternate Syntax into JSON

# Available Services

<a name="pull"/>

## http://api.lappsgrid.org/pull

The **webhook** that GitHub will POST messages to when code is pushed to the master branch of a repository.  After doing some error and sanity checking the service calls the bash script /var/lib/downloads/scripts/pull.sh which does a `git pull origin master` in the appropriate target directory. 

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>POST</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/pull</td>
     </tr>
     <tr>
        <td><b>Accepts</b></td>
        <td>application/json</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            text/plain
        </td>
     </tr>
</table>

**Notes**

The *pull* service will only accept requests originating from GitHub and the JSON data **MUST** be signed with the GitHub private key.

<a name="password"></a>

## http://api.lappsgrid.org/password

The password service uses a cryptographically secure random number generator to produce a random sequence of characters.  Use the password service any time a secure password and/or security key is required.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/password?type=:type&length=:length&chars=:string</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            text/plain
        </td>
     </tr>
</table>

**URL Parameters**

- **type** one of *default*, *safe*, or *hex*
- **chars** the set of characters used to generate the password
- **length** the number of characters to produce.

If *chars* is specified then *type* is ignored.  Returns *400 Bad Request* if neither of *type* or *chars* is specified, or if *type* is not one of *default*, *safe*, or *hex*.

**Types**

- **default** the set of most printable ASCII characters (minus quotes).
- **safe** letters (upper and lower case), digits, and the characters _-=,.<br/>
The *safe* type is intended to be used to generate passwords or keys that can be safely included in scripts.
- **hex** the hexadecimal digits 0123456789abcde

**Examples**

```
> curl http://api.lappsgrid.org/password
=F9sXKGn2lteDdvk

> curl http://api.lappsgrid.org/password?type=hex&length=32
a0edc2709c4ebb9ee43f35415c12af5b

> curl http://api.lappsgrid.org/password?chars=01&length=32
00001001101101111101000000001001
```
The password service will always produce at least 16 characters of output.

<a name="uuid"></a>

## http://api.lappsgrid.org/uuid

Generates a *Type 4* UUID (Universally Unique IDentifier) according to [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt). In practice the service simply calls `java.util.UUID.randomUUID().toString()` 

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/uuid<br/></td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            text/plain
        </td>
     </tr>
</table>

**Example**

```bash
> curl http://api.lappsgrid.org/uuid

d085f907-0c00-4dd6-b500-5f98cbc0827f
```

<a name="lookup"/>

## http://api.lappsgrid.org/lookup

The LAPPS Grid Peristent Identifier Registry. Use this service to lookup the PID assigned to any LAPPS web service.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td valign="top"><b>URL</b></td>
        <td>/lookup?url=:url<br/>
        /lookup?pid=:pid
        </td>
     </tr>
     <tr>
        <td valign="top"><b>Returns</b></td>
        <td>
            text/plain<br/>
            text/html
        </td>
     </tr>
</table>

**URL Parameters**

- **url** the URL of a LAPPS Grid service. In practice this can be any string.
- **pid** a persistent identifier assigned by the PID registry.

If *url* is specified a PID value will always be returned.  If the service does not already have a PID assigned a new one will be generated.

If *pid* is specified, but no such PID has been assigned by the registry service the response status will be *400 Bad Request*.

If neither *url* nor *pid* is specified an HTML page showing all currently assigned identifiers is returned.

**Example**

```bash
> curl http://api.lappsgrid.org/lookup?url=http://vassar.lappsgrid.org/invoker/anc:stanford.tagger_2.0.0
eca758ba-b298-4961-9670-b1a664b681c2

> curl http://api.lappsgrid.org/lookup?pid=9c32f045-9165-4327-bae2-adb7bf7f57b2
http://eldrad.cs-i.brandeis.edu:8080/service_manager/invoker/brandeis_eldrad_grid_1:stanfordnlp.postagger_2.0.1
```

<a name="producers"></a>

## http://api.lappsgrid.org/producers

Returns a list of services that produce a given annotation type.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/producers?annotation=:url</td>
     </tr>
     <tr>
        <td valign="top"><b>Returns</b></td>
        <td>
            text/html<br/>
            text/plain<br/>
            application/xml<br/>
            application/json
        </td>
     </tr>
</table>

If an `Accept` header is not specified `application/json` will be returned.

**Example**

```
> curl -i -H 'Accept: text/plain' http://api.lappsgrid.org/producers?annotation=http://vocab.lappsgrid.org/VerbChunk

HTTP/1.1 200 OK
Server: nginx/1.4.6 (Ubuntu)
Date: Thu, 23 Mar 2017 16:53:01 GMT
Content-Type: text/plain; charset=utf-8
Transfer-Encoding: chunked
Connection: keep-alive
Lapps-Result-Set-Size: 2

http://vassar.lappsgrid.org/invoker/anc:gate.vpchunker_2.1.0
http://vassar.lappsgrid.org/invoker/anc:gate.vpchunker_2.0.0
```

**NOTE** The database used by the `/producers` service is not up to date and the output from this
service should only be used for testing.

<a name="services"></a>
  
## http://api.lappsgrid.org/services

Display the services installed on a service manager instance.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/services/:node?:key=:value [&:key=value...]</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            application/json, text/html
        </td>
     </tr>
</table>

**Path Parameters**

- **node** One of `vassar` or `brandeis`

**URL Parameters**

- **key** a Service Manager search key used to filter services
- **value** the value to be matched. The value matches if it is a substring of the key's value. Text matches are case-insensitive.

Valid search keys are:

* active
* endpointUrl
* instanceType
* ownerUserId
* registeredDate
* serviceDescription
* serviceId
* serviceName
* serviceType
* serviceTypeDomain
* updatedDate

If no key/value pairs are specified as search terms then all services registered on the Service Manager instance will be listed.

**Example**

```
curl -H 'Accept: text/html' http://api.lappsgrid.org/service/brandeis
curl http://api.lappsgrid.org/service/vassar?serviceName=gate
```

<a name="metadata"></a>
  
## http://api.lappsgrid.org/metadata

Display metadata about a single service.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>GET</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/metadata?id=:id</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            application/json, text/html, application/x-cmdi+xml
        </td>
     </tr>
</table>

If an `Accept` header is not specified `application/json` will be returned.

**URL Parameters**

- **id** the ID, including gridId, of the service to get metadata from.

**Example**

```
curl http://api.lappsgrid.org/metadata?id=anc:gate.tokenzier_2.2.0
curl -H 'Accept: application/x-cmdi+xml' http://api.lappsgrid.org/metadata?id=anc:stanford.tagger_2.0.0
```

<a name="soap-proxy"></a>
  
## http://api.lappsgrid.org/soap-proxy

A RESTful proxy service for LAPPS Grid SOAP services.
  
<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>POST</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/soap-proxy</td>
     </tr>
     <tr>
        <td><b>Accepts</b></td>
        <td>application/json<br/>text/plain</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            application/json
        </td>
     </tr>
</table>

**HTTP Headers**

<table>
    <tr>
        <td style="width:20%"><b>LAPPS-PROXY-URL</b></td>
        <td>The URL of the SOAP service to be invoked</td>
     </tr>
     <tr>
        <td><b>LAPPS-PROXY-USERNAME</b></td>
        <td>The service manager username (optional)</td>
     </tr>
     <tr>
        <td><b>LAPPS-PROXY-PASSWORD</b></td>
        <td>the service manager password for the user specified above (optional)</td>
     </tr>
</table>


**Data Format**

If `application/json` is POSTed to the service then the JSON should represent a LAPPS [Data](http://wiki.lappsgrid.org/org.lappsgrid.serialization/groovydoc/org/lappsgrid/serialization/Data.html) object.  The service URL, username and password can either be specified in the `parameters` field of the Data object or via HTTP headers.  If the service URL is specified as a header then the username and password, if provided, must also be specified as headers.  If the parameters appear in the JSON an are specified as HTTP headers then the HTTP headers take precedence. 

```
{
    parameters {
        url: [string],
        username: [string] (optional),
        password: [string] (optional),
    }
    discriminator: [URI],
    payload: [string]
}

```
**Example**

Assume *data.json* contains the following:

```
{
    "parameters": {
        "url": "http://vassar.lappsgrid.org/invoker/anc:stanford.tokenizer_2.0.0",
        "username": "john_doe",
        "password": "s3cr3t"
    },
    "discriminator": "http://vocab.lappsgrid.org/ns/media/text",
    "payload": "Karen flew to New York."
}
```

```
> curl -i -H 'Content-Type: application/json' -X POST -d @data.json http://api.lappsgrid.org/soap-proxy

HTTP/1.1 200 OK
Server: nginx/1.4.6 (Ubuntu)
Date: Thu, 23 Mar 2017 19:16:33 GMT
Content-Type: application/json; charset=UTF-8
Transfer-Encoding: chunked
Connection: keep-alive

{
    "discriminator": "http://vocab.lappsgrid.org/ns/media/jsonld#lif",
    "payload": {
        "@context": "http://vocab.lappsgrid.org/context-1.0.0.jsonld",
        "text": {
            "@value": "Karen flew to New York"
        },
        "views": [
            ...
        ]
    }
}
```

Plain text can also be POSTed to services that accept http://vocab.lappsgrid.org/ns/media/text, which is mostly the tokenizers.

```bash
curl -i -X POST -H "Content-type: text/plain" -H "LAPPS-PROXY-URL: http://vassar.lappsgrid.org/invoker/anc:stanford.tokenizer_2.0.0" -d "Karen flew to New York." http://localhost:8888/soap-proxy
```

**Notes**

The *username* and *password* are the user's credentials on the Service Manager instance where the servcie resides.  If either is omitted the default value *tester* will be used.

<a name="json-compiler"></a>
  
## http://api.lappsgrid.org/json-compiler

Compiles the [LAPPS Alternative Syntax](https://github.com/oanc/org.anc.json.schema-compiler)
into an equivalent JSON document.

<table>
    <tr>
        <td style="width:20%"><b>Methods</b></td>
        <td>POST</td>
     </tr>
     <tr>
        <td><b>URL</b></td>
        <td>/json-compiler</td>
     </tr>
     <tr>
        <td><b>Accepts</b></td>
        <td>text/plain</td>
     </tr>
     <tr>
        <td><b>Returns</b></td>
        <td>
            application/json
        </td>
     </tr>
</table>

```
> curl -i -X POST -H "Content-type: text/plain" --data "type object; properties { name string }" http://api.lappsgrid.org/json-compiler

HTTP/1.1 200 OK
Server: nginx/1.4.6 (Ubuntu)
Date: Sun, 18 Jun 2017 18:30:18 GMT
Content-Type: application/json; charset=utf-8
Transfer-Encoding: chunked
Connection: keep-alive

{
    "type": "object",
    "properties": {
        "name": "string"
    }
}
```
