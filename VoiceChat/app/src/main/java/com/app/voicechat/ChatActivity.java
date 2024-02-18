package com.app.voicechat;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.app.voicechat.databinding.ActivityChatBinding;
import com.app.voicechat.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MediaRecorder mRecorder;
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private FirebaseUser firebaseUser;
    private ChatAdapter chatAdapter;
    private ArrayList<Chat> chatArrayList;

    private User user;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = (User) getIntent().getSerializableExtra("user");
        //gönderilen user nesnesini intent ile alır

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        initRecycler();

        getChats();

        binding.recordBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startRecording();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                pauseRecording();
            }
            return false;
        });

    }

    private void initRecycler(){
        //liste oluşturma, recyclerview tanımalama ve adapter atama vs.
        chatArrayList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter(chatArrayList,this);
        binding.recyclerView.setAdapter(chatAdapter);

    }

    private void getChats(){
        //mesajları listeleme. Chats child'ı altında tutulur ve gönderen ve alıcı kimliğine göre listeye eklenir
        FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        //Model dönüşümü
                        Chat chat = ds.getValue(Chat.class);
                        if (chat!=null){
                            if (chat.getSenderId().equals(firebaseUser.getUid()) && chat.getReceiverId().equals(user.getUserId())
                            || chat.getReceiverId().equals(firebaseUser.getUid()) && chat.getSenderId().equals(user.getUserId())){
                                chatArrayList.add(chat);

                            }
                        }

                    }
                    chatAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void startRecording() {
        if (checkPermissions()) {

            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/AudioRecording.3gp";

            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mRecorder.setOutputFile(mFileName);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            mRecorder.start();


        } else {
            requestPermissions();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                    } else {
                    }
                }
                break;
        }
    }

    public boolean checkPermissions() {
        //İzin verilip verilmediğinin kontrolü
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    public void pauseRecording() {

        mRecorder.stop();

        mRecorder.release();
        mRecorder = null;

        //ses kaydı durduğunda veri tabanına kaydetme işlemi başlar.
        //önce storage'a kaydedilir ve işlem başarılı olursa database'e yazılır

        StorageReference audioRef = FirebaseStorage.getInstance().getReference().child(System.currentTimeMillis() + "");
        UploadTask uploadTask = audioRef.putFile(Uri.fromFile(new File(mFileName)));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    String id = String.valueOf(UUID.randomUUID());

                    //Map oluşturmak yerine Chat nesnesi oluşturularak da kaydedilebilir.

                    Map<String,Object> map = new HashMap<>();
                    map.put("senderId",firebaseUser.getUid());
                    map.put("receiverId",user.getUserId());
                    map.put("url", downloadUrl);
                    map.put("time",System.currentTimeMillis()+"");
                    map.put("id",id);

                    FirebaseDatabase.getInstance().getReference().child("Chats").child(id).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ChatActivity.this, "gönderildi", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "hata oldu", Toast.LENGTH_SHORT).show());
                });
            }
        }).addOnFailureListener(e -> {
        });


    }

}
