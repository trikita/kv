package com.example.trikita.kv;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

import java.io.File;

import trikita.kv.FileStorage;
import trikita.kv.KV;
import trikita.kv.SerializedEncoder;

public class MainActivity extends Activity {

	static {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads()
				.detectDiskWrites()
				.detectAll()
				.penaltyLog()
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects()
				.penaltyLog()
				.penaltyDeath()
				.build());
	}

	private KV fileDB =
		new KV(new FileStorage(new File("/data/data/com.example.trikita.kv/kv.db")),
				new SerializedEncoder());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		fileDB.set("foo", "bar");
		String bar = fileDB.get("foo");
		System.out.println("Bar = " + bar);
	}
}
