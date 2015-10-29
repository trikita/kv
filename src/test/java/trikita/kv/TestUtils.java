package trikita.kv;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class TestUtils {
	public final static class Storage implements KV.Storage {
		public final Map<String, byte[]> m = new HashMap<>();
		public void set(String key, byte[] value) {
			System.out.println("SET " + key + " " + new String(value));
			m.put(key, value);
		}
		public byte[] get(String key) {
			return m.get(key);
		}
		public Set<String> keys(String mask) {
			return m.keySet();
		}
	}

	public final static class NullEncoder implements KV.Encoder {
		public <T> byte[] encode(String key, T value) {
			return new byte[]{};
		}
		public <T> T decode(String key, byte[] data) {
			return null;
		}
	}
}
