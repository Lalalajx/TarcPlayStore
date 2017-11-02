package com.example.rongfu.tarucplaystore.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rongfu.tarucplaystore.Activity.LoginActivity;
import com.example.rongfu.tarucplaystore.R;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button btnLinkLogin = (Button) findViewById(R.id.button_link_login);
        Button btnRegister = (Button) findViewById(R.id.button_register);

        btnLinkLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        //encrypt password after register button


    }
}
