package com.fredrick.forapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.format.DateFormat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.model.Chat;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.myHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context mContext;
    List<Chat> chatList;
    String imageUrls;
    FirebaseUser firebaseUser;

    public ChatAdapter(Context mContext, List<Chat> chatList, String imageUrls) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==MSG_TYPE_RIGHT){
            View  view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_right, parent, false);
            return new myHolder(view);
        }else {
            View  view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_left, parent, false);
            return new myHolder(view);
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull myHolder holder, final int position) {
 //    now lets Get some data;
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();

        //...........using vibrator on Adapter class...............
        final Vibrator my_vib = (Vibrator) holder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        final MediaPlayer mp = MediaPlayer.create(this.mContext,R.raw.ring_tone);

        // converting time stamp  to dd/mm/yyy hh:mm pm/am
        Calendar  calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyy, hh:mm aa", calendar).toString();

        if (type.equals("text")){
            // text message
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);

            holder.messageTv.setText(message);

        }else {
            // image message
            holder.messageTv.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
             Picasso.get().load(message).placeholder(R.drawable.ic_image_send).into(holder.messageImage);
        }

        // set Data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUrls).into(holder.profile);

        }catch (Exception e){

        }

//.............................click to show delete dialog......................................THE INITIAL ONCLICK
//        holder.linearLAyout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //show delete dialog
//                AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
//                builder.setTitle("Delete message?");
//               // builder.setMessage("Are you sure to delete this message?");
//                //delete buttons
//                builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                       deleteMessage(position);
//                    }
//                });
//                //cancel delete button
//                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                // create and show dialog
//                builder.create().show();
//            }
//        });

        holder.linearLAyout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                my_vib.vibrate(3);
                //show delete dialog
                AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
                builder.setTitle("Delete message?");
                // builder.setMessage("Are you sure to delete this message?");
                //delete buttons
                builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                        mp.start();
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


        // set SEEN/DELIVER status of message
        if (position==chatList.size()-1){
            if (chatList.get(position).isMessageSeen()){
                holder.isSeenTv.setText("Seen");

            }else {
                holder.isSeenTv.setText("Delivered");
            }

        }else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position2) {
        final String myUID = FirebaseAuth .getInstance().getCurrentUser().getUid();



        String msgTimeStamp = chatList.get(position2).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {

                    if (ds.child("sender").getValue().equals(myUID)) {
                       // ds.getRef().removeValue();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(mContext, "message deleted...", Toast.LENGTH_SHORT).show();

                    }else {

                        Toast.makeText(mContext, "Your messages delete only!", Toast.LENGTH_SHORT).show();
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
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;

        }else {
           return MSG_TYPE_LEFT;
        }

    }

    static class myHolder extends RecyclerView.ViewHolder{


        //Views
          CircleImageView profile;
          ImageView messageImage;
          MaterialTextView messageTv, timeTv, isSeenTv;
          LinearLayout linearLAyout; //onclick to show deleted message

        public myHolder(@NonNull View itemView) {
            super(itemView);

            //init Views
            profile = itemView.findViewById(R.id.row_cha_left_profile);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageTv = itemView.findViewById(R.id.row_chat_left_message);
            timeTv = itemView.findViewById(R.id.row_chat_left_timeTV);
            isSeenTv = itemView.findViewById(R.id.row_chat_left_deliver);
            linearLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
