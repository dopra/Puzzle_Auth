package com.dopra.authentication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private EditText fullName;
    private EditText email;
    private EditText psw1;
    private EditText psw2;
    private CheckBox tyc;
    private TextView tyc_link;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;

    //Declare and Initialize Flags of AuthListener
    private String authCase = "";
    private Boolean isLogged = false;
    private Boolean isNotified = false;
    private Boolean isRegistered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initialize the UI
        init_UI();

        auth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                //This flag will be used later
                isRegistered = false;

                //Is the user NOT logged?
                if (!isLogged) {

                    //Is user different than null?
                    if (user != null) {

                        //The account was verified?
                        if (user.isEmailVerified()) {

                            //GO TO MAIN MENU ACTIVITY!
                            startActivity(new Intent(Register.this, Login.class));
                            Toast.makeText(Register.this, "You are already registered, please Sign In!", Toast.LENGTH_LONG).show();

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
            }
        };


    }


    private void createAccount(String email, String password) {

        //If email or passoword input fail, just return
        if (!validateEmailInput() && !validatePasswordInput()) {
            return;
        }

        Log.d(TAG, "Please, create an account for: " + email);

        authCase = "Register";

        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "User creatation successful: " + task.isSuccessful());

                if (!task.isSuccessful()) {
                    Toast.makeText(Login.this, "Ya estás registrado =)", Toast.LENGTH_SHORT).show();

                    authCase = "";
                }

                hideProgressDialog();
            }
        });
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

    private boolean isEmptyPassword(EditText passwordField) {

        boolean valid = false;

        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Requerido");
            valid = true;
        }

        return valid;
    }

    private boolean validatePasswordInput(EditText passwordField) {

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


// ---- AUX FUNCTIONS

    private void init_UI() {

        //TODO: Comment this section

        fullName = (EditText) findViewById(R.id.reg_fullname);
        fullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //validateFullNameInput();
                }
            }
        });

        email = (EditText) findViewById(R.id.reg_email);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //validateEmailInput();
                }
            }
        });

        psw1 = (EditText) findViewById(R.id.reg_psw1);
        psw1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //validatePassword1Input();
                }
            }
        });

        psw2 = (EditText) findViewById(R.id.reg_psw2);
        psw2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //validatePassword2Input();
                }
            }
        });


        tyc = (CheckBox)findViewById(R.id.reg_tyc);

        tyc_link = (TextView)findViewById(R.id.reg_tyc_link);
        tyc_link.setText(Html.fromHtml("I have read and agree to the " +
                "<a href='www.google.com'>TERMS AND CONDITIONS</a>"));
        tyc_link.setClickable(true);
        tyc_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                startActivity(browserIntent);
            }
        });

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

    public void registerAction(View view) {

        //--- Input Validation Rutine

        //First check for a valid Email
        if (!validateEmailInput()) {
            //Return showing an error message
            return;
        }

        //After check if password1 is not empty
        else if (isEmptyPassword(psw1) && (isEmptyPassword(psw2))) {
            //Return showing a "Required" message
            return;
        }

         //After if it match the pattern and both are the same
        //TODO: Hacer una funcion para comparar las dos contraseñas, si no son iguales deberá mostrar un error en psw2
        else if (!validatePasswordInput(psw1) && !validatePasswordInput(psw2) && (psw1 == psw2)) {

            //Shows the "Wrong email or password" message
            Toast.makeText(Register.this, "Formato de contraseña Incorrecto", Toast.LENGTH_LONG).show();

            return;
        }

        else {

            //If all inputs were valid
            createAccount(email.getText().toString(), psw1.getText().toString());
        }

        //TODO: Generate Account, Update Profile
        //TODO: Dialogs, Loading Status, etc...

        //Shows Register Activity
        createAccount(email.getText().toString(), psw1.getText().toString());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Since Login is not in the history, we use this
        startActivity(new Intent(Register.this, Login.class));
    }

}
