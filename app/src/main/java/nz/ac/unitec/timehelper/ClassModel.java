package nz.ac.unitec.timehelper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.event.Subscribe;
import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eugene on 28/08/2017.
 */

public class ClassModel {

    private Context mContext;
    private MainActivity mListener;
    private java.net.URI cloudantUri;
    private Datastore ds;
    private Replicator mPullReplicator;
    private final Handler mHandler;

    ClassModel(Context context) {
        mContext = context;
        java.io.File path = mContext.getDir("datastores", android.content.Context.MODE_PRIVATE);
        DatastoreManager manager = DatastoreManager.getInstance(path);

        try {
            cloudantUri = new java.net.URI(mContext.getResources().getString(R.string.cloudantUrl) + "/lecturer_timetable_db");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            ds = manager.openDatastore("class_ds");
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        }

        mPullReplicator = ReplicatorBuilder.pull().to(ds).from(cloudantUri).build();
        mPullReplicator.getEventBus().register(this);

        mHandler = new Handler(Looper.getMainLooper());
    }

    List<ClassItem> allClasses(List<StudentClassItem> studentClasses) {
        int nDocs = this.ds.getDocumentCount();
        List<DocumentRevision> all = this.ds.getAllDocuments(0, nDocs, true);
        List<ClassItem> classes = new ArrayList<>();

        for(DocumentRevision rev : all) {
            ClassItem t = ClassItem.fromRevision(rev, studentClasses);
            if (t != null) {
                classes.add(t);
            }
        }

        return classes;
    }

    void startPullReplication() {
        mPullReplicator.start();
    }

    @Subscribe
    public void complete(ReplicationCompleted rc) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.classReplicationComplete();
                }
            }
        });
    }

    @Subscribe
    public void error(ReplicationErrored re) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.classReplicationError();
                }
            }
        });
    }

    void setReplicationListener(MainActivity listener) {
        this.mListener = listener;
    }
}
