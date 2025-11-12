package com.kan_n.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kan_n.R;
import com.kan_n.ui.auth.AuthFragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AuthFragment.newInstance())
                    .commitNow();
        }
    }
}