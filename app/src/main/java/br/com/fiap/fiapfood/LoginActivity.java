package br.com.fiap.fiapfood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import br.com.fiap.fiapfood.helpers.AuthHelper;
import br.com.fiap.fiapfood.models.User;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    public static int REGISTER_RESULT_CODE = 100;
    private CallbackManager callbackManager;

    @Bind(R.id.email) EditText email;
    @Bind(R.id.password) EditText password;
    @Bind(R.id.keepConnected) CheckBox keepConnected;

    @Bind(R.id.btnLoginFB) LoginButton btnLoginFB;
    private SimpleDateFormat dateFormatter;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        btnLoginFB.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));

        callbackManager = CallbackManager.Factory.create();
        btnLoginFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String name = object.getString("name");
                                    String email = object.getString("email");

                                    Date birthdate = null;
                                    try {
                                        birthdate = dateFormatter.parse(object.getString("birthday"));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    User user = User.find(email);
                                    if (user == null) {
                                        user = User.create(name, email, null, birthdate);
                                    }

                                    startMain(user);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,birthday");

                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, R.string.login_canceled, Toast.LENGTH_LONG).show();
                if (progress != null) progress.dismiss();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                if (progress != null) progress.dismiss();
            }
        });
    }

    private void startMain(User user){
        AuthHelper.createSession(this, user, keepConnected.isChecked());

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);

        finish();
    }

    @OnClick(R.id.btnLogin)
    public void doLogin(View view) {
        // check if the fields are filled
        email.setError(null);
        password.setError(null);

        if(email.getText().length() == 0){
            email.setError(getString(R.string.you_must_provide_your_email));
            return;
        }

        if(password.getText().length() == 0){
            password.setError(getString(R.string.you_must_provide_your_password));
            return;
        }

        // check if user && password match
        User user = User.login(email.getText().toString(), password.getText().toString());
        if(user == null){
            Toast.makeText(this, R.string.user_and_password_do_not_match, Toast.LENGTH_LONG).show();
            return;
        }

        // create session and start main
        startMain(user);
    }

    @OnClick(R.id.btnRegister)
    public void startRegister(View view) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivityForResult(i, REGISTER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progress = ProgressDialog.show(this, "", getString(R.string.loading), true);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_RESULT_CODE) {
            if(resultCode == RESULT_OK){

                long userId = data.getLongExtra(RegisterActivity.RETURN_INTENT_USERID_KEY, -1);

                User user = User.get(userId);

                startMain(user);

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, R.string.an_error_ocurred_when_trying_to_register_a_new_user, Toast.LENGTH_LONG).show();

                if(progress != null) progress.dismiss();

                return;

            }
        }

    }
}
