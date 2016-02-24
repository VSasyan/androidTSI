package eu.ensg.forester;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;

import eu.ensg.forester.DAO.ForesterDAO;
import eu.ensg.forester.POJO.ForesterPOJO;
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

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Init
        initDatabase();

        // Get Intent
        Intent intent = getIntent();
        String str_serialNumber = intent.getStringExtra(getString(R.string.serialNumber));
        forester = new ForesterPOJO(0, str_serialNumber);
        ForesterDAO dao = new ForesterDAO(database);
        if (dao.read(forester) == null) {finish();}

        Toast.makeText(MapActivity.this, forester.toString(), Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Load GUI
        formRecording = (LinearLayout)findViewById(R.id.formRecording);
        lblPosition = (TextView)findViewById(R.id.lblPosition);
        abort = (Button)findViewById(R.id.abort);
        save = (Button)findViewById(R.id.save);

        // Listener: abort
        abort.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setRecording(false);
                mapPolygon.remove();
                mapPolygon = null;
                currentPolygon = null;
            }
        });

        // Listener: save
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setRecording(false);
                mapPolygon.setFillColor(Color.GREEN);
                mapPolygon.setStrokeColor(Color.BLUE);
                Toast.makeText(MapActivity.this, ((Integer)mapPolygon.getPoints().size()).toString(), Toast.LENGTH_SHORT).show();
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void updateCurrent(LatLng latlgn) {
        XY point = new XY(latlgn);

        if (currentPosition == null) {
            currentPosition = new Point(point);
            mapMarker = mMap.addMarker(new MarkerOptions().position(latlgn));
            mapMarker.setTitle(getString(R.string.currentPosition));
            mapMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlgn, 14));
            lblPosition.setText(currentPosition.toString());
        } else {
            if (!currentPosition.getCoordinate().equals(point)) {
                currentPosition = new Point(point);
                lblPosition.setText(currentPosition.toString());

                mapMarker.setPosition(latlgn);

                if (isRecording()) {
                    currentPolygon.addCoordinate(currentPosition.getCoordinate());
                    showCurrentPolygon();
                    Toast.makeText(MapActivity.this, ((Integer)mapPolygon.getPoints().size()).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void showCurrentPolygon() {
        if (mapPolygon != null) {
            mapPolygon.remove();
            mapPolygon = null;
        }
        PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.BLUE).strokeColor(Color.BLACK);
        for (XY xy : currentPolygon.getCoordinates().getCoords()) {
            polygonOptions.add(new Point(xy).toLatLng());
        }
        mapPolygon = mMap.addPolygon(polygonOptions);
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
                Toast.makeText(MapActivity.this, "addPointofInterest", Toast.LENGTH_SHORT).show();
                Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentPosition.toLatLng()).draggable(true));
                newMarker.setTitle(getString(R.string.pointOfInterest));
                newMarker.setSnippet(currentPosition.toString());
                pointsOfInterest.add(newMarker);
                return true;

            case R.id.addSector:
                if (mapMarker != null) {
                    Toast.makeText(MapActivity.this, "addSector", Toast.LENGTH_SHORT).show();
                    currentPolygon = new eu.ensg.spatialite.geom.Polygon();
                    currentPolygon.addCoordinate(currentPosition.getCoordinate());
                    this.showCurrentPolygon();
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
        if (this.recording) {formRecording.setVisibility(View.VISIBLE);}
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
