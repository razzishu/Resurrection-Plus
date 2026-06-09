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

package com.resurrection.launcher.icons.cache

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.UserHandle
import com.resurrection.launcher.icons.BaseIconFactory.IconOptions
import com.resurrection.launcher.icons.BitmapInfo
import com.resurrection.launcher.icons.IconProvider
import com.resurrection.launcher.icons.cache.BaseIconCache.Companion.EMPTY_CLASS_NAME

/** Caching logic for ApplicationInfo */
class AppInfoCachingLogic(
    private val pm: PackageManager,
    private val instantAppResolver: (ApplicationInfo) -> Boolean,
    private val errorLogger: (String, Exception?) -> Unit = { _, _ -> },
) : CachingLogic<ApplicationInfo> {

    override fun getComponent(item: ApplicationInfo) =
        ComponentName(item.packageName, item.packageName + EMPTY_CLASS_NAME)

    override fun getUser(item: ApplicationInfo) = UserHandle.getUserHandleForUid(item.uid)

    override fun getLabel(item: ApplicationInfo) = item.loadLabel(pm)

    override fun getApplicationInfo(item: ApplicationInfo) = item

    override fun loadIcon(
        context: Context,
        cache: BaseIconCache,
        item: ApplicationInfo,
    ): BitmapInfo {
        // Load the full res icon for the application, but if useLowResIcon is set, then
        // only keep the low resolution icon instead of the larger full-sized icon
        val appIcon = cache.iconProvider.getIcon(item)
        if (context.packageManager.isDefaultApplicationIcon(appIcon)) {
            errorLogger.invoke(
                String.format("Default icon returned for %s", item.packageName),
                null,
            )
        }

        return cache.iconFactory.use { li ->
            li.createBadgedIconBitmap(
                appIcon,
                IconOptions()
                    .setUser(getUser(item))
                    .setInstantApp(instantAppResolver.invoke(item))
                    .setSourceHint(getSourceHint(item, cache)),
            )
        }
    }

    override fun getFreshnessIdentifier(item: ApplicationInfo, iconProvider: IconProvider) =
        iconProvider.getStateForApp(item)
}
