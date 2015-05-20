require 'rest-client'
require 'json'

bucket = ARGV[0] || "andrew"

random_body = (0...10).map { ('a'..'z').to_a[rand(26)] }.join
programmed_response = {'headers' => {'header1' => 'value1'}, 'tags' => [:tag1, :tag2], 'body' => random_body}
saved_response_result = RestClient.post 'http://localhost:8080/responses',
                                        programmed_response.to_json,
                                        :content_type => :json

r = JSON.parse(saved_response_result)

response_received = []

response_received.push RestClient.get "http://localhost:8080/#{bucket}/response/#{r['uuid']}"
response_received.push RestClient.get "http://localhost:8080/#{bucket}/path/to/something", 'X-REQBOT-RESPONSE' => r['uuid']
response_received.push RestClient.post "http://localhost:8080/#{bucket}/response/#{r['uuid']}", "stuff", :content_type => 'text/plain'
response_received.push RestClient.post "http://localhost:8080/#{bucket}/path/to/somethingelse", "stuff",:content_type => 'text/plain', 'X-REQBOT-RESPONSE' => r['uuid']
response_received.push RestClient.get "http://localhost:8080/#{bucket}/response/#{r['uuid']}?a=1&b=2"
response_received.push RestClient.get "http://localhost:8080/#{bucket}/path/to/something?c=1&d=4", 'X-REQBOT-RESPONSE' => r['uuid']
response_received.push RestClient.post "http://localhost:8080/#{bucket}/response/#{r['uuid']}?foo=bar", "stuff", :content_type => 'text/plain'
response_received.push RestClient.post "http://localhost:8080/#{bucket}/path/to/somethingelse?twee=lalala", "stuff",:content_type => 'text/plain', 'X-REQBOT-RESPONSE' => r['uuid']


response_received.each_with_index do |result, i|
  puts "#{i} worked: #{random_body.eql? result}     #{random_body} #{result}"
end
