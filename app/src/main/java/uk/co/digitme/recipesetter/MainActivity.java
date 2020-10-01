package uk.co.digitme.recipesetter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends BaseActivity {

    public static final int STATUS_REFRESH_INTERVAL_MS = 2000;

    public static final String TAG = "MainActivity";
    public static final String DEFAULT_URL = "192.168.0.100";

    Button changeRecipeButton;
    DbHelper dbHelper;
    String[] recipeArray;
    AlertDialog alertDialog;
    ServerSync serverSync;
    Handler handler;
    long lastUpdated;
    TextView lastUpdatedText;

    ServerSync.UpdateStatusVolleyCallback statusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("Len Wright Salads");
        }
        lastUpdatedText = findViewById(R.id.update_status);

        dbHelper = new DbHelper(getApplicationContext());
        serverSync = new ServerSync(getApplicationContext());
        serverSync.updateRecipeOptions(false);

        // Handles the updating of the trays passed/ current recipe
        handler = new Handler();
        startStatusChecker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Create a dialog to show a list of recipes when pressing the change recipe button
        // Get the list of recipes from the database and convert it to a string array
        List<String> recipeList = dbHelper.getRecipeOptions();
        recipeArray = new String[recipeList.size()];
        recipeList.toArray(recipeArray);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(recipeArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // POST to the server when a new recipe is selected
                serverSync.changeRecipe(recipeArray[which]);
                Log.i(TAG, "Attempting to set recipe to " + recipeArray[which]);
            }});
        alertDialog = builder.create();

        // Set the listener for the change recipe button
        changeRecipeButton = findViewById(R.id.change_recipe_button);
        changeRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });
    }

    Runnable statusChecker = new Runnable() {
        /**
         * A repeating task to contact the server
         */
        @Override
        public void run() {
            try {
                // Update the text
                updateStatus();
                // Check when the last successful update was and update the status text if it was too long ago to warn the user
                long lastUpdatedSecondsAgo = (System.currentTimeMillis() - lastUpdated)/1000;
                String updateText;
                if (lastUpdated == 0) {
                    updateText = "Connecting to server...";
                }
                else if(lastUpdatedSecondsAgo > 10){
                    updateText = "Last updated " + lastUpdatedSecondsAgo + " seconds ago";
                }
                else{
                    updateText = "";
                }
                lastUpdatedText.setText(updateText);
            } finally {
                handler.postDelayed(statusChecker, STATUS_REFRESH_INTERVAL_MS);
            }
        }
    };

    void startStatusChecker() {
        statusChecker.run();
    }


    /**
     * Contact the server and then update the screen with the new information from the server
     *  Updates tray number and selected recipe
     */
    private void updateStatus() {
        Log.d(TAG, "Contacting recipeSetter server for status update");
        serverSync.getStatus(statusCallback = new ServerSync.UpdateStatusVolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                TextView traysTextView = findViewById(R.id.trays_passed_number);
                TextView recipeTextView = findViewById(R.id.current_recipe_text);
                String recipe;
                String trays;
                try {
                    // Get the values from the server json response
                    recipe = response.getString("recipe");
                    trays = response.getString("trays");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error parsing JSON values from server response to get_status");
                    Toast.makeText(getApplicationContext(), "Error parsing JSON values from server response to get_status", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.d(TAG, "Got tray number: " + trays + " and recipe: " + recipe);
                // Change the text on the main screen
                traysTextView.setText(trays);
                recipeTextView.setText(recipe);

                // Set the last updated timestamp to the current time
                lastUpdated = (System.currentTimeMillis());
            }
        });

    }


}