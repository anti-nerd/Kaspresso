[kaspresso](../../index.md) / [com.kaspersky.kaspresso.proxy](../index.md) / [ViewAssertionProxy](./index.md)

# ViewAssertionProxy

`class ViewAssertionProxy : ViewAssertion`

The proxy-wrapper of [ViewAssertion](#) for watcher interceptors calls.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `ViewAssertionProxy(viewAssertion: ViewAssertion, watcherInterceptors: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ViewAssertionWatcherInterceptor`](../../com.kaspersky.kaspresso.interceptors.watcher.view/-view-assertion-watcher-interceptor/index.md)`>)`<br>The proxy-wrapper of [ViewAssertion](#) for watcher interceptors calls. |

### Functions

| Name | Summary |
|---|---|
| [check](check.md) | `fun check(view: `[`View`](https://developer.android.com/reference/android/view/View.html)`?, noViewFoundException: NoMatchingViewException?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Calls watcher interceptors before [ViewAssertion.check](#) on wrapped [viewAssertion](#) is called. |
