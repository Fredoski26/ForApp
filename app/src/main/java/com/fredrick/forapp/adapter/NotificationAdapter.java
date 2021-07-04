package com.fredrick.forapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.PostCommentActivity;
import com.fredrick.forapp.R;
import com.fredrick.forapp.model.NotificationGS;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.VIBRATOR_SERVICE;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.HolderNotification>{

    private Context context;
    private ArrayList<NotificationGS> notificationList;
    private FirebaseAuth firebaseAuth;

    public NotificationAdapter(Context context, ArrayList<NotificationGS> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
         firebaseAuth = FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate views  row_notifications.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);
        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotification holder, int position) {
        //get and set data to views

        //get data
        final NotificationGS  gs = notificationList.get(position);
        String name = gs.getsName();
        String notification = gs.getNotification();
        String image = gs.getsImage();
        final String timestamp = gs.getTimestamp();
        String sendUid = gs.getpUid();
        final String pId = gs.getpId();

//...........using vibrator on Adapter class...............
        final Vibrator my_vib = (Vibrator) holder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        final MediaPlayer mp = MediaPlayer.create(this.context,R.raw.ring_tone);

        //convert  timestamp to dd/mm/yy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        final String pTimes = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //we gonna get name, email and image of thr user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(sendUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String name = ""+ds.child("username").getValue();
                        String image = ""+ds.child("imageURL").getValue();
                        String email = ""+ds.child("email").getValue();

                        //add to model
                        gs.setsName(name);
                        gs.setsImage(image);
                        gs.setsEmail(email);

                        //set to views
                        holder.nametv.setText(name);

                        try {
                            Picasso.get().load(image).placeholder(R.drawable.ic_profile_post).into(holder.avatar);
                        } catch (Exception e) {
                            holder.avatar.setImageResource(R.drawable.ic_profile_post);
                        }
                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //set to Views
        holder.notificationtv.setText(notification);
        holder.timetv.setText(pTimes);

      //click the notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start postActivity
                Intent intent = new Intent(context,  PostCommentActivity.class);
                intent.putExtra("postId", pId);  // id will get all post details onclick
                context.startActivity(intent);

            }
        });

        //Long press to show delete option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                 my_vib.vibrate(3);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete notification?");
                // builder.setMessage("Are you sure to delete this message?");
                //delete buttons
                builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //delete logic
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                          ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue()
                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();
                                  mp.start();
                              }
                          }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                              }
                          });
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
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class HolderNotification extends RecyclerView.ViewHolder{
//declare views

        CircleImageView avatar;
        MaterialTextView nametv, notificationtv, timetv;
        public HolderNotification(@NonNull View itemView) {
            super(itemView);
            //init views


            avatar = itemView.findViewById(R.id.avatarTvs);
            nametv = itemView.findViewById(R.id.nameTvs);
            notificationtv = itemView.findViewById(R.id.notificationTv);
            timetv = itemView.findViewById(R.id.timeTvs);
        }
    }
}
