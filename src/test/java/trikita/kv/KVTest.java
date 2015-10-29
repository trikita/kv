package trikita.kv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class KVTest {

	private static class DummyStorage implements KV.Storage {
		private Map<String, byte[]> m = new HashMap<>();
		public void set(String key, byte[] value) {
			System.out.println("SET " + key + " " + new String(value));
			m.put(key, value);
		}
		public byte[] get(String key) {
			System.out.println("GET " + key);
			return m.get(key);
		}
		public List<String> keys(String mask) {
			return new ArrayList<String>();
		}
	}

	private static class Model {
		String a;
		int[] b;
		public String toString() {
			String bs = "";
			for (int i : b) {
				bs = bs + i + " ";
			}
			return "Model " + a + " " + bs;
		}
	}

	@Test
	public void testFoo() {
		KV kv = new KV(new DummyStorage(), new GsonEncoder());
		kv.set("foo", "bar");

		Model m = new Model();
		m.a = "modelName";
		m.b = new int[]{1, 3, 5, 7, 11};
		kv.set("model", m);

		kv.set("numbers", new int[]{2, 4, 6});
		List<Integer> items = new ArrayList<>();
		items.add(2);
		items.add(4);
		items.add(6);
		kv.set("numberList", items);

		List<Model> models = new ArrayList<>();
		models.add(m);
		models.add(m);
		kv.set("models", models);

		System.out.println(""+kv.get("foo"));
		System.out.println(""+kv.get("model"));
		int[] n = kv.get("numbers");
		System.out.println(""+n + " " + n.length);
		System.out.println(""+kv.get("numberList"));
		System.out.println(""+kv.get("models"));
	}
}
