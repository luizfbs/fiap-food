package br.com.fiap.fiapfood.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.List;

@Table(name = "Users")
public class User extends Model {

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Email")
    public String email;

    @Column(name = "Password")
    public String password;

    @Expose
    @Column(name = "Birthdate")
    public Date birthdate;

    @Expose
    @Column(name = "RegistrationDate")
    public Date registrationDate;

    public User(){
        super();
    }

    public User(String name, String email, String password, Date birthdate) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthdate = birthdate;
    }

    public static User login(String email, String password) {
        return new Select()
                .from(User.class)
                .where("Email = ? AND Password = ?", email, password)
                .executeSingle();
    }

    public static User create(String name, String email, String password, Date birthdate){
        User user = new User(name, email, password, birthdate);
        user.registrationDate = new Date();

        user.save();
        return user;
    }

    public static User find(String email) {
        return new Select()
                .from(User.class)
                .where("Email = ?", email)
                .executeSingle();
    }

    public static User get(long id) {
        return new Select()
                .from(User.class)
                .where("Id = ?", id)
                .executeSingle();
    }
}
