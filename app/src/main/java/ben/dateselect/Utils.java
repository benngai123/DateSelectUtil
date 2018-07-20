package ben.dateselect;


import android.content.Context;
import android.content.res.Resources;

public class Utils {

	private static Context sContext;

	public Utils(Context context) {
		this.sContext = context;
	}

	public static Context getContext() {
		return sContext;
	}

	private static Resources getResource() {
		return getContext().getResources();
	}

	public static String[] getStringArray(int res) {
		return getResource().getStringArray(res);
	}

	public static String getString(int weekDay) {
		return null;
	}
}
