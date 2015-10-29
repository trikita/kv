package trikita.kv;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericTypes {

	public static class Wrapper<T> {
		public final String type;
		public final String[] paramTypes;
		public final T value;

		public Wrapper(String type, String[] params, T value) {
			this.type = type;
			this.paramTypes = params;
			this.value = value;
		}
	}

	public interface Extractor<T> {
		public Class[] getTypes(T object);
	}

	public static final Map<Class, Extractor> TYPES = new HashMap<>();

	static {
		// TODO BitSet
		// TODO Queue
		// TODO android.util.Pair?
		TYPES.put(List.class, new Extractor<List>() {
			public Class[] getTypes(List list) {
				if (!list.isEmpty()) {
					return new Class[]{list.get(0).getClass()};
				}
				return null;
			}
		});

		TYPES.put(Map.class, new Extractor<Map<?, ?>>() {
			public Class[] getTypes(Map<?, ?> map) {
				if (!map.isEmpty()) {
					for (Map.Entry<?, ?> entry : map.entrySet()) {
						return new Class[] {
							entry.getKey().getClass(), entry.getValue().getClass(),
						};
					}
				}
				return null;
			}
		});

		TYPES.put(Set.class, new Extractor<Set>() {
			public Class[] getTypes(Set set) {
				if (!set.isEmpty()) {
					Iterator iterator = set.iterator();
					if (iterator.hasNext()) {
						return new Class[]{iterator.next().getClass()};
					}
				}
				return null;
			}
		});
	}

	public static <T> Wrapper<T> wrap(T value) {
		for (Map.Entry<Class, Extractor> kv : TYPES.entrySet()) {
			Class key = kv.getKey();
			Extractor e = kv.getValue();
			if (key.isAssignableFrom(value.getClass())) {
				String[] params = null;
				Class[] types = e.getTypes(value);
				if (types != null) {
					params = new String[types.length];
					for (int i = 0; i < types.length; i++) {
						params[i] = types[i].getName();
					}
				}
				return new Wrapper<T>(value.getClass().getName(), params, value);
			}
		}
		return new Wrapper<>(value.getClass().getName(), null, value);
	}
}
