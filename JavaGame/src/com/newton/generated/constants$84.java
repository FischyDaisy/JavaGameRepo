// Generated by jextract

package com.newton.generated;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;
class constants$84 {

    static final FunctionDescriptor NewtonUserJointAddLinearRow$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonUserJointAddLinearRow$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointAddLinearRow",
        constants$84.NewtonUserJointAddLinearRow$FUNC, false
    );
    static final FunctionDescriptor NewtonUserJointAddAngularRow$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonUserJointAddAngularRow$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointAddAngularRow",
        constants$84.NewtonUserJointAddAngularRow$FUNC, false
    );
    static final FunctionDescriptor NewtonUserJointAddGeneralRow$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonUserJointAddGeneralRow$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointAddGeneralRow",
        constants$84.NewtonUserJointAddGeneralRow$FUNC, false
    );
    static final FunctionDescriptor NewtonUserJointSetRowMinimumFriction$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT
    );
    static final MethodHandle NewtonUserJointSetRowMinimumFriction$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointSetRowMinimumFriction",
        constants$84.NewtonUserJointSetRowMinimumFriction$FUNC, false
    );
    static final FunctionDescriptor NewtonUserJointSetRowMaximumFriction$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_FLOAT$LAYOUT
    );
    static final MethodHandle NewtonUserJointSetRowMaximumFriction$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointSetRowMaximumFriction",
        constants$84.NewtonUserJointSetRowMaximumFriction$FUNC, false
    );
    static final FunctionDescriptor NewtonUserJointCalculateRowZeroAcceleration$FUNC = FunctionDescriptor.of(Constants$root.C_FLOAT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonUserJointCalculateRowZeroAcceleration$MH = RuntimeHelper.downcallHandle(
        "NewtonUserJointCalculateRowZeroAcceleration",
        constants$84.NewtonUserJointCalculateRowZeroAcceleration$FUNC, false
    );
}


