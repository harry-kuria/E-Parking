package com.example.e_parking.ui.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.e_parking.NormalUser.MainNormalActivity;
import com.example.e_parking.OwnerUser.MainOwnerActivity;
import com.example.e_parking.R;
import com.example.e_parking.RegisterLogin.RegisterActivity;
import com.example.e_parking.classes.User;
import com.example.e_parking.databinding.ActivityLoginBinding;
import com.example.e_parking.utils.BasicUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private EditText email;
    private EditText password;
    private Button loginBtn;
    private TextView forgotPasswordText,registerSwitchText;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    ProgressDialog progressDialog;
    //private Object utils;

    BasicUtils utils=new BasicUtils();

    private User userObj;

    public User getUserObj(){
        return userObj;
    }

    public void setUserObj(User userObj){
        this.userObj=userObj;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initComponents();
        attachListeners();
        if(!utils.isNetworkAvailable(getApplication())){
            Toast.makeText(LoginActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.emailField;
        final EditText passwordEditText = binding.passwordField;
        final Button loginButton = binding.loginBtn;

        Intent in = new Intent();
        String prevEmail = in.getStringExtra("EMAIL");
        email.setText(prevEmail);
        email.setSelection(email.getText().length());

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();

        //final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                //loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadingProgressBar.setVisibility(View.VISIBLE);
                /*loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()); */
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                if(TextUtils.isEmpty(txt_email)){
                    Toast.makeText(LoginActivity.this,"Email can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(txt_password)){
                    Toast.makeText(LoginActivity.this,"Password can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(utils.isNetworkAvailable(getApplication())){
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Signing-in...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    loginUser(txt_email,txt_password);
                }else{
                    Toast.makeText(LoginActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
   //     String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
     //   Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void initComponents() {
        Intent in = getIntent();
        String prevEmail = in.getStringExtra("EMAIL");
        email=findViewById(R.id.emailField);
        password=findViewById(R.id.passwordField);
        loginBtn=findViewById(R.id.loginBtn);
        registerSwitchText=findViewById(R.id.registerSwitchText);
        forgotPasswordText=findViewById(R.id.forgotPasswordText);

        email.setText(prevEmail);
        email.setSelection(email.getText().length());

        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
    }


    private void attachListeners() {
      /*  loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                if(TextUtils.isEmpty(txt_email)){
                    Toast.makeText(LoginActivity.this,"Email can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(txt_password)){
                    Toast.makeText(LoginActivity.this,"Password can't be blank!",Toast.LENGTH_SHORT).show();
                }else if(utils.isNetworkAvailable(getApplication())){
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Signing-in...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    loginUser(txt_email,txt_password);
                }else{
                    Toast.makeText(LoginActivity.this, "No Network Available!", Toast.LENGTH_SHORT).show();
                }
            }
        }); */

        registerSwitchText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        forgotPasswordText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String passEmail=email.getText().toString();
                Intent intent=new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                if(!passEmail.isEmpty()){
                    intent.putExtra("EMAIL",passEmail);
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
    private void loginUser(String email, String password) {
//        final AppConstants globalClass=(AppConstants)getApplicationContext();
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if(!Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()){
                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(0);
                        Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        auth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Verification email sent to " + auth.getCurrentUser().getEmail()+"!", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            try{ progressDialog.dismiss();
                                            }catch (Exception e){ e.printStackTrace();}
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to send verification email!", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            try{ progressDialog.dismiss();
                                            }catch (Exception e){ e.printStackTrace();}
                                        }
                                    }
                                });
                    }else{
                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("isVerified").setValue(1);
                        db.getReference("Users").child(auth.getCurrentUser().getUid()).child("email").setValue(auth.getCurrentUser().getEmail());
                        db.getReference().child("Users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User userObj=snapshot.getValue(User.class);
                                setUserObj(userObj);
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if(userObj.userType==2)
                                    intent=new Intent(LoginActivity.this, MainOwnerActivity.class);
                                else
                                    intent=new Intent(LoginActivity.this, MainNormalActivity.class);
                                intent.putExtra("FRAGMENT_NO", 0);
                                try{ progressDialog.dismiss();
                                }catch (Exception e){ e.printStackTrace();}
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                try{ progressDialog.dismiss();
                                }catch (Exception e){ e.printStackTrace();}
                            }
                        });
                    }
                }else{
                    try{ progressDialog.dismiss();
                    }catch (Exception e){ e.printStackTrace();}
                    try {
                        throw task.getException(); // if user enters wrong email.
                    }catch (FirebaseAuthInvalidCredentialsException invalid) {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                        Log.d(String.valueOf(LoginActivity.this.getClass()), "onComplete: Invalid Credentials");
                    } catch (Exception e) {
                        Log.d(String.valueOf(LoginActivity.this.getClass()), "onComplete: " + e.getMessage());
                        e.printStackTrace();
                        // TODO: some work
                    }
                }
            }
        });
    }
}