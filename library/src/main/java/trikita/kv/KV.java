package trikita.kv;

import java.util.Set;

public final class KV {

	public interface Storage {
		public void set(String key, byte[] value);
		public byte[] get(String key);
		public Set<String> keys(String mask);
		public void close();
	}

	public interface Encoder {
		public <T> byte[] encode(String key, T value);
		public <T> T decode(String key, byte[] data);
	}

	private final Storage mStorage;
	private final Encoder mEncoder;

	public KV(Storage storage, Encoder enc) {
		mEncoder = enc;
		mStorage = storage;
	}

	public <T> KV set(final String key, final T value) {
		if (value == null) {
			mStorage.set(key, null);
			return this;
		}
		mStorage.set(key, mEncoder.encode(key, value));
		return this;
	}

	public <T> T get(final String key) {
		byte[] data = mStorage.get(key);
		if (data == null) {
			return null;
		}
		return mEncoder.decode(key, data);
	}

	public Set<String> keys(final String mask) {
		return mStorage.keys(mask);
	}

	public void close() {
		mStorage.close();
	}

	// TODO rlock(), lock(), unlock, runlock()
	// TODO storage directory with plain files
	// TODO transform for gzip
	// TODO transform for base64
	// TODO transform for aes
	// TODO encoder for jackson
}
