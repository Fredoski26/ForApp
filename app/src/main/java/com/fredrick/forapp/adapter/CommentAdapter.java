package com.fredrick.forapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.R;
import com.fredrick.forapp.model.Comment;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {
    Context context;
    List<Comment> commentList;
    String myUid, postId;

    public CommentAdapter(Context context, List<Comment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        final String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        //convert  timestamp to dd/mm/yy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set data
        holder.nameTv_comments.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv_comment.setText(pTime);



        //set user dp
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_profile_post).into(holder.avatarTv_comment);

        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
          //comment click listeners
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if this comment is by currently signed in User
                if (myUid.equals(uid)){
                    // show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete comment?");
//                    builder.setMessage("Are you sure to delete this comment");
                    builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           deleteComments(cid);
                        }
                    });

                    //cancel delete button
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();;
                        }
                    });
                    // create and show dialog
                    builder.create().show();
                }else {
                    //not my comment
                    Toast.makeText(context, "Can`t delete other`s comment...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComments(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); //it will delete comment
        // now lets update the comment count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments =""+ snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) -1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        CircleImageView avatarTv_comment;
        MaterialTextView nameTv_comments, commentTv, timeTv_comment;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init Views
            avatarTv_comment = itemView.findViewById(R.id.avatarTv_comment);
            nameTv_comments = itemView.findViewById(R.id.nameTv_comment);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv_comment = itemView.findViewById(R.id.timeTv_comment);
        }
    }
}
