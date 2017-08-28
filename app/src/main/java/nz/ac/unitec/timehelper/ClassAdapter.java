package nz.ac.unitec.timehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by eugene on 28/08/2017.
 */

public class ClassAdapter extends BaseAdapter implements ListAdapter {

    private final Context context;
    private final List<ClassItem> classes;

    public ClassAdapter(Context context, List<ClassItem> classes) {
        this.context = context;
        this.classes = classes;
    }

    @Override
    public int getCount() {
        return classes.size();
    }

    @Override
    public Object getItem(int position) {
        return classes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.class_item, parent, false);
        }

        TextView desc = (TextView) convertView.findViewById(R.id.txtClassTitle);
        CheckBox completed = (CheckBox) convertView.findViewById(R.id.cbClass);

        ClassItem t = this.classes.get(position);
        desc.setText(t.getTitle());
        completed.setChecked(false);
        completed.setId(position);

        return convertView;
    }
}
