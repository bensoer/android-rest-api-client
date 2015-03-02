# android-rest-api-client

A RESTful Web API Client for Android designed to make API calls with JSON easy and minimal in your code

# Usage
Download the file and add your package name if required at the top of the file

To create an instance of the client call its constructor like so:
````java
RESTAPIClient client = new RESTAPIClient("http://api.google.com/api");
````
The parameter passed in the constructor is the base point for all of your api calls. Future calls will only require the route path. You can change the base path at anytime after creation with:
````java
client.changeBaseURL("http://myapi.com/api");
````
Now using your instance you can make all of the 4 standard GET, PUT, POST, and DELETE REST calls with the appropriate methods. See the API section for more details on these calls
````java
client.getData("/news");
client.postData("/news", jsonBody);
client.putData("/news/article/1", jsonBody);
client.deleteData("/news/article/5");
````
The json response from the server is captured within the instance after the call is made. You can retrieve it either as a stringified json or as a json object. Note in this current version, the response <b>must</b> come back as a json object. JSONArray support is still under development
````java
JSONObject response = client.getRawJSONObject();
String response = client.getRawResponse();
````
After that your ready to make another API call. At each call the previous captured json data is overwritten.

#API
Create Instance:
````java
RESTAPIClient client = new RESTAPIClient(String baseURL);
````
Change the Base URL
````java
client.setBaseURL(String baseURL);
````
Make a GET request call
````java
client.getData(String route);
````
Make a POST request call with a json body. Create a json object and assign it values. Then pass the instance to this postData method. To make a post request with an empty body, pass a new empty instance of a JSONObject. Passing null may work aswell but has not been tested
````java
client.postData(String route, JSONObject bodyContent);
````
Make a POST request call with a json body. Create a json object and assign it values. Then pass the instance to this putData method. To make a post request with an empty body, pass a new empty instance of a JSONObject. Passing null may work aswell but has not been tested
````java
client.putData(String route, JSONObject bodyContent);
````
Make a DELETE request call
````java
client.deleteData(String route);
````
Get json Response as a String or JSONOBject. Note the server must respond with a json object
````java
client.getRawResponse(); // returns serialized json as a string of the response
client.getRawJSONObject(); // returns JSONObject of response
````
