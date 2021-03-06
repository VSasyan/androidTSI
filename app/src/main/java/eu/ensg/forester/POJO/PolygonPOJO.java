package eu.ensg.forester.POJO;

import eu.ensg.spatialite.geom.Polygon;

/**
 * Created by vsasyan on 25/02/16.
 */
public class PolygonPOJO extends POJO {

    protected int foresterId;
    protected String name, description;
    protected Polygon area;

    public PolygonPOJO(int id) {
        super(id);
    }

    public PolygonPOJO(int id, int foresterId, String name, String description, Polygon area) {
        super(id);
        if (area == null) {throw new RuntimeException("Area is null");}
        this.foresterId = foresterId;
        this.name = name;
        this.description = description;
        this.area = area;
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
        name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Polygon getArea() {
        return area;
    }

    public void setArea(Polygon area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "PolygonPOJO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", foresterId='" + foresterId + '\'' +
                ", description='" + description + '\'' +
                ", area=" + area +
                '}';
    }
}
