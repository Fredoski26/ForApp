package com.fredrick.forapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.ThereProfileActivity;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.MessagingActivity;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.repassword.RecoverpassActivity;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.myHolder> {
    Context mContext;
    List<User> mUsers;
    //for getting current users ID
    FirebaseAuth firebaseAuth;
    String myUid;

    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(user_row.XML)
        View view  = LayoutInflater.from(mContext).inflate(R.layout.users_row, parent, false);
        return new myHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull final myHolder holder, final int position) {
        final String hisUid = mUsers.get(position).getId();
        String userImage = mUsers.get(position).getImageURL();
        final String userName = mUsers.get(position).getUsername();

        //...........using vibrator on Adapter class...............
        final Vibrator my_vib = (Vibrator) holder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        //set Data
        holder.textView.setText(userName);
        try
        {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_person_color).into(holder.imageView);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_person_color).into(holder.imageView);
        }

        holder.blockiv.setImageResource(R.drawable.ic_unblock);
        //lets check if users is block or not
        checkIsBlock(hisUid,holder, position);

        //handle item onclick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(mContext, MessagingActivity.class);
//                    intent.putExtra("hisUids", hisUid);
//                    mContext.startActivity(intent);

                    //show Dialog
                AlertDialog.Builder builder  = new AlertDialog.Builder(mContext);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         if (which==0){
                             //profile click
                             Intent intent = new Intent(mContext, ThereProfileActivity.class);
                             intent.putExtra("uid", hisUid);
                             mContext.startActivity(intent);
                         }
                         if (which==1){
                             //chat click
                        imBlockedORNot(hisUid);

                         }
                    }
                });
                builder.create().show();
            }
        });

         //................................ click to block unblock users....................................
        holder.blockiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsers.get(position).isBlocked()){
                    unBlockUser(hisUid);
                }
                else {
                    blockUser(hisUid);
                }
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                my_vib.vibrate(3);
                if (mUsers.get(position).isBlocked()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Unblock user?");
                    // builder.setMessage("Are you sure to delete this message?");
                    //delete buttons
                    builder.setPositiveButton("UNBLOCK FOR ME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete logic
                            unBlockUser(hisUid);
                        }
                    });
                    //cancel delete button
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // create and show dialog
                    builder.create().show();
                }
                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Block user?");
                    // builder.setMessage("Are you sure to delete this message?");
                    //delete buttons
                    builder.setPositiveButton("BLOCK FOR ME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete logic

                            blockUser(hisUid);
                        }
                    });
                    //cancel delete button
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // create and show dialog
                    builder.create().show();

                }
                return false;
            }
        });


//..............................................MAIN CODES FOR BLOCKING AND UNBLOCKING users............................................
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mUsers.get(position).isBlocked()){
//                    unBlockUser(hisUid);
//                }
//                else {
//                    blockUser(hisUid);
//                }
//                return false;
//            }
//        });
    }

    private void imBlockedORNot(final String hisUid){
//this will check if sender (is current user) is blocked by receiver or not
        // the sense here is that  if uid  of the sender (current user ) exists in BlockerUsers of receiver then  sender (current user )is blocked, otherwise
        //if blocked then just display the message e.g you are blocked by this User, cannot send message
        //if not blocked then start the message activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      for (DataSnapshot ds: snapshot.getChildren()){
                          if (ds.exists()){
                              Toast.makeText(mContext, "You`re blocked by this user, can`t send message!", Toast.LENGTH_SHORT).show();
                              //blocked don`t proceed further
                              return;
                          }
                      }
                     //if not blocked start activity
                        Intent intent = new Intent(mContext, MessagingActivity.class);
                        intent.putExtra("hisUids", hisUid);
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkIsBlock(String hisUid, final myHolder holder, final int position) {
//         check each users if blocked or not
        //if uid of the user exists in BlockerUsers then that user is blocked , otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       for (DataSnapshot ds: snapshot.getChildren()){
                           if (ds.exists()){
                               holder.blockiv.setImageResource(R.drawable.ic_block);
                               mUsers.get(position).setBlocked(true);
                           }
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void blockUser(String hisUid) {
        //block user, by adding uid to current user`s blockerUser node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
     ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
             .addOnSuccessListener(new OnSuccessListener<Void>() {
                 @Override
                 public void onSuccess(Void aVoid) {
                  //block successfully
                     Toast.makeText(mContext, "Blocked", Toast.LENGTH_SHORT).show();
                 }
             }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
            // block failed
             Toast.makeText(mContext, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();

         }
     });

    }

    private void unBlockUser(String hisUid) {
       //unblock user by removing uid from the current user`s BlockUser node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if (ds.exists()){
                            //Remove blocked user Data from current user`s BlockUsers list
                          ds.getRef().removeValue()
                                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             //Unblock successfully
                                             Toast.makeText(mContext, "Unblocked", Toast.LENGTH_SHORT).show();
                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  // Unblocked failed
                                  Toast.makeText(mContext, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                              }
                          });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    static class myHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        MaterialTextView textView;
        ImageView blockiv;

        public  myHolder (@NonNull View itemView){
            super(itemView);

            imageView = itemView.findViewById(R.id.avatarTv);
            textView = itemView.findViewById(R.id.nameTvs);
            blockiv = itemView.findViewById(R.id.blockIv);

//            Toolbar toolbar = itemView.findViewById(R.id.toolbar);
//            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//                }
//            });


        }
    }
}
