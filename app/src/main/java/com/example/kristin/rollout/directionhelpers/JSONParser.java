package com.example.kristin.rollout.directionhelpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONParser {

    public static String distance = "";
    
    public String parse(JSONObject jObject) {

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONObject jDistance;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                // Finding Legs Within Routes
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    // Find Distance Within Legs
                    JSONObject getJsonObject = jLegs.getJSONObject(j);
                    jDistance = getJsonObject.getJSONObject("distance");
                    distance = jDistance.get("text").toString();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return distance;
    }
}