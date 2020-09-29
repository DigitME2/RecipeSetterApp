package uk.co.digitme.recipesetter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerSync {

    public static final String TAG = "ServerSync";
    private Context context;
    DbHelper dbHelper;
    String serverAddress;

    public ServerSync(Context context){
        this.context = context;
        this.dbHelper = new DbHelper(context);
        this.serverAddress = dbHelper.getServerAddress();
    }


    public interface UpdateStatusVolleyCallback{
        void onSuccess(JSONObject response);
    }

    /**
     * Contacts the server to obtain an update
     * @param callback a function to run when the server responds
     */
    public void getStatus(final UpdateStatusVolleyCallback callback){
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = serverAddress + "/get_status";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            callback.onSuccess(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ErrorListener", String.valueOf(error));
                        }
                    });
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Contact the server and change the active recipe
     * @param recipe the new recipe, should match the string in the database exactly
     */
    public void changeRecipe(String recipe) {
        // Create the JSON to send to the server, containing the new recipe
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("recipe", recipe);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = serverAddress + "/change_recipe";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url,
                    jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, String.valueOf(e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Get the up to date options from the server
     */
    public void updateRecipeOptions(final Boolean showToast) {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = serverAddress + "/recipe_options";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ArrayList<String> recipeOptions = parseJsonList(response, "recipe_options");
                            if (recipeOptions != null) {
                                dbHelper.saveRecipeOptions(recipeOptions);
                                if (showToast) {
                                    Toast.makeText(context, "Successfully updated recipe list", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Log.e(TAG, "Failed to get options from server response");
                                if (showToast) {
                                    Toast.makeText(context, "Failed to get options from server response", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // If an error occurs, tell the user and show a toast

                            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                Log.i("ErrorListener", String.valueOf(error));
                            } else if (error instanceof ServerError){
                                Log.i("ErrorListener", String.valueOf(error));
                            }
                            Log.i("ErrorListener", String.valueOf(error));
                            if (showToast) {
                                Toast.makeText(context, "Could not reach server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the up to date options from the server.
     * Not used anymore, production line is set by IP
     */
    public void updateProductionLineOptions() {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = serverAddress + "/production_line_options";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ArrayList<String> productionLineOptions = parseJsonList(response, "production_line_options");
                            if (productionLineOptions != null) {
                                dbHelper.saveProductionLineOptions(productionLineOptions);
                            }
                            else {
                                Log.e(TAG, "Failed to get options from server response");
                                Toast.makeText(context, "Failed to get options from server response", Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // If an error occurs, tell the user and show the

                                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                    Log.i("ErrorListener", String.valueOf(error));
                                } else if (error instanceof ServerError){
                                    Log.i("ErrorListener", String.valueOf(error));
                                }
                                Log.i("ErrorListener", String.valueOf(error));
                                Toast.makeText(context, String.valueOf(error), Toast.LENGTH_LONG).show();
                            }
                        });
            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> parseJsonList(JSONObject json, String listName){
        ArrayList<String> parsedList = new ArrayList<>();
        JSONArray jsonArray;
        try {
            jsonArray = json.getJSONArray(listName);
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                parsedList.add(jsonArray.get(i).toString());

            }

        }catch (JSONException je){
            Log.v(TAG, je.toString());
            return null;
        }

        return parsedList;
    }

}
