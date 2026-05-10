package automation.generic;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;


public class JsonUtils {

	public static String path;
	public static String jsonPathTerm;


	//Reset Base URI (after test)
	public static void resetBaseURI() {
		RestAssured.baseURI = null;
	}
	//Sets Base URI
	public static void setBaseURI(String url) {
		RestAssured.baseURI = url;
	}

	//Sets base path
	public static void setBasePath(String basePathTerm) {
		RestAssured.basePath = basePathTerm;
	}
	//Reset base path
	public static void resetBasePath() {
		RestAssured.basePath = null;
	}

	//Sets ContentType
	public static void setContentType(ContentType Type) {
		given().contentType(Type);
	}

	//Sets Json path term
	public static void setJsonPathTerm(String jsonPath) {
		jsonPathTerm = jsonPath;
	}

	//Created search query path
	public static void createSearchQueryPath(String searchTerm, String param, String paramValue) {
		path = searchTerm + "/" + jsonPathTerm + "?" + param + "=" + paramValue;
	}

	//Returns response
	public static Response getResponse() {
		//System.out.print("path: " + path +"\n");
		return get(path);
	}

	//Returns JsonPath object
	public static JsonPath getJsonPath(Response res) {
		String json = res.asString();
		//System.out.print("returned json: " + json +"\n");
		return new JsonPath(json);
	}

	//Convert STRING -> JSON
	public static JSONObject convertJSON(String json) throws JSONException {
		return new JSONObject(json);
	}

	//Get a "property" value
	public static String getValueFromJSON(String json, String propertyName) throws JSONException {
		String value = convertJSON(convertJSON(json).get("data").toString()).getString(propertyName);
		return  value;
	}

	//Compare expectedResult vs actualResult
	public static boolean areEqualJSON(String expectedResult, String actualResult) throws JSONException {
		boolean areEqual = true;
		//String -> JSON
		JSONObject jsonExpectedResult = convertJSON(expectedResult);
		JSONObject jsonActualResult = convertJSON(actualResult);
		//Iterate jsonExpectedResult Data
		Iterator<?> keys = jsonExpectedResult.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String actualValue = jsonActualResult.get(key).toString();
			String expectedValue = jsonExpectedResult.get(key).toString();
			if (actualValue.startsWith("{")) {
				areEqual = areEqualJSON(expectedValue,actualValue);
			} else {
				//DO NOT compare fields with value "EXCLUDE"
				if (expectedValue.equals("EXCLUDE")) {
					System.out.println("INFO: EXCLUDE, the attribute [" + key + "]");
				} else if (!expectedValue.equals(actualValue)) {
					areEqual = false;
				}
				System.out.println("INFO: COMPARING, the attribute [" + key + "]:  actual value [" + actualValue + "] vs expected [" + expectedValue + "]");
			}
		}
		return areEqual;
	}






}
