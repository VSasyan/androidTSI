package eu.ensg.forester;

import android.content.Context;

import java.io.IOException;

import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.*;
import jsqlite.Exception;

/**
 * Created by vsasyan on 24/02/16.
 */
public class ForesterSpatialiteOpenHelper extends SpatialiteOpenHelper {
    private static final String DB_FILE_NAME = "Forester.sqlite";
    private static final int VERSION = 1;

    static final String CREATE_FORESTER = "CREATE TABLE Forester (ID integer PRIMARY KEY AUTOINCREMENT, FirstName string NOT NULL, LastName string NOT NULL, Serial string NULL);";
    static final String CREATE_POINT_OF_INTEREST = "CREATE TABLE PointOfInterest (ID integer PRIMARY KEY AUTOINCREMENT, ForesterID integer NOT NULL, Name string NOT NULL, Description string, CONSTRAINT FK_poi_forester FOREIGN KEY (foresterID) REFERENCES forester (id));";
    static final String CREATE_DISTRICT = "CREATE TABLE District (ID integer PRIMARY KEY AUTOINCREMENT, ForesterID integer NOT NULL, Name string NOT NULL, Description string, CONSTRAINT FK_poi_forester FOREIGN KEY (foresterID) REFERENCES forester (id));";
    static final String CREATE_COL_GEOM_POI = "SELECT AddGeometryColumn('PointOfInterest', 'position', 4326, 'POINT', 'XY', 0);";
    static final String CREATE_COL_GEOM_DISTRICT = "SELECT AddGeometryColumn('District', 'Area', 4326, 'POLYGON', 'XY', 0);";

    public ForesterSpatialiteOpenHelper(Context context) throws IOException, Exception {
        super(context, DB_FILE_NAME, VERSION);
    }

    @Override
    public void onCreate(Database db) throws Exception {
        getDatabase().exec(CREATE_FORESTER);
        getDatabase().exec(CREATE_POINT_OF_INTEREST);
        getDatabase().exec(CREATE_DISTRICT);
        getDatabase().exec(CREATE_COL_GEOM_POI);
        getDatabase().exec(CREATE_COL_GEOM_DISTRICT);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) throws jsqlite.Exception {

    }
}
