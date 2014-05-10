package sk.ursus.lokaltv.adapter;

import java.util.List;

import sk.ursus.lokaltv.R;
import sk.ursus.lokaltv.model.Video;
import sk.ursus.lokaltv.util.TypefaceUtils;
import sk.ursus.lokaltv.util.Utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.squareup.picasso.Picasso;

public class FeedAdapter extends ArrayAdapter<Video> {

	String[] mKeywords = new String[] {
			"Rytmaus", "Rytmausa", "Rytmausovi", "Tony", "Posledné vety", "Chlapci", "Chlapcom", "Hrdinovia",
			"Menežeris", "Menežerisom", "Dovolenkáris", "René", "Reného", "Renému", "Pišta", "Pištoviny", "K¾úèiar Šaman"
	};

	public interface OnListNearEndListener {
		public void onListNearEnd();
	}

	private OnListNearEndListener mOnListNearEndListener;

	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private int mHeight;
	private DecelerateInterpolator mInterpolator;
	private SparseBooleanArray mAnimatedMap;

	public FeedAdapter(Context context, List<Video> items, ImageLoader imageLoader) {
		super(context, -1, items);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageLoader = imageLoader;

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mHeight = windowManager.getDefaultDisplay().getHeight();

		mAnimatedMap = new SparseBooleanArray();
		mInterpolator = new DecelerateInterpolator();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mOnListNearEndListener != null) {
			// if (position == mItems.size() - 1) {
			if (position == getCount() - 1) {
				// Is Near end
				mOnListNearEndListener.onListNearEnd();
			}
		}

		ViewHolder holder;
		if (convertView == null) {
			// convertView = mInflater.inflate(R.layout.item_feed, parent,
			// false);
			convertView = mInflater.inflate(R.layout.item_feed5, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);
			holder.title.setTypeface(TypefaceUtils.get(getContext(), TypefaceUtils.ROBOTO_SLAB_REGULAR));
			holder.cathegory = (TextView) convertView.findViewById(R.id.cathegoryTextView);
			holder.meta = (TextView) convertView.findViewById(R.id.metaTextView);
			holder.desc = (TextView) convertView.findViewById(R.id.descTextView);
			holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			/* holder.imageView = (NetworkImageView) convertView.findViewById(R.id.imageView);
			holder.imageView.setErrorImageResId(R.drawable.placeholder);
			holder.imageView.setShouldAnimate(true); */

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Video video = getItem(position);
		holder.title.setText(video.title);
		holder.cathegory.setText(video.cathegory);
		holder.desc.setText(boldifyVideoDesc(video.desc));

		// holder.imageView.setImageUrl(video.imageUrl, mImageLoader);
		holder.meta.setText(Utils.timeAgoInWords(video.timestamp, true) + " • "
				+ Utils.formatViewCount(video.viewCount));
		
		Picasso.with(getContext()).load(video.imageUrl).into(holder.imageView);

		/* if (!mAnimatedMap.get(position)) {
			animateGooglePlusSlideIn(convertView, position);
			mAnimatedMap.put(position, true);
		} */
		return convertView;
	}

	private SpannableString boldifyVideoDesc(String desc) {
		SpannableString spannableString = new SpannableString(desc);
		int index;
		for (String keyword : mKeywords) {
			index = desc.indexOf(keyword);
			if (index != -1) {
				spannableString.setSpan(
						new StyleSpan(Typeface.BOLD),
						index, index + keyword.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		return spannableString;
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
				.setDuration(500)
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
		public TextView desc;
		public TextView meta;
		// public NetworkImageView imageView;
		public ImageView imageView;
	}

}
