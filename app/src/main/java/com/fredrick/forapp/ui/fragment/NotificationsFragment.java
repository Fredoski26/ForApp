package com.fredrick.forapp.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.NotificationAdapter;
import com.fredrick.forapp.model.NotificationGS;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {

RecyclerView notifications;
private FirebaseAuth firebaseAuth;
private ArrayList<NotificationGS> notificationList;
private NotificationAdapter notificationAdapter;
    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        //init
        notifications= view.findViewById(R.id.recycleView_notifications);
        firebaseAuth = FirebaseAuth.getInstance();
        getAllNotification();
        return view;
    }

    private void getAllNotification() {
        notificationList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
           ref.child(firebaseAuth.getUid()).child("Notifications")
                   .addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notificationList.clear();
                            for (DataSnapshot ds: snapshot.getChildren()){
                                //get data
                                NotificationGS gs = ds.getValue(NotificationGS.class);

                                //added to list
                                notificationList.add(gs);
                            }
                            //adapter
                           notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
                            //set recyleview
                           notifications.setAdapter(notificationAdapter);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });

    }
}