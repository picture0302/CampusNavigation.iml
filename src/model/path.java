package model;

public class path {
    private int id;
    private int startid;
    private int endid;
    private double distance;
    private int statue;
    public path() {
    }

    public path(double distance, int endid, int id, int startid, int statue) {
        this.distance = distance;
        this.endid = endid;
        this.id = id;
        this.startid = startid;
        this.statue = statue;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getEndid() {
        return endid;
    }

    public void setEndid(int endid) {
        this.endid = endid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartid() {
        return startid;
    }

    public void setStartid(int startid) {
        this.startid = startid;
    }

    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }
}
