package com.example.offlinetrading.ui.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.offlinetrading.MainActivity;
import com.example.offlinetrading.R;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private AppDatabase db;

    // Подключение к XML
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);

        db = AppDatabase.getDatabase(this);

        loginButton.setOnClickListener(view -> login());
        registerButton.setOnClickListener(view -> register());
    }

    // Авторизация
    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInputs(username, password)) {
            User user = db.userDao().authenticate(username, password);

            if (user != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", user.getUsername());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Неправильный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Регистрация
    private void register() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInputs(username, password)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            db.userDao().insert(user);
            Toast.makeText(this, "Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show();
        }
    }

    // Валидация полей
    private boolean validateInputs(String username, String password) {
        if (username.isEmpty()) {
            usernameEditText.setError("Вы не заполнили это поле!");
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Вы не заполнили это поле!");
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен содержать не менее 6 символов!");
            return false;
        }

        return true;
    }
}


