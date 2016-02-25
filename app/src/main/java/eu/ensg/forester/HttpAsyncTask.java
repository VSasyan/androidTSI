package eu.ensg.forester;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.ensg.commons.io.WebServices;
import eu.ensg.forester.DAO.PointOfInterestDAO;
import eu.ensg.forester.POJO.PointOfInterestPOJO;
import eu.ensg.spatialite.geom.XY;

/**
 * Created by vsasyan on 25/02/16.
 */
public class HttpAsyncTask extends AsyncTask<Object, Void, String> {

    protected URL url;
    protected String streetAddress = "";

    public HttpAsyncTask(URL url) {
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(getApplicationContext(), "Geocoding...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String json = WebServices.convertStreamToString(in);

                // JSON parsing - get the streetAddress
                JSONArray array = new JSONObject(json).getJSONArray("results");
                if (array.length() > 0) {
                    streetAddress = array.getJSONObject(0).getString("formatted_address");
                } else {
                    streetAddress = new JSONObject(json).getString("status");
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException| org.json.JSONException e) {
            e.printStackTrace();
        }
        return streetAddress;
    }

    @Override
    protected void onPostExecute(String o) {
        super.onPostExecute(o);
    }
}
