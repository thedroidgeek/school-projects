package ma.tdg.supcooking.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ma.tdg.supcooking.model.Recipe;
import ma.tdg.supcooking.model.User;

public class ServerApi {
    private static final String sEndpoint = "http://supinfo.steve-colinet.fr/supcooking/";

    public static String makeRequest(String action, HashMap<String,String> parameters) throws IOException {
        URL url = new URL(sEndpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        StringBuilder reqBody = new StringBuilder("action=" + URLEncoder.encode(action, "UTF-8"));
        for (HashMap.Entry<String, String> entry : parameters.entrySet()) {
            reqBody.append("&")
                    .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        DataOutputStream outStr = new DataOutputStream(conn.getOutputStream());
        outStr.write(reqBody.toString().getBytes(StandardCharsets.UTF_8));
        outStr.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        return output.toString();
    }

    public static User doAuth(final String username, final String password) throws Exception {
        try {
            String response = makeRequest("login", new HashMap<String, String>() {{
                put("login", username);
                put("password", password);
            }});
            JSONObject json = new JSONObject(response);
            if (json.getBoolean("success")) {
                JSONObject jsonUser = json.getJSONObject("user");
                User userProfile = new User();
                userProfile.setUsername(username);
                userProfile.setPassword(password);
                userProfile.setFirstName(jsonUser.getString("firstName"));
                userProfile.setLastName(jsonUser.getString("lastName"));
                userProfile.setAddress(jsonUser.getString("address"));
                userProfile.setEmail(jsonUser.getString("email"));
                userProfile.setPhoneNumber(jsonUser.getString("phoneNumber"));
                return userProfile;
            }
            else {
                String serverError = json.getString("message");
                if (serverError.equals("Bad User")) {
                    return null;
                }
                throw new Exception("Server: " + serverError);
            }
        } catch (IOException e) {
            throw new Exception("An error occurred while trying to communicate with the server", e);
        } catch (JSONException e) {
            throw new Exception("The server sent an unexpected response", e);
        }
    }

    public static List<Recipe> fetchRecipes() throws Exception {
        if (!User.isLoggedIn()) {
            throw new Exception("Please log in before being able to fetch recipes");
        }
        try {
            String response = makeRequest("getCooking", new HashMap<String, String>() {{
                put("login", User.getProfile().getUsername());
                put("password", User.getProfile().getPassword());
            }});
            JSONObject json = new JSONObject(response);
            if (json.getBoolean("success")) {
                JSONArray jsonRecipes = json.getJSONArray("recipes");
                int recipeCount = jsonRecipes.length();
                List<Recipe> recipes = new ArrayList<>();
                if (recipeCount > 0) {
                    for (int i = 0; i < recipeCount; i++) {
                        JSONObject jsonObj = jsonRecipes.getJSONObject(i);
                        Recipe curr = new Recipe();
                        curr.setId(jsonObj.getLong("id"));
                        curr.setName(jsonObj.getString("name"));
                        curr.setType(jsonObj.getString("type"));
                        curr.setCookingTime(jsonObj.getInt("cookingTime"));
                        curr.setPreparationTime(jsonObj.getInt("preparationtTime")); //whoops?
                        curr.setIngredients(jsonObj.getString("ingredients"));
                        curr.setPreparationSteps(jsonObj.getString("preparationSteps"));
                        curr.setRating((byte)jsonObj.getInt("rate")); //._.
                        curr.setPictureLocation(jsonObj.getString("picture"));
                        recipes.add(curr);
                    }
                }
                return recipes;
            } else {
                String serverError = json.getString("message");
                throw new Exception("Server: " + serverError);
            }
        } catch (IOException e) {
            throw new Exception("An error occurred while trying to communicate with the server", e);
        } catch (JSONException e) {
            throw new Exception("The server sent an unexpected response", e);
        }
    }

    public static void evaluateRecipe(final Recipe recipe, final int rating) throws Exception {
        if (!User.isLoggedIn()) {
            throw new Exception("Please log in before being able to rate recipes");
        }
        try {
            String response = makeRequest("evaluate", new HashMap<String, String>() {{
                put("login", User.getProfile().getUsername());
                put("password", User.getProfile().getPassword());
                put("cookingId", String.valueOf(recipe.getId()));
                put("rate", String.valueOf(rating));
            }});
            JSONObject json = new JSONObject(response);
            if (!json.getBoolean("success")) {
                String serverError = json.getString("message");
                throw new Exception("Server: " + serverError);
            }
        } catch (IOException e) {
            throw new Exception("An error occurred while trying to communicate with the server", e);
        } catch (JSONException e) {
            throw new Exception("The server sent an unexpected response", e);
        }
    }

    public static boolean testConnection() {
        try {
            URL url = new URL(sEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
