package com.fredrick.forapp.ui.repassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fredrick.forapp.R;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.setting.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RecoverpassActivity extends AppCompatActivity {
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoverpass);
        // Toolbar declaration
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecoverpassActivity.this, LoginActivity.class));
                finish();
                checkUserStatus();
            }
        });
        progressBar = findViewById(R.id.progressbar);
        relativeLayout = findViewById(R.id.circle_p);
        progressBar.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        Button btn_back = findViewById(R.id.btn_back);
        final Button btn_reset = findViewById(R.id.btn_reset_password);
        final MaterialEditText reset_mail = findViewById(R.id.reset_mail);

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = reset_mail.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(RecoverpassActivity.this, "Enter your mail!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    reset_mail.setError("Invalid Email");
                    reset_mail.setFocusable(true);
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    // progressBar inside relativeLayout both work together
                                    relativeLayout.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.VISIBLE);
                                    Toast.makeText(RecoverpassActivity.this, "Check mail to reset your password!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RecoverpassActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                   checkUserStatus();
                                }
                                else {
                                    Toast.makeText(RecoverpassActivity.this, "Failed! recheck and try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                      @Override
                       public void onFailure(@NonNull Exception e) {
                      relativeLayout.setVisibility(View.GONE);
                      progressBar.setVisibility(View.GONE);
                     Toast.makeText(RecoverpassActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                  }
               });
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecoverpassActivity.this, LoginActivity.class));
                finish();
                checkUserStatus();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){

           mAuth.signOut();

        }else {
            startActivity(new Intent(RecoverpassActivity.this, LoginActivity.class));
            finishAffinity();
        }
        finish();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }
}
