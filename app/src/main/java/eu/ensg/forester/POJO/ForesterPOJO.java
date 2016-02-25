package eu.ensg.forester.POJO;

/**
 * Created by vsasyan on 24/02/16.
 */
public class ForesterPOJO extends POJO {

    protected String name, lastName, serial;

    public ForesterPOJO(int id) {
        super(id);
    }

    public ForesterPOJO(int id, String serial) {
        super(id);
        this.serial = serial;
    }

    public ForesterPOJO(int id, String name, String lastName, String serial) {
        super(id);
        this.name = name;
        this.lastName = lastName;
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "ForesterPOJO{" +
                "ID='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", serial='" + serial + '\'' +
                '}';
    }
}
