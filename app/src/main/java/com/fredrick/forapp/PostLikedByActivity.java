package com.fredrick.forapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.fredrick.forapp.adapter.UserAdapter;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.home.HomeActivity;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {
    String postId;
   private RecyclerView recyclerView;
   private List<User> userList;
   private UserAdapter userAdapter;
   private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post Liked By");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));

         firebaseAuth = FirebaseAuth.getInstance();
//         getSupportActionBar().setTitle(firebaseAuth.getCurrentUser().getEmail());
        //get the post id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        recyclerView =  findViewById(R.id.recyleview_likes);

           userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
                ref.child(postId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       userList.clear();
                       for (DataSnapshot ds: snapshot.getChildren()){
                           String hiUid = ""+ds.getRef().getKey();

                           //get users from each id
                           getUsers(hiUid);
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getUsers(String hiUid) {
        //get info of each user, using id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                 ref.orderByChild("id").equalTo(hiUid)
                         .addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                              for (DataSnapshot ds:snapshot.getChildren()){
                                  User user = ds.getValue(User.class);
                                  userList.add(user);
                              }

                              //setup adapter
                                 userAdapter = new UserAdapter(PostLikedByActivity.this, userList);
                              //set adapter  to recycleview
                                 recyclerView.setAdapter(userAdapter);
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {

                             }
                         });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}