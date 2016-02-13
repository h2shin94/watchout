package grouplima.watchout;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.*;
import java.util.HashSet;
import grouplima.watchout.Hazard;
import grouplima.watchout.Location;
import grouplima.watchout.ServerInterface;

public class HazardManager {

    Set hazardSet = new HashSet<Hazard>();

    public Set getHazardSet(){
        return hazardSet;
    }

    Location currLoc = new Location(52.213388, 0.102448);

    public HazardManager(){
       populateHazardSet(currLoc);
    }

    public static volatile boolean flag = true;

    public void populateHazardSet(Location currentLoc){
        final Location currentLocation = currentLoc;
        flag = true;
        new Thread() {
            @Override
            public void run() {
                while (flag) {
                    int numberOfHazards;
                    JSONObject downloadedJSON;
                    JSONArray results;
                    ServerInterface serverInterface = new ServerInterface();
                    try {
                        downloadedJSON = serverInterface.getHazards(currentLocation);
                        numberOfHazards = downloadedJSON.getInt("size");
                        results = downloadedJSON.getJSONArray("hazards");
                        System.out.println(downloadedJSON.toString());
                        for (int i = 1; i <= numberOfHazards; i++) {
                            JSONObject thisHazardJSON = results.getJSONObject(i - 1);
                            Hazard newHazard = new Hazard((JSONObject) thisHazardJSON.get("hazard" + i));
                            System.out.println(newHazard.getLatitude());
                            hazardSet.add(newHazard);
                        }
                        flag = false;
                    } catch (JSONException e) {
                        flag = false;
                        e.printStackTrace();
                    } catch (IOException e) {
                        flag = false;
                        e.printStackTrace();

                    }
                }
            }
        }.start();
    }

    public void updateHazard(Hazard update){
        JSONArray upArray = new JSONArray();
        upArray.put(update.toJSON());
        JSONObject returnJson = new JSONObject();
        try {
            returnJson.put("update", upArray);
            ServerInterface serverInterface = new ServerInterface();
            serverInterface.uploadHazards(returnJson);
        } catch (JSONException je){
            je.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void newHazard(Hazard newHazard){
        JSONArray upArray = new JSONArray();
        upArray.put(newHazard.toJSON());
        JSONObject returnJson = new JSONObject();
        try {
            returnJson.put("new", upArray);
            ServerInterface serverInterface = new ServerInterface();
            serverInterface.uploadHazards(returnJson);
        } catch (JSONException je){
            je.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
