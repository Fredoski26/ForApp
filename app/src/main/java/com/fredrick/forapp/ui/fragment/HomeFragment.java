package com.fredrick.forapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import androidx.appcompat.widget.SearchView;
import android.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fredrick.forapp.AddPostActivity;
import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.PostAdapter;
import com.fredrick.forapp.model.Post;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.setting.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth mAuth;

    RecyclerView recyclerView;
    List<Post> postList;
    PostAdapter postAdapter;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//        }
//    }

    public HomeFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();

        //Recycler View and it properties
        recyclerView = view.findViewById(R.id.posts_RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                //show newest post  first , for this load to last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();
        loadPost();
        return view;
    }

    private void loadPost() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Post post = ds.getValue(Post.class);

                        postList.add(post);

                        //Adapter
                        postAdapter = new PostAdapter(getActivity(), postList);
                        //set adapter to recyclerview
                        recyclerView.setAdapter(postAdapter);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 //Let handle errors here
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void  searchPost(final String searchQuerys){

        //path of all posts FOR SEARCHING
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Post post = ds.getValue(Post.class);

                    assert post != null;
                    if (post.getpTitle().toLowerCase() .contains(searchQuerys.toLowerCase()) ||
                            post.getpDescr().toLowerCase() .contains(searchQuerys.toLowerCase())){

                        postList.add(post);
                    }
                  //  postList.add(post);

                    //Adapter
                    postAdapter = new PostAdapter(getActivity(), postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Let handle errors here
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Please Recheck this method
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        //searchView to search post by title and Description
        MenuItem item =  menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//               its called when user press seaarch button
                if (!TextUtils.isEmpty(query))
                {
                    searchPost(query);
                }else {
                    loadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
        // called as and when user press  any letter
                if (!TextUtils.isEmpty(newText))
                {
                    searchPost(newText);
                }else {
                    loadPost();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
               firebaseUser.getUid();
        }else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            mAuth.signOut();
            checkUserStatus();

        }
         else if (id == R.id.action_add_post){
             startActivity(new Intent(getActivity(), AddPostActivity.class));
             // checkUserStatus();
        }
        else if (id == R.id.settings){
            //navigate to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}