package main.engine.utility;

import java.util.ArrayList;
import java.util.List;

import main.engine.graphics.IndexedLinkedHashMap;

public abstract class Cache<T> {
	protected final IndexedLinkedHashMap<String, T> cacheMap;
	
	public Cache() {
		cacheMap = new IndexedLinkedHashMap<String, T>();
	}
	
	public abstract T get(String key);
	
	public List<T> getAsList() {
		List<T> list = new ArrayList<T>();
		for (T t : cacheMap.values()) {
			list.add(t);
		}
		return list;
	}
	
	public int getPosition(String id) {
		int result = -1;
        if (id != null) {
            result = cacheMap.getIndexOf(id);
        }
        return result;
	}
	
	public void cleanup() {
        cacheMap.clear();
    }
}
