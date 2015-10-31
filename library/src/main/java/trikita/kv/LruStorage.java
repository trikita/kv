package trikita.kv;

import java.lang.InterruptedException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LruStorage extends LinkedHashMap<String, byte[]>
	implements KV.Storage {

	private final ExecutorService mService = Executors.newSingleThreadExecutor();
	private final KV.Storage mStorage;
	private final int mMaxSize;

	public LruStorage(KV.Storage storage, int size) {
		super(size/2, 0.75f, true);
		mStorage = storage;
		mMaxSize = size;
	}

	public void set(final String key, final byte[] value) {
		if (value == null) {
			this.remove(key, value);
		} else {
			this.put(key, value);
		}
		mService.submit(new Callable<Void>() {
			public Void call() {
				mStorage.set(key, value);
				return null;
			}
		});
	}

	public byte[] get(final String key) {
		byte[] value = super.get(key);
		if (value == null) {
			value = wait(new Callable<byte[]>() {
				public byte[] call() {
					byte[] b = mStorage.get(key);
					put(key, b);
					return b;
				}
			});
		}
		return value;
	}

	public Set<String> keys(final String mask) {
		return wait(new Callable<Set<String>>() {
			public Set<String> call() {
				return mStorage.keys(mask);
			}
		});
	}

	public void close() {
		wait(new Callable<Void>() {
			public Void call() {
				mStorage.close();
				return null;
			}
		});
		mService.shutdown();
	}

	protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
		return size() > mMaxSize;
	}

	private <T> T wait(Callable<T> c) {
		try {
			return mService.submit(c).get();
		} catch (InterruptedException|ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
