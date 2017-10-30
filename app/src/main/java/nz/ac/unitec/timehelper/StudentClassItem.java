package nz.ac.unitec.timehelper;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.Map;

/**
 * Created by eugene on 31/10/2017.
 */

public class StudentClassItem {

    private String mClassId;
    private DocumentRevision rev;

    public String getClassId() {
        return mClassId;
    }

    public StudentClassItem(String classId) {
        this.mClassId = classId;
    }

    static StudentClassItem fromRevision(DocumentRevision rev, String userId) {
        Map<String, Object> map = rev.asMap();

        if(
                map.containsKey("studentId") &&
                map.containsKey("classId") &&
                map.get("studentId").equals(userId)) {
            StudentClassItem t = new StudentClassItem((String) map.get("classId"));
            t.rev = rev;
            return t;
        }
        return null;
    }
}
