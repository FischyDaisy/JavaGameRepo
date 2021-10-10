package main.engine.utility.physUtils;

public class FloatArrayList {

	private float[] array = new float[16];
	private int size;
	
	public void add(float value) {
		if (size == array.length) {
			expand();
		}
		
		array[size++] = value;
	}
	
	private void expand() {
		float[] newArray = new float[array.length << 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}

	public float remove(int index) {
		if (index >= size) throw new IndexOutOfBoundsException();
		float old = array[index];
		System.arraycopy(array, index+1, array, index, size - index - 1);
		size--;
		return old;
	}

	public float get(int index) {
		if (index >= size) throw new IndexOutOfBoundsException();
		return array[index];
	}

	public void set(int index, float value) {
		if (index >= size) throw new IndexOutOfBoundsException();
		array[index] = value;
	}

	public int size() {
		return size;
	}

}