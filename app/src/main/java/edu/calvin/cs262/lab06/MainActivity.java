package edu.calvin.cs262.lab06;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads openweathermap's RESTful API for weather forecasts.
 * The code is based on Deitel's WeatherViewer (Chapter 17), simplified based on Murach's NewsReader (Chapter 10).
 * <p>
 * for CS 262, lab 6
 *
 * @author kvlinden
 * @version summer, 2016
 *
 * Answers to questions:
 * 1) Invalid cities cause a toast to pop up saying there is a connection error
 * 2) API keys authorize access to certian information. This API key accesses weather information from openweathermap
 * 3) {"city":{"id":4994358,"name":"Grand Rapids","coord":{"lon":-85.668091,"lat":42.96336},"country":"US","population":0},"cod":"200","message":0.771,"cnt":7,"list":[{"dt":1476464400,"temp":{"day":67.28,"min":48.16,"max":67.28,"night":48.16,"eve":58.3,"morn":59.47},"pressure":1010.11,"humidity":68,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":11.9,"deg":190,"clouds":0},{"dt":1476550800,"temp":{"day":65.88,"min":48.29,"max":70.9,"night":65.95,"eve":68.14,"morn":48.29},"pressure":1000.92,"humidity":80,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":16.8,"deg":209,"clouds":92,"rain":6.68},{"dt":1476637200,"temp":{"day":68.4,"min":56.08,"max":68.59,"night":61.09,"eve":56.77,"morn":60.58},"pressure":1001.19,"humidity":88,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":4.74,"deg":248,"clouds":36,"rain":2.4},{"dt":1476723600,"temp":{"day":66.2,"min":53.69,"max":67.17,"night":53.69,"eve":60.78,"morn":67.17},"pressure":988.98,"humidity":0,"weather":[{"id":502,"main":"Rain","description":"heavy intensity rain","icon":"10d"}],"speed":18.92,"deg":298,"clouds":0,"rain":20.9},{"dt":1476810000,"temp":{"day":63.14,"min":54.43,"max":71.01,"night":59.4,"eve":71.01,"morn":54.43},"pressure":988.17,"humidity":0,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":16.42,"deg":157,"clouds":31,"rain":3.73},{"dt":1476896400,"temp":{"day":57.24,"min":52.45,"max":57.24,"night":52.45,"eve":56.34,"morn":54.88},"pressure":993.07,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":22.75,"deg":298,"clouds":80,"rain":1.32},{"dt":1476982800,"temp":{"day":55.98,"min":48.11,"max":55.98,"night":48.11,"eve":50.95,"morn":48.78},"pressure":1001.36,"humidity":0,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":12.06,"deg":297,"clouds":11,"rain":0.44}]}
 * 4) The program converts the JSON data into an array and gets the weather summary for each day
 * 5) The weather class converts the array into a readable text to display on the app.
 *
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    private EditText cityText;
    private Button fetchButton;

    private List<Weather> weatherList = new ArrayList<>();
    private ListView itemsListView;

    /* This formater can be used as follows to format temperatures for display.
     *     numberFormat.format(SOME_DOUBLE_VALUE)
     */
    //private NumberFormat numberFormat = NumberFormat.getInstance();

    private static String TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityText = (EditText) findViewById(R.id.cityText);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        itemsListView = (ListView) findViewById(R.id.weatherListView);

        // See comments on this formatter above.
        //numberFormat.setMaximumFractionDigits(0);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(cityText);
                new GetWeatherTask().execute(createURL(cityText.getText().toString()));
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Formats a URL for the webservice specified in the string resources.
     *
     * @param city the target city
     * @return URL formatted for openweathermap.com
     */
    private URL createURL(String city) {
        try {
            String urlString = getString(R.string.web_service_url) +
                    URLEncoder.encode(city, "UTF-8") +
                    "&units=" + getString(R.string.openweather_units) +
                    "&cnt=" + getString(R.string.openweather_count) +
                    "&APPID=" + getString(R.string.openweather_api_key);
            return new URL(urlString);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Deitel's method for programmatically dismissing the keyboard.
     *
     * @param view the TextView currently being edited
     */
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /**
     * Inner class for GETing the current weather data from openweathermap.org asynchronously
     */
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return new JSONObject(result.toString());
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            if (weather != null) {
//                Log.d(TAG, weather.toString());
                convertJSONtoArrayList(weather);
                MainActivity.this.updateDisplay();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Converts the JSON weather forecast data to an arraylist suitable for a listview adapter
     *
     * @param forecast
     */
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear(); // clear old weather data
        try {
            JSONArray list = forecast.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject day = list.getJSONObject(i);
                JSONObject temperatures = day.getJSONObject("temp");
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
                weatherList.add(new Weather(
                        day.getLong("dt"),
                        weather.getString("description"),
                        temperatures.getDouble("min"),
                        temperatures.getDouble("max")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the weather data on the forecast ListView through a simple adapter
     */
    private void updateDisplay() {
        if (weatherList == null) {
            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Weather item : weatherList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("day", item.getDay());
            map.put("description", item.getSummary());
            map.put("min", item.getMin().toString());
            map.put("max", item.getMax().toString());
            data.add(map);
        }

        int resource = R.layout.weather_item;
        String[] from = {"day", "description", "min", "max"};
        int[] to = {R.id.dayTextView, R.id.summaryTextView, R.id.minTextView, R.id.maxTextView};

        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }

}
