// Generated by jextract

package com.newton.generated;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.ValueLayout.*;
class constants$57 {

    static final FunctionDescriptor NewtonCollisionAggregateDestroy$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonCollisionAggregateDestroy$MH = RuntimeHelper.downcallHandle(
        "NewtonCollisionAggregateDestroy",
        constants$57.NewtonCollisionAggregateDestroy$FUNC, false
    );
    static final FunctionDescriptor NewtonCollisionAggregateAddBody$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonCollisionAggregateAddBody$MH = RuntimeHelper.downcallHandle(
        "NewtonCollisionAggregateAddBody",
        constants$57.NewtonCollisionAggregateAddBody$FUNC, false
    );
    static final FunctionDescriptor NewtonCollisionAggregateRemoveBody$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonCollisionAggregateRemoveBody$MH = RuntimeHelper.downcallHandle(
        "NewtonCollisionAggregateRemoveBody",
        constants$57.NewtonCollisionAggregateRemoveBody$FUNC, false
    );
    static final FunctionDescriptor NewtonCollisionAggregateGetSelfCollision$FUNC = FunctionDescriptor.of(Constants$root.C_LONG$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonCollisionAggregateGetSelfCollision$MH = RuntimeHelper.downcallHandle(
        "NewtonCollisionAggregateGetSelfCollision",
        constants$57.NewtonCollisionAggregateGetSelfCollision$FUNC, false
    );
    static final FunctionDescriptor NewtonCollisionAggregateSetSelfCollision$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_LONG$LAYOUT
    );
    static final MethodHandle NewtonCollisionAggregateSetSelfCollision$MH = RuntimeHelper.downcallHandle(
        "NewtonCollisionAggregateSetSelfCollision",
        constants$57.NewtonCollisionAggregateSetSelfCollision$FUNC, false
    );
    static final FunctionDescriptor NewtonSetEulerAngle$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle NewtonSetEulerAngle$MH = RuntimeHelper.downcallHandle(
        "NewtonSetEulerAngle",
        constants$57.NewtonSetEulerAngle$FUNC, false
    );
}


