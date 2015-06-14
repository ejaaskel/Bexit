package com.parasohjelmistoprojekti.bexit;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import serverconnection.RegisterUser;

/**
 * Created by Esa on 10.3.2015.
 */
public class Register extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText userNameField = (EditText)findViewById(R.id.username_edittext);
        final EditText emailField = (EditText)findViewById(R.id.email_edittext);
        final EditText password = (EditText)findViewById(R.id.password_edittext);
        final EditText confirmPassword =(EditText)findViewById(R.id.confirmpassword_edittext);

        Button signUpButton = (Button)findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().equals(confirmPassword.getText().toString())){
                    try {
                        JSONObject reply = new RegisterUser().execute(userNameField.getText().toString(), emailField.getText().toString(), password.getText().toString()).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }


            }
        });



    }
}
