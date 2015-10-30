package trikita.kv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GsonEncoder implements KV.Encoder {

	private static class ParameterizedTypeWrapper implements ParameterizedType {
		private final Class<?> type;
		private final Class<?>[] params;

		public ParameterizedTypeWrapper(Class type, Class[] params) {
			this.type = type;
			this.params = params;
		}

		public Type[] getActualTypeArguments() { return this.params; }
		public Type getRawType() { return this.type; }
		public Type getOwnerType() { return null; }
		public int hashCode() { return type.hashCode(); }
		public boolean equals(Object o) {
			return (o != null &&
					o instanceof ParameterizedTypeWrapper &&
					((ParameterizedTypeWrapper) o).type.equals(this.type) &&
					Arrays.equals(((ParameterizedTypeWrapper) o).params, this.params));
		}
	}

	private class GsonTypeWrapperDeserializer
			implements JsonDeserializer<GenericTypes.Wrapper<?>> {
		public GenericTypes.Wrapper<?> deserialize(JsonElement el, Type type,
				JsonDeserializationContext c) {
			try {
				Object value = null;
				JsonObject meta = el.getAsJsonObject();
				String typeName = meta.get("type").getAsString();
				Class typeClass = Class.forName(typeName);
				Class paramClasses[] = null;
				if (meta.has("paramTypes")) {
					JsonArray paramsArray = meta.get("paramTypes").getAsJsonArray();
					paramClasses = new Class[paramsArray.size()];
					for (int i = 0; i < paramsArray.size(); i++) {
						paramClasses[i] = Class.forName(paramsArray.get(i).getAsString());
					}
				}
				if (paramClasses != null) {
					value = gson.fromJson(meta.get("value"),
							new ParameterizedTypeWrapper(typeClass, paramClasses));
				} else {
					value = gson.fromJson(meta.get("value"), typeClass);
				}
				return new GenericTypes.Wrapper(typeName, null, value);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final Gson gson;

	public GsonEncoder() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(GenericTypes.Wrapper.class,
				new GsonTypeWrapperDeserializer());
		this.gson = builder.create();
	}

	public GsonEncoder(Gson gson) {
		this.gson = gson;
	}

	public <T> byte[] encode(String key, T value) {
		return gson.toJson(GenericTypes.wrap(value)).getBytes();
	}

	public <T> T decode(String key, byte[] data) {
		GenericTypes.Wrapper<T> t =
			gson.fromJson(new String(data), GenericTypes.Wrapper.class);
		return t.value;
	}
}

