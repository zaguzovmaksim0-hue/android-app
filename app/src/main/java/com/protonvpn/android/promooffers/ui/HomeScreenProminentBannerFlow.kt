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

import com.protonvpn.android.promooffers.data.ApiNotificationActions
import com.protonvpn.android.promooffers.data.ApiNotificationManager
import com.protonvpn.android.promooffers.data.ApiNotificationOffer
import com.protonvpn.android.promooffers.data.ApiNotificationOfferButton
import com.protonvpn.android.promooffers.data.ApiNotificationProminentBannerStyle
import com.protonvpn.android.promooffers.data.ApiNotificationTypes
import com.protonvpn.android.promooffers.data.PromoOffersPrefs
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ProminentBannerState(
    val title: String?,
    val description: String?,
    val actionButton: ApiNotificationOfferButton?,
    val dismissButtonText: String,
    val style: ApiNotificationProminentBannerStyle,
    val notificationId: String,
    val reference: String?,
)

@Reusable
class HomeScreenProminentBannerFlow @Inject constructor(
    apiNotificationManager: ApiNotificationManager,
    promoOffersPrefs: PromoOffersPrefs,
) : Flow<ProminentBannerState?> {

    private val activeNotificationsFlow = combine(
        apiNotificationManager.activeListFlow,
        promoOffersPrefs.visitedOffersFlow
    ) { notifications, dismissedOffers ->
        notifications.firstOrNull { notification ->
            val offer = notification.offer
            val banner = offer?.prominentBanner
            notification.type == ApiNotificationTypes.TYPE_HOME_PROMINENT_BANNER &&
                !dismissedOffers.contains(notification.id) && offer != null && banner != null &&
                (banner.style == ApiNotificationProminentBannerStyle.WARNING || !offer.hasPurchasePromotion())
        }
    }

    private val stateFlow: Flow<ProminentBannerState?> = activeNotificationsFlow.map { notification ->
        val banner = notification?.offer?.prominentBanner
        banner?.let {
            ProminentBannerState(
                title = it.title,
                description = it.description,
                actionButton = it.actionButton,
                dismissButtonText = it.dismissButtonText,
                style = it.style,
                notificationId = notification.id,
                reference = notification.reference
            )
        }
    }

    // REGULAR is not a reliable promotion marker. Keep notices without explicit purchase signals.
    private fun ApiNotificationOffer.hasPurchasePromotion(): Boolean =
        ApiNotificationActions.isInAppPurchasePopup(action) ||
            panel?.iapProductDetails != null || !panel?.incentivePrice.isNullOrBlank() ||
            ApiNotificationActions.isInAppPurchasePopup(panel?.button?.action) ||
            panel?.button?.panel?.iapProductDetails != null || !panel?.button?.panel?.incentivePrice.isNullOrBlank() ||
            ApiNotificationActions.isInAppPurchasePopup(prominentBanner?.actionButton?.action) ||
            prominentBanner?.actionButton?.panel?.iapProductDetails != null ||
            !prominentBanner?.actionButton?.panel?.incentivePrice.isNullOrBlank()

    override suspend fun collect(collector: FlowCollector<ProminentBannerState?>) {
        stateFlow.collect(collector)
    }
}
