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

import android.util.Log
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ENTITLEMENT_ID = "plus"

/**
 * Google Play implementation of BillingManager
 */
class PlayBillingManager : BillingManager {
    private val _isPlus = MutableStateFlow(false)
    override val isPlus = _isPlus.asStateFlow()

    private val purchases by lazy { Purchases.sharedInstance }

    init {
        purchases.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { customerInfo ->
                _isPlus.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
            }

        // Fetch initial customer info
        purchases.getCustomerInfoWith(
            onSuccess = { customerInfo ->
                _isPlus.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
            },
            onError = { error ->
                Log.e("GooglePlayPaywallManager", "Error fetching customer info: $error")
            }
        )
    }
}

object BillingManagerProvider {
    val manager: BillingManager = PlayBillingManager()
}