package com.fredrick.forapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.fredrick.forapp.adapter.CommentAdapter;
import com.fredrick.forapp.model.Comment;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentActivity extends AppCompatActivity {

    // to get comments and details
    String hisUid, myUid, myEmail, myName, myDp, pImage,
    postId, pLikes, hisDp, hisName;

    boolean mProgressComment = false;
    boolean mProgressLike = false;

    //Views from post_comment.XMl
    CircleImageView pictureTv, cAvatarTv;
    ImageView imageTv;
    MaterialTextView nameTv, timeTv, titleTv, descriptionTv, likeTv, commentTv;
    ImageButton moreBtn, cSendBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    MaterialEditText commentEt;
    //progress bar
    ProgressDialog pd;

    List<Comment> commentList;
    CommentAdapter commentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));

        // get id of the post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init Views
        pictureTv = findViewById(R.id.uPictureTv);
        imageTv = findViewById(R.id.pImageTv);
        nameTv = findViewById(R.id.uNameTv);
        timeTv = findViewById(R.id.pTimeTv);
        titleTv = findViewById(R.id.pTitleTv);
        descriptionTv = findViewById(R.id.pDescriptionTv);
        likeTv = findViewById(R.id.pLikesTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.like_Btn);
        commentTv = findViewById(R.id.pCommentsTv);
        shareBtn = findViewById(R.id.share_Btn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recycleView_comment);
        cAvatarTv = findViewById(R.id.cAvatarTv);
        cSendBtn = findViewById(R.id.cSendBtn);
        commentEt = findViewById(R.id.commentEt);

        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();
        loadComments();

        //send comment button click
        cSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = titleTv.getText().toString().trim();
                String pDescr = descriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)imageTv.getDrawable();
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
        //On Like click to start PostLikedByActivity
//        likeTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(PostCommentActivity.this, PostLikedByActivity.class);
//                intent.putExtra("postId", postId);
//                startActivity(intent);
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
        startActivity(Intent.createChooser(sIntent, "Share Via")); // message to show on the share

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
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exist
            File file = new File(imageFolder, "share_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.fredrick.forapp.fileprovider", file);
        }
        catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        //LinearLayout for recycle view
        LinearLayoutManager layoutManager  = new LinearLayoutManager(getApplicationContext());
        //set layout to recycle view
        recyclerView.setLayoutManager(layoutManager);

        //init comment list
        commentList  = new ArrayList<>();
        //To get comments from the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
               for (DataSnapshot ds: snapshot.getChildren()){
                   Comment comment = ds.getValue(Comment.class);
                   commentList.add(comment);

                   //SetUp Adapter
                   commentAdapter = new CommentAdapter(getApplicationContext(), commentList, myUid, postId);
                   //set Adapter
                   recyclerView.setAdapter(commentAdapter);

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showMoreOptions() {

       final PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //show delete option in one post... current user
        if (hisUid.equals(myUid)) {
            //add items in Menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }


        //items click Listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    // if delete is click
                    beginDelete();
                }
                else if (id==1){
                    // edit is click
                    Intent intent = new Intent(PostCommentActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        //post with or without image
        if (pImage.equals("noImage")){
            //post without image
            deleteWithoutImage();

        }else {
            // post with image
            deleteWithImage();

        }
    }

    private void deleteWithImage() {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //delete image and from database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue(); //remove value from firebase
                                }
                                pd.dismiss();
                                Toast.makeText(PostCommentActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostCommentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        //progressbar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue(); //remove value from firebase
                }
                pd.dismiss();
                Toast.makeText(PostCommentActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
         DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0 ,0, 0);
                    likeBtn.setText("Liked");
                }else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0 ,0, 0);

                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostCommentActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void likePost() {
    mProgressLike = true;
        //get id of the post clicked
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProgressLike){
                    if (snapshot.child(postId).hasChild(myUid)){
                        //for already , to remove like
                        postRef.child(postId).child("pLikes").setValue(""+ (Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProgressLike = false;
                        addToHisNotifications(""+hisUid, ""+postId, "Liked your post");

                    }else
                    {
                        //if not like , like it
                        postRef.child(postId).child("pLikes").setValue(""+ (Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //set value
                        mProgressLike = false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postComment() {
        pd =  new ProgressDialog(this);
        pd.setMessage("Posting comment...");

        //get data from edit text
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)){
            //if no value is enter
            Toast.makeText(this, "Comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        // making each post  to child "Comments"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        //load info inside the haspmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        // put the data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       //Added
                       pd.dismiss();
                        Toast.makeText(PostCommentActivity.this, "comment posted", Toast.LENGTH_SHORT).show();
                        //reset edit text
                        commentEt.setText("");
                        updateCommentCount();
                        addToHisNotifications(""+hisUid, ""+postId, "Commented on your post");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostCommentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        

    }


    private void updateCommentCount() {
        mProgressComment = true;
        // whenever user add comment increase the comment count
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProgressComment){
                    String comments = ""+ snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProgressComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        // get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("id").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
//                    User  user = ds.getValue(User.class);
//                    assert user != null;
//                    nameTv.setText(user.getUsername());
                    myName = ""+ds.child("username").getValue();
                    myDp = ""+ds.child("imageURL").getValue();


                    //set data
                    try {
                        // if image is received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_profile_dark).into(cAvatarTv);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_profile_dark).into(cAvatarTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostCommentActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadPostInfo() {
        //get post using the post id
        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               // keep checking the posts until get the required post
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes  = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp  = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();
                    // convert timestamp to format
                    //convert  timestamp to dd/mm/yy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTimes = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set Data
                    titleTv.setText(pTitle);
                    descriptionTv.setText(pDescr);
                    likeTv.setText(pLikes +" Likes");
                    timeTv.setText(pTimes);
                    commentTv.setText(commentCount +" comments");

                    nameTv.setText(hisName);

                    if (pImage.equals("noImage")){
                        //hide imageView
                        imageTv.setVisibility(View.GONE);

                    }else {
                        //show imageView
                        imageTv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(imageTv);

                        }catch (Exception e){
                            Toast.makeText(PostCommentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    }

                    //set user image in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_profile_dark).into(pictureTv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_profile_dark).into(pictureTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void  checkUserStatus(){
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null){
            // user is signed in
            myEmail = mUser.getEmail();
            myUid = mUser.getUid();

        }else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_post, menu);
//        //hide some menu items
//        menu.findItem(R.id.action_add_post).setVisible(false);
//        menu.findItem(R.id.app_bar_search).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();

        }

        return super.onOptionsItemSelected(item);
    }
}