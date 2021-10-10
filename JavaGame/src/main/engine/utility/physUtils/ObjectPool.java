package main.engine.utility.physUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ObjectPool<T> {
	
	private Class<T> cls;
	private ObjectArrayList<T> list = new ObjectArrayList<T>();
	
	public ObjectPool(Class<T> cls) {
		this.cls = cls;
	}

	private T create() {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Returns instance from pool, or create one if pool is empty.
	 * 
	 * @return instance
	 */
	public T get() {
		if (list.size() > 0) {
			return list.remove(list.size() - 1);
		}
		else {
			return create();
		}
	}
	
	/**
	 * Release instance into pool.
	 * 
	 * @param obj previously obtained instance from pool
	 */
	public void release(T obj) {
		list.add(obj);
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	private static ThreadLocal<Map> threadLocal = new ThreadLocal<Map>() {
		@Override
		protected Map initialValue() {
			return new HashMap();
		}
	};
	
	/**
	 * Returns per-thread object pool for given type, or create one if it doesn't exist.
	 * 
	 * @param cls type
	 * @return object pool
	 */
	@SuppressWarnings("unchecked")
	public static <T> ObjectPool<T> get(Class<T> cls) {
		Map map = threadLocal.get();
		
		ObjectPool<T> pool = (ObjectPool<T>)map.get(cls);
		if (pool == null) {
			pool = new ObjectPool<T>(cls);
			map.put(cls, pool);
		}
		
		return pool;
	}

	public static void cleanCurrentThread() {
		threadLocal.remove();
	}

}