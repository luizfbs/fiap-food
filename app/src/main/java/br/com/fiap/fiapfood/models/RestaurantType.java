package br.com.fiap.fiapfood.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.util.List;

@Table(name = "RestaurantTypes")
public class RestaurantType extends Model {

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Description")
    public String description;

    public RestaurantType(){
        super();
    }

    public RestaurantType(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public static void feed(){
        RestaurantType type1 =  new RestaurantType("All You Can Eat", "All You Can Eat");
        type1.save();

        RestaurantType type2 =  new RestaurantType("Fast Food", "Fast Food");
        type2.save();

        RestaurantType type3 =  new RestaurantType("Delivery", "Delivery");
        type3.save();

        RestaurantType type4 =  new RestaurantType("Undefined", "Undefined");
        type4.save();

        RestaurantType type5 =  new RestaurantType("Unknown", "Unknown");
        type5.save();
    }

    public static List<RestaurantType> retrieve() {
        return new Select()
                .from(RestaurantType.class)
                .execute();
    }

    public static RestaurantType find(String name) {
        return new Select()
                .from(RestaurantType.class)
                .where("Name = ?", name)
                .executeSingle();
    }

}
