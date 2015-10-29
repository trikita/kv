package trikita.kv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class KVEmptyTest {
	@Test
	public void testEmpty() {
		KV kv = new KV(new TestUtils.Storage(), new TestUtils.NullEncoder());
		Assert.assertEquals(kv.keys("").size(), 0);
		kv.set("foo", "bar").set("baz", 123);
		kv.set("foo", false);
		Assert.assertEquals(kv.keys("").size(), 2);
		Assert.assertTrue(kv.keys("").contains("foo"));
		Assert.assertTrue(kv.keys("").contains("baz"));
	}
}
