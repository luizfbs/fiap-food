package br.com.fiap.fiapfood.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.List;

@Table(name = "Restaurants")
public class Restaurant extends Model {

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Phone")
    public String phone;

    @Expose
    @Column(name = "Type")
    public RestaurantType type;

    @Expose
    @Column(name = "Cost")
    public RestaurantCost cost;

    @Expose
    @Column(name = "Notes")
    public String notes;

    @Expose
    @Column(name = "PictureURL")
    public String pictureURL;

    @Expose
    @Column(name = "Latitude")
    public double latitude;

    @Expose
    @Column(name = "Longitude")
    public double longitude;

    @Expose
    @Column(name = "CreatedBy")
    public User createdBy;

    @Expose
    @Column(name = "CreatedOn")
    public Date createdOn;

    public Restaurant(){
        super();
    }

    public Restaurant(String name, String phone, RestaurantType type, RestaurantCost cost, String notes, String pictureURL, long latitude, long longitude, User createdBy, Date createdOn) {
        super();
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.cost = cost;
        this.notes = notes;
        this.pictureURL = pictureURL;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public static Restaurant get(long userId, long id) {
        return new Select()
                .from(Restaurant.class)
                .where("CreatedBy = ?", userId)
                .where("Id = ?", id)
                .executeSingle();
    }

    public static Restaurant get(long userId, String name, double latitude, double longitude) {
        return new Select()
                .from(Restaurant.class)
                .where("CreatedBy = ?", userId)
                .where("Name = ? AND Latitude = ? AND Longitude = ?", name, latitude, longitude)
                .executeSingle();
    }

    public static List<Restaurant> retrieve(long userId) {
        return new Select()
                .from(Restaurant.class)
                .where("CreatedBy = ?", userId)
                .orderBy("Name")
                .execute();
    }

    public static List<Restaurant> find(long userId, String name, long typeFilter, long costFilter) {
        String clause = "1 = 1 AND ";

        From query = new Select()
                .from(Restaurant.class)
                .where("CreatedBy = ?", userId);

        if(name.length() > 0)
            query = query.where("Name LIKE ? ", new String[]{'%' + name + '%'});

        if(typeFilter > -1)
            query = query.where("Type = ? ", typeFilter);

        if(costFilter > -1)
            query = query.where("Cost = ? ", costFilter);

        return query.orderBy("Name")
            .execute();
    }
}
