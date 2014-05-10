package sk.ursus.lokaltv.model;


public class Category extends DrawerItem {

	public String url;

	public Category(CharSequence title, String url) {
		super(title);
		this.url = url;
	}

	@Override
	public boolean isSeparator() {
		return false;
	}

}
