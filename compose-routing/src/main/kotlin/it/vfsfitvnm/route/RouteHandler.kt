package it.vfsfitvnm.route

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun RouteHandler(
    modifier: Modifier = Modifier,
    listenToGlobalEmitter: Boolean = false,
    handleBackPress: Boolean = true,
    transitionSpec: AnimatedContentScope<RouteHandlerScope>.() -> ContentTransform = { fastFade },
    content: @Composable RouteHandlerScope.() -> Unit
) {
    var route by rememberRoute()

    RouteHandler(
        route = route,
        onRouteChanged = { route = it },
        listenToGlobalEmitter = listenToGlobalEmitter,
        handleBackPress = handleBackPress,
        transitionSpec = transitionSpec,
        modifier = modifier,
        content = content
    )
}

@ExperimentalAnimationApi
@Composable
fun RouteHandler(
    route: Route?,
    onRouteChanged: (Route?) -> Unit,
    modifier: Modifier = Modifier,
    listenToGlobalEmitter: Boolean = false,
    handleBackPress: Boolean = true,
    transitionSpec: AnimatedContentScope<RouteHandlerScope>.() -> ContentTransform = { fastFade },
    content: @Composable RouteHandlerScope.() -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val parameters = rememberSaveable {
        arrayOfNulls<Any?>(2)
    }

    val scope = remember(route) {
        RouteHandlerScope(
            route = route,
            parameters = parameters,
            push = onRouteChanged,
            pop = { if (handleBackPress) backDispatcher?.onBackPressed() else onRouteChanged(null) }
        )
    }

    if (listenToGlobalEmitter) {
        LaunchedEffect(route) {
            Route.GlobalEmitter.listener = if (route == null) ({ newRoute, newParameters ->
                newParameters.forEachIndexed(parameters::set)
                onRouteChanged(newRoute)
            }) else null
        }
    }

    BackHandler(enabled = handleBackPress && route != null) {
        onRouteChanged(null)
    }

    updateTransition(targetState = scope, label = null).AnimatedContent(
        transitionSpec = transitionSpec,
        contentKey = RouteHandlerScope::route,
        modifier = modifier,
    ) {
        it.content()
    }
}
