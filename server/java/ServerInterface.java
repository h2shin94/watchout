import java.io.*;
import java.net.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
* <h1>Server Interaction for Watchout</h1>
* Implements a class that allows retrieval and update of
* data on the database.
* <p>
*
* @author  Hyun-ho Shin
*/

public class ServerInterface{

    private static String site;
    private static String charset = java.nio.charset.StandardCharsets.UTF_8.name();

   /**
   * Constructor to create an instance of ServerInterface
   * @param Nothing.
   * @return ServerInterface object
   */
    public ServerInterface(){
        site = "http://watchout.h2shin.com";
    }

    /**
   * Method to return a set of hazard data within 10 miles of given location
   * @param location An instance of Location that stores latitude and longitude data
   * @return JSONObject an instance of JSONObject with json encoded hazard data
   * @throws IOException
   * @throws JSONException
   */

    public JSONObject getHazards(Location location) throws IOException, JSONException{
        //request hazards from long / lat data and retrieve json hazards
        //tested and working!

        String longitude = ""+location.getLongitude();
        String latitude = ""+location.getLatitude();

        String query = String.format("longitude=%s&latitude=%s", longitude, latitude);
        //encode the post query

        URL url = new URL(site + "/request.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        System.out.println(query);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(query);
        writer.flush();
        writer.close();


        BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer hazards = new StringBuffer();
        String inputLine;
        while ((inputLine = response.readLine()) != null){
            hazards.append(inputLine);
        }

        response.close();

        return new JSONObject(hazards.toString());
    }

    /**
   * Method to update database on the server with new and changed hazards given as a JSONObject instance
   * @param uploadHazrds A JSONObject instance with encoded new and update hazards
   * @return Nothing
   * @throws IOException
   */
    public void uploadHazards(JSONObject uploadHazards) throws IOException{
        //upload hazards in json to php (to use json_decode)
        //Hazard parameter should be encoded as json already

        URL url = new URL(site + "/update.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestMethod("POST");

        OutputStream writer = conn.getOutputStream();
        writer.write(uploadHazards.toString().getBytes("UTF-8")); //toString produces compact JSONString
        //no white space
        writer.close();
        //read response (success / error)

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer response = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }
        in.close();
    }

    /**
    *A Temporary testing main method
    */
    public static void main(String[] args){
        ServerInterface server = new ServerInterface();
        // try {
        //    System.out.println(server.getHazards(new Location(52.213388, 0.102448)).toString(4));
        // }catch (IOException | JSONException e) {
        //    e.printStackTrace();
        // }

        try{
            JSONObject json = new JSONObject(
                    "{\"new\":[{\"latitude\":\"52.1234\", \"longitude\":\"52.1234\",\"title\":\"New Hazard!\",\"reported\":\"16-12-12 11:11:11\",\"expires\":\"17-11-11 11:11:11\",\"description\":\"new inserted hazard\", \"acks\":\"0\", \"diss\":\"0\"}], \"update\":[{\"id\":\"5\", \"expires\":\"17-11-11 11:11:11\", \"acks\":\"6\", \"diss\":\"6\"}]}");

            server.uploadHazards(json);
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}