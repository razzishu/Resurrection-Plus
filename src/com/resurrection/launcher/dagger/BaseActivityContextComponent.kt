/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.resurrection.launcher.dagger

import com.resurrection.launcher.allapps.AllAppsStore
import com.resurrection.launcher.popup.PopupDataProvider
import com.resurrection.launcher.qsb.OseWidgetOptionsProvider
import com.resurrection.launcher.recyclerview.AllAppsRecyclerViewPool
import com.resurrection.launcher.recyclerview.AllAppsRecyclerViewPool.Companion.PRELOAD_ALL_APPS_DAGGER_KEY
import com.resurrection.launcher.secondarydisplay.SecondaryDisplayDelegate
import com.resurrection.launcher.views.ActivityContext
import dagger.BindsInstance
import javax.inject.Named

/** Base component for ActivityContext Dagger injection. */
interface BaseActivityContextComponent {

    fun getSecondaryDisplayDelegate(): SecondaryDisplayDelegate

    fun getOseWidgetOptionsProvider(): OseWidgetOptionsProvider

    val appsStore: AllAppsStore
    val popupDataProvider: PopupDataProvider
    val sharedAppsPool: AllAppsRecyclerViewPool

    /** Builder for BaseActivityContextComponent. */
    interface Builder {
        @BindsInstance fun activityContext(activityContext: ActivityContext): Builder

        @BindsInstance
        fun setAllAppsPreloaded(
            @Named(PRELOAD_ALL_APPS_DAGGER_KEY) preloadAllApps: Boolean
        ): Builder

        fun build(): BaseActivityContextComponent
    }
}
