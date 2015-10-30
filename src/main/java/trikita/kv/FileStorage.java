package trikita.kv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileStorage implements KV.Storage {

	private final Map<String, byte[]> mCache = new HashMap<>();
	private final DataOutputStream mStream;

	public FileStorage(File f) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(f));
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
			mStream = new DataOutputStream(new FileOutputStream(f, true));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void set(String key, byte[] value) {
		try {
			if (value == null) {
				mCache.remove(key, value);
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
		return mCache.get(key);
	}

	public Set<String> keys(String mask) {
		return mCache.keySet();
	}

	public void close() {
		try {
			mStream.flush();
			mStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
