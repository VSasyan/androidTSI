package eu.ensg.forester.DAO;

import eu.ensg.forester.POJO.ForesterPOJO;
import eu.ensg.forester.POJO.PolygonPOJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.geom.BadGeometryException;
import eu.ensg.spatialite.geom.Polygon;
import jsqlite.*;
import jsqlite.Exception;

/**
 * Created by vsasyan on 24/02/16.
 */
public class PolygonDAO extends DAO<PolygonPOJO> {

    public PolygonDAO(SpatialiteDatabase dataSource) {
        super(dataSource);
    }

    @Override
    public SpatialiteDatabase getDB() {
        return super.getDB();
    }

    @Override
    public PolygonPOJO create(PolygonPOJO POJO) {
        try {
            String sql = "INSERT INTO District (ForesterID, Name, Description, Area) VALUES (%1$d, '%2$s', '%3$s', '%4$s');";
            sql = String.format(sql, POJO.getForesterId(), POJO.getName(), POJO.getDescription(), POJO.getArea().toSpatialiteQuery(4326));
            getDB().exec(sql);
            int id = (int)getDB().last_insert_rowid();
            return read(new PolygonPOJO(id));
        } catch (Exception | BadGeometryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PolygonPOJO read(PolygonPOJO POJO) {
        try {
            String sql = "SELECT * FROM District WHERE ID = '%1$d';";
            sql = String.format(sql, POJO.getId());
            Stmt stmt = getDB().prepare(sql);
            if (stmt.step()) {
                int id = stmt.column_int(0);
                String str_name = stmt.column_string(1);
                int int_foresterId = stmt.column_int(2);
                String str_description = stmt.column_string(3);
                Polygon pol_area = new Polygon();
                pol_area.marshall(new StringBuilder(stmt.column_string(4)));
                return new PolygonPOJO(id, int_foresterId, str_name, str_description, pol_area);
            }
        } catch (Exception | BadGeometryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PolygonPOJO update(PolygonPOJO POJO) {
        try {
            String sql = "UPDATE District SET ForesterId = '%1$d', Name = '%2$s', Description = '%3$s', Area = '%4$s' WHERE id = %5$d";
            sql = String.format(sql, POJO.getForesterId(), POJO.getName(), POJO.getDescription(), POJO.getArea().toSpatialiteQuery(4326), POJO.getId());
            getDB().exec(sql);
        } catch (Exception | BadGeometryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(PolygonPOJO POJO) {
        try {
            String sql = "DELETE FROM District WHERE ID = %1$d;";
            getDB().exec(String.format(sql, POJO.getId()), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}