package com.kaspersky.kaspresso.compose

import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import com.agoda.kakao.common.actions.BaseActions
import com.agoda.kakao.common.assertions.BaseAssertions
import com.agoda.kakao.common.views.KBaseView
import com.agoda.kakao.intercept.Interceptable
import com.kaspersky.components.kautomator.dsl.common.actions.UiBaseActions
import com.kaspersky.components.kautomator.dsl.common.assertions.UiBaseAssertions
import com.kaspersky.components.kautomator.dsl.common.views.UiBaseView
import com.kaspersky.components.kautomator.intercepting.interaction.UiObjectInteraction
import com.kaspersky.components.kautomator.intercepting.intercept.UiInterceptable
import com.kaspersky.components.kautomator.intercepting.operation.UiObjectAction
import com.kaspersky.components.kautomator.intercepting.operation.UiObjectAssertion
import com.kaspersky.kaspresso.compose.pack.ActionsOnElementsPack
import com.kaspersky.kaspresso.compose.pack.ActionsPack
import com.kaspersky.kaspresso.failure.FailureLoggingProvider
import com.kaspersky.kaspresso.failure.withLoggingOnFailureIfNotNull
import com.kaspersky.kaspresso.flakysafety.FlakySafetyProvider
import com.kaspersky.kaspresso.flakysafety.flakySafelyIfNotNull
import com.kaspersky.kaspresso.interceptors.behavior.ViewBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.behavior.impl.failure.FailureLoggingViewBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.behavior.impl.flakysafety.FlakySafeViewBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.ObjectBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.impl.failure.FailureLoggingObjectBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.behaviorkautomator.impl.flakysafety.FlakySafeObjectBehaviorInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.kakao.compose.ComposeKakaoViewInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.kakao.impl.KakaoViewInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.kautomator.compose.ComposeKautomatorObjectInterceptor
import com.kaspersky.kaspresso.interceptors.tolibrary.kautomator.impl.KautomatorObjectInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso

/**
 * The implementation of the [ComposeProvider] interface.
 */
