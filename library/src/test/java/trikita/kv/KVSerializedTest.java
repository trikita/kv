package trikita.kv;

import java.lang.Class;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;

public class KVSerializedTest {

	@Test
	public void testPrimitives() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		kv.set("str", "hello world");
		kv.set("int", 12);
		kv.set("num", 3.1415926f);
		kv.set("bool", true);
		Assert.assertEquals(kv.keys("").size(), 4);

		String str = kv.get("str");
		Assert.assertEquals(str, "hello world");
		int i = kv.get("int");
		Assert.assertEquals(i, 12);

		float f = kv.get("num");
		Assert.assertEquals(f, 3.1415926f, 0.0000001f);

		boolean b = kv.get("bool");
		Assert.assertEquals(b, true);
	}

	@Test
	public void testObject() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		kv.set("user:1", new TestUtils.User("John Doe", "Los Angeles", 90009));
		kv.set("user:2", new TestUtils.User("Jane Doe", "New York", 10001));
		Assert.assertEquals(kv.keys("").size(), 2);

		TestUtils.User john = kv.get("user:1");
		Assert.assertEquals(john.name, "John Doe");
		Assert.assertEquals(john.address.city, "Los Angeles");
		Assert.assertEquals(john.address.zip, 90009);

		TestUtils.User jane = kv.get("user:2");
		Assert.assertEquals(jane.name, "Jane Doe");
		Assert.assertEquals(jane.address.city, "New York");
		Assert.assertEquals(jane.address.zip, 10001);
	}

	@Test
	public void testArray() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		TestUtils.User[] users = new TestUtils.User[]{
			new TestUtils.User("John Doe", "Los Angeles", 90009),
			new TestUtils.User("Jane Doe", "New York", 10001),
		};
		kv.set("users", users);
		Assert.assertEquals(kv.keys("").size(), 1);

		TestUtils.User[] u = kv.get("users");
		Assert.assertEquals(u.length, 2);
		Assert.assertEquals(u[0].name, "John Doe");
		Assert.assertEquals(u[0].address.city, "Los Angeles");
		Assert.assertEquals(u[0].address.zip, 90009);
		Assert.assertEquals(u[1].name, "Jane Doe");
		Assert.assertEquals(u[1].address.city, "New York");
		Assert.assertEquals(u[1].address.zip, 10001);
	}

	@Test
	public void testMap() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		Map<String, TestUtils.User> users = new LinkedHashMap<String, TestUtils.User>();
		users.put("user:1", new TestUtils.User("John Doe", "Los Angeles", 90009));
		users.put("user:2", new TestUtils.User("Jane Doe", "New York", 10001));
		kv.set("users", users);
		Assert.assertEquals(kv.keys("").size(), 1);
		LinkedHashMap<String, TestUtils.User> u = kv.get("users");
		Assert.assertEquals(u.size(), 2);
		Assert.assertEquals(u.get("user:1").name, "John Doe");
		Assert.assertEquals(u.get("user:1").address.city, "Los Angeles");
		Assert.assertEquals(u.get("user:1").address.zip, 90009);
		Assert.assertEquals(u.get("user:2").name, "Jane Doe");
		Assert.assertEquals(u.get("user:2").address.city, "New York");
		Assert.assertEquals(u.get("user:2").address.zip, 10001);
	}

	@Test
	public void testQueue() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		Queue<TestUtils.User> q = new ArrayDeque<TestUtils.User>();
		q.offer(new TestUtils.User("John Doe", "Los Angeles", 90009));
		q.offer(new TestUtils.User("Jane Doe", "New York", 10001));
		kv.set("users", q);
		Assert.assertEquals(kv.keys("").size(), 1);
		ArrayDeque<TestUtils.User> users = kv.get("users");
		Assert.assertEquals(users.size(), 2);

		TestUtils.User u;
		u = q.poll();
		Assert.assertEquals(u.name, "John Doe");
		Assert.assertEquals(u.address.city, "Los Angeles");
		Assert.assertEquals(u.address.zip, 90009);
		u = q.poll();
		Assert.assertEquals(u.name, "Jane Doe");
		Assert.assertEquals(u.address.city, "New York");
		Assert.assertEquals(u.address.zip, 10001);
	}

	@Test
	public void testBitSet() {
		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		BitSet bs = new BitSet();
		bs.set(31);
		bs.set(1000);
		kv.set("bits", bs);
		Assert.assertEquals(kv.keys("").size(), 1);
		BitSet bits = kv.get("bits");
		Assert.assertTrue(bits != bs);
		Assert.assertEquals(bits.size(), bs.size());
		Assert.assertEquals(bits.get(0), false);
		Assert.assertEquals(bits.get(31), true);
		Assert.assertEquals(bits.get(1000), true);
	}

	@Test
	public void testCustomPair() {
		// Register custom type for JSON serialization
		GenericTypes.TYPES.put(TestUtils.Pair.class,
				new GenericTypes.Extractor<TestUtils.Pair>() {
			public Class[] getTypes(TestUtils.Pair pair) {
				return new Class[]{pair.first.getClass(), pair.second.getClass()};
			}
		});

		KV kv = new KV(new TestUtils.Storage(), new SerializedEncoder());
		TestUtils.Pair<String, TestUtils.User> user =
			new TestUtils.Pair<>("user",
					new TestUtils.User("John Doe", "Los Angeles", 90009));
		kv.set("user", user);

		TestUtils.Pair<String, TestUtils.User> pair = kv.get("user");
		Assert.assertEquals(pair.first, "user");
		Assert.assertEquals(pair.second.name, "John Doe");
		Assert.assertEquals(pair.second.address.city, "Los Angeles");
		Assert.assertEquals(pair.second.address.zip, 90009);
	}
}


