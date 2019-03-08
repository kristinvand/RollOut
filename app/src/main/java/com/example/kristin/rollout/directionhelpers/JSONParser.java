package com.example.kristin.rollout.directionhelpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;


public class JSONParser {

    public static String distance = "";
    public static String duration = "";
    public static String[] resultArray = new String[2];

    public String[] parse(JSONObject jObject) {

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONObject jDistance;
        JSONObject jDuration;
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
                    jDuration = getJsonObject.getJSONObject("duration");
                    duration = jDuration.get("text").toString();
                    distance = jDistance.get("text").toString();
                    resultArray[0] = distance;
                    resultArray[1] = duration;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return resultArray;
    }
}