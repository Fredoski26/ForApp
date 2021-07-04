package com.fredrick.forapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.UserAdapter;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.home.HomeActivity;
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

public class UserFragment extends Fragment {
FirebaseUser firebaseUser;
RecyclerView recyclerView;
UserAdapter userAdapter;
List<User> mUsers;
FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu Options in fragment
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        mAuth = FirebaseAuth.getInstance();
       recyclerView = view.findViewById(R.id.users_recycle);
       //set it properties.
       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//init user List

        mUsers = new ArrayList<>();
        getAllUsers();

        return view;
    }



    private void getAllUsers() {
        //get current users
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid())){
                        mUsers.add(user);

                    }
                    userAdapter = new UserAdapter(getActivity(), mUsers);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //hid AddPost icon from the fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.app_bar_search);
        final SearchView  searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    searchUsers(query);
                }else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!TextUtils.isEmpty(newText.trim())){
                    searchUsers(newText);
                }else {
                    getAllUsers();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchUsers(final String querys) {
        //getting users by search
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (!user.getId().equals(firebaseUser.getUid())) {

                        //More Items can be search e.g Messages
                        if (user.getUsername().toLowerCase().contains(querys.toLowerCase())){
                            mUsers.add(user);

                        }

                    }
                        userAdapter = new UserAdapter(getActivity(), mUsers);
                    //Refresh Adapte
                       userAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(userAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private  void  checkUserStatus(){
        FirebaseUser  firebaseUser = mAuth.getCurrentUser();
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
        else if (id == R.id.settings){
            //navigate to settings activity
             startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}