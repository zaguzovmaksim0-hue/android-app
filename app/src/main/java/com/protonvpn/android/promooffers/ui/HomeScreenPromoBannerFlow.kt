/*
 * Copyright (c) 2024. Proton AG
 *
 * This file is part of ProtonVPN.
 *
 * ProtonVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonVPN.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.protonvpn.android.promooffers.ui

import com.protonvpn.android.promooffers.data.ApiNotificationManager
import com.protonvpn.android.promooffers.data.ApiNotificationOfferButton
import com.protonvpn.android.promooffers.data.PromoOffersPrefs
import com.protonvpn.android.promooffers.usecase.EnsureIapOfferStillValid
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class PromoOfferBannerState(
    val imageUrl: String,
    val alternativeText: String,
    val action: ApiNotificationOfferButton,
    val isDismissible: Boolean,
    val endTimestamp: Long?,
    val notificationId: String,
    val reference: String?,
)

@Reusable
@Suppress("UNUSED_PARAMETER")
class HomeScreenPromoBannerFlow @Inject constructor(
    apiNotificationManager: ApiNotificationManager,
    promoOffersPrefs: PromoOffersPrefs,
    ensureIapOfferStillValid: EnsureIapOfferStillValid,
) {

    operator fun invoke(isNighMode: Boolean): Flow<PromoOfferBannerState?> = flowOf(null)

    fun hasBannerFlow(): Flow<Boolean> = flowOf(false)
}
