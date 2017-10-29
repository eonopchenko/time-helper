package nz.ac.unitec.timehelper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.ListView;
import android.widget.Toast;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static MainActivity activity;

    private ClassModel sClasses;
    private ClassAdapter mClassAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        AppID.getInstance().initialize(getApplicationContext(), "382fc624-ad4d-461d-867c-99be7a1f2179", AppID.REGION_SYDNEY);
        Login(AppID.getInstance().getLoginWidget(), new LoginSuccessListener() {
            @Override
            public void onLoginSuccess(String userId) {
                BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

                sClasses = new ClassModel(activity.getApplicationContext());
                sClasses.setReplicationListener(activity);
                List<ClassItem> classes = sClasses.allClasses("eugene.onopchenko@bduncanltd.com");
//                activity.mClassAdapter = new ClassAdapter(activity, classes);
                MapFragment frMap = (MapFragment) getFragmentManager().findFragmentById(R.id.frMap);
                frMap.onClassLocationAvailable(classes, activity);
            }
        });
    }

    private void Login(LoginWidget loginWidget, final LoginSuccessListener loginSuccessListener) {

        loginWidget.launch(this, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure (AuthorizationException exception) {
            }

            @Override
            public void onAuthorizationCanceled () {
            }

            @Override
            public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken) {
                loginSuccessListener.onLoginSuccess(identityToken.getEmail());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// Does not invoked on the app launch
        if(sClasses != null) {
            sClasses.startPullReplication();
        }
    }
//
//    void replicationComplete() {
//        reloadClassesFromModel(mEmail);
//        mReplicationRequested = false;
//        Toast.makeText(getApplicationContext(),
//                R.string.replication_completed,
//                Toast.LENGTH_LONG).show();
//    }
}
