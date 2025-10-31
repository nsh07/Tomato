/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen.components

import android.app.LocaleConfig
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocaleBottomSheet(
    currentLocales: LocaleList,
    setShowSheet: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val supportedLocales = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleConfig(context).supportedLocales
        } else null
    }
    val supportedLocalesSize = supportedLocales?.size() ?: 0

    val supportedLocalesList: List<AppLocale>? = remember {
        if (supportedLocales != null) {
            buildList {
                for (i in 0 until supportedLocalesSize) {
                    add(AppLocale(supportedLocales.get(i), supportedLocales.get(i).displayName))
                }
                sortWith(compareBy { it.name })
            }
        } else null
    }

    val bottomSheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = { setShowSheet(false) },
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.choose_language),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (supportedLocalesList != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    state = listState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(shapes.largeIncreased)
                ) {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.system_default))
                            },
                            trailingContent = {
                                if (currentLocales.isEmpty)
                                    Icon(
                                        painterResource(R.drawable.check),
                                        contentDescription = stringResource(R.string.selected)
                                    )
                            },
                            colors =
                                if (currentLocales.isEmpty)
                                    ListItemDefaults.colors(
                                        containerColor = colorScheme.primaryContainer.copy(
                                            0.3f
                                        )
                                    )
                                else listItemColors,
                            modifier = Modifier
                                .clip(if (currentLocales.isEmpty) CircleShape else shapes.largeIncreased)
                                .clickable(
                                    onClick = {
                                        scope
                                            .launch { bottomSheetState.hide() }
                                            .invokeOnCompletion {
                                                if (Build.VERSION.SDK_INT >= 33) {
                                                    context
                                                        .getSystemService(LocaleManager::class.java)
                                                        .applicationLocales = LocaleList()
                                                }
                                                setShowSheet(false)
                                            }
                                    }
                                )
                        )
                    }
                    item {
                        Spacer(Modifier.height(12.dp))
                    }
                    itemsIndexed(
                        supportedLocalesList,
                        key = { _: Int, it: AppLocale -> it.name }
                    ) { index, it ->
                        ListItem(
                            headlineContent = {
                                Text(it.name)
                            },
                            trailingContent = {
                                if (!currentLocales.isEmpty && it.locale == currentLocales.get(0))
                                    Icon(
                                        painterResource(R.drawable.check),
                                        tint = colorScheme.primary,
                                        contentDescription = stringResource(R.string.selected)
                                    )
                            },
                            colors =
                                if (!currentLocales.isEmpty && it.locale == currentLocales.get(0))
                                    ListItemDefaults.colors(containerColor = colorScheme.primaryContainer)
                                else listItemColors,
                            modifier = Modifier
                                .clip(
                                    if (!currentLocales.isEmpty && it.locale == currentLocales.get(0))
                                        CircleShape
                                    else when (index) {
                                        0 -> topListItemShape
                                        supportedLocalesSize - 1 -> bottomListItemShape
                                        else -> middleListItemShape
                                    }
                                )
                                .clickable(
                                    onClick = {
                                        scope
                                            .launch { bottomSheetState.hide() }
                                            .invokeOnCompletion { _ ->
                                                if (Build.VERSION.SDK_INT >= 33) {
                                                    context.getSystemService(LocaleManager::class.java)
                                                        .applicationLocales =
                                                        LocaleList(it.locale)
                                                }
                                                setShowSheet(false)
                                            }
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

data class AppLocale(
    val locale: Locale,
    val name: String
)