package nz.ac.unitec.timehelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

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

    private StudentClassModel sStudentClasses;
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
        sStudentClasses = new StudentClassModel(this.getApplicationContext());
        sClasses = new ClassModel(this.getApplicationContext());

        AppID.getInstance().initialize(getApplicationContext(), "382fc624-ad4d-461d-867c-99be7a1f2179", AppID.REGION_SYDNEY);
        Login(AppID.getInstance().getLoginWidget(), new LoginSuccessListener() {
            @Override
            public void onLoginSuccess(String userId) {

                activity.mUserId = userId;

                BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

                sStudentClasses.setReplicationListener(activity);
                reloadStudentClassesFromModel(userId);
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
        if(sStudentClasses != null) {
            sStudentClasses.startPullReplication();
        }

        if(sClasses != null) {
            sClasses.startPullReplication();
        }
    }

    private void reloadStudentClassesFromModel(final String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<StudentClassItem> studentClasses = sStudentClasses.allStudentClasses(userId);
                sClasses.setReplicationListener(activity);
                reloadClassesFromModel(studentClasses);
            }
        });
    }

    private void reloadClassesFromModel(final List<StudentClassItem> studentClasses) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ClassItem> classes = sClasses.allClasses(studentClasses);
                sClasses.setReplicationListener(activity);
                activity.mClassAdapter = new ClassAdapter(activity, classes);
                lstClass.setAdapter(activity.mClassAdapter);
                MapFragment frMap = (MapFragment) getFragmentManager().findFragmentById(R.id.frMap);
                frMap.onClassLocationAvailable(classes, activity);
            }
        });
    }

    void classReplicationComplete() {
        reloadStudentClassesFromModel(mUserId);
        Toast.makeText(getApplicationContext(),
                R.string.replication_completed,
                Toast.LENGTH_LONG).show();
    }

    void classReplicationError() {
    }

    void studentClassReplicationComplete() {
        reloadStudentClassesFromModel(mUserId);
        Toast.makeText(getApplicationContext(),
                R.string.replication_completed,
                Toast.LENGTH_LONG).show();
    }

    void studentClassReplicationError() {
    }
}
