package eu.ensg.forester;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import eu.ensg.commons.io.WebServices;
import eu.ensg.forester.DAO.ForesterDAO;
import eu.ensg.forester.DAO.PointOfInterestDAO;
import eu.ensg.forester.DAO.PolygonDAO;
import eu.ensg.forester.POJO.ForesterPOJO;
import eu.ensg.forester.POJO.PointOfInterestPOJO;
import eu.ensg.forester.POJO.PolygonPOJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.XY;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected SpatialiteDatabase database;
    protected ForesterPOJO forester;
    protected GoogleMap mMap;
    protected TextView lblPosition;
    protected Marker mapMarker;
    protected Polygon mapPolygon;
    protected eu.ensg.spatialite.geom.Point currentPosition;
    protected eu.ensg.spatialite.geom.Polygon currentPolygon;
    protected boolean recording = false;
    protected Button save, abort;
    protected LinearLayout formRecording;
    protected ArrayList<Marker> pointsOfInterest = new ArrayList<Marker>();

    // Define a listener that responds to location updates
    public LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            updateCurrent(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Init
        initDatabase();

        // Get Intent
        Intent intent = getIntent();
        String str_serialNumber = intent.getStringExtra(getString(R.string.serialNumber));
        forester = new ForesterPOJO(0, str_serialNumber);
        ForesterDAO dao = new ForesterDAO(database);
        forester = dao.read(forester);
        if (forester == null) {finish();}

        Toast.makeText(MapActivity.this, forester.toString(), Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Load GUI
        formRecording = (LinearLayout) findViewById(R.id.formRecording);
        lblPosition = (TextView) findViewById(R.id.lblPosition);
        abort = (Button) findViewById(R.id.abort);
        save = (Button) findViewById(R.id.save);

        // Listener: abort
        abort.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                abortCurrentPolygon();
            }
        });

        // Listener: save
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                saveCurrentPolygon();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(MapActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        // Init the GPS
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapActivity.this, getString(R.string.gpsAccessDenied), Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void updateCurrent(LatLng latlgn) {
        XY point = new XY(latlgn);

        currentPosition = new Point(point);
        lblPosition.setText(currentPosition.toString());
        if (isRecording()) {updateCurrentPolygon();}

        if (mapMarker == null) {
            mapMarker = mMap.addMarker(new MarkerOptions().position(latlgn));
            mapMarker.setTitle(getString(R.string.currentPosition));
            mapMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlgn, 14));
        } else {
            mapMarker.setPosition(latlgn);
        }
    }

    public  void saveCurrentPosition() {
        try {
            // Geocoding
            XY xy = currentPosition.getCoordinate();
            String key = "AIzaSyB_aJha7D3ZAP3tHbdxGGy1m6gNgURs7Zs";
            String address = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1$s,%2$s&key=%3$s";
            URL url = new URL(String.format(address, String.valueOf(xy.getX()), String.valueOf(xy.getY()), key));
            String streetAddress = "";

            AsyncTask task = new HttpAsyncTask(url);
            task.execute();
            streetAddress = (String)task.get();

            Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentPosition.toLatLng()));
            newMarker.setTitle(getString(R.string.pointOfInterest));
            newMarker.setSnippet(streetAddress);
            pointsOfInterest.add(newMarker);
            newMarker.setSnippet(getString(R.string.streetAddressGeocodingError));

            PointOfInterestPOJO poi = new PointOfInterestPOJO(0, forester.getId(), "Mon poi", newMarker.getSnippet(), currentPosition);
            PointOfInterestDAO dao = new PointOfInterestDAO(database);
            if (dao.create(poi) == null) {
                Toast.makeText(MapActivity.this, getString(R.string.poiCreateError), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapActivity.this, getString(R.string.poiCreateSuccess), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void initCurrentPolygon() {
        currentPolygon = new eu.ensg.spatialite.geom.Polygon();
        updateCurrentPolygon();
    }

    public void updateCurrentPolygon() {
        currentPolygon.addCoordinate(currentPosition.getCoordinate());
        if (mapPolygon != null) {
            mapPolygon.remove();
            mapPolygon = null;
        }
        PolygonOptions polygonOptions = new PolygonOptions();
        for (XY xy : currentPolygon.getCoordinates().getCoords()) {
            polygonOptions.add(new Point(xy).toLatLng());
        }
        mapPolygon = mMap.addPolygon(polygonOptions);
        Toast.makeText(MapActivity.this, ((Integer)mapPolygon.getPoints().size()).toString(), Toast.LENGTH_SHORT).show();
    }

    public void saveCurrentPolygon() {
        setRecording(false);
        mapPolygon.setFillColor(Color.GREEN);

        PolygonPOJO polygon = new PolygonPOJO(0, forester.getId(), "Mon polygon", "Ma description", currentPolygon);
        PolygonDAO dao = new PolygonDAO(database);
        if (dao.create(polygon) == null) {
            Toast.makeText(MapActivity.this, getString(R.string.polygonCreateError), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MapActivity.this, getString(R.string.polygonCreateSuccess), Toast.LENGTH_SHORT).show();
        }
    }

    public void abortCurrentPolygon() {
        setRecording(false);
        mapPolygon.remove();
        mapPolygon = null;
        currentPolygon = null;
    }

    /**********************************************
     * MENU MANAGEMENT
     **********************************************/
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addPointofInterest:
                saveCurrentPosition();
                return true;

            case R.id.addSector:
                if (mapMarker != null) {
                    this.setRecording(true);
                } else {
                    Toast.makeText(MapActivity.this, getString(R.string.gpsNotAvailable), Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return false;
    }

    /**********************************************
     * GETTER/SETTER
     **********************************************/
    public void setRecording(boolean recording) {
        this.recording = recording;
        if (this.recording) {
            initCurrentPolygon();
            formRecording.setVisibility(View.VISIBLE);
        }
        else {formRecording.setVisibility(View.INVISIBLE);}
    }

    public boolean isRecording() {
        return this.recording;
    }

    private void initDatabase() {
        try {
            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(this);
            database = helper.getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.cannot_init_database), Toast.LENGTH_LONG).show();
            System.exit(0);
        }
    }

}
