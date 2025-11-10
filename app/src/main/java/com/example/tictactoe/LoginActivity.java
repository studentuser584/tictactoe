package com.example.tictactoe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS = "tictactoe_prefs";
    private static final String KEY_USER = "pref_user";
    private static final String KEY_PASS = "pref_pass";

    private EditText userEdit;
    private EditText passEdit;
    private TextView message;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        ensureDefaultCredentials();

        userEdit = findViewById(R.id.loginUsername);
        passEdit = findViewById(R.id.loginPassword);
        message = findViewById(R.id.loginMessage);

        Button loginBtn = findViewById(R.id.loginButton);
        Button configBtn = findViewById(R.id.configButton);

        loginBtn.setOnClickListener(v -> tryLogin());
        configBtn.setOnClickListener(v -> startActivity(new Intent(this, ConfigActivity.class)));
    }

    private void ensureDefaultCredentials() {
        if (!prefs.contains(KEY_USER) || !prefs.contains(KEY_PASS)) {
            prefs.edit().putString(KEY_USER, "admin").putString(KEY_PASS, "admin").apply();
        }
    }

    private void tryLogin() {
        String u = userEdit.getText().toString().trim();
        String p = passEdit.getText().toString().trim();
        String savedU = prefs.getString(KEY_USER, "admin");
        String savedP = prefs.getString(KEY_PASS, "admin");

        if (u.equals(savedU) && p.equals(savedP)) {
            message.setText("");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            message.setText("Invalid username or password");
        }
    }
}
