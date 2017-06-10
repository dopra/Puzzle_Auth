package com.dopra.authentication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    //TODO: El correo para verificar la cuenta no se manda a la primera, hay que reenviarlo

    //Declare a TAG for Log Messages
    private static final String TAG = "RegisterActivity";

    //Declare visual elements
    private EditText reg_fullName;
    private EditText reg_email;
    private EditText reg_psw1;
    private EditText reg_psw2;
    private CheckBox reg_checkTyC;
    private TextView reg_tyc_link;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;

    //Declare and Initialize Flags of AuthListener
    private Boolean isLogged = false;
    private Boolean isNotified = false;

    public ProgressDialog progressDialog;


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

                //Is the user NOT logged?
                if (!isLogged) {

                    //Is user different than null?
                    if (user != null) {

                        //The account was verified?
                        if (user.isEmailVerified()) {

                            //GO TO MAIN MENU ACTIVITY!
                            startActivity(new Intent(Register.this, Login.class));
                            Toast.makeText(Register.this, "Already registered, please go back to Login screen", Toast.LENGTH_LONG).show();

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
            }
        };


    }

    private void createAccount(String email, String password) {

        Log.d(TAG, "Please, create an account for: " + email);

        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "User creatation successful: " + task.isSuccessful());

                if (!task.isSuccessful()) {

                    //Hide the dialog
                    hideProgressDialog();

                    //Shows a message to the user
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Already registered, please go back to Login screen", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    auth.signOut();
                }

                else {

                    //TODO: La actualización de perfil funcionó una sola vez. Hay que ver que pasa porque ni debuggeando lo pude ver...
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(reg_fullName.getText().toString())
                            .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                    }
                                }
                            });

                    auth.getCurrentUser().sendEmailVerification();

                    //Necessary?? I put this in order to avoid some errors
                    auth.signOut();

                    //Hide the dialog
                    hideProgressDialog();

                    //Return to Login Screen
                    onBackPressed();
                }


            }
        });
    }


//----- VALIDATION FUNCTIONS

    private boolean validateFullNameInput() {

        boolean valid = true;

        String email = reg_fullName.getText().toString();

        if (TextUtils.isEmpty(email)) {
            reg_fullName.setError("Requerido");
            valid = false;
        } else if (!validateTextInput(reg_fullName)) {

            reg_fullName.setError("Formato de Nombre Incorrecto");
            valid = false;

        }

        return valid;
    }

    private boolean validateEmailInput() {

        boolean valid = true;

        String email = reg_email.getText().toString();

        if (TextUtils.isEmpty(email)) {
            reg_email.setError("Requerido");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            reg_email.setError("Formato de mail incorrecto");
            valid = false;

        }

        return valid;
    }

    private boolean isEmptyPassword(EditText passwordField) {

        boolean isEmpty = false;

        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Requerido");
            isEmpty = true;
        }

        return isEmpty;
    }

    private boolean validateTextInput(EditText textField) {

        Boolean valid = true;

        String text = textField.getText().toString();

        String pttrn = "^(?=.*[ ])[\\p{L}\\s.’\\-,]+$";

        Pattern pattern = Pattern.compile(pttrn);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) {
            valid = false;
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
            passwordField.setError("No cumple el formato");
            valid = false;
        }

        return valid;
    }


// ---- AUX FUNCTIONS

    private void init_UI() {

        // ----- Set the full name field and the validation script
        reg_fullName = (EditText) findViewById(R.id.reg_fullname);
        reg_fullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateFullNameInput();
                }
            }
        });

        // ----- Set the email field and the validation script
        reg_email = (EditText) findViewById(R.id.reg_email);
        reg_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateEmailInput();
                }
            }
        });

        // ----- Set the first password field and the validation script
        reg_psw1 = (EditText) findViewById(R.id.reg_psw1);
        reg_psw1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    isEmptyPassword(reg_psw1);
                    validatePasswordInput(reg_psw1);
                }
            }
        });

        // ----- Set the password confirmation field and the validation script
        reg_psw2 = (EditText) findViewById(R.id.reg_psw2);
        reg_psw2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    isEmptyPassword(reg_psw1);
                    validatePasswordInput(reg_psw2);
                }
            }
        });


        // ----- Set the T&C field, its link and the check box validation script
        reg_checkTyC = (CheckBox)findViewById(R.id.reg_tyc);

        reg_tyc_link = (TextView)findViewById(R.id.reg_tyc_link);
        reg_tyc_link.setText(Html.fromHtml("I have read and agree to the " +
                "<a href='www.google.com'>Terms and Conditions</a>"));
        reg_tyc_link.setClickable(true);
        reg_tyc_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                startActivity(browserIntent);
            }
        });

    }

    public void showVerifyDialog(Boolean isRegistered) {

        // 1. Instantiate an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);

        // 2. Configure the Alert Dialog
        builder.setMessage("Already sent an email to your account please, check it in order to access");

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

        //Check for a valid Name
        if (!validateFullNameInput()) {
            //Return showing an error message
            return;
        }

        //Check for a valid Email
        if (!validateEmailInput()) {
            //Return showing an error message
            return;
        }

        //Check if passwords are not empty
        else if (isEmptyPassword(reg_psw1) && (isEmptyPassword(reg_psw2))) {
            //Return showing an error message
            return;
        }

         //Check both passwords match the pattern
        else if (!validatePasswordInput(reg_psw1) || !validatePasswordInput(reg_psw2)) {
            //Return showing an error message
            return;
        }


        //Check if password 1 and 2 match
        else if (!reg_psw1.getText().toString().equals(reg_psw2.getText().toString())) {

            //Password 2 does not match 1
            reg_psw2.setError("Doesn't match with the previous one");
            return;
        }

        //Check if TyC was acepted
        else if (!reg_checkTyC.isChecked()){

            //Show snackbar message if was not checked
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Must read and agree Terms and Conditions in order to create an account", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        else {

            //If all inputs were valid
            createAccount(reg_email.getText().toString(), reg_psw1.getText().toString());

            //TODO: Generate Account, Update Profile

        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
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
