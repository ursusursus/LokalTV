package sk.ursus.lokaltv.adapter;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.DrawerItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.awaboom.ursus.agave.LOG;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

	private LayoutInflater mInflater;

	public DrawerAdapter(Context context, DrawerItem[] items) {
		super(context, -1, items);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItem item = getItem(position);
		LOG.d("getView: " + position + "=" + item.isSeparator());

		getItemViewType(position);
		ViewHolder holder;
		if (convertView == null) {
			int layout = item.isSeparator() ? R.layout.item_drawer_separator : R.layout.item_drawer;
			convertView = mInflater.inflate(layout, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.title.setText(item.title);
		/* if (item.isSeparator()) {
			holder.title.setText(item.title);
		} else {
			Category category = (Category) item;
		} */

		return convertView;
	}

	static class ViewHolder {
		public TextView title;
	}

}
