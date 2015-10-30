package trikita.kv;

import java.io.Serializable;
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

	// Test data classes

	public static class User implements Serializable {
		public final String name;
		public final Address address;
		public User(String name, String city, int zip) {
			this.name = name;
			this.address = new Address(city, zip);
		}
	}

	public static class Address implements Serializable {
		public final String city;
		public final int zip;
		public Address(String city, int zip) {
			this.city = city;
			this.zip = zip;
		}
	}

	public static class Pair<U, V> implements Serializable {
		public final U first;
		public final V second;
		public Pair(U u, V v) {
			this.first = u;
			this.second = v;
		}
	}
}
