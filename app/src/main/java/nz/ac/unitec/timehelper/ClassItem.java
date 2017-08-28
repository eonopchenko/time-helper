package nz.ac.unitec.timehelper;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.Map;

/**
 * Created by eugene on 28/08/2017.
 */

public class ClassItem {

    private String mTitle;
    private DocumentRevision rev;

    public String getTitle() {
        return mTitle;
    }

    public ClassItem(String title) {
        this.mTitle = title;
    }

    static ClassItem fromRevision(DocumentRevision rev) {
        Map<String, Object> map = rev.asMap();
        if(map.containsKey("description")) {
            ClassItem t = new ClassItem((String) map.get("description"));
            t.rev = rev;
            return t;
        }
        return null;
    }
}
