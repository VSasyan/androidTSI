package eu.ensg.forester.DAO;

import eu.ensg.forester.POJO.POJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import jsqlite.*;

/**
 * Created by vsasyan on 24/02/16.
 */
public abstract class DAO<P extends POJO> {

    private final SpatialiteDatabase dataSource;

    public DAO(SpatialiteDatabase dataSource) {
        this.dataSource = dataSource;
    }

    public SpatialiteDatabase getDB() {
        return dataSource;
    }

    public abstract P create(P POJO);
    public abstract P read(P POJO);
    public abstract P update(P POJO);
    public abstract void delete(P POJO);
}
