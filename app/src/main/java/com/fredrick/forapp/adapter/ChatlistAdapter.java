package com.fredrick.forapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.model.Chatlist;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.MessagingActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder>{
    //for getting current users ID
    FirebaseAuth firebaseAuth;
    String myUid;
    Context context;
    List<User> userList; //get user info
    private HashMap<String, String> lastMessageMap;

    public ChatlistAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        // get data
        final String hisUid = userList.get(position).getId();
        String userImage = userList.get(position).getImageURL();
        String userName = userList.get(position).getUsername();
        String lastMessage = lastMessageMap.get(hisUid);

        //set Data
        holder.nameTvs.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            holder.last_messageTv.setVisibility(View.GONE);
        }else {
            holder.last_messageTv.setVisibility(View.VISIBLE);
            holder.last_messageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_person_color).into(holder.profileTvs);

        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_person_color).into(holder.profileTvs);
        }
        // setting online status of other user in chatlist
        if (userList.get(position).getOnlineStatus().equals("online")){
            // online
            //Picasso.get().load(R.drawable.circle_online).into(holder.onlineStatus);
            holder.onlineStatus.setImageResource(R.drawable.circle_online);
        }
        else {
            // offline
           // holder.onlineStatus.setImageResource(R.drawable.circle_offline);
        }

        holder.block.setImageResource(R.drawable.ic_unblock);
        //lets check if users is block or not
        //checkIsBlocks(hisUid,holder, position);


        // Handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start chat Activity with that user
                Intent intent =  new Intent(context, MessagingActivity.class);
                intent.putExtra("hisUids", hisUid);
                context.startActivity(intent);
               // imBlockedORNot(hisUid);
            }
        });


    }

//    private void checkIsBlocks(String hisUid, final MyHolder holder, final int position) {
//
//
//       // check each users if blocked or not
//        //if uid of the user exists in BlockerUsers then that user is blocked , otherwise not
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            if (ds.exists()){
//                                holder.block.setImageResource(R.drawable.ic_block);
//                                userList.get(position).setBlocked(true);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void imBlockedORNot(final String hisUid){
//            //this will check if sender (is current user) is blocked by receiver or not
//        // the sense here is that  if uid  of the sender (current user ) exists in BlockerUsers of receiver then  sender (current user )is blocked, otherwise
//        //if blocked then just display the message e.g you are blocked by this User, cannot send message
//        //if not blocked then start the message activity
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            if (ds.exists()){
//                                Toast.makeText(context, "You`re blocked by this user, can`t send message!", Toast.LENGTH_SHORT).show();
//                                //blocked don`t proceed further
//                                return;
//                            }
//                        }
//                        //if not blocked start activity
//                        Intent intent =  new Intent(context, MessagingActivity.class);
//                        intent.putExtra("hisUids", hisUid);
//                         context.startActivity(intent);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//    }





    public void setLastMessageMap(String userIds , String lastMessage){
        lastMessageMap.put(userIds, lastMessage);
    }
    @Override
    public int getItemCount() {
        return userList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        //View of row_chatlist.xml
        MaterialTextView nameTvs, last_messageTv;
        CircleImageView profileTvs;
         ImageView onlineStatus, block;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            nameTvs = itemView.findViewById(R.id.nameTvs);
            last_messageTv = itemView.findViewById(R.id.last_messageTvs);
            profileTvs = itemView.findViewById(R.id.profileTvs);
            onlineStatus = itemView.findViewById(R.id.onlineStatusTvs);
            block = itemView.findViewById(R.id.blockIv);
        }
    }
}
