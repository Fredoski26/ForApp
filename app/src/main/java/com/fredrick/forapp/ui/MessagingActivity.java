package com.fredrick.forapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.ChatAdapter;
import com.fredrick.forapp.adapter.UserAdapter;
import com.fredrick.forapp.model.Chat;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.notifications.Data;
import com.fredrick.forapp.notifications.Sender;
import com.fredrick.forapp.notifications.Token;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.setting.SettingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
//import retrofit2.Call;
//import retrofit2.Callback;

public class MessagingActivity extends AppCompatActivity {
Toolbar toolbar;
RecyclerView recyclerView;
MaterialEditText editText;
ImageButton sendbtn, attachBtn, emojibtn;
ImageView backArrow, blockIv;
MaterialTextView nameTv, onlineTv;
CircleImageView circleImageView;
FirebaseAuth mAuth;
FirebaseDatabase firebaseDatabase;
DatabaseReference  reference;
//checking if user has seen Message or not
    ValueEventListener seenListeners;
    DatabaseReference referenceForSeen;
    List<Chat> chatList;
    ChatAdapter chatAdapter;
    private Vibrator myVib;

String hisUids;
String myUids;
String hisImage;
boolean isBlocked = false;

//volley request queue for notifications
private RequestQueue requestQueue;
private boolean notify = false;

    //permission constants
    private  final static int CAMERA_REQUEST_CODE = 100;
    private  final static int STORAGE_REQUEST_CODE =200;
    //image pick constants
    private  final static int IMAGE_PICK_CAMERA_CODE =300;
    private  final static int IMAGE_PICK_GALLERY_CODE =400;
    Uri image_Uri = null;

    //Array of permission to be requested
    String[] cameraPermission;
    String[] storagePermission;

    //progress bar
    ProgressDialog dialog;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
       Toolbar toolbar = findViewById(R.id.show_toolbar);
       setSupportActionBar(toolbar);
       toolbar.setTitle("");
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.ring_send);
       recyclerView = findViewById(R.id.show_chat_recycleView);
       editText = findViewById(R.id.show_edit_text);
       sendbtn = findViewById(R.id.show_sendbtn);
