package eu.ensg.forester.DAO;

import eu.ensg.forester.POJO.ForesterPOJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import jsqlite.*;
import jsqlite.Exception;

/**
 * Created by vsasyan on 24/02/16.
 */
public class ForesterDAO extends DAO<ForesterPOJO> {

    public ForesterDAO(SpatialiteDatabase dataSource) {
        super(dataSource);
    }

    @Override
    public SpatialiteDatabase getDB() {
        return super.getDB();
    }

    @Override
    public ForesterPOJO create(ForesterPOJO POJO) {
        try {
            String sql = "INSERT INTO Forester (FirstName, LastName, Serial) VALUES ('%1$s', '%2$s', '%3$s');";
            sql = String.format(sql, POJO.getLastName(), POJO.getName(), POJO.getSerial());
            getDB().exec(sql);
            return read(POJO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ForesterPOJO read(ForesterPOJO POJO) {
        try {
            String sql = "SELECT * FROM Forester WHERE Serial = '%1$s';";
            sql = String.format(sql, POJO.getSerial());
            Stmt stmt = getDB().prepare(sql);
            if (stmt.step()) {
                int id = stmt.column_int(0);
                String str_name = stmt.column_string(1);
                String str_lastName = stmt.column_string(2);
                String str_serial = stmt.column_string(3);
                return new ForesterPOJO(id, str_name, str_lastName, str_serial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ForesterPOJO read(int id) {
        try {
            String sql = "SELECT * FROM Forester WHERE ID = '%1$s';";
            sql = String.format(sql, id);
            Stmt stmt = getDB().prepare(sql);
            if (stmt.step()) {
                //int id = stmt.column_int(0);
                String str_name = stmt.column_string(1);
                String str_lastName = stmt.column_string(2);
                String str_serial = stmt.column_string(3);
                return new ForesterPOJO(id, str_name, str_lastName, str_serial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ForesterPOJO update(ForesterPOJO POJO) {
        try {
            String sql = "UPDATE Forester SET FirstName = '%1$s', LastName = '%2$s', Serial = '%3$s' WHERE id = %4$d";
            sql = String.format(sql, POJO.getName(), POJO.getLastName(), POJO.getSerial(), POJO.getId());
            getDB().exec(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public boolean delete(ForesterPOJO POJO) {
        try {
            String sql = "DELETE FROM Forester WHERE ID = %1$d;";
            getDB().exec(String.format(sql, POJO.getId()), null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
