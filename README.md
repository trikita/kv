# kv

The most simple Key-Value store for Android and Java.

## Usage

```java
// Create new store using the given storage and encoder implementation
KV kv = new KV(new FileStorage(new File("store.kv")), new SerializedEncoder());
// Put keys into the store
kv.set("user:name", "John Doe");
kv.set("user:age", 42);
// Get keys from the store
String name = kv.get("user:name");
int age = kv.get("user:age");
// Iterate over the keys with some prefix
for (String s : kv.keys("user:")) {

}
kv.close();
```

KV is just a tiny wrapper over your storage and encoder implementations.  When
you want to put the value into the store - it is encoded and then raw bytes are
written into the storage. When you want to fetch the value - the raw bytes are
read from the storage and decoded.

## Storage

```java
public interface Storage {
	void set(String key, byte[] value);
	byte[] get(String key);
	Set<String> keys(String mask);
	void close();
}
```

Implementations:

* FileStorage(File f) - writes data into an append-only file. This storage also
  has an in-memory cache keeping all the values.
* SharedPrefsStorage(Context c, String name) - Android only, writes each value
	as a separate storage key of BASE64-encoded raw data. SharedPreferences used
	to have internal in-memory cache, too, so access if really fast.
* SqliteStorage(Context c, String table) - Android only, writes each value as a
	separate row in a given table. This storage uses no caching by default.
* LruStorage(Storage backend, int size) - wraps an existing storage with an LRU
	cache of the given size. Writes are delegated to the backend storage in a
	lazy manner, reads try to happen immediately, but if the data is missing in
	the cache - read blocks until the backend returns the value.

## Encoders

```java
public interface Encoder {
	<T> byte[] encode(String key, T value);
	<T> T decode(String key, byte[] data);
}
```

Nothing fancy here. Convert data into byte arrays and back. The following implementations are supported:

* SerializedEncoder - using plain java ObjectOutputStream writeObject() and readObject().
* GsonEncoder -using Gson.toJSON() and Gson.fromJSON().

## Performance

Some benchmarks done on real device (Moto G), usec/operation:

                      | Set(Primiive) | Get(Primitive) | Set(Object) | Get(Object) | Set(List) | Get(List)
----------------------|---------------|----------------|-------------|-------------|-----------|-----------
SharedPrefs/Serialized| 157           | 228            | 381         | 394         | 3151      | 4038
SharedPrefs/Gson      | 433           | 133            | 203         | 202         | 4345      | 7918
Sqlite/Serialized     | 7479          | 3704           | 7553        | 3751        | 10048     | 7064
Sqlite/Gson           | 7754          | 3825           | 7581        | 3905        | 12987     | 11876
LRU/Sqlite/Serialized | 41            | 106            | 82          | 194         | 2402      | 3731
LRU/Sqlite/Gson       | 98            | 176            | 397         | 175         | 4209      | 8446
Hawk/SharedPrefs      | 3515          | 809            | 3406        | 935         | 11970     | 17322
Hawk/Sqlite           | 8541          | 4710           | 8690        | 5084        | 14492     | 22971

Some benchmarks on Genymotion:

                      | Set(Primiive) | Get(Primitive) | Set(Object) | Get(Object) | Set(List) | Get(List)
----------------------|---------------|----------------|-------------|-------------|-----------|-----------
SharedPrefs/Serialized| 39            | 36             | 56          | 50          | 511       | 524
SharedPrefs/Gson      | 84            | 16             | 93          | 63          | 657       | 1102
Sqlite/Serialized     | 2448          | 699            | 2215        | 1045        | 2544      | 1160
Sqlite/Gson           | 2311          | 716            | 2562        | 1228        | 3783      | 1523
LRU/Sqlite/Serialized | 7             | 24             | 26          | 25          | 349       | 505
LRU/Sqlite/Gson       | 13            | 29             | 29          | 78          | 409       | 848
Hawk/SharedPrefs      | 1302          | 82             | 1276        | 58          | 1771      | 1523
Hawk/Sqlite           | 2725          | 854            | 2703        | 793         | 3538      | 2469

KV is always faster than Hawk, however it's Sqlite-based storage is slow
(because there is no cache by default). Adding cache to the Sqlite storage
makes it one of the most performant. Also, there is no dramatic different
between Gson and Serializable implementations.

## License

Code is distributed under MIT license, feel free to use it in your proprietary
projects as well.
