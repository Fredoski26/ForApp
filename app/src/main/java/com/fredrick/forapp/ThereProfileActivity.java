package com.fredrick.forapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActionBar;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.fredrick.forapp.adapter.PostAdapter;
import com.fredrick.forapp.model.Post;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postRecyclerView;
    List<Post> postList;
    PostAdapter postAdapter;
    String uid;

    MaterialTextView name, email, phone;
    CircleImageView profile_pic;
    ImageView coverTv;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;
    Uri image_Uri;
    String profileOrCoverPhoto;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));

        postRecyclerView = findViewById(R.id.recycleView_posts);
        name = findViewById(R.id.nameTv);
        email = findViewById(R.id.emailTv);
        profile_pic = findViewById(R.id.profile_picture);
        phone = findViewById(R.id.phoneTv);
//        coverTv = findViewById(R.id.coverTv_image);

        mAuth = FirebaseAuth.getInstance();

        firebaseUser = mAuth.getCurrentUser();
       // firebaseDatabase = FirebaseDatabase.getInstance();
       // reference = firebaseDatabase.getReference("Users");

       // storageReference = FirebaseStorage.getInstance().getReference(); //FirebaseStorage reference

        //get uid of click user to retrieve all post
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    name.setText(user.getUsername());
                    email.setText(user.getEmail());
                    phone.setText(user.getNumber());

                    try {
                        //if image is Default set
                        if (user.getImageURL().equals("default")) {
                            profile_pic.setImageResource(R.drawable.ic_light_camera);
                            // Picasso.get().load(picture).into(profile);
                        }else {
                            //else set received image
                            // Picasso.get().load(R.drawable.ic_light_camera).into(profile);
                            Picasso.get().load(user.getImageURL()).into(profile_pic);
                        }
                    } catch (Exception e) {
                        // if any Error while getting image then set default
                        Picasso.get().load(R.drawable.ic_light_camera).into(profile_pic);
                        Toast.makeText(ThereProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                   // String picture = ""+ ds.child("imageURL").getValue();
                   // String cover = ""+ ds.child("cover_image").getValue();
//                    try {
//                        //if image received then set it
//                        Picasso.get().load(picture).into(profile_pic);
//                    } catch (Exception e) {
//                        // if any Error while getting image then set default
//                        Picasso.get().load(R.drawable.ic_light_camera).into(profile_pic);
//                    }
//                    try {
//                        //if image received then set it
//                        Picasso.get().load(cover).into(coverTv);
//                    } catch (Exception e) {
//                        // if any Error while getting image then set default
//                        // Picasso.get().load(R.drawable.ic_camera).into(coverTv);
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadHisPosts();
    }

    private void loadHisPosts() {

        //linearLayout for recycleView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //show newwest post, from first to last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycleView
        postRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Post myPost = ds.getValue(Post.class);

                    //add to list
                    postList.add(myPost);

                    //set adapter
                    postAdapter = new PostAdapter(ThereProfileActivity.this, postList);
                    //set adapter to recyclerview
                    postRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private  void searchHisPosts(final String searchQuerts){

        //linearLayout for recycleView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);

        //show newest post, from first to last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycleView
        postRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Post myPost = ds.getValue(Post.class);

                    assert myPost != null;
                    if (myPost.getpTitle().toLowerCase() .contains(searchQuerts.toLowerCase()) ||
                            myPost.getpDescr().toLowerCase() .contains(searchQuerts.toLowerCase())){

                        postList.add(myPost);
                    }
                    //  postList.add(post);

                    //set adapter
                    postAdapter = new PostAdapter(ThereProfileActivity.this, postList);
                    //set adapter to recyclerview
                    postRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private  void  checkUserStatus() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
           // uid = firebaseUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false); // hide from this Activity

        MenuItem item =  menu.findItem(R.id.app_bar_search);
        //search specific post
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//               its called when user press seaarch button
                if (!TextUtils.isEmpty(query))
                {

                    searchHisPosts(query);
                }else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // called as and when user press  any letter
                if (!TextUtils.isEmpty(newText))
                {
                    searchHisPosts(newText);
                }else {
                    loadHisPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            mAuth.signOut();
            checkUserStatus();

        }

        return super.onOptionsItemSelected(item);
    }
}