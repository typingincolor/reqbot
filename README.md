# reqbot

* [![Build Status](https://travis-ci.org/typingincolor/reqbot.svg?branch=master)](https://travis-ci.org/typingincolor/reqbot) - Master
* [![Build Status](https://travis-ci.org/typingincolor/reqbot.svg?branch=v1)](https://travis-ci.org/typingincolor/reqbot) - v1

## What is it?

reqbot is a programmable mock backend to allow you test your application is calling a web api correctly.

You can tell reqbot what response you want when your client calls the api, all from inside your unit or integration tests.

## Example usage

This is a ruby example

``` ruby
require 'rest-client'
require 'json'

# this is the body of the response you want to recieve
random_body = (0...10).map { ('a'..'z').to_a[rand(26)] }.join

# you can also tell reqbot what headers to put in the response
programmed_response = {'headers' => {'header1' => 'value1'}, 'body' => random_body}

# tell reqbot about the response you want
saved_response_result = RestClient.post 'http://localhost:8080/response',
                                        programmed_response.to_json,
                                        :content_type => :json

# reqbot tells gives you a uuid for the response so you can ask for it
r = JSON.parse(saved_response_result)

# reqbot will store your request in a bucket so you can look at it later, 
# this example will store it in the andrew bucket.
#
# you can specify the response by putting the it's uuid as a path parameter
RestClient.get 'http://localhost:8080/bucket/andrew/repsonse/' + r['uuid']

# or in the X-REQBOT-RESPONSE header
RestClient.get 'http://localhost:8080/bucket/andrew/a/path/to/somewhere', 'X-REQBOT-RESPONSE' => r['uuid']
```

## Magic Headers
There are three magic headers that reqbot uses when calling a bucket:

`X-REQBOT-GO-SlOW` if you set this to a number, then reqbot will wait that number of milliseconds before sending the response.

`X-REQBOT-HTTP-CODE` this tell reqbot what HTTP status code to return to you

`X-REQBOT-RESPONSE` this tells reqbot what response to send back

## Web App
Reqbot has a web application at `/` which allows you to see the requests that reqbot has received.


## Running reqbot

You will need to install redis and gradle...

Then `gradle run`

This will start everything at `http://localhost:8080/`

## Redis settings

The connect settings are found in the application.yml, but can be override using enviroment variables.

The example below connects to database 1 on localhost port 6379

```bash
echo ================================
echo = Setting redis properties
echo ================================
export REQBOT_REDIS_HOST=localhost
export REQBOT_REDIS_PORT=6379
export REQBOT_REDIS_INDEX=1
export REQBOT_REDIS_RESPONSETTL=450
```