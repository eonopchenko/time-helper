package nz.ac.unitec.timehelper;

import android.graphics.Path;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.List;
import java.util.Map;

/**
 * Created by eugene on 28/08/2017.
 */

public class ClassItem {

    private String mStart;
    private String mDuration;
    private String mTitle;
    private double mLat;
    private double mLng;
    private String mVenue;
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

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public String getVenue() {
        return mVenue;
    }

    public ClassItem(String start, String duration, String title, double lat, double lng, String venue) {
        this.mStart = start;
        this.mDuration = duration;
        this.mTitle = title;
        this.mLat = lat;
        this.mLng = lng;
        this.mVenue = venue;
    }

    static ClassItem fromRevision(DocumentRevision rev, List<StudentClassItem> classes) {
        Map<String, Object> map = rev.asMap();

        if(map.containsKey("_id") &&
           map.containsKey("start") &&
           map.containsKey("duration") &&
           map.containsKey("title")) {

            boolean found = false;
            for(StudentClassItem classItem : classes) {
                if(classItem.getClassId().equals(map.get("_id"))) {
                    found = true;
                    break;
                }
            }

            if(found) {
                ClassItem t = new ClassItem(
                        (String) map.get("start"),
                        (String) map.get("duration"),
                        (String) map.get("title"),
                        Double.parseDouble((String) map.get("lat")),
                        Double.parseDouble((String) map.get("lng")),
                        (String) map.get("venue"));
                t.rev = rev;
                return t;
            }
        }
        return null;
    }
}
