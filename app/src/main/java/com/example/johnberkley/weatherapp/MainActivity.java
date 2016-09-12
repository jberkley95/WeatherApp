package com.example.johnberkley.weatherapp;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    static final String API_KEY = "e93fc9d0cbf4513f724cc60990a9f731";
    static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?";

    HashMap<String, String> weatherInfo = new HashMap<>();

    DecimalFormat decimalFormat = new DecimalFormat("#.#");

    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    double mLatitude, mLongitude;

    //TextView weather;
    TextView city, main, description, temperature, humidity, high, low, wind;
    Button weatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //weather = (TextView) findViewById(R.id.weather);
        city = (TextView) findViewById(R.id.city);
        main = (TextView) findViewById(R.id.main);
        description = (TextView) findViewById(R.id.description);
        temperature = (TextView) findViewById(R.id.temperature);
        humidity = (TextView) findViewById(R.id.humidity);
        high = (TextView) findViewById(R.id.high);
        low = (TextView) findViewById(R.id.low);
        wind = (TextView) findViewById(R.id.wind);
        weatherButton = (Button) findViewById(R.id.weatherButton);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLocation();
                new RetrieveFeedTask().execute();
            }
        });

    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(API_URL + "lat=" + mLatitude + "&lon=" + mLongitude + "&APPID=" + API_KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i("INFO", response);
            //weather.setText(response);

            try {
                JSONObject jsonObject = new JSONObject(response);

                JSONArray weatherArray = jsonObject.optJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);

                String mainWeather = weather.getString("main");
                String description = weather.getString("description");

                JSONObject main = jsonObject.getJSONObject("main");

                String temp = String.valueOf(decimalFormat.format((main.getDouble("temp") * 1.8) - 459.67));
                String high = String.valueOf(decimalFormat.format((main.getDouble("temp_max") * 1.8) - 459.67));
                String low = String.valueOf(decimalFormat.format((main.getDouble("temp_min") * 1.8) - 459.67));
                String humidity = String.valueOf(main.getInt("humidity"));

                JSONObject wind = jsonObject.getJSONObject("wind");

                String windSpeed = String.valueOf(wind.getDouble("speed"));

                String cityName = jsonObject.getString("name");

                weatherInfo.put("main", mainWeather);
                weatherInfo.put("description", description);
                weatherInfo.put("temp", temp);
                weatherInfo.put("high", high);
                weatherInfo.put("low", low);
                weatherInfo.put("humidity", humidity);
                weatherInfo.put("wind", windSpeed);
                weatherInfo.put("city", cityName);

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            if (weatherInfo != null) {
                city.setText("City: " + weatherInfo.get("city"));
                main.setText("Weather: " + weatherInfo.get("main"));
                description.setText("Description: " + weatherInfo.get("description"));
                temperature.setText("Temperature: " + weatherInfo.get("temp") + " F");
                humidity.setText("Humidity: " + weatherInfo.get("humidity") + "%");
                high.setText("High: " + weatherInfo.get("high") + " F");
                low.setText("Low: " + weatherInfo.get("low") + " F");
                wind.setText("Wind: " + weatherInfo.get("wind") + " mph");
            } else {
                Toast.makeText(MainActivity.this, "Failed To Get Weather Data", Toast.LENGTH_SHORT).show();
            }

        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
    }

    private void displayLocation() {
        try {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();

            }
        } catch (SecurityException e) {
            Toast.makeText(MainActivity.this, "You fucked up somewhere, go fix it", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}