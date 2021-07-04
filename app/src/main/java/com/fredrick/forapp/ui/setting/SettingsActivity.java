package com.fredrick.forapp.ui.setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.fredrick.forapp.R;
import com.fredrick.forapp.ui.repassword.RecoverpassActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
SwitchMaterial postSwitch;
CardView reset_btn;

//use SharedPreferences to save the state of switch
SharedPreferences sp;
SharedPreferences.Editor editor; // to edit value of share pref

    // CONSTANT FOR TOPIC
    private  static  final String TOPIC_POST_NOTIFICATION = "POST"; //assign any value but use same for this  kind notifications
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));

        postSwitch =  findViewById(R.id.postSwitch);
        reset_btn = findViewById(R.id.reset_btn);

        //init sp
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnable = sp.getBoolean(""+TOPIC_POST_NOTIFICATION, false);
        //if enable check  switch, otherwise uncheck switch -- by default/unchecked is false
                   if (isPostEnable){
                       postSwitch.setChecked(true);
                   }else {
                       postSwitch.setChecked(false);
                   }

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //  edit switch state
                editor =  sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                if (isChecked){
                   subscribePostNotifications(); //call to subscribe
                }else {
                    unsubscribePostNotifications(); // call to unsubscribe
                }
            }
        });


//                   reset_btn.setOnClickListener(new View.OnClickListener() {
//                       @Override
//                       public void onClick(View v) {
//                           // inflate (create) another copy of our custom layout
//
//                       }
//                   });



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribePostNotifications() {
        // unsubscribe to Topic post to disable its notification
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notifications";
                        if (!task.isSuccessful()){
                            msg = "Failed to unsubscribe";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void subscribePostNotifications() {
        // unsubscribe to Topic post to enable its notification
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "You will receive post notifications";
                            if (!task.isSuccessful()){
                                msg = "Subscription failed";
                            }
                            Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();

                        }
                    });
    }
}