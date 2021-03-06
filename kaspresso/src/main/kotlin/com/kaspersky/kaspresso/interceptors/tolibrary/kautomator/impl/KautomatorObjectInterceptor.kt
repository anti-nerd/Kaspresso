package com.kaspersky.kaspresso.interceptors.tolibrary.kautomator.impl

import com.kaspersky.components.kautomator.intercepting.interaction.UiObjectInteraction
import com.kaspersky.components.kautomator.intercepting.operation.UiObjectAction
import com.kaspersky.components.kautomator.intercepting.operation.UiObjectAssertion
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.ObjectBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.LibraryInterceptor
import com.kaspersky.kaspresso.interceptors.watcher.kautomator.ObjectWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso

/**
 * Kaspresso's implementation of Kautomator's UiObjectInteraction interceptor.
 */
internal class KautomatorObjectInterceptor(
    kaspresso: Kaspresso
) : LibraryInterceptor<UiObjectInteraction, UiObjectAssertion, UiObjectAction>(kaspresso) {

    /**
     * Folds all [ObjectBehaviorInterceptor]'s one into another in the order from the first to the last with the actual
     * [UiObjectInteraction.check] call as the initial, and invokes the resulting lambda.
     * Also, adds a call of all [ObjectWatcherInterceptor] in the initial lambda
     */
    override fun interceptCheck(interaction: UiObjectInteraction, assertion: UiObjectAssertion) {
        kaspresso.objectBehaviorInterceptors.fold(
            initial = {
                kaspresso.objectWatcherInterceptors.forEach {
                    it.interceptCheck(interaction, assertion)
                }
                interaction.check(assertion)
            },
            operation = {
                acc, objectBehaviorInterceptor ->
                { objectBehaviorInterceptor.interceptCheck(interaction, assertion, acc) }
            }
        ).invoke()
    }

    /**
     * Folds all [ObjectBehaviorInterceptor]'s one into another in the order from the first to the last with the actual
     * [UiObjectInteraction.perform] call as the initial, and invokes the resulting lambda.
     * Also, adds a call of [ObjectWatcherInterceptor] in the initial lambda
     */
    override fun interceptPerform(interaction: UiObjectInteraction, action: UiObjectAction) {
        kaspresso.objectBehaviorInterceptors.fold(
            initial = {
                kaspresso.objectWatcherInterceptors.forEach {
                    it.interceptPerform(interaction, action)
                }
                interaction.perform(action)
            },
            operation = {
                    acc, objectBehaviorInterceptor ->
                { objectBehaviorInterceptor.interceptPerform(interaction, action, acc) }
            }
        ).invoke()
    }
}