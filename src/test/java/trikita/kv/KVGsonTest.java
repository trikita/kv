package trikita.kv;

import java.lang.Class;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class KVGsonTest {

	@Test
	public void testPrimitives() {
		KV kv = new KV(new TestUtils.Storage(), new GsonEncoder());
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
		KV kv = new KV(new TestUtils.Storage(), new GsonEncoder());
		kv.set("user:1", new User("John Doe", new Address("Los Angeles", 90009)));
		kv.set("user:2", new User("Jane Doe", new Address("New York", 10001)));
		Assert.assertEquals(kv.keys("").size(), 2);

		User john = kv.get("user:1");
		Assert.assertEquals(john.name, "John Doe");
		Assert.assertEquals(john.address.city, "Los Angeles");
		Assert.assertEquals(john.address.zip, 90009);

		User jane = kv.get("user:2");
		Assert.assertEquals(jane.name, "Jane Doe");
		Assert.assertEquals(jane.address.city, "New York");
		Assert.assertEquals(jane.address.zip, 10001);
	}

	@Test
	public void testArray() {
		KV kv = new KV(new TestUtils.Storage(), new GsonEncoder());
		User[] users = new User[]{
			new User("John Doe", new Address("Los Angeles", 90009)),
			new User("Jane Doe", new Address("New York", 10001)),
		};
		kv.set("users", users);
		Assert.assertEquals(kv.keys("").size(), 1);

		User[] u = kv.get("users");
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
		KV kv = new KV(new TestUtils.Storage(), new GsonEncoder());
		Map<String, User> users = new LinkedHashMap<String, User>();
		users.put("user:1", new User("John Doe", new Address("Los Angeles", 90009)));
		users.put("user:2", new User("Jane Doe", new Address("New York", 10001)));
		kv.set("users", users);
		Assert.assertEquals(kv.keys("").size(), 1);
		LinkedHashMap<String, User> u = kv.get("users");
		Assert.assertEquals(u.size(), 2);
		Assert.assertEquals(u.get("user:1").name, "John Doe");
		Assert.assertEquals(u.get("user:1").address.city, "Los Angeles");
		Assert.assertEquals(u.get("user:1").address.zip, 90009);
		Assert.assertEquals(u.get("user:2").name, "Jane Doe");
		Assert.assertEquals(u.get("user:2").address.city, "New York");
		Assert.assertEquals(u.get("user:2").address.zip, 10001);
	}

	@Test
	public void testCustomPair() {
		// Register custom type for JSON serialization
		GenericTypes.TYPES.put(Pair.class, new GenericTypes.Extractor<Pair>() {
			public Class[] getTypes(Pair pair) {
				System.out.println("getTypes " + pair.first.getClass() + " " +
					pair.second.getClass());
				return new Class[]{pair.first.getClass(), pair.second.getClass()};
			}
		});

		KV kv = new KV(new TestUtils.Storage(), new GsonEncoder());
		Pair<String, User> user = new Pair<>("user",
				new User("John Doe", new Address("Los Angeles", 90009)));
		kv.set("user", user);

		Pair<String, User> pair = kv.get("user");
		Assert.assertEquals(pair.first, "user");
		Assert.assertEquals(pair.second.name, "John Doe");
		Assert.assertEquals(pair.second.address.city, "Los Angeles");
		Assert.assertEquals(pair.second.address.zip, 90009);
	}

	// Test data classes

	private static class User {
		public final String name;
		public final Address address;
		public User(String name, Address addr) {
			this.name = name;
			this.address = addr;
		}
	}

	private static class Address {
		public final String city;
		public final int zip;
		public Address(String city, int zip) {
			this.city = city;
			this.zip = zip;
		}
	}

	private static class Pair<U, V> {
		public final U first;
		public final V second;
		public Pair(U u, V v) {
			this.first = u;
			this.second = v;
		}
	}
}

