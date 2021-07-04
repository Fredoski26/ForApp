package com.fredrick.forapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.registers.RegisterActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
ActionBar actionBar;
FirebaseAuth mAuth;
FirebaseUser firebaseUser;
FirebaseDatabase firebaseDatabase;
DatabaseReference reference;
DatabaseReference userDbRef;
EditText titleEt, descriptionEt;
ShapeableImageView imageTv ;
Button uploadBtn;

    //user Info
    String name, email , uid, dp;

//permission constants
    private  final static int CAMERA_REQUEST_CODE = 100;
    private  final static int STORAGE_REQUEST_CODE =200;
    //image pick constants
    private  final static int IMAGE_PICK_CAMERA_CODE =300;
    private  final static int IMAGE_PICK_GALLERY_CODE =400;
    Uri image_Uri = null;

    //information of post to edit
    String editTitle, editDescription, edtitImage;

    //progress bar
    ProgressDialog dialog;

    //second progress
    ProgressBar progressBar;
    RelativeLayout relativeLayout;

    //Array of permission to be requested
    String[] cameraPermission;
    String[] storagePermission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                finish();
//            }
//        });
        mAuth =  FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        dialog = new ProgressDialog(this);



        checkUserStatus();

        //init View
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageTv = findViewById(R.id.pImageTv);
        uploadBtn = findViewById(R.id.pUploadBtn);


         
         //getSupportActionBar().setSubtitle(email);

          //get data through intent from previous activities adapter
        Intent intent = getIntent();

        //get data and its type from intent
        String action =  intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type!=null){
            if ("text/plain".equals(type)) {
                //text type data
                handleSendText(intent);

            }else if (type.startsWith("image")){
                //image type data
                handleSendImage(intent);
            }
        }

        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editPostId = ""+intent.getStringExtra("editPostId");

        //validate if we are  for update
        if (isUpdateKey.equals("editPost")){
            getSupportActionBar().setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }else {
              getSupportActionBar().setTitle("Add New Post");
              uploadBtn.setText("Upload");
        }


         // Getting some info from current user
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {

                    name = "" + ds.child("username").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("imageURL").getValue();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        

        //storageReference = FirebaseStorage.getInstance().getReference(); //FirebaseStorage reference
        //init Array of permissions

        //second progressbar
//        progressBar = findViewById(R.id.progressbar);
//        relativeLayout = findViewById(R.id.circle_p);
//        relativeLayout.setVisibility(View.GONE);
//        progressBar.setVisibility(View.GONE);

        //get image from Gallery or Camera
        imageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        //upload button
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get data(title, description from edittexts)
                String title =  titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title))
                {
                    Toast.makeText(AddPostActivity.this, "# designate", Toast.LENGTH_SHORT).show();
                    return; 
                }
                if (TextUtils.isEmpty(description))
                {
                    Toast.makeText(AddPostActivity.this, "type description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){
                    beginUpdate(title, description, editPostId);
                }else {
                    //post without image
                    upLoadData(title, description );
                }

//                if (image_Uri==null){
//                      //post without image
//                    upLoadData(title, description, "noImage");
//                }else {
//                       //post with image
//                    upLoadData(title, description, String.valueOf(image_Uri));
//                }
            }
        });


    }

    private void handleSendImage(Intent intent) {
        //handle the received image(uri)
        Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI !=null){
            image_Uri = imageURI;
            //set image
            imageTv.setImageURI(image_Uri);
        }
    }

    private void handleSendText(Intent intent) {
        // handle the received text
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText!=null){
            //set to descrip edit text
            descriptionEt.setText(sharedText);
        }

    }

    private void beginUpdate(String title, String description, String editPostId) {
        dialog.setMessage("Updating Post");
        dialog.show();

        if (!edtitImage.equals("noImage"))
        {
            //without image
            upadeteWasWithImage(title, description, editPostId);

        }else if (imageTv.getDrawable() != null)
        {
            //with image
            upadeteWasWithNowImage(title, description, editPostId);
        }else {
            upadeteWasWithOutImage(title, description, editPostId);
        }
    }

    private void upadeteWasWithOutImage(String title, String description, String editPostId) {

        //uri is received, upload to firebase database
        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        hashMap.put("pLikes", "0");
        hashMap.put("pComments", "0");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                        //reset Viws
                        dialog.dismiss();
                        titleEt.setText("");
                        descriptionEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void upadeteWasWithNowImage(final String title, final String description, final String editPostId) {

        //image deleted , upload new image
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timestamp;

        //get image for view
        Bitmap bitmap = ((BitmapDrawable)imageTv.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,outputStream);
        byte[] data = outputStream.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploade get it uri
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            //uri is received, upload to firebase database
                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments", "0");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                                            //reset Viws
                                            dialog.dismiss();
                                            titleEt.setText("");
                                            descriptionEt.setText("");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //image not upload
                dialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void upadeteWasWithImage(final String title, final String description, final String editPostId) {
        // post with image
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(edtitImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted , upload new image
                        final String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timestamp;

                        //get image for view
                        Bitmap bitmap = ((BitmapDrawable)imageTv.getDrawable()).getBitmap();
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100,outputStream);
                        byte[] data = outputStream.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                          ref.putBytes(data)
                                  .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                      @Override
                                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                         //image uploade get it uri
                                          Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                          while (!uriTask.isSuccessful());
                                          String downloadUri = uriTask.getResult().toString();
                                          if (uriTask.isSuccessful()){
                                              //uri is received, upload to firebase database
                                              HashMap<String, Object> hashMap = new HashMap<>();
                                              //put post info
                                              hashMap.put("uid", uid);
                                              hashMap.put("uName", name);
                                              hashMap.put("uEmail", email);
                                              hashMap.put("uDp", dp);
                                              hashMap.put("pTitle", title);
                                              hashMap.put("pDescr", description);
                                              hashMap.put("pImage", downloadUri);
                                              hashMap.put("pLikes", "0");
                                              hashMap.put("pComments", "0");

                                              DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                              ref.child(editPostId)
                                                      .updateChildren(hashMap)
                                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                          @Override
                                                          public void onSuccess(Void aVoid) {
                                                              dialog.dismiss();
                                                              Toast.makeText(AddPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                                                              //reset Viws
                                                              dialog.dismiss();
                                                              titleEt.setText("");
                                                              descriptionEt.setText("");
                                                          }
                                                      }).addOnFailureListener(new OnFailureListener() {
                                                  @Override
                                                  public void onFailure(@NonNull Exception e) {
                                                   dialog.dismiss();
                                                      Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                  }
                                              });

                                          }
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  //image not upload
                                  dialog.dismiss();
                                  Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                              }
                          });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    edtitImage = ""+ds.child("pImage").getValue();
                    //set Data to Views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set Image
                    if (!edtitImage.equals("noImage")){
                        try {
                            Picasso.get().load(edtitImage).into(imageTv);
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void upLoadData(final String title, final String description) {
        dialog.setMessage("Publishing post");
        dialog.show();

        //for post -image name, post-id , post-publish time
        final String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timestamp;
        if (imageTv.getDrawable() != null){
            //get image for view
            Bitmap bitmap = ((BitmapDrawable)imageTv.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,outputStream);
            byte[] data = outputStream.toByteArray();

            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               //Image is uploaded to firebase storage and then get it to uri
                             Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                             while (!uriTask.isSuccessful());

                             String downloadUri = uriTask.getResult().toString();

                             if (uriTask.isSuccessful()){
                                 //uri is uploading post to firebase database
                                 HashMap<Object, String> hashMap = new HashMap<>();
                                 //put post info
                                 hashMap.put("uid", uid);
                                 hashMap.put("uName", name);
                                 hashMap.put("uEmail", email);
                                 hashMap.put("uDp", dp);
                                 hashMap.put("pId", timestamp);
                                 hashMap.put("pTitle", title);
                                 hashMap.put("pDescr", description);
                                 hashMap.put("pImage", downloadUri);
                                 hashMap.put("pTime", timestamp);
                                 hashMap.put("pLikes", "0");
                                 hashMap.put("pComments", "0");

                                 //path to store  post data
                                 DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                 //put data in ref
                                 ref.child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                     //add into database
                                                    dialog.show();
                                                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                    //reset Viws
                                                    dialog.dismiss();
                                                    titleEt.setText("");
                                                    descriptionEt.setText("");
                                                    imageTv.setImageURI(null);
                                                    image_Uri =  null;
                                                    //send notification
                                                    prepareNotification(
                                                            ""+timestamp,
                                                            ""+name+" Added new post",
                                                            ""+title+"\n"+description,
                                                            "PostNotification",
                                                            "POST");

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                       //failed adding post to database
                                         dialog.dismiss();
                                         Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                 });
                             }
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timestamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timestamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");

            //path to store  post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in ref
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //add into database
                            dialog.show();
                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                            //reset Viws
                            dialog.dismiss();
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageTv.setImageURI(null);
                            image_Uri =  null;

                            //send notification
                            prepareNotification(
                                    ""+timestamp,
                                    ""+name+" Added new post",
                                    ""+title+"\n"+description,
                                    "PostNotification",
                                    "POST");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post to database
                    dialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic ){
        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; // topic must match with what the receiver subscribed to
        String NOTIFICATION_TITLE = title; // e.g fredoski added new post
        String NOTIFICATION_MESSAGE = description; // contents of post
        String NOTIFICATION_TYPE = notificationType; //Now there are two notifications type , chat and post. so to differentiate in FirebaseMessaging.java class
        // prepare Json what to send and where to receive
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            // what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid);//uid of the current user/sender
            notificationBodyJo.put("pId", pId); // post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescr", NOTIFICATION_MESSAGE);
//             where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);

            notificationJo.put("data", notificationBodyJo); // combine data to send
        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

            sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {
      //  JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // Log.d("FCM_RESPONSE", "onResponse: "+error.toString());
                Toast.makeText(AddPostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                // put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAlLqgGCQ:APA91bHnDs-Bbz8I_jLIePstFB8uRSnD-CSo5Hpb7cQqACnVwUeXnOPvsRJ9eK26LR4LSnXjLBEC1ybEsbT8cQDneqmrbSdFHznT8fXqMZc4_LzJ431kyDCvBL-t8QrHLmsm_KQwh0E9");
                return headers;
                //  return super.getHeaders();
            }
        };
        // add request to queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);


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
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private  void  checkUserStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
            email = firebaseUser.getEmail();
            uid = firebaseUser.getUid();

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
//
//        //Its being comment, no action_add_post or app_bar_search in menu_post
////        menu.findItem(R.id.action_add_post).setVisible(false);
//       // menu.findItem(R.id.app_bar_search).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//        int id =  item.getItemId();
//        if (id == R.id.logout){
//            mAuth.signOut();
//            checkUserStatus();
//        }
//        return super.onOptionsItemSelected(item);
//    }

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

                //set to imageView
               imageTv.setImageURI(image_Uri);
            }
            if (requestCodes == IMAGE_PICK_CAMERA_CODE) {
                //The image is picked from CAMERA, get the uri of image

               imageTv.setImageURI(image_Uri);
            }
        }
        super.onActivityResult(requestCodes, resultCode, data);
    }
}