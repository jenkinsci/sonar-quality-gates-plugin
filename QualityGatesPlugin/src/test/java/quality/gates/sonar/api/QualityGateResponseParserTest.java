package quality.gates.sonar.api;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;
        import org.junit.Before;
        import org.junit.Test;
        import quality.gates.jenkins.plugin.QGException;

        import java.util.ArrayList;
        import java.util.List;

        import static org.junit.Assert.*;

public class QualityGateResponseParserTest {

    private QualityGateResponseParser qualityGateResponseParser;

    private String jsonArrayString;

    @Before
    public void init() {
        qualityGateResponseParser = new QualityGateResponseParser();
        jsonArrayString = "[\n" +
                "{\n" +
                "id: \"455\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Green (was Red)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-25T12:01:31+0100\",\n" +
                "ds: \"\"\n" +
                "},\n" +
                "{\n" +
                "id: \"430\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Red (was Green)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-24T16:28:40+0100\",\n" +
                "ds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n" +
                "}]";
    }


    @Test
    public void testGetQualityGateResultFromJSONWithOneObjectShouldReturnStatusError() {
        String jsonArray = "[\n" +
                "{\n" +
                "id: \"430\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Red (was Green)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-24T16:28:40+0100\",\n" +
                "ds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n" +
                "}]";
        assertEquals(new QualityGatesStatus("ERROR"), qualityGateResponseParser.getQualityGateResultFromJSON(jsonArray));
    }

    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusOK() {
        assertEquals(new QualityGatesStatus("OK"), qualityGateResponseParser.getQualityGateResultFromJSON(jsonArrayString));
    }

    @Test
    public void testGetQualityGateResultFromJSONWithMultipleObjectsShouldReturnStatusError() {
        jsonArrayString = "[\n" +
                "{\n" +
                "id: \"455\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Red (was Red)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-26T12:01:31+0100\",\n" +
                "ds: \"\"\n" +
                "},\n" +
                "{\n" +
                "id: \"455\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Green (was Red)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-25T12:01:31+0100\",\n" +
                "ds: \"\"\n" +
                "},\n" +
                "{\n" +
                "id: \"430\",\n" +
                "rk: \"com.opensource:quality-gates\",\n" +
                "n: \"Red (was Green)\",\n" +
                "c: \"Alert\",\n" +
                "dt: \"2016-03-24T16:28:40+0100\",\n" +
                "ds: \"Major issues variation > 2 over 30 days (2016 Mar 15), Coverage variation < 60 since previous analysis (2016 Mar 24)\"\n" +
                "}]";
        assertEquals(new QualityGatesStatus("ERROR"), qualityGateResponseParser.getQualityGateResultFromJSON(jsonArrayString));
    }

    @Test
    public void testParseData() {
        String expected = "Thu Mar 24 18:28:40 CET 2016";
        String str = "2016-03-24T16:28:40+0100";
        assertEquals(expected, qualityGateResponseParser.parseDate(str).toString());
    }

    @Test(expected = QGException.class)
    public void testParseDataThrowsException(){
        String str = "2016-03-24T8:40-0100";
        qualityGateResponseParser.parseDate(str);
    }

    @Test
    public void testGetLatestEventResultWhenFirstObjectIsntWithLatestDate() throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject firstJsonObject = new JSONObject();
        firstJsonObject.put("id", "455");
        firstJsonObject.put("rk", "com.opensource:quality-gates");
        firstJsonObject.put("n", "Green (was Red)");
        firstJsonObject.put("c", "Alert");
        firstJsonObject.put("dt", "2016-03-25T12:01:31+0100");
        firstJsonObject.put("ds", "");
        JSONObject secondJsonObject = new JSONObject();
        secondJsonObject.put("id", "456");
        secondJsonObject.put("rk", "com.opensource:quality-gates");
        secondJsonObject.put("n", "Green (was Red)");
        secondJsonObject.put("c", "Alert");
        secondJsonObject.put("dt", "2016-03-26T12:01:31+0100");
        secondJsonObject.put("ds", "");
        array.put(firstJsonObject);
        array.put(secondJsonObject);
        assertEquals(secondJsonObject.toString(), qualityGateResponseParser.getLatestEventResult(array).toString());
    }

    @Test
    public void testCreateJSONArrayFromString(){
        JSONArray expected= new JSONArray();
        assertEquals(expected.toString(), qualityGateResponseParser.createJSONArrayFromString("[]").toString());
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringWhenStringNotInJSONFormatShouldThrowQGException(){
        qualityGateResponseParser.createJSONArrayFromString("Random string as a response");
    }

    @Test(expected = QGException.class)
    public void testCreateJSONArrayFromStringThrowsExceptionWhenStringISAJSONObjectShouldThrowQGException(){
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
        jsonObject.put("rk", "com.opensource:quality-gates");
        jsonObject.put("n", "Green (was Red)");
        jsonObject.put("c", "Alert");
        jsonObject.put("dt", "2016-03-25T12:01:31+0100");
        jsonObject.put("ds", "");
        assertEquals(jsonObject.toString(), qualityGateResponseParser.getJSONObjectFromArray(array, 0).toString());
    }

    @Test(expected = QGException.class)
    public void testGetJSONObjectFromArrayThrowsExceptionDueToArrayOutOfBounds(){
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString(jsonArrayString);
        qualityGateResponseParser.getJSONObjectFromArray(array, 2);
    }

    @Test
    public void testGetJSONObjectFromArrayWhenEmptyArrayShouldReturnJSONObjectWithStatusGreen() throws JSONException{
        JSONArray array = qualityGateResponseParser.createJSONArrayFromString("[]");
        JSONObject expectedObject = new JSONObject();
        expectedObject.put("dt", "2000-01-01T12:00:00+0100");
        expectedObject.put("n", "Green");
        JSONObject actual = qualityGateResponseParser.getJSONObjectFromArray(array, 0);
        assertEquals(expectedObject.toString(), actual.toString());
    }

    @Test
    public void testGetValueForJSONKeyGivenArrayAndIndex() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dt", "2016-03-25T12:01:31+0100");
        list.add(jsonObject);
        String expected = "2016-03-25T12:01:31+0100";
        assertEquals(expected, qualityGateResponseParser.getValueForJSONKey(list, 0, "dt"));
    }

    @Test(expected = QGException.class)
    public void testGetValueForJSONKeyGivenArrayAndIndexNonExistentKey() throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dt", "2016-03-25T12:01:31+0100");
        list.add(jsonObject);
        String expected = "2016-03-25T12:01:31+0100";
        String actual = qualityGateResponseParser.getValueForJSONKey(list, 0, "dateeee");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValueForJSONKeyGivenJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dt", "2016-03-25T12:01:31+0100");
        String expected = "2016-03-25T12:01:31+0100";
        assertEquals(expected, qualityGateResponseParser.getValueForJSONKey(jsonObject, "dt"));
    }

    @Test(expected = QGException.class)
    public void testGetValueForJSONKeyNonExistentKeyGivenJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dt", "2016-03-25T12:01:31+0100");
        String expected = "2016-03-25T12:01:31+0100";
        String actual = qualityGateResponseParser.getValueForJSONKey(jsonObject, "dateeee");
        assertEquals(expected, actual);
    }

}