package com.example.trikita.kv;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import trikita.kv.FileStorage;
import trikita.kv.GsonEncoder;
import trikita.kv.KV;
import trikita.kv.LruStorage;
import trikita.kv.SerializedEncoder;
import trikita.kv.SharedPrefsStorage;
import trikita.kv.SqliteStorage;

public class MainActivity extends Activity {

	public final static int ITERATIONS = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Button button = (Button) findViewById(R.id.start_button);
		final TextView textView = (TextView) findViewById(R.id.results);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final StringBuilder sb = new StringBuilder();
				button.setEnabled(false);
				new AsyncTask<Void, Void, Void>() {
					public Void doInBackground(Void... args) {
						KV kv;

						kv = new KV(
							new SharedPrefsStorage(MainActivity.this, "kv"),
							new SerializedEncoder());
						benchKV("SharedPrefs/Serialized", kv, sb);
						publishProgress();

						kv = new KV(
							new SharedPrefsStorage(MainActivity.this, "kv"),
							new GsonEncoder());
						benchKV("SharedPrefs/Gson", kv, sb);
						publishProgress();

						kv = new KV(
							new SqliteStorage(MainActivity.this, "kv"),
							new SerializedEncoder());
						benchKV("Sqlite/Serialized", kv, sb);
						publishProgress();

						kv = new KV(
							new SqliteStorage(MainActivity.this, "kv"),
							new GsonEncoder());
						benchKV("Sqlite/Gson", kv, sb);
						publishProgress();

						kv = new KV(
							new LruStorage(new SqliteStorage(MainActivity.this, "kv"),
								ITERATIONS),
							new SerializedEncoder());
						benchKV("Sqlite+LRU/Serialized", kv, sb);
						publishProgress();

						kv = new KV(
							new LruStorage(new SqliteStorage(MainActivity.this, "kv"),
								ITERATIONS),
							new GsonEncoder());
						benchKV("Sqlite+LRU/Gson", kv, sb);
						publishProgress();

						Hawk.init(MainActivity.this)
							.setStorage(HawkBuilder.newSharedPrefStorage(MainActivity.this))
							.build();
						benchHawk("Hawk/SharedPrefs", sb);
						Hawk.clear();
						publishProgress();

						Hawk.init(MainActivity.this)
								.setStorage(HawkBuilder.newSqliteStorage(MainActivity.this))
								.build();
						benchHawk("Hawk/Sqlite", sb);
						publishProgress();
						Hawk.clear();
						return null;
					}

					public void onProgressUpdate(Void... values) {
						textView.setText(sb.toString());
					}
					public void onPostExecute(Void result) {
						button.setEnabled(true);
					}
				}.execute();
			}
		});
	}

	private void benchKV(String name, final KV kv, StringBuilder out) {
		out.append("=== TEST: " + name + "\n");
		out.append(bench("SET primitives", new Runnable() {
			public void run() {
				kv.set("foo", "bar");
				kv.set("bar", true);
				kv.set("baz", 123);
			}
		}, 3));

		out.append(bench("GET primitives", new Runnable() {
			public void run() {
				String s = kv.get("foo");
				boolean b = kv.get("bar");
				int n = kv.get("baz");
			}
		}, 3));

		out.append(bench("SET object", new Runnable() {
			public void run() {
				kv.set("user", new User("John", "Doe"));
			}
		}, 1));

		out.append(bench("GET object", new Runnable() {
			public void run() {
				User u = kv.get("user");
			}
		}, 1));

		final ArrayList<User> users = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			users.add(new User("John", "Doe"+i));
		}
		out.append(bench("SET list", new Runnable() {
			public void run() {
				kv.set("users", users);
			}
		}, 1));

		out.append(bench("GET list", new Runnable() {
			public void run() {
				ArrayList<User> u = kv.get("users");
			}
		}, 1));
		kv.close();
	}

	private void benchHawk(String name, StringBuilder out) {
		out.append("=== TEST: " + name + "\n");
		out.append(bench("SET primitives", new Runnable() {
			public void run() {
				Hawk.put("foo", "bar");
				Hawk.put("bar", true);
				Hawk.put("baz", 123);
			}
		}, 3));

		out.append(bench("GET primitives", new Runnable() {
			public void run() {
				String s = Hawk.get("foo");
				boolean b = Hawk.get("bar");
				int n = Hawk.get("baz");
			}
		}, 3));

		out.append(bench("SET object", new Runnable() {
			public void run() {
				Hawk.put("user", new User("John", "Doe"));
			}
		}, 1));

		out.append(bench("GET object", new Runnable() {
			public void run() {
				User u = Hawk.get("user");
			}
		}, 1));

		final ArrayList<User> users = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			users.add(new User("John", "Doe"+i));
		}
		out.append(bench("SET list", new Runnable() {
			public void run() {
				Hawk.put("users", users);
			}
		}, 1));

		out.append(bench("GET list", new Runnable() {
			public void run() {
				ArrayList<User> u = Hawk.get("users");
			}
		}, 1));
	}

	private String bench(String name, Runnable r, int ops) {
		long start = System.nanoTime();
		for (int i = 0; i < ITERATIONS; i++) {
			r.run();
		}
		long speed = (System.nanoTime() - start) / ITERATIONS;
		return name + ":" + speed/1000/ops + " us/op\n";
	}

	public static class User implements Serializable {
		public final String firstName;
		public final String lastName;
		public User(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
	}
}
