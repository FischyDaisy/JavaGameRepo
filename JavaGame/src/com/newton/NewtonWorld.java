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
	
	public int getBroadphaseAlgorithm() {
		return Newton_h.NewtonGetBroadphaseAlgorithm(address);
	}
	
	public void selectBroadphaseAlgorithm(int algorithmType) {
		Newton_h.NewtonSelectBroadphaseAlgorithm(address, algorithmType);
	}
	
	public void resetBroadphase() {
		Newton_h.NewtonResetBroadphase(address);
	}
	
	public void update(float timestep) {
		Newton_h.NewtonUpdate(address, timestep);
	}
	
	public void updateAsync(float timestep) {
		Newton_h.NewtonUpdateAsync(address, timestep);
	}
	
	public void waitForUpdateToFinish() {
		Newton_h.NewtonWaitForUpdateToFinish(address);
	}
	
	public int getNumberOfSubsteps() {
		return Newton_h.NewtonGetNumberOfSubsteps(address);
	}
	
	public void setNumberOfSubsteps(int substeps) {
		Newton_h.NewtonSetNumberOfSubsteps(address, substeps);
	}
	
	public float getLastUpdateTime() {
		return Newton_h.NewtonGetLastUpdateTime(address);
	}
	
	public void serializeToFile(String filename, NewtonOnBodySerializationCallback bodyCallback, Addressable bodyUserData) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment filePath = allocator.allocateUtf8String(filename);
		NativeSymbol callbackFunc = NewtonOnBodySerializationCallback.allocate(bodyCallback, scope);
		Newton_h.NewtonSerializeToFile(address, filePath, callbackFunc, bodyUserData);
	}
	
	public void serializeToFile(String filename, NewtonOnBodySerializationCallback bodyCallback, Addressable bodyUserData,
			ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment filePath = allocator.allocateUtf8String(filename);
		NativeSymbol callbackFunc = NewtonOnBodySerializationCallback.allocate(bodyCallback, scope);
		Newton_h.NewtonSerializeToFile(address, filePath, callbackFunc, bodyUserData);
	}
	
	public void deserializeFromFile(String filename, NewtonOnBodyDeserializationCallback bodyCallback, Addressable bodyUserData) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment filePath = allocator.allocateUtf8String(filename);
		NativeSymbol callbackFunc = NewtonOnBodyDeserializationCallback.allocate(bodyCallback, scope);
		Newton_h.NewtonDeserializeFromFile(address, filePath, callbackFunc, bodyUserData);
	}
	
	public void deserializeFromFile(String filename, NewtonOnBodyDeserializationCallback bodyCallback, Addressable bodyUserData,
			ResourceScope scope) {
		SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
		MemorySegment filePath = allocator.allocateUtf8String(filename);
		NativeSymbol callbackFunc = NewtonOnBodyDeserializationCallback.allocate(bodyCallback, scope);
		Newton_h.NewtonDeserializeFromFile(address, filePath, callbackFunc, bodyUserData);
	}
	
	public void serializeScene(NewtonOnBodySerializationCallback bodyCallback, Addressable bodyUserData, NewtonSerializeCallback serializeCallback, Addressable serializeHandle) {
		NativeSymbol bodyFunc = NewtonOnBodySerializationCallback.allocate(bodyCallback, scope);
		NativeSymbol serializeFunc = NewtonSerializeCallback.allocate(serializeCallback, scope);
		Newton_h.NewtonSerializeScene(address, bodyFunc, bodyUserData, serializeFunc, serializeHandle);
	}
	
	public void serializeScene(NewtonOnBodySerializationCallback bodyCallback, Addressable bodyUserData, NewtonSerializeCallback serializeCallback, Addressable serializeHandle,
			ResourceScope scope) {
		NativeSymbol bodyFunc = NewtonOnBodySerializationCallback.allocate(bodyCallback, scope);
		NativeSymbol serializeFunc = NewtonSerializeCallback.allocate(serializeCallback, scope);
		Newton_h.NewtonSerializeScene(address, bodyFunc, bodyUserData, serializeFunc, serializeHandle);
	}
	
	public void deserializeScene(NewtonOnBodyDeserializationCallback bodyCallback, Addressable bodyUserData, NewtonDeserializeCallback serializeCallback, Addressable serializeHandle) {
		NativeSymbol bodyFunc = NewtonOnBodyDeserializationCallback.allocate(bodyCallback, scope);
		NativeSymbol deserializeFunc = NewtonDeserializeCallback.allocate(serializeCallback, scope);
		Newton_h.NewtonDeserializeScene(address, bodyFunc, bodyUserData, deserializeFunc, serializeHandle);
	}
	
	public void deserializeScene(NewtonOnBodyDeserializationCallback bodyCallback, Addressable bodyUserData, NewtonDeserializeCallback serializeCallback, Addressable serializeHandle,
			ResourceScope scope) {
		NativeSymbol bodyFunc = NewtonOnBodyDeserializationCallback.allocate(bodyCallback, scope);
		NativeSymbol deserializeFunc = NewtonDeserializeCallback.allocate(serializeCallback, scope);
		Newton_h.NewtonDeserializeScene(address, bodyFunc, bodyUserData, deserializeFunc, serializeHandle);
	}
	
	public NewtonBody findSerializedBody(int bodySerializedID) {
		MemoryAddress body = Newton_h.NewtonFindSerializedBody(address, bodySerializedID);
		int bodyType = Newton_h.NewtonBodyGetType(body);
		switch (bodyType) {
			case 0:
				return new NewtonDynamicBody(body, scope);
			case 1:
				return new NewtonKinematicBody(body, scope);
			default:
				throw new RuntimeException("Error finding serialized body");
		}
	}
	
	public void lockCriticalSection(int threadIndex) {
		Newton_h.NewtonWorldCriticalSectionLock(address, threadIndex);
	}
	
	public void unlockCriticalSection() {
		Newton_h.NewtonWorldCriticalSectionUnlock(address);
	}
	
	public void setThreadCount(int threads) {
		Newton_h.NewtonSetThreadsCount(address, threads);
	}
	
	public int getThreadCount() {
		return Newton_h.NewtonGetThreadsCount(address);
	}
	
	public int getMaxThreadCount() {
		return Newton_h.NewtonGetMaxThreadsCount(address);
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
