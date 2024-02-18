package com.app.voicechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.voicechat.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();


        //firebaseuser boş değilse direkt mainactivity'ye gönder
        if (auth.getCurrentUser()!=null){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        binding.showLogin.setOnClickListener(view -> {
            binding.loginLayout.setVisibility(View.VISIBLE);
            binding.registerLayout.setVisibility(View.GONE);

        });
        binding.showRegister.setOnClickListener(view -> {
            binding.loginLayout.setVisibility(View.GONE);
            binding.registerLayout.setVisibility(View.VISIBLE);

        });



        binding.btnLogin.setOnClickListener(view -> {
            if (binding.emailLogin.getText().toString().length()>0 && binding.passwordLogin.getText().toString().length()>0){
                login(binding.emailLogin.getText().toString(),binding.passwordLogin.getText().toString());
            }
        });

        binding.btnRegister.setOnClickListener(view -> {
            if (binding.usernameRegister.getText().toString().length()>0 && binding.emailRegister.getText().toString().length()>0
            && binding.passwordRegister.getText().toString().length()>0){
                register();
            }
        });




    }

    private void register(){
        auth.createUserWithEmailAndPassword(binding.emailRegister.getText().toString(),binding.passwordRegister.getText().toString()).addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                Map<String,Object> map = new HashMap<>();
                String userId =auth.getCurrentUser().getUid();

                map.put("username",binding.usernameRegister.getText().toString());
                map.put("email",binding.emailRegister.getText().toString());
                map.put("userId",userId);

                FirebaseDatabase.getInstance().getReference().child("Users").child(userId).setValue(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });

            }

        });
    }





    private void login(String email, String pass) {
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);



                    } else {

                        Toast.makeText(getApplicationContext(), "Error " + task.getException(),
                                Toast.LENGTH_SHORT).show();


                    }


                });


    }



}