package com.fredrick.forapp.ui.login;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fredrick.forapp.R;
import com.fredrick.forapp.ui.home.HomeActivity;
import com.fredrick.forapp.ui.registers.RegisterActivity;
import com.fredrick.forapp.ui.repassword.RecoverpassActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
private static final int RC_SIGN_IN = 100;
GoogleSignInClient mGoogleSignInClient;
    //SignInButton mGoogle;

Button btn_login, recoverpass, register, mGoogle ;
ProgressBar progressBar;
RelativeLayout relativeLayout;
private MaterialEditText  email, password;
private FirebaseAuth mAuth;
ProgressDialog dialog;
DatabaseReference reference;
FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogle = findViewById(R.id.googleLoginBtn);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Logging in with Google...");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                 finish();

            }
        });

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        relativeLayout = findViewById(R.id.circle_p);
        relativeLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btn_login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_sign_up);
        recoverpass = findViewById(R.id.btn_forgot_password);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);


        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(LoginActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(txt_email).matches()){
                    email.setError("Invalid Email");
                    email.setFocusable(true);
                }else if (txt_password.length() < 6 ) {
                    password.setError("password must be at least 6 characters");
                    password.setFocusable(true);
                }else {
                    loginUser(txt_email, txt_password );
                }
            }
        });
        recoverpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RecoverpassActivity.class));
               // finish();
               // showRecoverPassDialog();

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
                // showRecoverPassDialog();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
//        relativeLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.VISIBLE);
        dialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser()) {
                                assert firebaseUser != null;
                                String email = firebaseUser.getEmail();
                                String userid = firebaseUser.getUid();
                                //  final String txt_phone = phone.getText().toString();
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("email", email);
                                hashMap.put("username", "");
                                hashMap.put("number", "");
                                hashMap.put("address", "");
                                hashMap.put("birth", "");
                                hashMap.put("gender", "");
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "ontyping");
                                hashMap.put("cover_image", "");
                                hashMap.put("imageURL", "default");
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(userid).setValue(hashMap);
                            }
                            assert firebaseUser != null;
                             Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                relativeLayout.setVisibility(View.GONE);
//                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void showRecoverPassDialog() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Recovery password");
//        builder.setMessage(R.string.emailbox);
//        LinearLayout linearLayout = new LinearLayout(this);
//
//        final MaterialEditText emailET = new MaterialEditText(this);
//        emailET.setHint("Email");
//
//        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//        emailET.setMinEms(16);
//
//        linearLayout.addView(emailET);
//        linearLayout.setPadding(10, 10,10,10);
//        builder.setView(linearLayout);
//        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//              String email = emailET.getText().toString().trim();
//                if (TextUtils.isEmpty(email)) {
//                    Toast.makeText(LoginActivity.this, "Email required", Toast.LENGTH_SHORT).show();
//                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                    emailET.setError("Invalid Email");
//                    emailET.setFocusable(true);
//                }else if (!showRecoverPassDialog().){
//
//                }
//
//
//
//                beginRecovery(email);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                relativeLayout.setVisibility(View.GONE);
//                progressBar.setVisibility(View.GONE);
//            }
//        });
//        builder.create().show();
//    }
//
//    private void beginRecovery(String email) {
//        relativeLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.VISIBLE);
//        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                   if (task.isSuccessful()){
//                       Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
//                   }else {
//                       Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                   }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                relativeLayout.setVisibility(View.GONE);
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void loginUser(String txt_email, String txt_password) {
        relativeLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error. Dismiss the progressbar and show error message
                relativeLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){

            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();

        }else {

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        checkUserStatus();
        finishAffinity();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}
