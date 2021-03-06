package com.kaspersky.kaspresso.interceptors.tolibrary.kautomator.impl

import com.kaspersky.components.kautomator.intercepting.interaction.UiDeviceInteraction
import com.kaspersky.components.kautomator.intercepting.operation.UiDeviceAction
import com.kaspersky.components.kautomator.intercepting.operation.UiDeviceAssertion
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.DeviceBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.LibraryInterceptor
import com.kaspersky.kaspresso.interceptors.watcher.kautomator.DeviceWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso

/**
 * Kaspresso's implementation of Kautomator's UiDeviceInteraction interceptor.
 */
internal class KautomatorDeviceInterceptor(
    kaspresso: Kaspresso
) : LibraryInterceptor<UiDeviceInteraction, UiDeviceAssertion, UiDeviceAction>(kaspresso) {

    /**
     * Folds all [DeviceBehaviorInterceptor]'s one into another in the order from the first to the last with the actual
     * [UiDeviceInteraction.check] call as the initial, and invokes the resulting lambda.
     * Also, adds a call of all [DeviceWatcherInterceptor] in the initial lambda
     */
    override fun interceptCheck(interaction: UiDeviceInteraction, assertion: UiDeviceAssertion) {
        kaspresso.deviceBehaviorInterceptors.fold(
            initial = {
                kaspresso.deviceWatcherInterceptors.forEach {
                    it.interceptCheck(interaction, assertion)
                }
                interaction.check(assertion)
            },
            operation = {
                    acc, deviceBehaviorInterceptor ->
                { deviceBehaviorInterceptor.interceptCheck(interaction, assertion, acc) }
            }
        ).invoke()
    }

    /**
     * Folds all [DeviceBehaviorInterceptor]'s one into another in the order from the first to the last with the actual
     * [UiDeviceInteraction.perform] call as the initial, and invokes the resulting lambda.
     * Also, adds a call of [DeviceWatcherInterceptor] in the initial lambda
     */
    override fun interceptPerform(interaction: UiDeviceInteraction, action: UiDeviceAction) {
        kaspresso.deviceBehaviorInterceptors.fold(
            initial = {
                kaspresso.deviceWatcherInterceptors.forEach {
                    it.interceptPerform(interaction, action)
                }
                interaction.perform(action)
            },
            operation = {
                    acc, deviceBehaviorInterceptor ->
                { deviceBehaviorInterceptor.interceptPerform(interaction, action, acc) }
            }
        ).invoke()
    }
}