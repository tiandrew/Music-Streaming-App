package poc.cevt.hmi.com.musicplayerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

class StringListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<String> strings;

    StringListAdapter(Context context, ArrayList<String> strings) {
        this.strings = strings;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.string_list_item, parent, false);

        TextView item = (TextView) layout.findViewById(R.id.itemText);
        item.setText(strings.get(position));

        return layout;
    }
}
