package trikita.kv;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.util.Set;

public class SharedPrefsStorage implements KV.Storage {
	private final SharedPreferences mPrefs;

	public SharedPrefsStorage(Context c, String name) {
    mPrefs = c.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public void set(final String key, final byte[] value) {
		String s = Base64.encodeToString(value, Base64.DEFAULT);
    mPrefs.edit().putString(key, s).apply();
	}

	public byte[] get(String key) {
		return Base64.decode(mPrefs.getString(key, ""), Base64.DEFAULT);
	}

	public Set<String> keys(String mask) {
		return mPrefs.getAll().keySet();
	}

	public void close() {
		mPrefs.edit().commit();
	}
}

