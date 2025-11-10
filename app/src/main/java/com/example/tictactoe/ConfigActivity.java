package com.example.tictactoe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {
    private static final String PREFS = "tictactoe_prefs";
    private static final String KEY_USER = "pref_user";
    private static final String KEY_PASS = "pref_pass";

    private EditText newUser;
    private EditText currentPass;
    private EditText newPass;
    private EditText newPassConfirm;
    private TextView info;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        newUser = findViewById(R.id.configUsername);
        currentPass = findViewById(R.id.configCurrentPassword);
        newPass = findViewById(R.id.configPassword);
        newPassConfirm = findViewById(R.id.configPasswordConfirm);
        info = findViewById(R.id.configInfo);

        newUser.setText(prefs.getString(KEY_USER, "admin"));

        Button saveBtn = findViewById(R.id.saveConfigButton);
        Button resetBtn = findViewById(R.id.resetToDefaultButton);

        saveBtn.setOnClickListener(v -> {
            String savedPass = prefs.getString(KEY_PASS, "admin");
            String current = currentPass.getText().toString();
            String u = newUser.getText().toString().trim();
            String p = newPass.getText().toString();
            String pConfirm = newPassConfirm.getText().toString();

            if (u.isEmpty()) {
                info.setText("Username cannot be empty");
                return;
            }

            if (current.isEmpty()) {
                info.setText("Enter current password to change settings");
                return;
            }

            if (!current.equals(savedPass)) {
                info.setText("Current password is incorrect");
                return;
            }

            if (p.isEmpty()) {
                info.setText("New password cannot be empty");
                return;
            }

            if (!p.equals(pConfirm)) {
                info.setText("New password and confirmation do not match");
                return;
            }

            prefs.edit().putString(KEY_USER, u).putString(KEY_PASS, p).apply();
            info.setText("Credentials updated");
            currentPass.setText("");
            newPass.setText("");
            newPassConfirm.setText("");
        });

        resetBtn.setOnClickListener(v -> {
            prefs.edit().putString(KEY_USER, "admin").putString(KEY_PASS, "admin").apply();
            newUser.setText("admin");
            currentPass.setText("");
            newPass.setText("");
            newPassConfirm.setText("");
            info.setText("Reset to default admin/admin");
        });
    }
}
