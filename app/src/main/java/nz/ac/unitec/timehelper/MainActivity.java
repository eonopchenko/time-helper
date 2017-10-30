package nz.ac.unitec.timehelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;

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
                List<ClassItem> classes = sClasses.allClasses(userId);
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
}
