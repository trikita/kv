package trikita.kv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileStorage implements KV.Storage {

	private final File mFile;
	private final Map<String, byte[]> mCache = new HashMap<>();

	private DataOutputStream mStream;

	public FileStorage(final File f) {
		mFile = f;
	}

	private void lazyLoad() {
		if (mStream == null) {
			DataInputStream in = null;
			try {
				in = new DataInputStream(new FileInputStream(mFile));
				while (true) {
					int size;
					try {
						size = in.readInt();
					} catch (EOFException e) {
						break;
					}
					String key = in.readUTF();
					if (size == -1) {
						mCache.remove(key);
					} else {
						byte[] value = new byte[size];
						in.readFully(value);
						mCache.put(key, value);
					}
				}
			} catch (IOException err) {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {}
			}

			try {
				mStream = new DataOutputStream(new FileOutputStream(mFile, true));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void set(final String key, final byte[] value) {
		try {
			lazyLoad();
			if (value == null) {
				mCache.remove(key);
				mStream.writeInt(-1);
				mStream.writeUTF(key);
			} else {
				mCache.put(key, value);
				mStream.writeInt(value.length);
				mStream.writeUTF(key);
				mStream.write(value, 0, value.length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] get(String key) {
		lazyLoad();
		return mCache.get(key);
	}

	public Set<String> keys(String mask) {
		lazyLoad();
		return mCache.keySet();
	}

	public void close() {
		try {
			if (mStream != null) {
				mStream.flush();
				mStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
