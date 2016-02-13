package grouplima.watchout;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Hazard {

    private  int id, acks, diss;
    private  String title, description;
    private  double latitude, longitude;
    private  java.util.Date reported, expires;

    public Hazard(JSONObject jsonInput) {

        try {
            id = jsonInput.getInt("id");
            acks = jsonInput.getInt("acks");
            diss = jsonInput.getInt("diss");
            title = jsonInput.getString("title");
            description = jsonInput.getString("description");
            latitude = jsonInput.getDouble("latitude");
            longitude = jsonInput.getDouble("longitude");
            SimpleDateFormat dateParser = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
            //reported = dateParser.parse(jsonInput.getString("reported"));
            //Set expires to 1 day after reported just as a test for now
            //expires.setTime(reported.getTime() + 86400000);

        } catch (JSONException je) {

            je.printStackTrace();

        } //catch (ParseException pe){

           // pe.printStackTrace();

        //}

    }

    //LatLng required by maps API to draw pin, use this for drawing pins
    public LatLng getLatLong(){
        return new LatLng(latitude, longitude);
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public int getAcks(){
        return acks;
    }

    public int getId(){
        return id;
    }

    public int getDiss(){
        return diss;
    }

    public void increaseAcks(){
        acks++;
    }

    public void increaseDiss(){
        diss++;
    }

    public JSONObject toJSON(){
        JSONObject outputJSON = new JSONObject();
        try {
            outputJSON.put("latitude", latitude);
            outputJSON.put("longitude", longitude);
            outputJSON.put("title", title);
            outputJSON.put("id", id);
            outputJSON.put("acks", acks);
            outputJSON.put("diss", diss);
            outputJSON.put("description", description);

        } catch ( JSONException e) {

            e.printStackTrace();

        }

        return outputJSON;
    }

}
