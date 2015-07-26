

package com.example.patrick.findmyfriends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
//import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebSettings;

import android.app.Activity;



public class MainActivity extends Activity implements SensorEventListener{

    private TextView etResponse;
    //Button button;

    private TextView tvIsConnected;
    private WebView mWebView;
    private Spinner mSpinner;
    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private LocationListener mLocationListener;


    public void setCurrentlocation(Location currentlocation) {
        this.currentlocation = currentlocation;
    }

    private Location currentlocation =new Location("current");
    private Location targetlocation =new Location("target");
    private List<User> userlist;
    private int targetuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to the views
        etResponse = (TextView) findViewById(R.id.etResponse);

        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        mWebView = (WebView) findViewById(R.id.webView);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mLocationListener = new MyLocationListener(currentlocation);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, this.mLocationListener);

        image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        currentlocation.setLatitude(0);
        currentlocation.setLongitude(0);

        targetlocation.setLatitude(0);
        targetlocation.setLongitude(0);
        userlist = new ArrayList<User>();
        targetuser = 0;

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheMaxSize(5 * 1024 * 1024); // 5MB
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode( WebSettings.LOAD_DEFAULT); // load online by default




        // check if you are connected or not
        if(isConnected()){
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are conncted");

        }
        else{
            tvIsConnected.setText("You are NOT conncted");
        }

        mWebView.loadUrl("file:///android_asset/map.html");
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                for (int i = 0; i < userlist.size(); i++) {
                    User user = userlist.get(i);
                    mWebView.loadUrl("javascript:setPointer(" + user.getLon() + "," + user.getLat() + ",\"" + user.getUsername() + "\"," + "\"img/marker.png\"" + ")");
                }

            }
        });

        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("http://decembersoul.dd-dns.de/~patrick/sharemylocation/location.json");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        double distance = currentlocation.distanceTo(targetlocation);
        float bearing = currentlocation.bearingTo(targetlocation);

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        float degree2 = (degree - bearing);
        if (degree2 > 360.0)
            degree2 -= 360.0;
        if (degree2 < 0)
            degree2 += 360.0;

        etResponse.setTextSize(18);
        String str = "distance:\n";
        Log.d("bearing ", Float.toString(bearing));
        Log.d("degree ", Float.toString(degree));
        Log.d("degree2 ", Float.toString(degree2));
        //str += "degrees=" + Float.toString(degree) + "\n";
        //str += "degrees2=" + Float.toString(degree2) + "\n";
        str += Math.round(distance) + "m\n";
        Long unixstime = 0L;
        if (userlist.size()>0) {
            unixstime = Long.decode(userlist.get(targetuser).getTime()) * 1000;
        }
        java.util.Date df = new java.util.Date(unixstime);
        String vv = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(df);
        //Date date = Date.from(Instant.ofEpochSecond(unixstime));
        //Calendar mydate = Calendar.getInstance();
        //mydate.setTimeInMillis(unixstime*1000);
        str += "last update\n" + vv +"\n";
        str += "cLat=" + currentlocation.getLatitude() + "\n";
        str += "cLon=" + currentlocation.getLongitude() + "\n";
        etResponse.setText(str);


        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree2,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree2;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner(List<String> list) {

        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(dataAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                targetuser = mSpinner.getSelectedItemPosition();
                mWebView.loadUrl("javascript:map.setCenter(new OpenLayers.LonLat(" + userlist.get(targetuser).getLon() + "," + userlist.get(targetuser).getLat() +").transform(epsg4326, projectTo),15)");
                //mWebView.loadUrl("http://decembersoul.dd-dns.de/~patrick/sharemylocation/map.php?user="+userlist.get(targetuser).getIndex());
                targetlocation.setLatitude(userlist.get(targetuser).getLat());
                targetlocation.setLongitude(userlist.get(targetuser).getLon());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            try {
                JSONObject json = new JSONObject(result);
                JSONArray jsonUserArray = json.getJSONArray("userList");
                List<String> list = new ArrayList<String>();
                String str = "";
                for ( int i = 0; i < jsonUserArray.length(); i++) {
                    JSONObject jsonuser = jsonUserArray.getJSONObject(i);
                    User user = new User(i, jsonuser.getString("user"), Float.parseFloat(jsonuser.getString("lat")), Float.parseFloat(jsonuser.getString("lon")), jsonuser.getString("time"));
                    //str += "Name=" + jsonuser.getString("user") + " lat=" + jsonuser.getString("lat") + " lon=" + jsonuser.getString("lon") + "\n";
                    list.add(user.getUsername());
                    userlist.add(user);
                    //mWebView.loadUrl("javascript:setPointer(" + user.getLon() + "," + user.getLat() + ",\"" + user.getUsername() + "\"," + "\"img/marker.png\"" + ")");

                    //mWebView.loadUrl("javascript:setPointer( 9.366212,  54.034524 , \"hallo\", \"img/marker.png\")");
                    //setPointer( 9.654365,  53.736746 , "hallo", "img/marker.png");
                }
                addItemsOnSpinner(list);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}