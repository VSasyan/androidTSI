package eu.ensg.forester;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.ensg.commons.io.WebServices;

/**
 * Created by vsasyan on 25/02/16.
 */
public class HttpAsyncTask extends AsyncTask<Object, Void, String> {

    protected String streetAddress = "";
    protected Context context;
    protected URL url;
    protected ProgressDialog dialog;

    public HttpAsyncTask(Context context, ProgressDialog dialog, URL url) {
        this.context = context;
        this.dialog = dialog;
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setTitle(context.getString(R.string.geocoding));
        dialog.setMessage(context.getString(R.string.loading));
        dialog.setIndeterminate(true);
        dialog.show();
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
        if (dialog.isShowing()) {dialog.dismiss();}
    }
}
