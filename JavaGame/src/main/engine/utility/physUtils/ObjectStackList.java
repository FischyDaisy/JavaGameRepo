package main.engine.utility.physUtils;

import java.lang.reflect.InvocationTargetException;

import com.bulletphysics.util.StackList;

public class ObjectStackList<T> extends StackList<T> {

	private Class<T> cls;
	
	public ObjectStackList(Class<T> cls) {
		super(false);
		this.cls = cls;
	}

	@Override
	protected T create() {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void copy(T dest, T src) {
		throw new UnsupportedOperationException();
	}
	
}