//       emojibtn = findViewById(R.id.show_emoji);
        attachBtn = findViewById(R.id.attachBtn);
       nameTv = findViewById(R.id.show_name);
       onlineTv =  findViewById(R.id.show_online);
       circleImageView =  findViewById(R.id.show_profile);
       backArrow = findViewById(R.id.back_arrow);
       blockIv = findViewById(R.id.blockIv);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //Init permission Arrays
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        dialog = new ProgressDialog(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
       //Layout
        LinearLayoutManager  linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerview_properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create Api Service
//        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


       mAuth = FirebaseAuth.getInstance();

       Intent intent = getIntent();
       hisUids = intent.getStringExtra("hisUids");

       firebaseDatabase = FirebaseDatabase.getInstance();
       reference = firebaseDatabase.getReference("Users");

       //search user to get that users info
        Query userQuery = reference.orderByChild("id").equalTo(hisUids);

        // get Ueser picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check until required information is received
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
//                    User user = ds.getValue(User.class);
//                    assert user != null;
//                    nameTv.setText(user.getUsername());
                    String name =""+ ds.child("username").getValue();
                    hisImage = ""+ ds.child("imageURL").getValue();
                    String typingStatus = ""+ ds.child("typingTo").getValue();


                    if (typingStatus.equals(myUids)){
                        onlineTv.setText("typing...");
                    }else {

                        //get value of onlinestatus
                        String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            onlineTv.setText(onlineStatus);

                        }else {
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyy hh:mm aa", cal).toString();
                            onlineTv.setText("Last seen at: "+dateTime);
                        }


                    }

                    //set data
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_profile_img).into(circleImageView);
                    }
                    catch (Exception e) {

                        Picasso.get().load(R.drawable.ic_profile_img).into(circleImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myVib.vibrate(5);
               onBackPressed();
            }

        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
             //get text from the edit text
                String message = editText.getText().toString().trim();
                //check if editText is empty
                if (TextUtils.isEmpty(message))
                {
                    Toast.makeText(MessagingActivity.this, "Type something...", Toast.LENGTH_SHORT).show();
                }else {
                    myVib.vibrate(5);
                    sendMessage(message);
                    mp.start();

                }
                //Reset editText after sending message.
                editText.setText("");
            }
        });

        //Check edit text Listener
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                  if (s.toString().trim().length() ==0){
                      checkTypingStatus("ontyping");
                  }else {
                      checkTypingStatus(hisUids);  //ID of receiver
                  }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // click to block unblock users
        blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked){
                    unBlockUser();
                }
                else {
                    blockUser();
                }
            }
        });


        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show image pick Dialog
                showImagePickDialog();

            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (editText.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

        readMessages();
        checkIsBlock();
        seeMessage();
    }


    private void checkIsBlock() {
//         check each users if blocked or not
        //if uid of the user exists in BlockerUsers then that user is blocked , otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUids)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                blockIv.setImageResource(R.drawable.ic_block);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void blockUser() {
        //block user, by adding uid to current user`s blockerUser node

        //put values in hashmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUids);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUids).child("BlockedUsers").child(hisUids).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //block successfully
                        Toast.makeText(MessagingActivity.this, "Blocked", Toast.LENGTH_SHORT).show();
                        blockIv.setImageResource(R.drawable.ic_block);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // block failed
                Toast.makeText(MessagingActivity.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void unBlockUser() {
        //unblock user by removing uid from the current user`s BlockUser node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUids).child("BlockedUsers").orderByChild("uid").equalTo(hisUids)
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
                                                Toast.makeText(MessagingActivity.this, "Unblocked", Toast.LENGTH_SHORT).show();
                                                blockIv.setImageResource(R.drawable.ic_unblock_green);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Unblocked failed
                                        Toast.makeText(MessagingActivity.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void showImagePickDialog() {
        //options (Camera or Gallery)
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        // set options to  dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which==0){
                    //camera click
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
                if (which==1){
                    //Gallery click
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        });
        //Create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //pick from Camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_Uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent galleryIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_Uri);
        //  galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    private void seeMessage() {
        referenceForSeen  =  FirebaseDatabase.getInstance().getReference("Chats");
        seenListeners = referenceForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myUids) && chat.getSender().equals(hisUids)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("messageSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(myUids) && chat.getSender().equals(hisUids) ||
                    chat.getReceiver().equals(hisUids) && chat.getSender().equals(myUids)){
                        chatList.add(chat);

                    }
                    //adapter
                    chatAdapter  = new ChatAdapter(MessagingActivity.this, chatList, hisImage);
                    chatAdapter.notifyDataSetChanged();
                    //set adapter to recycleview
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendMessage(final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
 String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap =  new HashMap<>();
        hashMap.put("sender", myUids);
        hashMap.put("receiver", hisUids);
        hashMap.put("message",message);
        hashMap.put("messageSeen",false);
        hashMap.put("timestamp", timestamp);
        hashMap.put("type","text");
        databaseReference.child("Chats").push().setValue(hashMap);


        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUids);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify){
                    assert user != null;
                    sendNotifications1(hisUids, user.getUsername(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //create chatlist node/child firebase Daabase
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUids).child(hisUids);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef1.child("ids").setValue(hisUids);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUids).child(myUids);
           chatRef2.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if (!snapshot.exists()){
                       chatRef2.child("ids").setValue(myUids);
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {
                   Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
               }
           });
    }

    private void sendImageMessage(Uri image_ruri) throws IOException {
        notify =  true;
        //progress Dialog
        dialog.setMessage("sending image...");
        dialog.show();

        final String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;
        //Chats node will be created that will contain all images sent via chat

        //get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_ruri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[]  data = baos.toByteArray(); //convert image to byte
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image upload
                        dialog.show();
                        //get uri of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){
                                // add image uri and others info to database
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                //set up required data
                                HashMap<String, Object> hashMap =  new HashMap<>();
                                hashMap.put("sender", myUids);
                                hashMap.put("receiver", hisUids);
                                hashMap.put("message", downloadUri);
                                hashMap.put("timestamp", timeStamp);
                                hashMap.put("type", "imageURL");
                                hashMap.put("messageSeen",false);

                                databaseReference.child("Chats").push().setValue(hashMap);

                                // send notification
                                dialog.dismiss();
                                DatabaseReference database =  FirebaseDatabase.getInstance().getReference("Users").child(myUids);
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        if (notify){
                                            assert user != null;
                                            sendNotifications1(hisUids, user.getUsername(), "sent you photo...");
                                        }
                                        notify = false;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                //create chatlist node/child firebase Daabase
                                final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUids).child(hisUids);
                                chatRef1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()){
                                            chatRef1.child("ids").setValue(hisUids);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUids).child(myUids);
                                chatRef2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()){
                                            chatRef2.child("ids").setValue(myUids);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(MessagingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });

    }

    private void sendNotifications1(final String hisUids, final String username, final String message) {
        DatabaseReference  allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUids);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(

                            ""+myUids,
                            ""+username + ": " + message,
                            "ForApp Message",
                            ""+hisUids,
                            "ChatNotification",
                            R.drawable.ic_iconics);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                   //fcm json object for request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                // put params
                                 Map<String, String> headers = new HashMap<>();
                                 headers.put("Content-Type", "application/json");
                                 headers.put("Authorization", "key=AAAAlLqgGCQ:APA91bHnDs-Bbz8I_jLIePstFB8uRSnD-CSo5Hpb7cQqACnVwUeXnOPvsRJ9eK26LR4LSnXjLBEC1ybEsbT8cQDneqmrbSdFHznT8fXqMZc4_LzJ431kyDCvBL-t8QrHLmsm_KQwh0E9");
                                return headers;
                              //  return super.getHeaders();
                            }
                        };
                        // add request to queue
                     requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){

            myUids = firebaseUser.getUid();

        }else {
            startActivity(new Intent(this, LoginActivity.class));
           finish();
        }
    }

    private void checkOnlineStatus(String status){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUids);
        HashMap<String, Object>  hashMap =  new HashMap<>();
        hashMap.put("onlineStatus", status);
        //This will update value of onlinestatus for current user
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUids);
        HashMap<String, Object>  hashMap =  new HashMap<>();
        hashMap.put("typingTo", typing);
        //This will update value of typingstatus for current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with last seen time stamp
        checkOnlineStatus(timestamp);
        referenceForSeen.removeEventListener(seenListeners);
        checkTypingStatus("ontyping");
    }

    @Override
    protected void onResume() {
        //set onlne
        checkOnlineStatus("online");
        super.onResume();
    }

    //        handle permission  results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case CAMERA_REQUEST_CODE:{

                if (grantResults.length >0){

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        // both permission are granted
                        pickFromCamera();

                    }else {
                        // If permission were Denied
                        Toast.makeText(this, "Please enable Camera and Storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{

                if (grantResults.length >0){

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){

                        pickFromGallery();

                    }else {
                        // If permission were Denied
                        Toast.makeText(this, "Please enable Storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCodes, int resultCode, Intent data) {
        //Fredoski said: This method will be call after picking image from gallery or camera.
        if (!(requestCodes == RESULT_OK)) {

            if (requestCodes == IMAGE_PICK_GALLERY_CODE) {

                    // The image is picked from GALLERY, get the uri of image
                    image_Uri = data.getData();

                 // using this uri to upload to firebase storage
                try {
                    sendImageMessage(image_Uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (requestCodes == IMAGE_PICK_CAMERA_CODE) {
                //The image is picked from CAMERA, get the uri of image
                try {
                    sendImageMessage(image_Uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCodes, resultCode, data);
    }




//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_message_users, menu);
//       // menu.findItem(R.id.app_bar_search).setVisible(false);
//       // menu.findItem(R.id.action_add_post).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) {
//            mAuth.signOut();
//            checkUserStatus();
//
//        }
//        else if (id == R.id.settings){
//            Intent intent = new Intent(MessagingActivity.this, SettingsActivity.class);
//            startActivity(intent);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}