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

import com.squareup.picasso.Picasso;

public class NewsFeedAdapter extends ArrayAdapter<Video> {

	String[] mKeywords = new String[] {
			"Rytmaus", "Rytmausa", "Rytmausovi", "Tony", "Posledn� vety", "Chlapci", "Chlapcom", "Hrdinovia",
			"Mene�eris", "Mene�erisom", "Dovolenk�ris", "Ren�", "Ren�ho", "Ren�mu", "Pi�ta", "Pi�toviny",
			"K���iar �aman", "Roman", "Romana"
	};

	private LayoutInflater mInflater;
	private int mHeight;
	private DecelerateInterpolator mInterpolator;
	private SparseBooleanArray mAnimatedMap;

	public NewsFeedAdapter(Context context, List<Video> items) {
		super(context, -1, items);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mHeight = windowManager.getDefaultDisplay().getHeight();

		mAnimatedMap = new SparseBooleanArray();
		mInterpolator = new DecelerateInterpolator();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_feed_news, parent, false);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.titleTextView);
			holder.title.setTypeface(TypefaceUtils.get(getContext(), TypefaceUtils.ROBOTO_SLAB_REGULAR));
			holder.cathegory = (TextView) convertView.findViewById(R.id.cathegoryTextView);
			holder.meta = (TextView) convertView.findViewById(R.id.metaTextView);
			holder.desc = (TextView) convertView.findViewById(R.id.descTextView);
			holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Video video = getItem(position);
		holder.title.setText(video.title);
		holder.cathegory.setText(video.cathegory);
		holder.desc.setText(boldifyVideoDesc(video.desc));
		holder.meta.setText(Utils.makeMetaData(video.timestamp, video.viewCount));

		Picasso.with(getContext())
				.load(video.imageUrl)
				.placeholder(R.drawable.placeholder)
				.into(holder.imageView);

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
		public ImageView imageView;
	}

}
