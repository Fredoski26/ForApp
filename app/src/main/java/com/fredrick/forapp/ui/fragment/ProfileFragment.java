package com.fredrick.forapp.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.fredrick.forapp.AddPostActivity;
import com.fredrick.forapp.R;
import com.fredrick.forapp.adapter.PostAdapter;
import com.fredrick.forapp.model.Post;
import com.fredrick.forapp.model.User;
import com.fredrick.forapp.ui.home.HomeActivity;
import com.fredrick.forapp.ui.login.LoginActivity;
import com.fredrick.forapp.ui.setting.SettingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
FirebaseAuth mAuth;
FirebaseUser firebaseUser;
MaterialTextView name, email, phone;
FirebaseDatabase firebaseDatabase;
DatabaseReference reference;
CircleImageView profile;
ImageView coverTv;
FloatingActionButton floatingActionButton;
ProgressDialog dialogP;
private  final static int CAMERA_REQUEST_CODE = 100;
private  final static int STORAGE_REQUEST_CODE =200;
private  final static int IMAGE_PICK_GALLERY_CODE =300;
private  final static int IMAGE_PICK_CAMERA_CODE =400;
Uri image_Uri;
String profileOrCoverPhoto;
RecyclerView postRecyclerView;

StorageReference storageReference;
    //The storage of user pictures and Cover photo.
String storagePath = "Users_Profile_Cover_Img/";

//Array of permission to be requested
    String[] cameraPermission;
    String[] storagePermission;

    List<Post> postList;
    PostAdapter postAdapter;
    String uid;

//public ProfileFragment(){
//
//}
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");

        storageReference = FirebaseStorage.getInstance().getReference(); //FirebaseStorage reference
        //init Array of permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //init views
        name = view.findViewById(R.id.nameTv);
        email = view.findViewById(R.id.emailTv);
        profile = view.findViewById(R.id.profile_picture);
//        coverTv = view.findViewById(R.id.coverTv_image);
        phone = view.findViewById(R.id.phoneTv);
        floatingActionButton = view.findViewById(R.id.float_fab);
        dialogP = new ProgressDialog(getActivity());
        postRecyclerView = view.findViewById(R.id.recycleView_post);


        Query query = reference.orderByChild("id").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    User  user = ds.getValue(User.class);
                    assert user != null;
                    name.setText(user.getUsername());
                     email.setText(user.getEmail());
                     phone.setText(user.getNumber());
                    // String picture = ""+ ds.child("imageURL").getValue();
                    // String cover = ""+ ds.child("cover_image").getValue();
                     try {
                      //if image is Default set
                         if (user.getImageURL().equals("default")) {
                             profile.setImageResource(R.drawable.ic_light_camera);
                            // Picasso.get().load(picture).into(profile);
                         }else {
                             //else set received image
                            // Picasso.get().load(R.drawable.ic_light_camera).into(profile);
                             Picasso.get().load(user.getImageURL()).into(profile);
                         }
                     } catch (Exception e) {
                         // if any Error while getting image then set default
                              Picasso.get().load(R.drawable.ic_light_camera).into(profile);
                         Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                     }
