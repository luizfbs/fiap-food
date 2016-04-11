package br.com.fiap.fiapfood.models;

import java.util.List;

public class Place {

    public Geometry geometry;
    public String icon;
    public String id;
    public String name;
    public OpeningHours opening_hours;
    public List<Photo> photos;
    public String place_id;
    public double rating;
    public String reference;
    public String scope;
    public List<String> types;
    public String vicinity;

    public class Geometry{
        public Location location;
    }

    public class Location{
        public double lat;
        public double lng;
    }

    public class OpeningHours{
        public boolean open_now;
        public Object weekday_text;
    }

    public class Photo{
        public int width;
        public int height;
        public String photo_reference;
        public List<String> html_attributions;
    }

}
