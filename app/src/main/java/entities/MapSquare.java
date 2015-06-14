package entities;

/**
 * Created by Esa on 15.2.2015.
 */
public class MapSquare {


    double northWestLat;
    double northWestLon;
    double southEastLat;
    double southEastLon;
    String type;
    String ownedId;
    String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public double getNorthWestLat() {
        return northWestLat;
    }

    public void setNorthWestLat(double northWestLat) {
        this.northWestLat = northWestLat;
    }

    public double getNorthWestLon() {
        return northWestLon;
    }

    public void setNorthWestLon(double northWestLon) {
        this.northWestLon = northWestLon;
    }

    public double getSouthEastLat() {
        return southEastLat;
    }

    public void setSouthEastLat(double southEastLat) {
        this.southEastLat = southEastLat;
    }

    public double getSouthEastLon() {
        return southEastLon;
    }

    public void setSouthEastLon(double southEastLon) {
        this.southEastLon = southEastLon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwnedId() {
        return ownedId;
    }

    public void setOwnedId(String ownedId) {
        this.ownedId = ownedId;
    }


    /*public MapSquare(String id, double northWestLat, double northWestLon, double southEastLat, double southEastLon, String ownedId) {
        this.id = id;
        this.northWestLat = northWestLat;
        this.northWestLon = northWestLon;
        this.southEastLat = southEastLat;
        this.southEastLon = southEastLon;
        this.ownedId = ownedId;
    }*/

    public MapSquare(String id, double northWestLat, double northWestLon, double southEastLat, double southEastLon, String type) {
        this.id = id;
        this.northWestLat = northWestLat;
        this.northWestLon = northWestLon;
        this.southEastLat = southEastLat;
        this.southEastLon = southEastLon;
        this.type = type;
    }

}