//                     try
//                    {
//                          //if image received then set it
//                        Picasso.get().load(cover).into(coverTv);
//                    } catch (Exception e) {
//                         // if any Error while getting image then set default
//                       // Picasso.get().load(R.drawable.ic_camera).into(coverTv);
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfilePage();
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();
        return view;

    }

    private void loadMyPosts() {
        //linearLayout for recycleView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newwest post, from first to last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycleView
        postRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Post myPost = ds.getValue(Post.class);

                    //add to list
                    postList.add(myPost);

                    //set adapter
                    postAdapter = new PostAdapter(getActivity(), postList);
                    //set adapter to recyclerview
                    postRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPosts(final String searchQuerys) {
        //linearLayout for recycleView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show newest post, from first to last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recycleView
        postRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Post myPost = ds.getValue(Post.class);

                    assert myPost != null;
                    if (myPost.getpTitle().toLowerCase() .contains(searchQuerys.toLowerCase()) ||
                            myPost.getpDescr().toLowerCase() .contains(searchQuerys.toLowerCase())){

                        postList.add(myPost);
                    }
                    //  postList.add(post);

                    //set adapter
                    postAdapter = new PostAdapter(getActivity(), postList);
                    //set adapter to recyclerview
                    postRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private  void requestStoragePermission(){
        requestPermissions( storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private  void requestCameraPermission(){
       requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void showEditProfilePage() {
        //options to show in Dialog
        String[] options = {"Edit Profile Picture","Edit Name", "Edit Phone", "Change Password"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set item to Dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  //handle items onclick
                if (which == 0){
                    //edit profile pic
                    dialogP.setMessage("Updating profile picture...");
                    profileOrCoverPhoto = "imageURL";
                    showImageDialog();

                }else if (which == 2){
                    dialogP.setMessage("Updating phone...");
                    showPhoneUpdateDialog("number");

                }else if (which == 1){
                    //Edit name
                    dialogP.setMessage("Updating name...");
                    showNameUpdateDialog("username");

                }else if (which == 4){
                    //edit Cover pic
                    dialogP.setMessage("Updating cover photo...");
                    profileOrCoverPhoto = "cover_image";
                    showImageDialog();

                }else if (which == 3){
                    //edit Cover pic
                    dialogP.setMessage("Changing Password");
                    showChangePasswordDialog();

                }
            }
        });
        //create, show Dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_password, null);

        final MaterialEditText  passwordet = view.findViewById(R.id.password);
        final MaterialEditText  npassword = view.findViewById(R.id.npassword);
        Button btn_update = view.findViewById(R.id.btn_pass_update);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();
//        builder.create().show();
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String oldpassword = passwordet.getText().toString().trim();
                String newPassword = npassword.getText().toString().trim();
                if (TextUtils.isEmpty(oldpassword)){
                    Toast.makeText(getActivity(), "Enter Your Current Password.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password Length must be atleast 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldpassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldpassword, final String newPassword) {
        dialogP.show();
        final FirebaseUser user = mAuth.getCurrentUser();
        //before changing the password  re-authenticate, begin update
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldpassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                           user.updatePassword(newPassword)
                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {
                                           //password updated
                                           Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                                           dialogP.dismiss();
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   //failed
                                   dialogP.dismiss();
                                   Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
             dialogP.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showPhoneUpdateDialog(final String number) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update  "+number);

        //Set Layout for Dialog
        LinearLayout  linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        //Add edit text
        final EditText  editText = new EditText(getActivity());
        editText.setHint("Enter  "+number+" +234");
        linearLayout.addView(editText);


        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                //validate if user enter something
                if (!TextUtils.isEmpty(value)){
                    if (!Patterns.PHONE.matcher(value).matches()){
                        Toast.makeText(getActivity(), "Invalid Number", Toast.LENGTH_SHORT).show();
                        editText.setFocusable(true);
                        return;
                    }
                    if (value.length() != 11){
                        Toast.makeText(getActivity(), "Number must be 11 characters", Toast.LENGTH_SHORT).show();
                        editText.setFocusable(true);
                       return;
                    }
                    dialogP.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(number, value);
                    //........................................................
                    reference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialogP.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogP.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else {
                    Toast.makeText(getActivity(), " Please enter "+number, Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();

    }

    private void showNameUpdateDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle("Update  "+key);

         //Set Layout for Dialog
        LinearLayout  linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        //Add edit text
        final EditText  editText = new EditText(getActivity());
        editText.setHint("Enter  "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                //validate if user enter something
                if (!TextUtils.isEmpty(value)){
                     dialogP.show();
                     HashMap<String, Object> result = new HashMap<>();
                     result.put(key, value);
                     //........................................................
                     reference.child(firebaseUser.getUid()).updateChildren(result)
                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                  dialogP.dismiss();
                                     Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                 }
                             }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             dialogP.dismiss();
                             Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });

                     //if user edit hie name  and also change it from uid post
                     if (key.equals("username")){
                         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                         Query query = ref.orderByChild("uid").equalTo(uid);
                         query.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 for (DataSnapshot ds: snapshot.getChildren()){
                                     String child = ds.getKey();
                                     assert child != null;
                                     snapshot.getRef().child(child).child("uName").setValue(value);
                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {

                             }
                         });

                         //update name in current user comment in the post
                         ref.addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 for (DataSnapshot ds: snapshot.getChildren()){
                                     String child = ds.getKey();
                                     if (snapshot.child(child).hasChild("Comments")){
                                         String child1 = ""+snapshot.child(child).getKey();
                                         Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                         child2.addValueEventListener(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                 for (DataSnapshot ds: snapshot.getChildren()){
                                                     String child = ds.getKey();
                                                     snapshot.getRef().child(child).child("uName").setValue(value);
                                                 }
                                             }

                                             @Override
                                             public void onCancelled(@NonNull DatabaseError error) {

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

                }else {
                    Toast.makeText(getActivity(), " Please enter  "+key, Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImageDialog() {
        //options to show in Dialog
        String[] Options = {"Gallery","Camera"};
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set item to Dialog
        builder.setItems(Options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle items onclick
                if (which == 0){
//                 Gallery clicks
                     if (!checkStoragePermission()){
                         requestStoragePermission();
                     }else {
                         pickFromGallery();
                     }

                }else if (which == 1){
//                   Camera click
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
            }
        });
        //create, show Dialog
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(getActivity(), "Please enable Camera & Storage permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Please enable Storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
       // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    @Override
    public void onActivityResult(int requestCodes, int resultCode, Intent data) {
        //Fredoski said: This method will be call after picking image from gallery or camera.
        if (!(requestCodes == RESULT_OK)) {

            if (requestCodes == IMAGE_PICK_GALLERY_CODE) {
                // The image is picked from GALLERY, get the uri of image
                image_Uri = data.getData();

                uploadProfileCoverPhoto(image_Uri);
            }
            if (requestCodes == IMAGE_PICK_CAMERA_CODE) {
                    //The image is picked from CAMERA, get the uri of image

                 uploadProfileCoverPhoto(image_Uri);
                }
        }
        super.onActivityResult(requestCodes, resultCode, data);
    }

    private void uploadProfileCoverPhoto( final Uri image_Uri) {
        dialogP.show();
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ firebaseUser.getUid();
        StorageReference  storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(image_Uri)
                                       .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                           @Override
                                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                               //Image is stored in storage, then get to uri and upload into Users database
                                               Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                               while (!uriTask.isSuccessful());
                                               final Uri downloadUri = uriTask .getResult();

                                               //its gonna check if image is uploaded or the uri is received
                                               if (uriTask.isSuccessful()){
                                                   //uploaded image
                                                   HashMap<String, Object> hashMap = new HashMap<>();
                                                   assert downloadUri != null;
                                                   hashMap.put(profileOrCoverPhoto, downloadUri.toString());

                                                   reference.child(firebaseUser.getUid()).updateChildren(hashMap)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        //uri in database  of user is
                                                                        dialogP.dismiss();
                                                                        Toast.makeText(getActivity(), "Image Successfully Uploaded...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                       @Override
                                                       public void onFailure(@NonNull Exception e) {
                                                                dialogP.dismiss();
                                                           Toast.makeText(getActivity(), "Error Updating Image!...", Toast.LENGTH_SHORT).show();
                                                       }
                                                   });


                                                   //if user edit hie name  and also change it from uid post
                                                   if (profileOrCoverPhoto.equals("imageURL")){
                                                       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                                       Query query = ref.orderByChild("uid").equalTo(uid);
                                                       query.addValueEventListener(new ValueEventListener() {
                                                           @Override
                                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                               for (DataSnapshot ds: snapshot.getChildren()){
                                                                   String child = ds.getKey();
                                                                   assert child != null;
                                                                   snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                               }
                                                           }

                                                           @Override
                                                           public void onCancelled(@NonNull DatabaseError error) {

                                                           }
                                                       });

                                                       //update name in current user comment in the post
                                                       ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                           @Override
                                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                               for (DataSnapshot ds: snapshot.getChildren()){
                                                                   String child = ds.getKey();
                                                                   if (snapshot.child(child).hasChild("Comments")){
                                                                       String child1 = ""+snapshot.child(child).getKey();
                                                                       Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                                       child2.addValueEventListener(new ValueEventListener() {
                                                                           @Override
                                                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                               for (DataSnapshot ds: snapshot.getChildren()){
                                                                                   String child = ds.getKey();
                                                                                   snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                                               }
                                                                           }

                                                                           @Override
                                                                           public void onCancelled(@NonNull DatabaseError error) {

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

                                               }else {
                                                   dialogP.dismiss();
                                                   Toast.makeText(getActivity(), "Something went Wrong!...", Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Any some Error get and show it and dismiss progress dialog.
                                                dialogP.dismiss();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

    }

    private void pickFromCamera() {
        // To pick Image from Camera. "Let Begins With Fredrick Obarafor"
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        // put the image in uri
        image_Uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start CAMERA
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_Uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }

    private void pickFromGallery() {

        //pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    // Please Recheck this method
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem item =  menu.findItem(R.id.app_bar_search);
        //search specific post
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//               its called when user press seaarch button
                if (!TextUtils.isEmpty(query))
                {

                    searchMyPosts(query);
                }else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // called as and when user press  any letter
                if (!TextUtils.isEmpty(newText))
                {
                    searchMyPosts(newText);
                }else {
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private  void  checkUserStatus(){
        FirebaseUser  firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
                   uid = firebaseUser.getUid();
        }else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            mAuth.signOut();
            checkUserStatus();

        }
       else if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }
        else if (id == R.id.settings){
            //navigate to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}