package trikita.kv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializedEncoder implements KV.Encoder {
	public <T> byte[] encode(String key, T value) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(b);
			out.writeObject(value);
			return b.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public <T> T decode(String key, byte[] data) {
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
			return (T) in.readObject();
		} catch (IOException|ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
