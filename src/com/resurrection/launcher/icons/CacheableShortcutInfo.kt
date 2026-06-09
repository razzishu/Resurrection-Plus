/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.resurrection.launcher.icons

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Log
import com.resurrection.launcher.BuildConfig
import com.resurrection.launcher.LauncherAppState
import com.resurrection.launcher.icons.BaseIconFactory.IconOptions
import com.resurrection.launcher.icons.cache.BaseIconCache
import com.resurrection.launcher.icons.cache.CachingLogic
import com.resurrection.launcher.shortcuts.ShortcutKey
import com.resurrection.launcher.util.ApiWrapper
import com.resurrection.launcher.util.ApplicationInfoWrapper
import com.resurrection.launcher.util.PackageUserKey
import com.resurrection.launcher.util.Themes

/** Wrapper over ShortcutInfo to provide extra information related to ShortcutInfo */
class CacheableShortcutInfo
@JvmOverloads
constructor(
    val shortcutInfo: ShortcutInfo,
    val appInfo: ApplicationInfoWrapper,
    val fallbackIconProvider: (BaseIconFactory) -> BitmapInfo? = { null },
) {

    @JvmOverloads
    constructor(
        info: ShortcutInfo,
        ctx: Context,
        fallbackIconProvider: (BaseIconFactory) -> BitmapInfo? = { null },
    ) : this(
        info,
        ApplicationInfoWrapper(ctx, info.getPackage(), info.userHandle),
        fallbackIconProvider,
    )

    companion object {
        private const val TAG = "CacheableShortcutInfo"

        /**
         * Similar to [LauncherApps.getShortcutIconDrawable] with additional Launcher specific
         * checks
         */
        @JvmStatic
        fun getIcon(context: Context, shortcutInfo: ShortcutInfo, density: Int): Drawable? {
            if (!BuildConfig.WIDGETS_ENABLED) {
                return null
            }
            try {
                return context
                    .getSystemService(LauncherApps::class.java)
                    .getShortcutIconDrawable(shortcutInfo, density)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get shortcut icon", e)
                return null
            }
        }

        /**
         * Converts the provided list of Shortcuts to CacheableShortcuts by using the application
         * info from the provided list of apps
         */
        @JvmStatic
        fun convertShortcutsToCacheableShortcuts(
            shortcuts: List<ShortcutInfo>,
            activities: List<LauncherActivityInfo>,
        ): List<CacheableShortcutInfo> {
            // Create a map of package to applicationInfo
            val appMap =
                activities.associateBy(
                    { PackageUserKey(it.componentName.packageName, it.user) },
                    { it.applicationInfo },
                )

            return shortcuts.map {
                CacheableShortcutInfo(
                    it,
                    ApplicationInfoWrapper(appMap[PackageUserKey(it.getPackage(), it.userHandle)]),
                )
            }
        }
    }
}

/** Caching logic for CacheableShortcutInfo. */
object CacheableShortcutCachingLogic : CachingLogic<CacheableShortcutInfo> {

    override fun getComponent(info: CacheableShortcutInfo): ComponentName =
        ShortcutKey.fromInfo(info.shortcutInfo).componentName

    override fun getUser(info: CacheableShortcutInfo): UserHandle = info.shortcutInfo.userHandle

    override fun getLabel(info: CacheableShortcutInfo): CharSequence? = info.shortcutInfo.shortLabel

    override fun getApplicationInfo(info: CacheableShortcutInfo) = info.appInfo.getInfo()

    override fun loadIcon(context: Context, cache: BaseIconCache, info: CacheableShortcutInfo) =
        LauncherIcons.obtain(context).use { li ->
            CacheableShortcutInfo.getIcon(
                    context,
                    info.shortcutInfo,
                    LauncherAppState.getIDP(context).fillResIconDpi,
                )
                ?.let { d ->
                    li.createBadgedIconBitmap(
                        d,
                        IconOptions()
                            .setExtractedColor(Themes.getColorAccent(context))
                            .setSourceHint(
                                getSourceHint(info, cache)
                                    .copy(
                                        isFileDrawable =
                                            ApiWrapper.INSTANCE[context].isFileDrawable(
                                                info.shortcutInfo
                                            )
                                    )
                            ),
                    )
                } ?: info.fallbackIconProvider.invoke(li) ?: BitmapInfo.LOW_RES_INFO
        }

    override fun getFreshnessIdentifier(
        item: CacheableShortcutInfo,
        provider: IconProvider,
    ): String? =
        // Manifest shortcuts get updated on every reboot. Don't include their change timestamp as
        // it gets covered by the app's version
        (if (item.shortcutInfo.isDeclaredInManifest) ""
        else item.shortcutInfo.lastChangedTimestamp.toString()) +
            "-" +
            provider.getStateForApp(getApplicationInfo(item))
}
