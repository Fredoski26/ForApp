package com.fredrick.forapp.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.fredrick.forapp.AddPostActivity;
import com.fredrick.forapp.PostCommentActivity;
import com.fredrick.forapp.PostLikedByActivity;
import com.fredrick.forapp.R;
import com.fredrick.forapp.ThereProfileActivity;
import com.fredrick.forapp.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends  RecyclerView.Adapter<PostAdapter.MyHolder> {

    Context context;
    List<Post> postList;
    String myUid;
    private DatabaseReference likesRef; //for likes database nodes
    private DatabaseReference postRef; //reference of post
    boolean mProcessLike = false;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef =  FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
                  //get Data
        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescr = postList.get(position).getpDescr();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); //Total numbers of likes for a post
        String pComments = postList.get(position).getpComments();

        //convert  timestamp to dd/mm/yy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
         calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
         final String pTimes = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


         //set data
        holder.nameTv.setText(uName);
        holder.timeTv.setText(pTimes);
        holder.titleTv.setText(pTitle);
        holder.descriptionTv.setText(pDescr);
        holder.likeTv.setText(pLikes +" Likes");
        holder.commentTv.setText(pComments +" comments");
        //setting like to each post
        setLikes(holder, pId);


         //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_profile_post).into(holder.pitureTv);

        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        if (pImage.equals("noImage")){
            //hide imageView
            holder.imageTv.setVisibility(View.GONE);

        }else {
            //show imageView
            holder.imageTv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.imageTv);

            }catch (Exception e){
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }
              //set post image


        //handle button onclick
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        //handle button onclick
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get total number of like when current user click
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //get id of the post clicked
                final String  postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      if (mProcessLike){
                          if (snapshot.child(postIde).hasChild(myUid)){
                              //for already , to remove like
                              postRef.child(postIde).child("pLikes").setValue(""+ (pLikes -1));
                              likesRef.child(postIde).child(myUid).removeValue();
                              mProcessLike = false;
                          }else
                          {
                           //if not like , like it
                           postRef.child(postIde).child("pLikes").setValue(""+ (pLikes +1));
                           likesRef.child(postIde).child(myUid).setValue("Liked"); //set value
                              mProcessLike = false;
                              addToHisNotifications(""+uid, ""+pId, "Liked your post");
                          }
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //handle button onclick
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,  PostCommentActivity.class);
                intent.putExtra("postId", pId);  // id will get all post details
                context.startActivity(intent);
            }
        });

        //handle button onclick
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.imageTv.getDrawable();
                if (bitmapDrawable == null){
                    // post without image
                     shareTextOnly(pTitle, pDescr);
                }else {
                    // post with image
                    Bitmap bitmap = bitmapDrawable.getBitmap(); // convert image to bitmap
                    shareImageAndText(pTitle, pDescr, bitmap);

                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        //On Like click to start PostLikedByActivity
//        holder.likeTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, PostLikedByActivity.class);
//                intent.putExtra("postId", pId);
//                context.startActivity(intent);
//            }
//        });

    }

    private void addToHisNotifications(String hisUid, String pId, String notification){
        HashMap<Object, String> hashMap = new HashMap<>();
        String timestamp = ""+System.currentTimeMillis();

        hashMap.put("pId",pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", myUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", hisUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
             ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                           //added complete

                         }
                     }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {

                 }
             });
    }



    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody = pTitle +"\n"+ pDescr;

        //share Intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); // thats incase you share by email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); // message to show on the share

    }

    private void shareImageAndText(String pTitle, String pDescr, Bitmap bitmap) {
        String shareBody = pTitle +"\n"+ pDescr;
        // first it will save image in cache, and get
        Uri uri = saveImageToShare(bitmap);

        // share Intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exist
            File file = new File(imageFolder, "share_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.fredrick.forapp.fileprovider", file);
        }
         catch (Exception e) {
             Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }



    private void setLikes(final MyHolder holderLike, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){

                    holderLike.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0 ,0, 0);
                    holderLike.likeBtn.setText("Liked");
                }else {
                    holderLike.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0 ,0, 0);

                    holderLike.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        final PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in one post... current user
        if (uid.equals(myUid)) {
            //add items in Menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2,0, "View Comments");

        //items click Listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               int id = item.getItemId();
               if (id==0){
                   // if delete is click
                   beginDelete(pId, pImage);
               }
               else if (id==1){
                   // edit is click
                   Intent intent = new Intent(context, AddPostActivity.class);
                   intent.putExtra("key", "editPost");
                   intent.putExtra("editPostId", pId);
                   context.startActivity(intent);
               }
               else if (id==2){
                   Intent intent = new Intent(context,  PostCommentActivity.class);
                   intent.putExtra("postId", pId);  // id will get all post details
                   context.startActivity(intent);
               }
                return false;
            }
        });
       popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post with or without image
        if (pImage.equals("noImage")){
            //post without image
            deleteWithoutImage(pId);

        }else {
            // post with image
            deleteWithImage(pId, pImage);

        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //delete image and from database
                            Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                            fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        ds.getRef().removeValue(); //remove value from firebase
                                    }
                                    pd.dismiss();
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue(); //remove value from firebase
                }
                pd.dismiss();
                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    //View Holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //Views from row_post.XMl
        CircleImageView pitureTv;
        ImageView imageTv;
        MaterialTextView nameTv, timeTv, titleTv, descriptionTv, likeTv, commentTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init Views
            pitureTv = itemView.findViewById(R.id.uPictureTv);
            imageTv = itemView.findViewById(R.id.pImageTv);
            nameTv = itemView.findViewById(R.id.uNameTv);
            timeTv = itemView.findViewById(R.id.pTimeTv);
            titleTv = itemView.findViewById(R.id.pTitleTv);
            descriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            likeTv = itemView.findViewById(R.id.pLikesTv);
            commentTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.like_Btn);
            commentBtn = itemView.findViewById(R.id.comment_Btn);
            shareBtn = itemView.findViewById(R.id.share_Btn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
