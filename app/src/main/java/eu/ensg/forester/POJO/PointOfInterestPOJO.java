package eu.ensg.forester.POJO;

import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import jsqlite.*;

/**
 * Created by vsasyan on 25/02/16.
 */
public class PointOfInterestPOJO extends POJO {

    protected int foresterId;
    protected String name, description;
    protected Point position;

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getForesterId() {
        return foresterId;
    }

    public void setForesterId(int foresterId) {
        this.foresterId = foresterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PointOfInterestPOJO(int id) {
        super(id);
    }

    public PointOfInterestPOJO(int id, int foresterId, String name, String description, Point position) {
        super(id);
        if (position == null) {throw new RuntimeException("Position is null");}
        this.foresterId = foresterId;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    @Override
    public String toString() {
        return "PointOfInterestPOJO{" +
                "id=" + id +
                ", foresterId=" + foresterId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", position=" + position +
                '}';
    }
}
