package quality.gates.sonar.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import quality.gates.jenkins.plugin.QGException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QualityGateResponseParser {

    public QualityGatesStatus getQualityGateResultFromJSON(String jsonString) throws QGException {
        JSONArray resultArray = createJSONArrayFromString(jsonString);

        JSONObject latestEventResult = getLatestEventResult(resultArray);

        String gateStatus = getValueForJSONKey(latestEventResult, "n");
        if (gateStatus.startsWith("Green"))
            return new QualityGatesStatus("OK");
        return new QualityGatesStatus("ERROR");
    }

    protected JSONObject getLatestEventResult(JSONArray jsonArray) throws QGException {
        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject returnObject;
        int jsonArrayLength = jsonArray.length();

        if(jsonArrayLength == 0){
            jsonObjects.add(createObjectWithStatusGreen(jsonArray));
        }else {
            for (int i = 0; i < jsonArrayLength; i++) {
                jsonObjects.add(getJSONObjectFromArray(jsonArray, i));
            }
        }

        String latestDate = getValueForJSONKey(jsonObjects, 0, "dt");
        returnObject = jsonObjects.get(0);

        Date latestDateParsed = parseDate(latestDate);
        for (int i = 0; i < jsonObjects.size(); i++) {
            String dt = getValueForJSONKey(jsonObjects, i, "dt");
            Date parsedDateString = parseDate(dt);
            if (latestDateParsed.before(parsedDateString)) {
                returnObject = jsonObjects.get(i);
            }
        }

        return returnObject;
    }

    protected Date parseDate(String date) throws QGException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        date = date.replaceAll("\\+[0-9]{4}", ".000-0100");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new QGException("Wrong date format", e);
        }
    }

    protected JSONObject createObjectWithStatusGreen(JSONArray array) {
        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put("dt", "2000-01-01T12:00:00+0100");
            returnObject.put("n", "Green");
            return returnObject;
        } catch (JSONException e) {
            throw new QGException(e);
        }
    }

    protected JSONObject getJSONObjectFromArray(JSONArray array, int index) throws QGException {
        try {
            if (array.length() == 0) {
                JSONObject returnObject = new JSONObject();
                returnObject.put("dt", "2000-01-01T12:00:00+0100");
                returnObject.put("n", "Green");
                return returnObject;
            } else
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

    protected String getValueForJSONKey(JSONObject jsonObject, String key) throws QGException {
        try {
            return jsonObject.getString(key);
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
}
