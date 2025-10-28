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

package org.nsh07.pomodoro.billing

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter
import org.nsh07.pomodoro.R

@Composable
fun TomatoPlusPaywallDialog(
    isPlus: Boolean,
    onDismiss: () -> Unit
) {
    val paywallOptions = remember {
        PaywallOptions.Builder(dismissRequest = onDismiss).build()
    }

    Scaffold { innerPadding ->
        if (!isPlus) {
            Paywall(paywallOptions)

            FilledTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    null
                )
            }
        } else {
            CustomerCenter(onDismiss = onDismiss)
        }
    }
}