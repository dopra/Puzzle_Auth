package com.dopra.authentication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    //Declare a TAG for Log Messages
    private static final String TAG = "LoginActivity";

    //Declare visual elements of UI
    private EditText emailField;
    private EditText passwordField;

    //Declare and Initialize Flags of AuthListener
    private String authCase = "";
    private Boolean isLogged = false;
    private Boolean isNotified = false;
    private Boolean isRegistered;

    //Firebase Authentication Elements
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init_UI();

        //Initialize Auth Instance
        auth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                switch (authCase) {

                    //First Time User
                    case "Register":

                        //This flag will be used later
                        isRegistered = false;

                        //Is the user NOT logged?
                        if (!isLogged) {

                            //Is user different than null?
                            if (user != null) {

                                //The account was verified?
                                if (user.isEmailVerified()) {

                                    //GO TO MAIN MENU ACTIVITY!
                                    //startActivity(new Intent(Login.this, Registry.class));
                                    Toast.makeText(Login.this, "Bienvenido a bordo! Sólo unos datos más...", Toast.LENGTH_SHORT).show();

                                } else {

                                    //If the user is not verified, send him a verification email
                                    user.sendEmailVerification();

                                    //Show a message notifying about the email
                                    showVerifyDialog(isNotified);

                                    //Set notified flag as TRUE
                                    isNotified = true;
                                }
                            }
                        }

                        //Reset the authCase
                        authCase = "";
                        break;


                    default:

                        //User already REGISTERED
                        isRegistered = true;

                        //Is the user NOT Logged?
                        if (!isLogged) {

                            //Is different than null?
                            if (user != null) {

                                //The account was verified?
                                if (user.isEmailVerified()) {

                                    Log.d(TAG, "User Name: " + auth.getCurrentUser().getDisplayName());

                                    //GO TO MAIN MENU ACTIVITY!
                                    startActivity(new Intent(Login.this, MainMenu.class));

                                    //Set the logged flag
                                    isLogged = true;

                                } else {

                                    //TODO: Make a progress dialog to keep the user informed

                                    //Was NOT the user notified about the verification email?
                                    if (!isNotified) {

                                        //Show the message
                                        showVerifyDialog(isRegistered);

                                        //Set the notified flag as TRUE
                                        isNotified = true;
                                    }
                                }
                            }
                        }

                        authCase = "";
                        break;
                }
            }
        };
    }

//----- ADDING OR REMOVING THE AUTHLISTENER

    @Override
    protected void onStart() {
        super.onStart();

        auth.addAuthStateListener(authStateListener);
        isLogged = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        //TODO: WHY IS SET isLogged AS FALSE AGAIN?

        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
            isLogged = false;
        }

        isLogged = false;
    }


//----- VALIDATION FUNCTIONS

    private boolean validateEmailInput() {

        boolean valid = true;

        String email = emailField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Requerido");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailField.setError("Formato de mail incorrecto");
            valid = false;

        }

        return valid;
    }

    private boolean isEmptyPassword() {

        boolean valid = false;

        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Requerido");
            valid = true;
        }

        return valid;
    }

    private boolean validatePasswordInput() {

        Boolean valid = true;

        String password = passwordField.getText().toString();

        String pttrn = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?!=.*[@#$%]).{8,15}$";

        Pattern pattern = Pattern.compile(pttrn);
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches()) {
            valid = false;
        }

        return valid;
    }


//----- MAIN FUNCTIONS

    private void signIn(String email, String password) {

        isNotified = false;

        showProgressDialog();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "Login was Successful? -> " + task.isSuccessful());

                if (!task.isSuccessful()) {

                    //Clear the password field on an error
                    passwordField.setText("");
                    Toast.makeText(Login.this, "Usuario o Contraseña Incorrectos", Toast.LENGTH_LONG).show();

                    authCase = "";
                }

                else {
                    passwordField.setText("");
                }

                hideProgressDialog();
            }
        });
    }

    private void resetPassword(String email) {

        if (!validateEmailInput()) {
            return;
        }

        Log.d(TAG, "Please reset my password, I'm: " + email);

        //showProgressDialog();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Te enviamos un correo para restablecer tu password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


//----- AUX FUNCTIONS

    private void init_UI() {

        // ---- Initialize Email Field
        emailField = (EditText) findViewById(R.id.email);

        //This will check for a valid email when the field lose focus
        emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateEmailInput();
                }
            }
        });

        // ---- Initialize Password Field
        passwordField = (EditText) findViewById(R.id.password);

        //This will check for a valid password when the field lose focus
        passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String password = passwordField.getText().toString();

                    if (TextUtils.isEmpty(password)) {
                        passwordField.setError("Requerido");
                    }
                }
            }
        });
    }

    public void goToRegister(View view) {

        //Shows Register Activity
        startActivity(new Intent(Login.this, Register.class));

        //createAccount(emailField.getText().toString(), passwordField.getText().toString());
    }

    public void signInAction(View view) {

    //--- Input Validation Rutine

        //First check for a valid Email
        if (!validateEmailInput()) {
            //Return showing an error message
            return;
        }

        //After check if password is not empty
        else if (isEmptyPassword()) {
            //Return showing a "Required" message
            return;
        }

        //After if it match the pattern
        else if (!validatePasswordInput()) {

            //Simulates make a querys
            showProgressDialog();

            //Wait for 2 seconds
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    hideProgressDialog();

                    //Clear the password field on an error
                    passwordField.setText("");

                    //Shows the "Wrong email or password" message
                    Toast.makeText(Login.this, "Usuario o Contraseña Incorrectos", Toast.LENGTH_LONG).show();
                    return;

                }
            }, 2000);

            return;
        }

        else {
            //If all inputs were valid
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }

    }

    public void resetPassword(View view) {

        resetPassword(emailField.getText().toString());
    }

    public void showVerifyDialog(Boolean isRegistered) {

        // 1. Instantiate an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);

        // 2. Configure the Alert Dialog
        builder.setMessage("Enviamos un correo a tu cuenta, por favor verifica tu correo");

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (isRegistered) {
            builder.setPositiveButton("Reenviar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    auth.getCurrentUser().sendEmailVerification();
                    dialog.dismiss();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Cargando...");
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
