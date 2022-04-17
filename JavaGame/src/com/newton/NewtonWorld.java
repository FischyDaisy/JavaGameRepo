package com.newton;

import jdk.incubator.foreign.*;
import com.newton.generated.*;

public class NewtonWorld {
	
	protected final MemoryAddress address;
	protected final ResourceScope scope;
	
	private NewtonWorld(MemoryAddress address) {
		this.address = address;
		this.scope = ResourceScope.newConfinedScope();
	}
	
	public static NewtonWorld create() {
		return new NewtonWorld(Newton_h.NewtonCreate(new Object[] {}));
	}
	
	public static int getWorldVersion() {
		return Newton_h.NewtonWorldGetVersion(new Object[] {});
	}
	
	public static int getFloatSizes() {
		return Newton_h.NewtonWorldFloatSize(new Object[] {});
	}
	
	public static int getMemoryUsed() {
		return Newton_h.NewtonGetMemoryUsed(new Object[] {});
	}
	
	public static void setMemorySystem(NewtonAllocMemory alloc, NewtonFreeMemory free, ResourceScope scope) {
		NativeSymbol allocFunc = NewtonAllocMemory.allocate(alloc, scope);
		NativeSymbol freeFunc = NewtonFreeMemory.allocate(free, scope);
		Newton_h.NewtonSetMemorySystem(allocFunc, freeFunc);
	}
	
	public static MemoryAddress newtonAlloc(int sizeInBytes) {
		return Newton_h.NewtonAlloc(sizeInBytes);
	}
	
	public static MemorySegment newtonAlloc(int sizeInBytes, ResourceScope scope) {
		return MemorySegment.ofAddress(Newton_h.NewtonAlloc(sizeInBytes), sizeInBytes, scope);
	}
	
	public static MemoryAddress newtonAlloc(MemoryLayout layout) {
		return Newton_h.NewtonAlloc((int) layout.byteSize());
	}
	
	public static MemorySegment newtonAlloc(MemoryLayout layout, ResourceScope scope) {
		return MemorySegment.ofAddress(Newton_h.NewtonAlloc((int) layout.byteSize()), layout.byteSize(), scope);
	}
	
	public static void newtonFree(Addressable ptr) {
		Newton_h.NewtonFree(ptr);
	}
	
	public void destroy() {
		Newton_h.NewtonDestroy(address);
		if (scope.isAlive()) {
			scope.close();
		}
	}
	
	public NewtonPostUpdateCallback getPostUpdateCallback() {
		return NewtonPostUpdateCallback.ofAddress(Newton_h.NewtonGetPostUpdateCallback(address), scope);
	}
	
	public NewtonPostUpdateCallback getPostUpdateCallback(ResourceScope scope) {
		return NewtonPostUpdateCallback.ofAddress(Newton_h.NewtonGetPostUpdateCallback(address), scope);
	}
	
	public void setPostUpdateCallback(NewtonPostUpdateCallback callback) {
		NativeSymbol callbackFunc = NewtonPostUpdateCallback.allocate(callback, scope);
		Newton_h.NewtonSetPostUpdateCallback(address, callbackFunc);
	}
	
	public void setPostUpdateCallback(NewtonPostUpdateCallback callback, ResourceScope scope) {
		NativeSymbol callbackFunc = NewtonPostUpdateCallback.allocate(callback, scope);
		Newton_h.NewtonSetPostUpdateCallback(address, callbackFunc);
	}
	
	public void loadPlugins(String pluginPath) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment path = allocator.allocateUtf8String(pluginPath);
		Newton_h.NewtonLoadPlugins(address, path);
	}
	
	public void loadPlugins(String pluginPath, ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment path = allocator.allocateUtf8String(pluginPath);
		Newton_h.NewtonLoadPlugins(address, path);
	}
	
	public void loadPlugins(String pluginPath, SegmentAllocator allocator) {
		MemorySegment path = allocator.allocateUtf8String(pluginPath);
		Newton_h.NewtonLoadPlugins(address, path);
	}
	
	public void unloadPlugins() {
		Newton_h.NewtonUnloadPlugins(address);
	}
	
	public String currentPlugin() {
		MemoryAddress pluginStrAddress = Newton_h.NewtonCurrentPlugin(address);
		return pluginStrAddress.getUtf8String(0);
	}
	
	public String firstPlugin() {
		MemoryAddress pluginStrAddress = Newton_h.NewtonGetFirstPlugin(address);
		return pluginStrAddress.getUtf8String(0);
	}
	
	public String preferedPlugin() {
		MemoryAddress pluginStrAddress = Newton_h.NewtonGetPreferedPlugin(address);
		return pluginStrAddress.getUtf8String(0);
	}
	
	public float getContactMergeTolerance() {
		return Newton_h.NewtonGetContactMergeTolerance(address);
	}
	
	public void setContactMergeTolerance(float tolerance) {
		Newton_h.NewtonSetContactMergeTolerance(address, tolerance);
	}
	
	public void invalidateCache() {
		Newton_h.NewtonInvalidateCache(address);
	}
	
	public void setSolverIterations(int model) {
		Newton_h.NewtonSetSolverIterations(address, model);
	}
	
	public int getSolverIterations() {
		return Newton_h.NewtonGetSolverIterations(address);
	}
	
	public void setParallelSolverOnLargeIsland(int mode) {
		Newton_h.NewtonSetParallelSolverOnLargeIsland(address, mode);
	}
	
	public int getParallelSolverOnLargeIsland() {
		return Newton_h.NewtonGetParallelSolverOnLargeIsland(address);
	}
	
	public void update(float timestep) {
		Newton_h.NewtonUpdate(address, timestep);
	}
	
	public void destroyAllBodies() {
		Newton_h.NewtonDestroyAllBodies(address);
	}
	/**
	 * This method wraps a memory address into a NewtonWorld object.
	 * This method is only meant to be used internally. Improper use of this method could
	 * result in errors or an exception.
	 * @param address - MemoryAddress of NewtonWorld
	 * @return NewtonWorld object
	 */
	protected static NewtonWorld wrap(MemoryAddress address) {
		return new NewtonWorld(address);
	}
}
