// Generated by jextract

package com.newton.generated;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;
class constants$23 {

    static final FunctionDescriptor NewtonGetBroadphaseAlgorithm$FUNC = FunctionDescriptor.of(Constants$root.C_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonGetBroadphaseAlgorithm$MH = RuntimeHelper.downcallHandle(
        "NewtonGetBroadphaseAlgorithm",
        constants$23.NewtonGetBroadphaseAlgorithm$FUNC, false
    );
    static final FunctionDescriptor NewtonSelectBroadphaseAlgorithm$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG$LAYOUT
    );
    static final MethodHandle NewtonSelectBroadphaseAlgorithm$MH = RuntimeHelper.downcallHandle(
        "NewtonSelectBroadphaseAlgorithm",
        constants$23.NewtonSelectBroadphaseAlgorithm$FUNC, false
    );
    static final FunctionDescriptor NewtonResetBroadphase$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonResetBroadphase$MH = RuntimeHelper.downcallHandle(
        "NewtonResetBroadphase",
        constants$23.NewtonResetBroadphase$FUNC, false
    );
    static final FunctionDescriptor NewtonUpdate$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT
    );
    static final MethodHandle NewtonUpdate$MH = RuntimeHelper.downcallHandle(
        "NewtonUpdate",
        constants$23.NewtonUpdate$FUNC, false
    );
    static final FunctionDescriptor NewtonUpdateAsync$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT
    );
    static final MethodHandle NewtonUpdateAsync$MH = RuntimeHelper.downcallHandle(
        "NewtonUpdateAsync",
        constants$23.NewtonUpdateAsync$FUNC, false
    );
    static final FunctionDescriptor NewtonWaitForUpdateToFinish$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonWaitForUpdateToFinish$MH = RuntimeHelper.downcallHandle(
        "NewtonWaitForUpdateToFinish",
        constants$23.NewtonWaitForUpdateToFinish$FUNC, false
    );
}