class ComposeProviderImpl(
    private val kaspresso: Kaspresso
) : ComposeProvider {

    /**
     * Composes a [block] of actions with their views to invoke on in one composite action that succeeds if at least
     * one of it's parts succeeds.
     * Allows to use in one compose block views from Kakao and Kautomator simultaneously
     *
     * @param block the actions to compose.
     */
    override fun compose(block: ActionsOnElementsPack.() -> Unit) {
        val composeBranches = ActionsOnElementsPack().apply(block).build()
        // elements preparing
        composeBranches.forEach { rollComposeInterception(it.element) }
        // action
        val succeedBranchOrderNumber = callComposeActionsIntoFlakySafety(
            isKautomatorHere = composeBranches.find { it.element is UiBaseView<*> } != null,
            actions = composeBranches.map { it.check }
        )
        // elements restoring the previous state
        composeBranches.forEach { rollbackSystemInterception(it.element) }
        // execution `then` section of a success branch
        composeBranches[succeedBranchOrderNumber].postAction?.invoke()
    }

    /**
     * Composes a [block] of actions on the given view of type [T] in one composite action that succeeds if at least
     * one of it's parts succeeds.
     *
     * @param block the actions to compose.
     */
    override fun <Type> Type.compose(block: ActionsPack<Type>.() -> Unit)
            where Type : BaseActions, Type : BaseAssertions,
                  Type : Interceptable<ViewInteraction, ViewAssertion, ViewAction> {
        val actions: List<() -> Unit> = ActionsPack(this).apply(block).build()
        // preparing
        rollKakaoComposeInterception()
        // action
        callComposeActionsIntoFlakySafety(
            isKautomatorHere = this is UiBaseView<*>,
            actions = actions
        )
        // restoring the previous state
        rollbackKakaoInterception()
    }

    /**
     * Composes a [block] of actions on the given view of type [T] in one composite action that succeeds if at least
     * one of it's parts succeeds.
     *
     * @param block the actions to compose.
     */
    override fun <Type> Type.compose(block: ActionsPack<Type>.() -> Unit)
            where Type : UiBaseActions, Type : UiBaseAssertions,
                  Type : UiInterceptable<UiObjectInteraction, UiObjectAssertion, UiObjectAction> {
        val actions: List<() -> Unit> = ActionsPack(this).apply(block).build()
        // preparing
        rollKautomatorComposeInterception()
        // action
        callComposeActionsIntoFlakySafety(
            isKautomatorHere = this is UiBaseView<*>,
            actions = actions
        )
        // restoring the previous state
        rollbackKautomatorInterception()
    }

    private fun rollComposeInterception(element: Any) {
        when (element) {
            is KBaseView<*> -> element.rollKakaoComposeInterception()
            is UiBaseView<*> -> element.rollKautomatorComposeInterception()
            else -> throw IllegalArgumentException(
                "Incorrect type of the element is using in compose functionality." +
                        "You can use only KBaseView with its inheritors or UiBaseView with its inheritors." +
                        "Your Element here=$element."
            )
        }
    }

    private fun Interceptable<ViewInteraction, ViewAssertion, ViewAction>.rollKakaoComposeInterception() {
        val interceptor = ComposeKakaoViewInterceptor(kaspresso)

        intercept {
            onCheck(true, interceptor::interceptCheck)
            onPerform(true, interceptor::interceptPerform)
        }
    }

    private fun UiInterceptable<UiObjectInteraction, UiObjectAssertion, UiObjectAction>.rollKautomatorComposeInterception() {
        val interceptor = ComposeKautomatorObjectInterceptor(kaspresso)

        intercept {
            onCheck(true, interceptor::interceptCheck)
            onPerform(true, interceptor::interceptPerform)
        }
    }

    private fun rollbackSystemInterception(element: Any) {
        when (element) {
            is KBaseView<*> -> element.rollbackKakaoInterception()
            is UiBaseView<*> -> element.rollbackKautomatorInterception()
            else -> throw RuntimeException(
                "Incorrect type of the element is using in compose functionality." +
                        "You can use only KBaseView with its inheritors or UiBaseView with its inheritors." +
                        "Your Element here=$element."
            )
        }
    }

    private fun Interceptable<ViewInteraction, ViewAssertion, ViewAction>.rollbackKakaoInterception() {
        val interceptor = KakaoViewInterceptor(kaspresso)

        intercept {
            onCheck(true, interceptor::interceptCheck)
            onPerform(true, interceptor::interceptPerform)
        }
    }

    private fun UiInterceptable<UiObjectInteraction, UiObjectAssertion, UiObjectAction>.rollbackKautomatorInterception() {
        val interceptor = KautomatorObjectInterceptor(kaspresso)

        intercept {
            onCheck(true, interceptor::interceptCheck)
            onPerform(true, interceptor::interceptPerform)
        }
    }

    /**
     * @param isKautomatorHere boolean parameter showing whether there are actions with Kautomator views among @parameter actions
     * @param actions list of actions with views participating in compose
     * @return the order number of a succeed branch in compose
     */
    private fun callComposeActionsIntoFlakySafety(isKautomatorHere: Boolean, actions: List<() -> Unit>): Int {
        // the choosing of FlakySafetyProvider and FailureLoggingProvider
        // if there is Kautomator here then we will use Kautomator providers to guarantee defending from flakiness
        val (kakaoFlakySafetyProvider, kakaoFailureLoggingProvider) = getKakaoProviders()
        val (kautomatorFlakySafetyProvider, kautomatorFailureLoggingProvider) = getKautomatorProviders()

        val resultFlakySafetyProvider =
            if (isKautomatorHere && kautomatorFlakySafetyProvider != null) kautomatorFlakySafetyProvider else kakaoFlakySafetyProvider
        val resultFailureLoggingProvider = if (isKautomatorHere && kautomatorFailureLoggingProvider != null)
            kautomatorFailureLoggingProvider else kakaoFailureLoggingProvider

        // call compose actions inside providers
        return resultFailureLoggingProvider.withLoggingOnFailureIfNotNull {
            resultFlakySafetyProvider.flakySafelyIfNotNull {
                invokeComposed(actions, kaspresso.libLogger)
            }
        }
    }

    private fun getKakaoProviders(): Pair<FlakySafetyProvider?, FailureLoggingProvider?> {
        var flakySafetyProvider: FlakySafetyProvider? = null
        var failureLoggingProvider: FailureLoggingProvider? = null

        kaspresso.viewBehaviorInterceptors.forEach { viewBehaviorInterceptor: ViewBehaviorInterceptor ->
            when (viewBehaviorInterceptor) {
                is FlakySafeViewBehaviorInterceptor -> flakySafetyProvider = viewBehaviorInterceptor
                is FailureLoggingViewBehaviorInterceptor -> failureLoggingProvider = viewBehaviorInterceptor
            }
        }

        return Pair(flakySafetyProvider, failureLoggingProvider)
    }

    private fun getKautomatorProviders(): Pair<FlakySafetyProvider?, FailureLoggingProvider?> {
        var flakySafetyProvider: FlakySafetyProvider? = null
        var failureLoggingProvider: FailureLoggingProvider? = null

        kaspresso.objectBehaviorInterceptors.forEach { objectBehaviorInterceptor: ObjectBehaviorInterceptor ->
            when (objectBehaviorInterceptor) {
                is FlakySafeObjectBehaviorInterceptor -> flakySafetyProvider = objectBehaviorInterceptor
                is FailureLoggingObjectBehaviorInterceptor -> failureLoggingProvider = objectBehaviorInterceptor
            }
        }

        return Pair(flakySafetyProvider, failureLoggingProvider)
    }
}