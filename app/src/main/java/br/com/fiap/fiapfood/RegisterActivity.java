package br.com.fiap.fiapfood;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.com.fiap.fiapfood.models.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    public static String RETURN_INTENT_USERID_KEY = "register-userid";

    @Bind(R.id.name) EditText name;
    @Bind(R.id.email) EditText email;
    @Bind(R.id.password) EditText password;
    @Bind(R.id.birthdate) EditText birthdate;
    @Bind(R.id.checkTerms) CheckBox checkTerms;

    private DatePickerDialog birthdatePickerDialog;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        birthdate.setFocusable(false);
        Calendar calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

        birthdatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(year, monthOfYear, dayOfMonth);
                birthdate.setText(dateFormatter.format(date.getTime()));
            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @OnClick(R.id.birthdate)
    public void clickBirthdate(View view){
        birthdatePickerDialog.show();
    }

    @OnClick(R.id.btnRegister)
    public void doRegister(View view) {
        // check if the fields are filled
        name.setError(null);
        email.setError(null);
        password.setError(null);
        birthdate.setError(null);

        if(name.getText().length() == 0){
            name.setError(getString(R.string.you_must_provide_your_name));
            return;
        }

        if(email.getText().length() == 0){
            email.setError(getString(R.string.you_must_provide_your_email));
            return;
        }

        if(password.getText().length() == 0){
            password.setError(getString(R.string.you_must_provide_your_password));
            return;
        }

        if(birthdate.getText().length() == 0){
            birthdate.setError(getString(R.string.you_must_provide_your_birthdate));
            return;
        }

        if(!checkTerms.isChecked()){
            checkTerms.setError(getString(R.string.you_must_agree_to_the_terms_of_service));
            return;
        }

        Date parsedBirthdate = null;
        try {
            parsedBirthdate = dateFormatter.parse(birthdate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User user = User.find(email.getText().toString());
        if(user != null){
            email.setError(getString(R.string.email_is_already_registered));
            return;
        }

        // register
        user = User.create(
            name.getText().toString(),
            email.getText().toString(),
            password.getText().toString(),
            parsedBirthdate
        );

        Intent returnIntent = new Intent();
        returnIntent.putExtra(RETURN_INTENT_USERID_KEY, user.getId());

        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
