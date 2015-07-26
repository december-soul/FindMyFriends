package com.example.patrick.findmyfriends;

/**
 * Created by patrick on 10.07.15.
 */
public class User {
    private int index;
    private String username;
    private float lat;
    private float lon;
    private String time;

    public User(int index, String username, float lat, float lon, String time) {
        this.time = time;
        this.index = index;
        this.username = username;
        this.lat = lat;
        this.lon = lon;
    }

    public int getIndex() {
        return index;
    }

    public String getUsername() {
        return username;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public String getTime() {
        return time;
    }
}
