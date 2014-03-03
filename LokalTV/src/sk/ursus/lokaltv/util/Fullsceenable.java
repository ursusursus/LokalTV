package sk.ursus.lokaltv.util;

import android.content.res.Configuration;
import android.view.View;

public interface Fullsceenable {
	
	static final int FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN // Removed status bar
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Prevents resizing after status bar is gone
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Removes nav bar
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION; // Prevents resizing after status nav is gone
	
	public void onConfigurationChanged(Configuration newConfig);
	public void enableFullscreen();
	public void disableFullscreen();

}
