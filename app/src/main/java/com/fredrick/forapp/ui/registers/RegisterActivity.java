package com.fredrick.forapp.ui.registers;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fredrick.forapp.R;
import com.fredrick.forapp.ui.home.HomeActivity;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    //SignInButton mGoogle;
    Button btn_login, btn_register, mGoogle;
    DatePickerDialog.OnDateSetListener setListener;
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    private MaterialEditText name, email, password, phone, address;
    private MaterialTextView birth;
    private MaterialBetterSpinner gender;
    private FirebaseAuth mAuth;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogle = findViewById(R.id.googleLoginBtn);
        mAuth = FirebaseAuth.getInstance();
        btn_register = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressbar);
        relativeLayout = findViewById(R.id.circle_p);
        relativeLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        birth = findViewById(R.id.birth);
        gender = findViewById(R.id.gender);
        btn_login = findViewById(R.id.btn_already);

        //selection of Date from DatePickerDialog
       // final MaterialEditText editText = findViewById(R.id.birth);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog =  new DatePickerDialog(
                        RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date = dayOfMonth+"/"+month+"/"+year;
                        birth.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        //collecting genders Arrays into gender editText
       // MaterialBetterSpinner spinner =  findViewById(R.id.gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this, R.array.genders, android.R.layout.simple_spinner_dropdown_item);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      gender.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Logging in with Google...");
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_name = name.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_phone = phone.getText().toString();
                String txt_address = address.getText().toString();
                String txt_birth = birth.getText().toString();
                String txt_gender = gender.getText().toString();

                if (TextUtils.isEmpty(txt_name) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_phone) || TextUtils.isEmpty(txt_address) || TextUtils.isEmpty(txt_birth) || TextUtils.isEmpty(txt_gender)) {
                    Toast.makeText(RegisterActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(txt_email).matches()) {
                    email.setError("Invalid Email");
                    email.setFocusable(true);
                } else if (txt_password.length() < 6) {
                    password.setError("password must be at least 6 characters");
                    password.setFocusable(true);
                    //Toast.makeText(RegisterActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(txt_name, txt_email, txt_password, txt_phone, txt_address, txt_birth, txt_gender);
                }
            }
        });
    }

    private void registerUser(final String name, String email, String password, final String number, final String address, final String birth, final String gender) {
        relativeLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            relativeLayout.setVisibility(View.GONE);
//                            progressBar.setVisibility(View.GONE);
                            final FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String email = firebaseUser.getEmail();
                            String userid = firebaseUser.getUid();
                            //  final String txt_phone = phone.getText().toString();
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("email", email);
                            hashMap.put("username", name);
                            hashMap.put("number", number);
                            hashMap.put("address", address);
                            hashMap.put("birth", birth);
                            hashMap.put("gender", gender);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "ontyping");
                            hashMap.put("cover_image", "");
                            hashMap.put("imageURL", "default");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            reference = database.getReference("Users");
                            reference.child(userid).setValue(hashMap);

                            //Toast.makeText(RegisterActivity.this, "Registered...\n" + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            relativeLayout.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error. Dismiss the progressbar and show error message
                relativeLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                String name = firebaseUser.getDisplayName();
                                String phone = firebaseUser.getPhoneNumber();
                                //  final String txt_phone = phone.getText().toString();
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("email", email);
                                hashMap.put("username", name);
                                hashMap.put("number", phone);
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
                           // Toast.makeText(RegisterActivity.this, "" + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void checkUserStatus() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
////          User signed in stays here
//            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
//            finish();
//        } else {
//            mAuth.signOut();
//            finish();
//        }
//    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAffinity();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
       // checkUserStatus();
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}