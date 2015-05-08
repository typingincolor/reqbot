require 'rest-client'
require 'json'

random_body = (0...10).map { ('a'..'z').to_a[rand(26)] }.join
programmed_response = {'headers' => {'header1' => 'value1'}, 'body' => random_body}
saved_response_result = RestClient.post 'http://localhost:8080/response',
                                        programmed_response.to_json,
                                        :content_type => :json

r = JSON.parse(saved_response_result)

response_received = []

response_received.push RestClient.get 'http://localhost:8080/bucket/andrew/' + r['uuid']
response_received.push RestClient.get 'http://localhost:8080/bucket/andrew', 'X-REQBOT-RESPONSE' => r['uuid']
response_received.push RestClient.post 'http://localhost:8080/bucket/andrew/' + r['uuid'], "stuff", :content_type => 'text/plain'
response_received.push RestClient.post 'http://localhost:8080/bucket/andrew', "stuff",:content_type => 'text/plain', 'X-REQBOT-RESPONSE' => r['uuid']

response_received.each_with_index do |result, i|
  puts "#{i} worked: #{random_body.eql? result}     #{random_body} #{result}"
end
