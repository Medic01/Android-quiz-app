package com.fsre.quiz_app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fsre.quiz_app.menu.Home;
import com.fsre.quiz_app.R;
import com.fsre.quiz_app.models.RegistrationForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference usersDatabaseRef;

    private EditText nameTxt;
    private EditText emailTxt;
    private EditText passwordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        this.db = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.usersDatabaseRef = this.db.getReference("users");

        this.nameTxt = findViewById(R.id.registration_name_input);
        this.emailTxt = findViewById(R.id.registration_email_input);
        this.passwordTxt = findViewById(R.id.registration_password_input);

        TextView loginLink = findViewById(R.id.login_link);
        Button submitBtn = findViewById(R.id.registration_button);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

        // Login link handler
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void validateAndRegister() {
        String name = nameTxt.getText().toString();
        String email = emailTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        // Reset errors
        nameTxt.setError(null);
        emailTxt.setError(null);
        passwordTxt.setError(null);

        // Validate fields
        if (TextUtils.isEmpty(name)) {
            nameTxt.setError("Ime je obavezno");
            nameTxt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailTxt.setError("E-mail adresa je obavezna");
            emailTxt.requestFocus();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTxt.setError("Unesite ispravnu e-mail adresu");
            emailTxt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordTxt.setError("Lozinka je obavezna");
            passwordTxt.requestFocus();
            return;
        } else if (password.length() < 6) {
            passwordTxt.setError("Lozinka mora sadržavati minimalno 6 znakova");
            passwordTxt.requestFocus();
            return;
        }

        // If all validations pass, proceed with registration
        RegistrationForm currentUser = new RegistrationForm(name, email, password, 0, false);
        auth.createUserWithEmailAndPassword(currentUser.email, currentUser.password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // Reset fields
                    nameTxt.setText("");
                    emailTxt.setText("");
                    passwordTxt.setText("");
                    // Add user to realtime database
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        usersDatabaseRef.child(user.getUid()).setValue(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Uspješno ste se registrirali", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(Registration.this, Home.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Došlo je do pogreške prilikom registracije", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Došlo je do pogreške prilikom registracije", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
