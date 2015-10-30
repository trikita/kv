package trikita.kv;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class KVFileStorageTest {
	@Test
	public void testStorage() {
		KV kv = new KV(new FileStorage(new File("test.bin")),
					new SerializedEncoder());
		kv.set("str", "hello world");
		kv.set("int", 1234);
		kv.set("int", 12);
		kv.set("num", 3.1415926f);
		kv.set("bool", true);
		kv.set("del", true);
		kv.set("del", null);
		kv.close();

		kv = new KV(new FileStorage(new File("test.bin")),
					new SerializedEncoder());
		Assert.assertEquals(kv.keys("").size(), 4);
		Assert.assertFalse(kv.keys("").contains("del"));

		String str = kv.get("str");
		Assert.assertEquals(str, "hello world");
		int i = kv.get("int");
		Assert.assertEquals(i, 12);

		float f = kv.get("num");
		Assert.assertEquals(f, 3.1415926f, 0.0000001f);

		boolean b = kv.get("bool");
		Assert.assertEquals(b, true);

		Assert.assertNull(kv.get("del"));
		kv.close();
	}
}
