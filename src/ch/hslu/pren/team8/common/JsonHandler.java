package ch.hslu.pren.team8.common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Peter Gisler on 23.03.17
 */
public class JsonHandler {

    private static JsonHandler jsonHandler;
    private File jsonFile;
    private JSONParser parser;
    private JSONObject rootObject;

    private JsonHandler() {
        parser = new JSONParser();
        rootObject = getRootObject();
    }

    public static JsonHandler getInstance() {
        if (jsonHandler == null) {
            jsonHandler = new JsonHandler();
        }
        return jsonHandler;
    }

    private JSONObject getRootObject() {
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(jsonFile));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return (JSONObject) obj;
    }

    public int getInt(String propertyPath) {
        return getInt(rootObject, getPathList(propertyPath));
    }

    public int getInt(JSONObject object, String propertyPath) {
        List<String> pathArray = getPathList(propertyPath);
        return getInt(object, pathArray);
    }

    private int getInt(JSONObject object, List<String> propertyPath) {
        if (propertyPath.size() > 1) {
            return getInt(getObject(object, propertyPath.remove(0)), propertyPath);
        } else {
            return (int) (long) object.get(propertyPath.get(0));
        }
    }

    private List<String> getPathList(String path) {
        return Arrays.asList(path.split("."));
    }

    public JSONObject getObject(JSONObject object, String key) {
        if (!object.containsKey(key)) {
            throw new InvalidParameterException("The given object contains no key named '" + key + "'");
        }
        return (JSONObject) object.get(key);
    }

    public void setJsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
        rootObject = getRootObject();
    }

    public void setJsonFile(String relativePath) {
        this.jsonFile = new File(getPathRelativeToUserHome(relativePath));
        rootObject = getRootObject();
    }

    private String getPathRelativeToUserHome(String relativePath) {
        String userHome = System.getProperty("user.home");
        return userHome + relativePath;
    }
}
