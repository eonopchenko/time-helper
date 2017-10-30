package nz.ac.unitec.timehelper;

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

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static MainActivity activity;

    private ClassModel sClasses;
    private ClassAdapter mClassAdapter;
    private ListView lstClass;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        lstClass = (ListView) findViewById(R.id.lvClassList);
        sClasses = new ClassModel(this.getApplicationContext());

        AppID.getInstance().initialize(getApplicationContext(), "382fc624-ad4d-461d-867c-99be7a1f2179", AppID.REGION_SYDNEY);
        Login(AppID.getInstance().getLoginWidget(), new LoginSuccessListener() {
            @Override
            public void onLoginSuccess(String userId) {

                activity.mUserId = userId;

                BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

                /// Initialize put-request to Bluemix cloud
                Request request = new Request(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/lecturer_timetable_db", Request.PUT);

                /// Get auth string and convert to UTF-8
                byte[] byteAuth = new byte[0];
                String byteString = getString(R.string.cloudantUsername) + ":" + getString(R.string.cloudantPassword);
                try {
                    byteAuth = byteString.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                /// Convert auth string to Base64 scheme
                String base64Auth = Base64.encodeToString(byteAuth, Base64.NO_WRAP);

                /// Set request headers
                HashMap headers = new HashMap();
                List<String> authorization = new ArrayList<>();
                authorization.add("Basic " + base64Auth);
                headers.put("authorization", authorization);
                request.setHeaders(headers);

                /// Send request
                request.send(getApplicationContext(), new ResponseListener() {
                    @Override
                    public void onSuccess(Response response) {
                    }

                    @Override
                    public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                    }
                });

                sClasses.setReplicationListener(activity);

                reloadClassesFromModel(userId);
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

    private void reloadClassesFromModel(final String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ClassItem> classes = sClasses.allClasses(userId);
                activity.mClassAdapter = new ClassAdapter(activity, classes);
                lstClass.setAdapter(activity.mClassAdapter);
                MapFragment frMap = (MapFragment) getFragmentManager().findFragmentById(R.id.frMap);
                frMap.onClassLocationAvailable(classes, activity);
            }
        });
    }

    void replicationComplete() {
        reloadClassesFromModel(mUserId);
        Toast.makeText(getApplicationContext(),
                R.string.replication_completed,
                Toast.LENGTH_LONG).show();
    }

    void replicationError() {
        reloadClassesFromModel(mUserId);
    }
}
