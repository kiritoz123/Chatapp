package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.StoryPostAdapters;
import com.example.chatapplication.databinding.ActivityProfileBinding;
import com.example.chatapplication.models.PostStory;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private User user;
    private String myID, myName, myImage, myEmail;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();

        user = (User) getIntent().getSerializableExtra("user");
        myID = getIntent().getStringExtra("myID");
        myName = getIntent().getStringExtra("myName");
        myImage = getIntent().getStringExtra("myImage");
        myEmail = preferenceManager.getString(Constants.KEY_EMAIL);

        getFriendStatus();

        if (user != null) {
            setData();
        }

        setListenner();

        setStatusBarColor();
    }

    private void getFriendStatus() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                binding.btnAdd.setVisibility(View.GONE);
                binding.btnMessage.setVisibility(View.GONE);

                binding.btnDeleteRequest.setVisibility(View.GONE);
                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                binding.btnAcceptRequest.setVisibility(View.GONE);
                binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                binding.btnDeleteAdd.setVisibility(View.VISIBLE);
                binding.btnMessageDeleteAdd.setVisibility(View.VISIBLE);
            } else {
                database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                        binding.btnAdd.setVisibility(View.GONE);
                        binding.btnMessage.setVisibility(View.GONE);

                        binding.btnDeleteRequest.setVisibility(View.VISIBLE);
                        binding.btnMessageDeleteRequest.setVisibility(View.VISIBLE);

                        binding.btnAcceptRequest.setVisibility(View.GONE);
                        binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                        binding.btnDeleteAdd.setVisibility(View.GONE);
                        binding.btnMessageDeleteAdd.setVisibility(View.GONE);
                    } else {
                        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful() && !task3.getResult().isEmpty()) {
                                binding.btnAdd.setVisibility(View.GONE);
                                binding.btnMessage.setVisibility(View.GONE);

                                binding.btnDeleteRequest.setVisibility(View.GONE);
                                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                                binding.btnAcceptRequest.setVisibility(View.VISIBLE);
                                binding.btnMessageAcceptRequest.setVisibility(View.VISIBLE);

                                binding.btnDeleteAdd.setVisibility(View.GONE);
                                binding.btnMessageDeleteAdd.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }

    private void setListenner() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        // gửi lời kết bạn
        binding.btnAdd.setOnClickListener(v -> {
            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);
            userFriend.put(Constants.KEY_EMAIL, user.email);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).add(userFriend).addOnSuccessListener(documentReference -> {

            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

            HashMap<String, Object> myUser = new HashMap<>();
            myUser.put(Constants.KEY_USER_ID, myID);
            myUser.put(Constants.KEY_NAME, myName);
            myUser.put(Constants.KEY_IMAGE, myImage);
            myUser.put(Constants.KEY_EMAIL, myEmail);

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).add(myUser).addOnSuccessListener(documentReference -> {
                showToast("Đã gửi lời mời kết bạn đến " +user.name);
            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

            binding.btnAdd.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);

            binding.btnDeleteRequest.setVisibility(View.VISIBLE);
            binding.btnMessageDeleteRequest.setVisibility(View.VISIBLE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);
        });

        // chấp nhận lời mời kết bạn
        binding.btnAcceptRequest.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);
            userFriend.put(Constants.KEY_EMAIL, user.email);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteRequest(queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        hashMyUser(user.id, queryDocumentSnapshot.getString(Constants.KEY_NAME), queryDocumentSnapshot.getString(Constants.KEY_IMAGE), queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                        deleteWait(user.id, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).add(userFriend).addOnSuccessListener(documentReference -> {
                showToast("Bạn vừa có thêm một người bạn mới ^^");
            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

//            if (!preferenceManager.getString(Constants.NUMBLE_NOTIFICATION_REQUEST).isEmpty()) {
//                int number = Integer.parseInt(preferenceManager.getString(Constants.NUMBLE_NOTIFICATION_REQUEST));
//                number++;
//                preferenceManager.putString(Constants.NUMBLE_NOTIFICATION_REQUEST, ""+ number);
//            }

            binding.btnAdd.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.VISIBLE);
            binding.btnMessageDeleteAdd.setVisibility(View.VISIBLE);

        });

        // hủy kết bạn
        binding.btnDeleteAdd.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);
            userFriend.put(Constants.KEY_EMAIL, user.email);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteFriend(myID, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteFriend(user.id, queryDocumentSnapshot.getId());
                        showToast("Bạn vừa hủy kết bạn với " +user.name);
                    }
                }
            });

            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);

        });

        // hủy lời mời kết bạn đi
        binding.btnDeleteRequest.setOnClickListener(v -> {
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            HashMap<String, Object> userFriend = new HashMap<>();
            userFriend.put(Constants.KEY_USER_ID, user.id);
            userFriend.put(Constants.KEY_NAME, user.name);
            userFriend.put(Constants.KEY_IMAGE, user.image);
            userFriend.put(Constants.KEY_EMAIL, user.email);

            database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, user.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteWait(myID, queryDocumentSnapshot.getId());
                    }
                }
            });

            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).whereEqualTo(Constants.KEY_USER_ID, myID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        deleteRequest(user.id, queryDocumentSnapshot.getId());
                    }
                }
            });

            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);

            binding.btnDeleteRequest.setVisibility(View.GONE);
            binding.btnMessageDeleteRequest.setVisibility(View.GONE);

            binding.btnAcceptRequest.setVisibility(View.GONE);
            binding.btnMessageAcceptRequest.setVisibility(View.GONE);

            binding.btnDeleteAdd.setVisibility(View.GONE);
            binding.btnMessageDeleteAdd.setVisibility(View.GONE);

        });

    }

    private void deleteRequest(String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(myID).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteRequest(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_REQUEST_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteWait(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_WAIT_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void deleteFriend(String id, String string) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_FRIENDS).document(string).delete().addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });
    }

    private void hashMyUser(String id, String name, String imgag, String email) {
        HashMap<String, Object> myUser = new HashMap<>();
        myUser.put(Constants.KEY_USER_ID, myID);
        myUser.put(Constants.KEY_NAME, name);
        myUser.put(Constants.KEY_IMAGE, imgag);
        myUser.put(Constants.KEY_EMAIL, email);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(id).collection(Constants.KEY_COLLECTION_FRIENDS).add(myUser).addOnSuccessListener(documentReference -> {
        }).addOnFailureListener(exception -> {
            showToast(exception.getMessage());
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setData() {
        binding.image.setImageBitmap(getBitmapFromEncodedString(user.image));
        binding.name.setText(user.name);

        if (!getIntent().getStringExtra("you").isEmpty()) {
            if (getIntent().getStringExtra("you").equals("1")) {
                binding.btnAdd.setVisibility(View.GONE);
                binding.btnMessage.setVisibility(View.GONE);

                binding.btnDeleteRequest.setVisibility(View.GONE);
                binding.btnMessageDeleteRequest.setVisibility(View.GONE);

                binding.btnAcceptRequest.setVisibility(View.GONE);
                binding.btnMessageAcceptRequest.setVisibility(View.GONE);

                binding.btnDeleteAdd.setVisibility(View.GONE);
                binding.btnMessageDeleteAdd.setVisibility(View.GONE);

                binding.sup.setVisibility(View.GONE);

                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).get().addOnSuccessListener(documentReference -> {
                    binding.imageBackground.setImageBitmap(getBitmapFromEncodedString(documentReference.getString(Constants.KEY_IMAGE_BACKGROUND)));
                    binding.introduceYourself.setText(documentReference.getString(Constants.KEY_INTRODUCE_YOURSEFT));

                }).addOnFailureListener(exception -> {
                    showToast(exception.getMessage());
                });

                getPosts(preferenceManager.getString(Constants.KEY_USER_ID));
            } else {
                database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).get().addOnSuccessListener(documentReference -> {
                    binding.imageBackground.setImageBitmap(getBitmapFromEncodedString(documentReference.getString(Constants.KEY_IMAGE_BACKGROUND)));
                    binding.introduceYourself.setText(documentReference.getString(Constants.KEY_INTRODUCE_YOURSEFT));
                }).addOnFailureListener(exception -> {
                    showToast(exception.getMessage());
                });

                getPosts(user.id);
            }
        } else {
            database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).get().addOnSuccessListener(documentReference -> {
                binding.imageBackground.setImageBitmap(getBitmapFromEncodedString(documentReference.getString(Constants.KEY_IMAGE_BACKGROUND)));
                binding.introduceYourself.setText(documentReference.getString(Constants.KEY_INTRODUCE_YOURSEFT));
            }).addOnFailureListener(exception -> {
                showToast(exception.getMessage());
            });

            getPosts(user.id);
        }

    }

    private void setStatusBarColor() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.background));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }



    private void getPosts(String id) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_POSTS).whereEqualTo(Constants.KEY_POST_ID_AUTHOR, id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<PostStory> postStoryList = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    PostStory postStory = new PostStory();
                    postStory.idAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_ID_AUTHOR);
                    postStory.nameAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_NAME_AUTHOR);
                    postStory.imageAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE_AUTHOR);
                    postStory.emailAuthor = queryDocumentSnapshot.getString(Constants.KEY_POST_EMAIL_AUTHOR);
                    postStory.text = queryDocumentSnapshot.getString(Constants.KEY_POST_TEXT);
                    postStory.image = queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE);
                    postStory.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_POST_TIMESTAMP);

                    postStoryList.add(postStory);
                }
                setAdapterFriends(postStoryList);
            }
        });

    }

    private void setAdapterFriends(List<PostStory> postStoryList) {
        if (postStoryList.size() > 0) {
            Collections.sort(postStoryList, (user, t1) -> t1.dateObject.compareTo(user.dateObject));

            StoryPostAdapters storyPostAdapters = new StoryPostAdapters(postStoryList);
            binding.storyRecyclerView.setAdapter(storyPostAdapters);
            binding.storyRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}