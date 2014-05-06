package sk.ursus.lokaltv.adapter;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Category;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerAdapter extends ArrayAdapter<Category> {

	private LayoutInflater mInflater;

	public DrawerAdapter(Context context, Category[] categories) {
		super(context, -1, categories);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_drawer, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Category category = getItem(position);
		holder.title.setText(category.title);

		return convertView;
	}

	static class ViewHolder {
		public TextView title;
	}

}
