require 'rest-client'
require 'json'

random_body = (0...50).map { ('a'..'z').to_a[rand(26)] }.join
programmed_response = {'headers' => {'header1' => 'value1'}, 'body' => random_body}
saved_response_result = RestClient.post 'http://localhost:8080/response', programmed_response.to_json, :content_type => :json

r = JSON.parse(saved_response_result)

response_received = RestClient.get 'http://localhost:8080/bucket/andrew/' + r['uuid']

puts "worked: #{random_body.eql? response_received}"