package poc.cevt.hmi.com.musicplayerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

class MenuListAdapter extends BaseAdapter {

    private Context context;
    private List<String> menuItems;

    MenuListAdapter(Context context, List<String> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false);
        }

        TextView menuItem = (TextView) convertView.findViewById(R.id.menuItem);
        menuItem.setText(menuItems.get(position));

        return convertView;
    }
}
