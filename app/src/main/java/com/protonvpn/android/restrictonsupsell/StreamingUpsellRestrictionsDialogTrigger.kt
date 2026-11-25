/*
 * Copyright (c) 2026. Proton AG
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

package com.protonvpn.android.restrictonsupsell

import android.app.Activity
import com.protonvpn.android.auth.usecase.CurrentUser
import com.protonvpn.android.di.WallClock
import com.protonvpn.android.telemetry.UpgradeSource
import com.protonvpn.android.telemetry.UpgradeTrigger
import com.protonvpn.android.tv.IsTvCheck
import com.protonvpn.android.ui.ForegroundActivityTracker
import com.protonvpn.android.ui.planupgrade.UpgradeDialogLauncher
import com.protonvpn.android.utils.flatMapLatestFreeUser
import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Reusable
class OpenUpgradeStreamingBlockDialog @Inject constructor(
    private val upgradeDialogLauncher: UpgradeDialogLauncher,
) {
    operator fun invoke(activity: Activity) {
        upgradeDialogLauncher.launch(activity, UpgradeSource.STREAMING_ACTIVITY, UpgradeTrigger.NETWORK_RESTRICTION)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
@Suppress("UNUSED_PARAMETER", "unused")
class StreamingUpsellRestrictionsDialogTrigger @Inject constructor(
    private val mainScope: CoroutineScope,
    private val isTv: IsTvCheck,
    private val eventStreamingRestricted: StreamingUpsellRestrictionsFlow,
    private val currentUser: CurrentUser,
    private val foregroundActivityTracker: ForegroundActivityTracker,
    private val restrictionsUpsellStore: RestrictionsUpsellStore,
    private val openUpgradeDialog: OpenUpgradeStreamingBlockDialog,
    @param:WallClock private val now: () -> Long,
) {
    fun start() {
        // TV upsell will be implemented in the future.
        if (isTv()) return

        currentUser.vpnUserFlow
            .flatMapLatestFreeUser {
                eventStreamingRestricted.onEach { restrictions ->
                    restrictionsUpsellStore.update { current ->
                        val streaming =
                            current.streaming.copy(
                                lastEventTimestamp = now(),
                                lastEventConnectionId = restrictions.connectionId.toString(),
                            )
                        current.copy(streaming = streaming)
                    }
                }
            }
            .launchIn(mainScope)
    }

    // Retain the entry point for existing notification intents without opening a promotion.
    suspend fun showNow(activity: Activity) = Unit
}
