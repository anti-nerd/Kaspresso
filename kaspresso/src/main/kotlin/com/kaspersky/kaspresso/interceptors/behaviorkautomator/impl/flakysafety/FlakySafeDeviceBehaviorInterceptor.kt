package com.kaspersky.kaspresso.interceptors.behaviorkautomator.impl.flakysafety

import com.kaspersky.components.kautomator.intercepting.interaction.UiDeviceInteraction
import com.kaspersky.components.kautomator.intercepting.operation.UiDeviceAction
import com.kaspersky.components.kautomator.intercepting.operation.UiDeviceAssertion
import com.kaspersky.kaspresso.flakysafety.FlakySafetyProvider
import com.kaspersky.kaspresso.flakysafety.FlakySafetyProviderImpl
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.DeviceBehaviorInterceptor
import com.kaspersky.kaspresso.logger.UiTestLogger
import com.kaspersky.kaspresso.params.FlakySafetyParams

/**
 * The implementation of [DeviceBehaviorInterceptor] and [FlakySafetyProvider] interfaces.
 * Provides system flaky safety functionality for [UiDeviceInteraction.perform] and [UiDeviceInteraction.check] calls.
 */
class FlakySafeDeviceBehaviorInterceptor(
    params: FlakySafetyParams,
    logger: UiTestLogger
) : DeviceBehaviorInterceptor,
    FlakySafetyProvider by FlakySafetyProviderImpl(params, logger) {

    /**
     * Wraps the given [activity] invocation with the flaky safety.
     *
     * @param interaction the intercepted [UiDeviceInteraction].
     * @param assertion the intercepted [UiDeviceAssertion].
     * @param activity the activity to invoke.
     */
    override fun <T> interceptCheck(
        interaction: UiDeviceInteraction,
        assertion: UiDeviceAssertion,
        activity: () -> T
    ): T = flakySafely(action = activity)

    /**
     * Wraps the given [activity] invocation with the flaky safety.
     *
     * @param interaction the intercepted [UiDeviceInteraction].
     * @param action the intercepted [UiDeviceAction].
     * @param activity the activity to invoke.
     */
    override fun <T> interceptPerform(
        interaction: UiDeviceInteraction,
        action: UiDeviceAction,
        activity: () -> T
    ): T = flakySafely(action = activity)
}