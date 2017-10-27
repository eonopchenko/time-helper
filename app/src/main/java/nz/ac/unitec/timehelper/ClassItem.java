package nz.ac.unitec.timehelper;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.Map;

/**
 * Created by eugene on 28/08/2017.
 */

public class ClassItem {

    private String mStart;
    private String mDuration;
    private String mTitle;
    private DocumentRevision rev;

    public String getStart() {
        return mStart;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getTitle() {
        return mTitle;
    }

    public ClassItem(String start, String duration, String title) {
        this.mStart = start;
        this.mDuration = duration;
        this.mTitle = title;
    }

    static ClassItem fromRevision(DocumentRevision rev, String email) {
        Map<String, Object> map = rev.asMap();

        if(map.containsKey("start") &&
           map.containsKey("duration") &&
           map.containsKey("title") &&
           map.containsKey("user") &&
           map.get("user").equals(email)) {
            ClassItem t = new ClassItem((String) map.get("start"), (String) map.get("duration"), (String) map.get("title"));
            t.rev = rev;
            return t;
        }
        return null;
    }
}
