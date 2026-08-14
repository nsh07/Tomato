@file:OptIn(ExperimentalFoundationStyleApi::class)

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.clear

@Composable
fun TopicNameTextField(
    name: String,
    onNameChange: (String) -> Unit,
    isError: Boolean,
    maxLines: Int,
    supportingText: String?,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isError = isError
    }
    val colorScheme = colorScheme
    val motionScheme = motionScheme

    BasicTextField(
        value = name,
        onValueChange = onNameChange,
        interactionSource = interactionSource,
        textStyle = typography.bodyLargeEmphasized.copy(color = colorScheme.onSurface),
        maxLines = maxLines,
        cursorBrush = SolidColor(colorScheme.onSurface),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .height(56.dp)
                    .styleable(styleState) {
                        shape(RoundedCornerShape(16.dp))
                        contentPadding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        background(colorScheme.secondaryContainer)
                        contentColor(colorScheme.onSurface)

                        error {
                            animate(spec = motionScheme.slowEffectsSpec()) {
                                background(colorScheme.errorContainer)
                                contentColor(colorScheme.onErrorContainer)
                            }
                        }

                        focused {
                            animate(spec = motionScheme.slowSpatialSpec()) {
                                shape(CircleShape)
                            }
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    innerTextField()
                    Spacer(Modifier.weight(1f))
                    AnimatedVisibility(
                        visible = name.isNotEmpty(),
                        enter = fadeIn() + scaleIn(motionScheme.defaultSpatialSpec()),
                        exit = fadeOut() + scaleOut(motionScheme.defaultSpatialSpec())
                    ) {
                        IconButton(
                            onClick = { onNameChange("") },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(painterResource(Res.drawable.clear), null)
                        }
                    }
                }
            }
        }
    )
}

private val ErrorKey = StyleStateKey(false)

private var MutableStyleState.isError: Boolean
    get() = this[ErrorKey]
    set(value) {
        this[ErrorKey] = value
    }

private fun StyleScope.error(block: () -> Unit) {
    state(ErrorKey, block) { key, state -> state[key] }
}

@Preview
@Composable
private fun TopicNameTextFieldPreview() {
    TomatoTheme {
        Surface {
            TopicNameTextField(
                name = "Work",
                onNameChange = {},
                isError = false,
                maxLines = 1,
                supportingText = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TopicNameTextFieldErrorPreview() {
    TomatoTheme {
        Surface {
            TopicNameTextField(
                name = "Work",
                onNameChange = {},
                isError = true,
                maxLines = 1,
                supportingText = "Topic name taken",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
