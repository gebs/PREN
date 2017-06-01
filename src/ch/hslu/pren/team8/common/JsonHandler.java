package ch.hslu.pren.team8.common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Peter Gisler on 23.03.17.
 */
public class JsonHandler {

    private static JsonHandler jsonHandler;
    private File jsonFile;
    private JSONParser parser;
    private JSONObject rootObject;

    /**
     * private constructor
     */
    private JsonHandler() {
        parser = new JSONParser();
    }

    /**
     * Singleton pattern implementation
     *
     * @return single instance of JsonHandler class
     */
    public static JsonHandler getInstance() {
        if (jsonHandler == null) {
            jsonHandler = new JsonHandler();
        }
        return jsonHandler;
    }

    /**
     * Returns root json object.
     * If no file is set, an exception will be thrown
     *
     * @return root json object in specified file
     */
    private JSONObject getRootObject() {
        Object obj = null;

        if (jsonFile == null) {
            throw new InvalidParameterException("No file is set for json handler!");
        }

        try {
            obj = parser.parse(new FileReader(jsonFile));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return (JSONObject) obj;
    }

    /**
     * Returns an integer value for the specified property path.
     *
     * @param propertyPath traversal path, parts separated with "."
     * @return integer value at specified path
     */
    public int getInt(String propertyPath) {
        ArrayList<String> pathArray = getPathList(propertyPath);
        return getInt(rootObject, pathArray);
    }

    /**
     * Retrieves an integer value from the specified object
     *
     * @param object       object to retrieve integer value from
     * @param propertyPath property path, split into separate parts
     * @return integer value of object at specified path
     */
    private int getInt(JSONObject object, ArrayList<String> propertyPath) {
        if (propertyPath.size() > 1) {
            String key = propertyPath.remove(0);
            return getInt(getObject(object, key), propertyPath);
        } else {
            return (int) (long) object.get(propertyPath.get(0));
        }
    }

    /**
     * Returns a boolean value from the specified property path.
     *
     * @param propertyPath traversal path, parts separated with "."
     * @return integer value at specified path
     */
    public boolean getBoolean(String propertyPath) {
        List<String> pathArray = getPathList(propertyPath);
        return getBoolean(rootObject, pathArray);
    }

    /**
     * Retrieves a boolean value from the specified object.
     *
     * @param object       object to retrieve boolean value from
     * @param propertyPath property path, split into separate parts
     * @return boolean value of object at specified path
     */
    private boolean getBoolean(JSONObject object, List<String> propertyPath) {
        if (propertyPath.size() > 1) {
            String key = propertyPath.remove(0);
            return getBoolean(getObject(object, key), propertyPath);
        } else {
            return (boolean) object.get(propertyPath.get(0));
        }
    }

    /**
     * Converts a dot-separated path string into a list of path fragments
     *
     * @param path dot-separated path
     * @return list with string item path fragments
     */
    private ArrayList<String> getPathList(String path) {
        String[] pathArray = path.split("[.]");
        return new ArrayList<>(Arrays.asList(pathArray));
    }

    /**
     * Returns a json object from the given object at the specified key
     *
     * @param object the object to retrieve the json object from
     * @param key    the key of the object to return
     * @return the requested object :-)
     */
    private JSONObject getObject(JSONObject object, String key) {
        if (!object.containsKey(key)) {
            throw new InvalidParameterException("The given object contains no key named '" + key + "'");
        }
        return (JSONObject) object.get(key);
    }

    /**
     * Sets the json handlers json file
     *
     * @param jsonFile the json file to set
     */
    public void setJsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
        rootObject = getRootObject();
    }

    /**
     * Sets the json handlers json file by file path
     *
     * @param relativePath path of file, relative to the users home directory
     * @return whether the json config file exists
     */
    public boolean setJsonFile(String relativePath) {
        File file = new File(getPathRelativeToUserHome(relativePath));
        boolean fileExists = file.exists();

        if (fileExists) {
            jsonFile = file;
            rootObject = getRootObject();
        }

        return fileExists;
    }

    /**
     * Checks whether the instance of JsonHandler has a valid file set.
     * A valid file is any existing file.
     *
     * @return true if a file is available
     */
    public boolean hasValidFile() {
        return jsonFile != null && jsonFile.exists();
    }

    /**
     * Gets an absolute file path of a given path relative to the users home directory.
     * On raspberry pi the pi-user home directory is:   /home/pi
     * On raspberry pi de root-user home directory is:  /root
     *
     * @param relativePath path, relative to the users home directory
     * @return absolute file path
     */
    private String getPathRelativeToUserHome(String relativePath) {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + relativePath;
    }
}
