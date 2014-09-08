package org.neo4j.example.unmanagedextension;

/**
 * Created by lyonwj on 8/11/14.
 */
public class BusinessNode {

    public String business_id;
    public Double lat;
    public Double lon;

    public void setBusiness_id(String business_id) {
        this.business_id = business_id;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public void setAddresss(String addresss) {
        this.addresss = addresss;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String addresss;

    public String getName() {
        return name;
    }

    public String getBusiness_id() {
        return business_id;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getAddresss() {
        return addresss;
    }

    public String name;



}
