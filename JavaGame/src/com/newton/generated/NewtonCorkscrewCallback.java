// Generated by jextract

package com.newton.generated;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;
public interface NewtonCorkscrewCallback {

    int apply(jdk.incubator.foreign.MemoryAddress x0, jdk.incubator.foreign.MemoryAddress x1);
    static NativeSymbol allocate(NewtonCorkscrewCallback fi, ResourceScope scope) {
        return RuntimeHelper.upcallStub(NewtonCorkscrewCallback.class, fi, constants$16.NewtonCorkscrewCallback$FUNC, "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)I", scope);
    }
    static NewtonCorkscrewCallback ofAddress(MemoryAddress addr, ResourceScope scope) {
        NativeSymbol symbol = NativeSymbol.ofAddress("NewtonCorkscrewCallback::" + Long.toHexString(addr.toRawLongValue()), addr, scope);
return (jdk.incubator.foreign.MemoryAddress x0, jdk.incubator.foreign.MemoryAddress x1) -> {
            try {
                return (int)constants$16.NewtonCorkscrewCallback$MH.invokeExact(symbol, (jdk.incubator.foreign.Addressable)x0, (jdk.incubator.foreign.Addressable)x1);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}


