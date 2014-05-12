package sk.ursus.lokaltv.adapter;

import java.util.List;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.util.TypefaceUtils;
import sk.ursus.lokaltv.util.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class FeedAdapter extends ArrayAdapter<Video> {

	public interface OnListNearEndListener {
		public void onListNearEnd();
	}

	private OnListNearEndListener mOnListNearEndListener;
	private LayoutInflater mInflater;

	public FeedAdapter(Context context, List<Video> items) {
		super(context, -1, items);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mOnListNearEndListener != null) {
			if (position == getCount() - 1) {
				mOnListNearEndListener.onListNearEnd();
			}
		}

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_feed, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);
			holder.title.setTypeface(TypefaceUtils.get(getContext(), TypefaceUtils.ROBOTO_SLAB_REGULAR));
			holder.meta = (TextView) convertView.findViewById(R.id.metaTextView);
			holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Video video = getItem(position);
		holder.title.setText(video.title);
		holder.meta.setText(Utils.makeMetaData(video.timestamp, video.viewCount));

		Picasso.with(getContext())
				.load(video.imageUrl)
				.placeholder(R.drawable.placeholder)
				.into(holder.imageView);

		return convertView;
	}

	public void setOnListNearEndListener(OnListNearEndListener listener) {
		mOnListNearEndListener = listener;
	}

	static class ViewHolder {
		public TextView title;
		public TextView meta;
		public ImageView imageView;
	}

}
