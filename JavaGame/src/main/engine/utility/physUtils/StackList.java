package main.engine.utility.physUtils;

import com.bulletphysics.util.ObjectArrayList;

public abstract class StackList<T> {

	private final ObjectArrayList<T> list = new ObjectArrayList<T>();
	private T returnObj;
	
	private int[] stack = new int[512];
	private int stackCount = 0;
	
	private int pos = 0;
	
	public StackList() {
		returnObj = create();
	}
	
	protected StackList(boolean unused) {
	}
	
	/**
	 * Pushes the stack.
	 */
	public final void push() {
		/*if (stackCount == stack.length-1) {
			resizeStack();
		}*/
		
		stack[stackCount++] = pos;
	}

	/**
	 * Pops the stack.
	 */
	public final void pop() {
		pos = stack[--stackCount];
	}
	
	/**
	 * Returns instance from stack pool, or create one if not present. The returned
	 * instance will be automatically reused when {@link #pop} is called.
	 * 
	 * @return instance
	 */
	public T get() {
		//if (true) return create();
		
		if (pos == list.size()) {
			expand();
		}
		
		return list.getQuick(pos++);
	}
	
	/**
	 * Copies given instance into one slot static instance and returns it. It's
	 * essential that caller of method (that uses this method for returning instances)
	 * immediately copies it into own variable before any other usage.
	 * 
	 * @param obj stack-allocated instance
	 * @return one slot instance for returning purposes
	 */
	public final T returning(T obj) {
		//if (true) { T ret = create(); copy(ret, obj); return ret; }
		
		copy(returnObj, obj);
		return returnObj;
	}
	
	/**
	 * Creates a new instance of type.
	 * 
	 * @return instance
	 */
	protected abstract T create();
	
	/**
	 * Copies data from one instance to another.
	 * 
	 * @param dest
	 * @param src
	 */
	protected abstract void copy(T dest, T src);

	private void expand() {
		list.add(create());
	}
	
}