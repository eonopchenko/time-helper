package nz.ac.unitec.timehelper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.ListView;
import android.widget.Toast;

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

    ClassModel sClasses;
    public ClassAdapter mClassAdapter;
    private ListView lstClass;

    private MFPPush push;
    private MFPPushNotificationListener notificationListener;

    private boolean mReplicationRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// Initialize BMSClient (https://console.bluemix.net/docs/cloudnative/sdk_BMSClient.html)
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

        /// Initialize put-request to Bluemix cloud
        Request request = new Request(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/timetable_db", Request.PUT);

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

        if(sClasses == null) {
            sClasses = new ClassModel(this.getApplicationContext());
        }
        sClasses.setReplicationListener(this);
        lstClass = (ListView) findViewById(R.id.lstClass);

        reloadClassesFromModel();

        /// Subscribe on push-notifications https://console.bluemix.net/docs/services/mobilepush/getting-started.html
        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), "1e1125cd-32ed-4e96-bb35-ab62cb806322", "cddc35ba-0793-4ebc-aee7-4f908f767c2e");

        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive(final MFPSimplePushNotification message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Received a Push Notification")
                                .setMessage(message.getAlert())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if(!mReplicationRequested) {
                                            sClasses.startPullReplication();
                                        }
                                    }
                                })
                                .show();
                    }
                });
            }
        };

        push.registerDeviceWithUserId("Sample UserID", new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                push.listen(notificationListener);
            }

            @Override
            public void onFailure(MFPPushException ex) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (push != null) {
            push.listen(notificationListener);
        }
        sClasses.startPullReplication();
        mReplicationRequested = true;
    }

    private void reloadClassesFromModel() {
        List<ClassItem> classes = sClasses.allClasses();
        this.mClassAdapter = new ClassAdapter(this, classes);
        lstClass.setAdapter(this.mClassAdapter);
    }

    void replicationComplete() {
        reloadClassesFromModel();
        mReplicationRequested = false;
        Toast.makeText(getApplicationContext(),
                R.string.replication_completed,
                Toast.LENGTH_LONG).show();
    }

    void replicationError() {
        reloadClassesFromModel();
    }
}
