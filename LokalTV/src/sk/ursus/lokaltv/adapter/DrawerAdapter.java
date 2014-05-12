package sk.ursus.lokaltv.adapter;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Category;
import sk.ursus.lokaltv.model.DrawerItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

	private static final int TYPE_SEPARATOR = 0;
	private static final int TYPE_CATEGORY = 1;

	private LayoutInflater mInflater;

	public DrawerAdapter(Context context, DrawerItem[] items) {
		super(context, -1, items);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (getItemViewType(position)) {
			case TYPE_CATEGORY: {
				ViewHolder holder;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_drawer, parent, false);
					holder = new ViewHolder();
					holder.title = (TextView) convertView.findViewById(R.id.titleTextView);

					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				Category category = (Category) getItem(position);
				holder.title.setText(category.title);
				break;
			}

			case TYPE_SEPARATOR: {
				ViewHolder holder;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_drawer_separator, parent, false);
					holder = new ViewHolder();
					holder.title = (TextView) convertView.findViewById(R.id.titleTextView);

					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				DrawerItem item = getItem(position);
				holder.title.setText(item.title);
				break;
			}

		}

		return convertView;
	}

	/**
	 * Overridovanim getViewTypeCount a getItemViewType
	 * sa garantuje vratenie korektneho view typu (layoutu)
	 * v getView
	 */
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 1 || position == 4 || position == 15) {
			return TYPE_SEPARATOR;
		} else {
			return TYPE_CATEGORY;
		}
	}
	
	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_CATEGORY;
	}

	static class ViewHolder {
		public TextView title;
	}

}
