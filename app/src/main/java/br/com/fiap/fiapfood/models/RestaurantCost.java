package br.com.fiap.fiapfood.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.util.List;

@Table(name = "RestaurantCosts")
public class RestaurantCost extends Model {

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Description")
    public String description;

    public RestaurantCost(){
        super();
    }

    public RestaurantCost(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public static void feed() {
        RestaurantCost cost1 = new RestaurantCost("$", "$");
        cost1.save();

        RestaurantCost cost2 = new RestaurantCost("$$", "$$");
        cost2.save();

        RestaurantCost cost3 = new RestaurantCost("$$$", "$$$");
        cost3.save();

        RestaurantCost cost4 = new RestaurantCost("$$$$", "$$$$");
        cost4.save();

        RestaurantCost cost5 = new RestaurantCost("$$$$$", "$$$$$");
        cost5.save();
    }

    public static List<RestaurantCost> retrieve() {
        return new Select()
                .from(RestaurantCost.class)
                .execute();
    }

    public static RestaurantCost find(String name) {
        return new Select()
                .from(RestaurantCost.class)
                .where("Name = ?", name)
                .executeSingle();
    }
}
