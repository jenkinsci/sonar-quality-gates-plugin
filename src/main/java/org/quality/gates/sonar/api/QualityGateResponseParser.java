package org.quality.gates.sonar.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quality.gates.jenkins.plugin.QGException;

import java.util.ArrayList;
import java.util.List;

public class QualityGateResponseParser {

    public QualityGatesStatus getQualityGateResultFromJSON(String jsonString) throws QGException {

        JSONObject qualityGatesProjectStatus = createJSONObjectFromString(jsonString);

        String qualityGatesStatusResult = getStatusFromJSONObject(qualityGatesProjectStatus);

        if (qualityGatesStatusResult.startsWith("OK")) {
            return new QualityGatesStatus("OK");
        }

        return new QualityGatesStatus("ERROR");
    }

    protected JSONObject getLatestEventResult(JSONArray jsonArray) throws QGException {

        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject returnObject;

        int jsonArrayLength = jsonArray.length();

        if (jsonArrayLength == 0) {
            jsonObjects.add(createObjectWithStatusGreen());
        } else {
            for (int i = 0; i < jsonArrayLength; i++) {
                jsonObjects.add(getJSONObjectFromArray(jsonArray, i));
            }
        }

        String mostRecentAlertID = getValueForJSONKey(jsonObjects, 0, "id");
        returnObject = jsonObjects.get(0);

        for (int i = 0; i < jsonObjects.size(); i++) {
            String alertId = getValueForJSONKey(jsonObjects, i, "id");
            if (Integer.parseInt(alertId) > Integer.parseInt(mostRecentAlertID)) {
                returnObject = jsonObjects.get(i);
            }
        }

        return returnObject;
    }

    protected JSONObject createObjectWithStatusGreen() {

        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put("id", "1");
            returnObject.put("dt", "2000-01-01T12:00:00+0100");
            returnObject.put("n", "Green");

            return returnObject;
        } catch (JSONException e) {
            throw new QGException(e);
        }
    }

    protected JSONObject getJSONObjectFromArray(JSONArray array, int index) throws QGException {

        try {
            return array.getJSONObject(index);
        } catch (JSONException e) {
            throw new QGException("The request returned an empty array", e);
        }
    }

    protected String getValueForJSONKey(List<JSONObject> array, int index, String key) throws QGException {

        try {
            return array.get(index).getString(key);
        } catch (JSONException e) {
            throw new QGException("JSON Key was not found", e);
        }
    }

    protected String getStatusFromJSONObject(JSONObject jsonObject) throws QGException {

        try {
            return ((JSONObject) jsonObject.get("projectStatus")).get("status").toString();
        } catch (JSONException e) {
            throw new QGException("JSON Key was not found ", e);
        }
    }

    protected JSONArray createJSONArrayFromString(String jsonString) throws QGException {

        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            throw new QGException("There was a problem handling the JSON response " + jsonString, e);
        }
    }

    protected JSONObject createJSONObjectFromString(String jsonString) throws QGException {

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new QGException("There was a problem handling the JSON response " + jsonString, e);
        }
    }
}
