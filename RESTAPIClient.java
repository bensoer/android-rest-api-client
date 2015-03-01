import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

enum ClientState { ROUTE_INVALID, BODY_INVALID, READY, SENT_SUCCESS, SENT_FAILED, RECEIVED_FAILED, PARSED_FAILED, RECEIVED_SUCCESS, PARSED_SUCCESS };

/**
 * Created by Ben on 08/12/2014.
 */
public class RESTAPIClient {

    private String baseURL;

    private final HttpClient httpClient = new DefaultHttpClient();
    private HttpGet httpGet;
    private HttpPost httpPost;
    private HttpPut httpPut;
    private HttpDelete httpDelete;
    private JSONObject response;

    private String responseString;

    private ClientState status;


    RESTAPIClient(String baseURL){
        this.baseURL = baseURL;
        response = new JSONObject();
        responseString = new String();
        status = ClientState.READY;


    }

    public void setBaseURL(String baseURL){
        this.baseURL = baseURL;
    }

    private void resetJSON(){
        response = new JSONObject();
        responseString = new String();
    }

    public void getData(String route){
        try{
            status = ClientState.READY;
            resetJSON();
            httpGet = new HttpGet(new URI(this.baseURL + route));

            ExecuteCall ec = new ExecuteCall(httpGet);
            ec.execute();

        }catch(URISyntaxException urie){
            System.out.println("The URL for the GET route is invalid");
            Log.d("RESTAPIClient - getData", "The URL for this route is invalid. \n"
                    + "The route given is: " + this.baseURL + route);
            status = ClientState.ROUTE_INVALID;
            return; //should return a state so that program can also react to invalid routes ?
        }
    }

    public void putData(String route, JSONObject bodyContent){
        try{
            status = ClientState.READY;
            resetJSON();
            httpPut = new HttpPut(new URI(this.baseURL + route));
            StringEntity body = prepareBodyContent(bodyContent);
            httpPut.setEntity(body);

            ExecuteCall ec = new ExecuteCall(httpPut);
            ec.execute();

        }catch(URISyntaxException urie){
            System.out.println("The URL for the PUT route is invalid");
            Log.d("RESTAPIClient - getData", "The URL for this route is invalid. \n"
                    + "The route given is: " + this.baseURL + route);
            status = ClientState.ROUTE_INVALID;
            return; //should return a state so that program can also react to invalid routes ?
        }catch(UnsupportedEncodingException uee){
            status = ClientState.BODY_INVALID;
            return; //should return a state so that program can also react to invalid body
        }catch(IOException ioe){
            status = ClientState.SENT_FAILED;
            return; //should return a state so that program can also react to failed transmissions
        }

    }
    public void postData(String route, JSONObject bodyContent){
        try{
            status = ClientState.READY;
            resetJSON();
            httpPost = new HttpPost(new URI(this.baseURL + route));
            StringEntity body = prepareBodyContent(bodyContent);
            httpPost.setEntity(body);

            ExecuteCall ec = new ExecuteCall(httpPost);
            ec.execute();

        }catch(URISyntaxException urie){
            System.out.println("The URL for the POST route is invalid");
            Log.d("RESTAPIClient - getData", "The URL for this route is invalid. \n"
                    + "The route given is: " + this.baseURL + route);
            status = ClientState.ROUTE_INVALID;
            return; //should return a state so that program can also react to invalid routes ?
        }catch(UnsupportedEncodingException uee) {
            status = ClientState.BODY_INVALID;
            return; //should return a state so that program can also react to invalid body
        }
    }
    public void deleteData(String route){
        try{
            status = ClientState.READY;
            resetJSON();
            httpDelete = new HttpDelete(new URI(this.baseURL + route));

            ExecuteCall ec = new ExecuteCall(httpDelete);
            ec.execute();

        }catch(URISyntaxException urie) {
            System.out.println("The URL for the GET route is invalid");
            Log.d("RESTAPIClient - getData", "The URL for this route is invalid. \n"
                    + "The route given is: " + this.baseURL + route);
            status = ClientState.ROUTE_INVALID;
            return; //should return a state so that program can also react to invalid routes ?
        }
    }

    private StringEntity prepareBodyContent(JSONObject body) throws UnsupportedEncodingException{
        try{
            final StringEntity entity = new StringEntity(body.toString(), HTTP.UTF_8);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            return entity;
        }catch(UnsupportedEncodingException uee){
            System.out.println("The JSON Format is not supported");
            Log.d("RESTAPIClient - JSON", "The JSON Format is not supported");
            uee.printStackTrace();
            throw uee;
        }

    }

    public String getRawResponse(){
        return responseString;
    }
    public JSONObject getRawJSONObject(){
        return response;
    }

    public String getState(){
        return status.name();
    }




    private class ExecuteCall extends AsyncTask<Void,Void,Boolean>{

        private HttpRequestBase callEntity;

        ExecuteCall(HttpRequestBase call){
            this.callEntity = call;
        }

        public Boolean doInBackground(Void... params){
            try{
                HttpResponse httpResponse = httpClient.execute(callEntity);
                HttpEntity httpEntity = httpResponse.getEntity();

                status = ClientState.SENT_SUCCESS;
                if(httpEntity != null){ // null means there is no body
                    responseString = EntityUtils.toString(httpEntity);
                    status = ClientState.RECEIVED_SUCCESS;
                    //response.getJSONObject(responseString);
                }

                return true;

            }catch(IOException ioe){
                System.out.println("Http was unable to acces I/O");
                Log.d("RESTAPIClient - HTTP", "HTTP was unable to access I/O");
                status = ClientState.SENT_FAILED;
                return false;
            }
        }

        public void onPostExecute(boolean success){
            if(success){
                try{
                    response.getJSONObject(responseString);
                }catch(JSONException jsone){
                    System.out.println("The returned data is not in a valid JSON format and could not be "
                            + "parsed");
                    Log.d("RESTAPIClient - JSON", "The returned data is not in a valid JSON format and"
                            + "could not be parsed");
                    jsone.printStackTrace();
                    //TODO return or set a state of some kind to tell user the call has partialy failed
                    status = ClientState.PARSED_FAILED;
                    return;

                }
                status = ClientState.PARSED_SUCCESS;
            }
        }

    }


}