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

	private static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(GsonTypeWrapper.class,
				new GsonTypeWrapperDeserializer());
		gson = builder.create();
	}

	private static class GsonTypeWrapper<T> {
		String type;
		String[] paramTypes;
		T value;
		public GsonTypeWrapper(String type, T value) {
			this.type = type;
			this.value = value;
		}
	}

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

	private static class GsonTypeWrapperDeserializer
			implements JsonDeserializer<GsonTypeWrapper<?>> {
		public GsonTypeWrapper<?> deserialize(JsonElement el, Type type,
				JsonDeserializationContext c) {
			try {
				Object value = null;
				JsonObject meta = el.getAsJsonObject();
				String typeName = meta.get("type").getAsString();
				Class typeClass = Class.forName(typeName);
				Class paramClasses[] = new Class[]{};
				if (meta.has("paramTypes")) {
					JsonArray paramsArray = meta.get("paramTypes").getAsJsonArray();
					paramClasses = new Class[paramsArray.size()];
					for (int i = 0; i < paramsArray.size(); i++) {
						paramClasses[i] = Class.forName(paramsArray.get(i).getAsString());
					}
				}
				if (List.class.isAssignableFrom(typeClass) || 
						Map.class.isAssignableFrom(typeClass) ||
						Set.class.isAssignableFrom(typeClass)) {
					value = gson.fromJson(meta.get("value"),
							new ParameterizedTypeWrapper(typeClass, paramClasses));
				} else {
					value = gson.fromJson(meta.get("value"), typeClass);
				}
				return new GsonTypeWrapper(typeName, value);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// FIXME add BitSet
	// FIXME add Queue
	// FIXME add android.util.Pair
	public <T> byte[] encode(String key, T value) {
		GsonTypeWrapper<T> t = null;

    if (List.class.isAssignableFrom(value.getClass())) {
      List<?> list = (List<?>) value;
			t = new GsonTypeWrapper<>(list.getClass().getName(), value);
      if (!list.isEmpty()) {
        t.paramTypes = new String[]{list.get(0).getClass().getName()};
      }
    } else if (Map.class.isAssignableFrom(value.getClass())) {
      Map<?, ?> map = (Map) value;
      if (!map.isEmpty()) {
				t = new GsonTypeWrapper<>(map.getClass().getName(), value);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
					t.paramTypes = new String[]{
						entry.getKey().getClass().getName(),
						entry.getValue().getClass().getName(),
					};
          break;
        }
      }
    } else if (Set.class.isAssignableFrom(value.getClass())) {
      Set<?> set = (Set<?>) value;
      if (!set.isEmpty()) {
        Iterator<?> iterator = set.iterator();
				t = new GsonTypeWrapper<>(set.getClass().getName(), value);
        if (iterator.hasNext()) {
          t.paramTypes = new String[]{iterator.next().getClass().getName()};
        }
      }
    } else {
			t = new GsonTypeWrapper<>(value.getClass().getName(), value);
    }
		return gson.toJson(t).getBytes();
	}

	public <T> T decode(String key, byte[] data) {
		GsonTypeWrapper<T> t = gson.fromJson(new String(data), GsonTypeWrapper.class);
		return t.value;
	}
}

