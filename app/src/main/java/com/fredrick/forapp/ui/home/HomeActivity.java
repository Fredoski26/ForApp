package com.fredrick.forapp.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.model.Chat;
import com.fredrick.forapp.notifications.Token;
import com.fredrick.forapp.ui.fragment.ChatListFragment;
import com.fredrick.forapp.ui.fragment.HomeFragment;
import com.fredrick.forapp.ui.fragment.NotificationsFragment;
import com.fredrick.forapp.ui.fragment.ProfileFragment;
import com.fredrick.forapp.ui.fragment.UserFragment;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.registers.RegisterActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
private FirebaseAuth mAuth;
DatabaseReference reference;
FirebaseDatabase firebaseDatabase;
FirebaseUser user;
MaterialTextView onlineTv;
BottomNavigationView bottomNavigationView;
TextView  unseen;
private Vibrator myVib;
String mUID;
    String hisUids;
    String myUids;
    String hisImage;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finishAffinity();
//                System.exit(0);
//            }
//        });
       onlineTv = findViewById(R.id.show_online);
        unseen = findViewById(R.id.unseen_count);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        getSupportActionBar().setTitle("Home");
        HomeFragment homeFragment =  new HomeFragment();
        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, homeFragment, "");
        transaction.commit();


        checkUserStatus();

        final FirebaseUser  firebaseUser = mAuth.getCurrentUser();
       reference = FirebaseDatabase.getInstance().getReference("Users");
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;
                int fredColor = Color.rgb(255,0,0);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Chat chat = snapshot1.getValue(Chat.class);
                    assert chat != null;
                    assert firebaseUser != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isMessageSeen()){
                        unread++;
                    }
                }

                String  count = "("+unread+")";

                if (unread == 0 ){
                   unseen.setVisibility(View.GONE);
                }else {
                    unseen.setVisibility(View.VISIBLE);
                    unseen.setText(count);
                    unseen.setTextColor(fredColor);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    mAuth = FirebaseAuth.getInstance();
    Intent intent = getIntent();
    hisUids = intent.getStringExtra("hisUids");
    firebaseDatabase = FirebaseDatabase.getInstance();
    reference = firebaseDatabase.getReference("Users");

    //search user to get that users info
    Query userQuery = reference.orderByChild("id").equalTo(hisUids);

    // get Ueser picture and name
    userQuery.addValueEventListener(new ValueEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            // check until required information is received
            for (DataSnapshot ds: snapshot.getChildren()){

                String typingStatus = ""+ ds.child("typingTo").getValue();


                if (typingStatus.equals(myUids)){
                    onlineTv.setText("typing...");
                }else {

                    //get value of onlinestatus
                    String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")){
                        onlineTv.setText(onlineStatus);

                    }else {
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();
                        onlineTv.setText("Last seen at: "+dateTime);
                    }


                }



            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    }



    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
       ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
              new BottomNavigationView.OnNavigationItemSelectedListener() {
                  @Override
                  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                      switch (item.getItemId()){
                          case R.id.nav_home:
                              myVib.vibrate(3);
                             getSupportActionBar().setTitle("Home");
                              HomeFragment homeFragment =  new HomeFragment();
                              FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
                              transaction.replace(R.id.content, homeFragment, "");
                              transaction.commit();
                              return true;

                          case R.id.nav_profile:
                              myVib.vibrate(3);
                              getSupportActionBar().setTitle("Profile");
                              ProfileFragment profileFragment =  new ProfileFragment();
                              FragmentTransaction transaction2  = getSupportFragmentManager().beginTransaction();
                              transaction2.replace(R.id.content, profileFragment, "");
                              transaction2.commit();
                              return true;

                          case R.id.nav_user:
                              myVib.vibrate(3);
                              getSupportActionBar().setTitle("Users");
                              UserFragment userFragment =  new UserFragment();
                              FragmentTransaction transaction3  = getSupportFragmentManager().beginTransaction();
                              transaction3.replace(R.id.content, userFragment, "");
                              transaction3.commit();
                              return true;

                          case R.id.nav_chat:
                              myVib.vibrate(3);
                              getSupportActionBar().setTitle("Chats");
                              ChatListFragment chatListFragment = new ChatListFragment();
                              FragmentTransaction transaction4  = getSupportFragmentManager().beginTransaction();
                              transaction4.replace(R.id.content, chatListFragment, "");
                              transaction4.commit();
                              return true;

                          case R.id.nav_notification:
                              myVib.vibrate(3);
                              getSupportActionBar().setTitle("Notifications");
                              NotificationsFragment notificationsFragment  = new NotificationsFragment();
                              FragmentTransaction transaction5  = getSupportFragmentManager().beginTransaction();
                              transaction5.replace(R.id.content, notificationsFragment, "");
                              transaction5.commit();
                              return true;
                      }
                      return false;
                  }
              };

private  void  checkUserStatus(){
    FirebaseUser  firebaseUser = mAuth.getCurrentUser();
    if (firebaseUser != null){

        mUID = firebaseUser.getUid();
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Current_USERID", mUID);
        editor.apply();

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }else {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
    }
}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onBackPressed() {
        finishAffinity();
//        System.exit(0);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
      checkUserStatus();
        super.onStart();
    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) {
//            mAuth.signOut();
//            checkUserStatus();
//
//        }
//        else if (id == R.id.change_email){
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
