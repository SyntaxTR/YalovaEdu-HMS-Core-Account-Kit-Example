package com.example.yalovaedutest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;
    TextView tvResult;
    boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnLogin = findViewById(R.id.btnLogin);
        tvResult = findViewById(R.id.tvResult);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLogged){
                    HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
                    HuaweiIdAuthService service = HuaweiIdAuthManager.getService(MainActivity.this, authParams);
                    startActivityForResult(service.getSignInIntent(), 8888);
                }else{
                    Toast.makeText(getApplicationContext(), "Zaten hesabınıza giriş yapmışsınız.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Process the authorization result and obtain the authorization code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            authHuaweiIdTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // If the error returned is a ResolvableApiException instance, prompt the user to install or update HMS Core (APK).
                    if (e instanceof ResolvableApiException) {
                        ResolvableApiException apiException = (ResolvableApiException)e;
                        // Call pending intent in apiException to display the prompt. Pass requestCode to pending intent.
                        try {
                            apiException.startResolutionForResult(MainActivity.this, 8888);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            if (authHuaweiIdTask.isSuccessful()) {
                // The sign-in is successful, and the user's HUAWEI ID information and authorization code are obtained.
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                Log.i("TAG", "Authorization code:" + huaweiAccount.getAuthorizationCode());
                tvResult.setText(huaweiAccount.displayName);
                isLogged = true;
            } else {
                isLogged = false;
                // The sign-in failed.
                Log.e("TAG", "sign in failed : " + ((ApiException)authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }
}
