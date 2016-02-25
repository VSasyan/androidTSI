package eu.ensg.forester.DAO;

import eu.ensg.forester.POJO.PointOfInterestPOJO;
import eu.ensg.forester.POJO.PolygonPOJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.geom.BadGeometryException;
import eu.ensg.spatialite.geom.Point;
import jsqlite.*;
import jsqlite.Exception;

/**
 * Created by vsasyan on 25/02/16.
 */
public class PointOfInterestDAO extends DAO<PointOfInterestPOJO> {

    public PointOfInterestDAO(SpatialiteDatabase dataSource) {
        super(dataSource);
    }

    @Override
    public SpatialiteDatabase getDB() {
        return super.getDB();
    }

    @Override
    public PointOfInterestPOJO create(PointOfInterestPOJO POJO) {
        try {
            String sql = "INSERT INTO PointOfInterest (ForesterID, Name, Description, position) VALUES (%1$d, '%2$s', '%3$s', %4$s);";
            sql = String.format(sql, POJO.getForesterId(), POJO.getName(), POJO.getDescription(), POJO.getPosition().toSpatialiteQuery(4326));
            getDB().exec(sql);
            int id = (int)getDB().last_insert_rowid();
            return read(new PointOfInterestPOJO(id));
        } catch (jsqlite.Exception | BadGeometryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PointOfInterestPOJO read(PointOfInterestPOJO POJO) {
        try {
            String sql = "SELECT Id, ForesterID, Name, Description, ST_ASTEXT(position) FROM PointOfInterest WHERE ID = %1$d;";
            sql = String.format(sql, POJO.getId());
            Stmt stmt = getDB().prepare(sql);
            if (stmt.step()) {
                int id = stmt.column_int(0);
                int int_foresterId = stmt.column_int(1);
                String str_name = stmt.column_string(2);
                String str_description = stmt.column_string(3);
                String str_position = stmt.column_string(4);
                Point poi_position = Point.unMarshall(str_position);
                return new PointOfInterestPOJO(id, int_foresterId, str_name, str_description, poi_position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PointOfInterestPOJO update(PointOfInterestPOJO POJO) {
        try {
            String sql = "UPDATE PointOfInterest SET ForesterId = '%1$d', Name = '%2$s', Description = '%3$s', position = %4$s WHERE id = %5$d";
            sql = String.format(sql, POJO.getForesterId(), POJO.getName(), POJO.getDescription(), POJO.getPosition().toSpatialiteQuery(4326), POJO.getId());
            getDB().exec(sql);
        } catch (Exception | BadGeometryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean delete(PointOfInterestPOJO POJO) {
        try {
            String sql = "DELETE FROM PointOfInterest WHERE ID = %1$d;";
            getDB().exec(String.format(sql, POJO.getId()), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
