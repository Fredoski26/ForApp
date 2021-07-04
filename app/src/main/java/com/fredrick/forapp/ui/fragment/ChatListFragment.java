package com.fredrick.forapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.SearchView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.ChatlistAdapter;
import com.fredrick.forapp.model.Chat;
import com.fredrick.forapp.model.Chatlist;
import com.fredrick.forapp.model.User;
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

public class ChatListFragment extends Fragment {
    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    List<Chatlist> chatlistList;
    List<User> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    ChatlistAdapter chatlistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu Options in fragment
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycleView_chatlist);
        chatlistList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chatlist chatlist = ds.getValue(Chatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
      return view;
    }

    private void    loadChats() {
        userList = new ArrayList<>();
        reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    for (Chatlist chatlist: chatlistList){
                        assert user != null;
                        if (user.getId() != null &&  user.getId().equals(chatlist.getIds())){
                            userList.add(user);
                            break;
                        }
                    }
                    //adapter
                    chatlistAdapter =  new ChatlistAdapter(getContext(), userList);
                    // set adapter
                    recyclerView.setAdapter(chatlistAdapter);
                    //set last message
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userIds) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userIds) || chat.getReceiver().equals(userIds ) &&
                            chat.getSender().equals(currentUser.getUid())){
                        //instead of display uri in message show sent photo"
                        if (chat.getType().equals("imageURL"))
                        {
                            theLastMessage = "sent a photo";
                        }else {
                            theLastMessage = chat.getMessage();
                        }

                    }
                }
                chatlistAdapter.setLastMessageMap(userIds, theLastMessage);
                chatlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);

        //hid AddPost icon from the fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
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

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
              firebaseUser.getUid();
        }else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}