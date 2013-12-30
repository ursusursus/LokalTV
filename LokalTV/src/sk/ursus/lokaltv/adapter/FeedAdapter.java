package sk.ursus.lokaltv.adapter;

import java.util.List;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Video;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class FeedAdapter extends ArrayAdapter<Video> {

	public interface OnListNearEndListener {
		public void onListNearEnd();
	}

	private OnListNearEndListener mOnListNearEndListener;

	private List<Video> mItems;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private int mHeight;
	private DecelerateInterpolator mInterpolator;
	private SparseBooleanArray mAnimatedMap;

	public FeedAdapter(Context context, List<Video> items, ImageLoader imageLoader) {
		super(context, -1, items);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageLoader = imageLoader;
		mItems = items;

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mHeight = windowManager.getDefaultDisplay().getHeight();

		mAnimatedMap = new SparseBooleanArray();
		mInterpolator = new DecelerateInterpolator();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mOnListNearEndListener != null) {
			if (position == mItems.size() - 1) {
				// Is Near end 
				mOnListNearEndListener.onListNearEnd();
			}
		}

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_feed, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);
			holder.cathegory = (TextView) convertView.findViewById(R.id.cathegoryTextView);
			holder.imageView = (NetworkImageView) convertView.findViewById(R.id.imageView);
			holder.imageView.setErrorImageResId(R.drawable.image_stub);
			holder.imageView.setShouldAnimate(true);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Video feedItem = mItems.get(position);
		holder.title.setText(feedItem.title);
		holder.cathegory.setText(feedItem.cathegory);
		holder.imageView.setImageUrl(feedItem.imageUrl, mImageLoader);

		/* if (!mAnimatedMap.get(position)) {
			animateGooglePlusSlideIn(convertView, position);
			mAnimatedMap.put(position, true);
		} */
		return convertView;
	}

	public void setOnListNearEndListener(OnListNearEndListener listener) {
		mOnListNearEndListener = listener;
	}

	@SuppressLint("NewApi")
	private void animateGooglePlusSlideIn(View view, final int position) {
		view.setTranslationX(0.0F);
		view.setTranslationY(mHeight);
		view.setRotationX(45.0F);
		view.setScaleX(0.7F);
		view.setScaleY(0.55F);

		view.animate()
				.rotationX(0.0F)
				.rotationY(0.0F)
				.translationX(0)
				.translationY(0)
				.setDuration(1000)
				.scaleX(1.0F)
				.scaleY(1.0F)
				.setInterpolator(mInterpolator);
		/* .withEndAction(new Runnable() {
			
			@Override
			public void run() {
				mAnimatedMap.put(position, true);
			}
		}); */
	}

	static class ViewHolder {
		public TextView title;
		public TextView cathegory;
		public NetworkImageView imageView;
	}

}
