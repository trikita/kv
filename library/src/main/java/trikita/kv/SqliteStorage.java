package trikita.kv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

public class SqliteStorage implements KV.Storage {

	private final SqliteHelper mHelper;

	public SqliteStorage(Context c, String table) {
		mHelper = new SqliteHelper(c, table);
	}

	public void set(final String key, final byte[] value) {
		mHelper.set(key, value);
	}

	public byte[] get(String key) {
		return mHelper.get(key);
	}

	public Set<String> keys(String mask) {
		return mHelper.keys();
	}

	public void close() {}

	private static class SqliteHelper extends SQLiteOpenHelper {

		private final static String DB_NAME = "kv";
		private final static String KEY = "k";
		private final static String VALUE = "v";
		private final static int VERSION = 1;
		private final String TABLE;

		public SqliteHelper(Context c, String table) {
			super(c, DB_NAME, null, VERSION);
			TABLE = table;
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE +
					" ( " + KEY + " text primary key not null, " +
					VALUE + " blob null, unique(" +
					KEY + "), on conflict replace);");
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

		public void set(String key, byte[] value) {
			SQLiteDatabase db = this.getWritableDatabase();
			if (value == null) {
				db.delete(TABLE, KEY + " = '" + key + "'", null);
			} else {
				ContentValues values = new ContentValues();
				values.put(KEY, key);
				values.put(VALUE, value);
				db.insertOrThrow(TABLE, null, values);
			}
			db.close();
		}

		public byte[] get(String key) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT " + VALUE + " FROM " +
					TABLE + " WHERE " + KEY + " = '" + key + "'", null);
			if (cursor == null) {
				return null;
			}
			cursor.moveToFirst();
			if (cursor.getCount() == 0) {
				return null;
			}
			byte[] value = cursor.getBlob(cursor.getColumnIndex(VALUE));
			db.close();
			return value;
		}

		public Set<String> keys() {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor =
				db.rawQuery("SELECT " + KEY + " FROM " + TABLE, null);
			if (cursor == null) {
				return null;
			}
			cursor.moveToFirst();
			Set<String> keys = new HashSet<>(cursor.getCount());
			while (!cursor.isAfterLast()) {
				String key = cursor.getString(cursor.getColumnIndex(KEY));
				keys.add(key);
				cursor.moveToNext();
			}
			cursor.close();
			db.close();
			return keys;
		}
	}
}

