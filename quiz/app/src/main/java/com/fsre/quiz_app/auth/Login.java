package com.fsre.quiz_app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fsre.quiz_app.menu.Backoffice;
import com.fsre.quiz_app.menu.Home;
import com.fsre.quiz_app.R;
import com.fsre.quiz_app.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        this.db = FirebaseDatabase.getInstance().getReference();

        EditText emailTxt = findViewById(R.id.email_input);
        EditText passwordTxt = findViewById(R.id.password_input);
        Button loginBtn = findViewById(R.id.login_button);
        TextView registrationLink = findViewById(R.id.registration_link);

        // Login handler
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if a user is already authenticated, and sign them out if needed
                auth.signOut();

                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();

                // Reset errors
                emailTxt.setError(null);
                passwordTxt.setError(null);

                // Validate fields
                if (TextUtils.isEmpty(email)) {
                    emailTxt.setError("E-mail adresa je obavezna");
                    emailTxt.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailTxt.setError("Unesite ispravnu e-mail adresu");
                    emailTxt.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordTxt.setError("Lozinka adresa je obavezna");
                    passwordTxt.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    passwordTxt.setError("Lozinka mora sadržavati minimalno 6 znakova");
                    passwordTxt.requestFocus();
                    return;
                }

                // Perform login
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if(user != null){
                                db.child("users").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> dbTask) {
                                        emailTxt.setText("");
                                        passwordTxt.setText("");
                                        if(task.isSuccessful()){
                                            try {
                                                User currentUser = dbTask.getResult().getValue(User.class);
                                                if (currentUser != null) {
                                                    Intent intent;
                                                    if(currentUser.isAdmin){
                                                        intent = new Intent(Login.this, Backoffice.class);
                                                    }
                                                    else{
                                                        intent = new Intent(Login.this, Home.class);
                                                    }
                                                    startActivity(intent);
                                                }
                                            }
                                            catch (NullPointerException e){
                                                Log.e("NoData", e.getMessage());
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Došlo je do pogreške prilikom prijave", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

         // Registration link handler
        registrationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
            }
        });
    }
}
