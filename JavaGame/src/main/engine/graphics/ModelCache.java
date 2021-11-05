package main.engine.graphics;

public class ModelCache {
	private final IndexedLinkedHashMap<String, IModel> modelMap;
	
	public ModelCache() {
		modelMap = new IndexedLinkedHashMap<String, IModel>();
	}
	
	public void cleanup() {
        modelMap.forEach((k, v) -> v.cleanup());
        modelMap.clear();
    }
}
