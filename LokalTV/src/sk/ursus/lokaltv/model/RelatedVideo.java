package sk.ursus.lokaltv.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RelatedVideo implements Parcelable {

	public String title;
	public String url;
	public String imageUrl;
	public String timestamp;

	public RelatedVideo(String title, String url, String imageUrl, String timestamp) {
		this.title = title;
		this.url = url;
		this.imageUrl = imageUrl;
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "\nTitle:" + title + "\nUrl: " + url + "\nImageUrl: " + imageUrl + "\nTimestamp: " + timestamp;
	}

	/**
	 * Parcelable boilerplate
	 */
	public static final Parcelable.Creator<RelatedVideo> CREATOR = new Parcelable.Creator<RelatedVideo>() {

		@Override
		public RelatedVideo createFromParcel(Parcel source) {
			return new RelatedVideo(source);
		}

		@Override
		public RelatedVideo[] newArray(int size) {
			return new RelatedVideo[size];
		}
	};

	public RelatedVideo(Parcel source) {
		title = source.readString();
		url = source.readString();
		imageUrl = source.readString();
		timestamp = source.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(url);
		dest.writeString(imageUrl);
		dest.writeString(timestamp);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
