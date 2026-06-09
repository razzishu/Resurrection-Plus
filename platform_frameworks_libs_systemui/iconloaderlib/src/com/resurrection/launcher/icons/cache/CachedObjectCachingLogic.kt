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

package com.resurrection.launcher.icons.cache

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.resurrection.launcher.icons.BaseIconFactory.IconOptions
import com.resurrection.launcher.icons.BitmapInfo
import com.resurrection.launcher.icons.IconProvider

/** Caching logic for ComponentWithLabelAndIcon */
object CachedObjectCachingLogic : CachingLogic<CachedObject> {

    override fun getComponent(item: CachedObject): ComponentName = item.component

    override fun getUser(item: CachedObject): UserHandle = item.user

    override fun getLabel(item: CachedObject): CharSequence? = item.label

    override fun loadIcon(context: Context, cache: BaseIconCache, item: CachedObject): BitmapInfo {
        val d = item.getFullResIcon(cache) ?: return BitmapInfo.LOW_RES_INFO
        cache.iconFactory.use { li ->
            return li.createBadgedIconBitmap(
                d,
                IconOptions().setUser(item.user).setSourceHint(getSourceHint(item, cache)),
            )
        }
    }

    override fun getApplicationInfo(item: CachedObject) = item.applicationInfo

    override fun getFreshnessIdentifier(item: CachedObject, provider: IconProvider): String? =
        item.getFreshnessIdentifier(provider)
}
