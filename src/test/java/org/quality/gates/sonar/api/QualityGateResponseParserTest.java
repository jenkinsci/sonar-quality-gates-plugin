package org.quality.gates.sonar.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.quality.gates.jenkins.plugin.QGException;
import org.quality.gates.sonar.api.QualityGateResponseParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QualityGateResponseParserTest {

    public static final String COM_OPENSOURCE_QUALITY_GATES = "com.opensource:quality-gates";

    public static final String GREEN_WAS_RED = "Green (was Red)";

    public static final String ALERT = "Alert";

    public static final String T12_01_31_0100 = "2016-03-25T12:01:31+0100";

    public static final String DT = "dt";

    private QualityGateResponseParser qualityGateResponseParser;

    private String jsonArrayString;

    @Before
    public void init() {
        qualityGateResponseParser = new QualityGateResponseParser();
        jsonArrayString = "[\n{\nid: \"455\",\nrk: \"com.opensource:quality-gates\",\nn: \"Green (was Red)\",\nc: \"Alert\",\ndt: \"2016-03-25T12:01:31+0100\",\nds: \"\"\n},\n{\nid: \"430\",\nrk: \"com.opensource:quality-gates\",\nn: \"Red (was Green)\",\nc: \"Alert\",\ndt: \"2016-03-24T16:28:40+0100\",\nds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n}]";
    }

    @Test
    public void testGetLatestEventResultWhenFirstObjectIsntWithLatestDate() throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject firstJsonObject = new JSONObject();
        firstJsonObject.put("id", "455");
        firstJsonObject.put("rk", COM_OPENSOURCE_QUALITY_GATES);
        firstJsonObject.put("n", GREEN_WAS_RED);
        firstJsonObject.put("c", ALERT);
        firstJsonObject.put(DT, T12_01_31_0100);
        firstJsonObject.put("ds", "");
        JSONObject secondJsonObject = new JSONObject();
        secondJsonObject.put("id", "456");
        secondJsonObject.put("rk", COM_OPENSOURCE_QUALITY_GATES);
        secondJsonObject.put("n", GREEN_WAS_RED);
        secondJsonObject.put("c", ALERT);
        secondJsonObject.put(DT, "2016-03-26T12:01:31+0100");
        secondJsonObject.put("ds", "");
        array.put(firstJsonObject);
        array.put(secondJsonObject);
        assertEquals(secondJsonObject.toString(), qualityGateResponseParser.getLatestEventResult(array).toString());
    }

    @Test
    public void testCreateJSONArrayFromString() {
        JSONArray expected = new JSONArray();
        assertEquals(expected.toString(), qualityGateResponseParser.createJSONArrayFromString("[]").toString());
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringWhenStringNotInJSONFormatShouldThrowQGException() {
        qualityGateResponseParser.createJSONArrayFromString("Random string as a response");
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringThrowsExceptionWhenStringISAJSONObjectShouldThrowQGException() {
        qualityGateResponseParser.createJSONArrayFromString("{\n" +
                "err_code: 404,\n" +
                "err_msg: \"Resource not found: wrongProjectKey\"\n" +
                "}");
    }

    @Test
    public void testGetJSONObjectFromArray() throws JSONException {
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString(jsonArrayString);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "455");
        jsonObject.put("rk", COM_OPENSOURCE_QUALITY_GATES);
        jsonObject.put("n", GREEN_WAS_RED);
        jsonObject.put("c", ALERT);
        jsonObject.put(DT, T12_01_31_0100);
        jsonObject.put("ds", "");
        assertEquals(jsonObject.toString(), qualityGateResponseParser.getJSONObjectFromArray(array, 0).toString());
    }

    @Test(expected = QGException.class)
    public void testGetJSONObjectFromArrayThrowsExceptionDueToArrayOutOfBounds() {
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString(jsonArrayString);
        qualityGateResponseParser.getJSONObjectFromArray(array, 2);
    }

    @Test
    public void testCreateObjectWithStatusGreenWhenEmptyArrayShouldReturnJSONObjectWithStatusGreen() throws JSONException {
        JSONObject expectedObject = new JSONObject();
        expectedObject.put("id", "1");
        expectedObject.put(DT, "2000-01-01T12:00:00+0100");
        expectedObject.put("n", "Green");
        JSONObject actual = qualityGateResponseParser.createObjectWithStatusGreen();
        assertEquals(expectedObject.toString(), actual.toString());
    }

    @Test
    public void testGetValueForJSONKeyGivenArrayAndIndex() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        list.add(jsonObject);
        String expected = T12_01_31_0100;
        assertEquals(expected, qualityGateResponseParser.getValueForJSONKey(list, 0, DT));
    }

    @Test(expected = QGException.class)
    public void testGetValueForJSONKeyGivenArrayAndIndexNonExistentKey() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DT, T12_01_31_0100);
        list.add(jsonObject);
        String expected = T12_01_31_0100;
        String actual = qualityGateResponseParser.getValueForJSONKey(list, 0, "dateeee");
        assertEquals(expected, actual);
    }
}
