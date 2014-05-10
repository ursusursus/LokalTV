package sk.ursus.lokaltv.model;

public class DrawerItem {
	public CharSequence title;

	public DrawerItem(CharSequence title) {
		this.title = title;
	}

	public boolean isSeparator() {
		return true;
	}
}
