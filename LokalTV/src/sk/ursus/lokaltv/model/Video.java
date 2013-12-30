package sk.ursus.lokaltv.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {

	public String title;
	public String cathegory;
	public String url;
	public String imageUrl;
	public String videoUrl;
	public String timestamp;
	public String viewCount;
	public String desc;
	public ArrayList<RelatedVideo> relatedItems;

	public Video(String title, String desc, String cathegory, String url, String imageUrl, String videoUrl, String timestamp, String viewCount, ArrayList<RelatedVideo> relatedItems) {
		this.title = title;
		this.desc = desc;
		this.cathegory = cathegory;
		this.url = url;
		this.imageUrl = imageUrl;
		this.videoUrl = videoUrl;
		this.timestamp = timestamp;
		this.viewCount = viewCount;
		this.relatedItems = relatedItems;
	}

	/**
	 * Parcelable boilerplate
	 */
	public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {

		@Override
		public Video createFromParcel(Parcel source) {
			return new Video(source);
		}

		@Override
		public Video[] newArray(int size) {
			return new Video[size];
		}
	};

	public Video(Parcel source) {
		title = source.readString();
		desc = source.readString();
		cathegory = source.readString();
		url = source.readString();
		imageUrl = source.readString();
		videoUrl = source.readString();
		timestamp = source.readString();
		viewCount = source.readString();
		
		relatedItems = new ArrayList<RelatedVideo>();
		source.readTypedList(relatedItems, RelatedVideo.CREATOR);
		
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(desc);
		dest.writeString(cathegory);
		dest.writeString(url);
		dest.writeString(imageUrl);
		dest.writeString(videoUrl);
		dest.writeString(timestamp);
		dest.writeString(viewCount);
		dest.writeTypedList(relatedItems);
	}

	@Override
	public int describeContents() {
		return 0;
	}

}
