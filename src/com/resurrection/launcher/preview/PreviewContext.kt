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
package com.resurrection.launcher.preview

import android.content.Context
import android.text.TextUtils
import com.resurrection.launcher.InvariantDeviceProfile
import com.resurrection.launcher.Item
import com.resurrection.launcher.LauncherModel
import com.resurrection.launcher.LauncherPrefs
import com.resurrection.launcher.ProxyPrefs
import com.resurrection.launcher.WorkspaceLayoutManager
import com.resurrection.launcher.compose.core.widgetpicker.NoOpWidgetPickerModule
import com.resurrection.launcher.concurrent.ExecutorsModule
import com.resurrection.launcher.dagger.ApiWrapperModule
import com.resurrection.launcher.dagger.AppModule
import com.resurrection.launcher.dagger.ApplicationContext
import com.resurrection.launcher.dagger.HomeScreenFilesModule
import com.resurrection.launcher.dagger.LauncherAppComponent
import com.resurrection.launcher.dagger.LauncherAppSingleton
import com.resurrection.launcher.dagger.LauncherComponentProvider.appComponent
import com.resurrection.launcher.dagger.LauncherConcurrencyModule
import com.resurrection.launcher.dagger.LauncherModelModule
import com.resurrection.launcher.dagger.PerDisplayModule
import com.resurrection.launcher.dagger.PluginManagerWrapperModule
import com.resurrection.launcher.dagger.SettingsModule
import com.resurrection.launcher.dagger.StaticObjectModule
import com.resurrection.launcher.dagger.SystemDragModule
import com.resurrection.launcher.dagger.WindowManagerProxyModule
import com.resurrection.launcher.model.ModelInitializer
import com.resurrection.launcher.model.data.LoaderParams
import com.resurrection.launcher.provider.LauncherDbUtils.selectionForWorkspaceScreen
import com.resurrection.launcher.qsb.QsbAppWidgetHost
import com.resurrection.launcher.util.SandboxContext
import com.resurrection.launcher.util.dagger.LauncherExecutorsModule
import com.resurrection.launcher.widget.LauncherWidgetHolder
import com.resurrection.launcher.widget.LauncherWidgetHolder.WidgetHolderFactory
import com.resurrection.launcher.widget.LocalColorExtractor
import com.resurrection.launcher.widget.util.WidgetSizeHandler
import com.android.systemui.shared.Flags
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.io.File
import java.util.Arrays
import java.util.UUID
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Context used just for preview. It also provides a few objects (e.g. UserCache) just for preview
 * purposes.
 */
class PreviewContext
@JvmOverloads
constructor(
    base: Context,
    gridName: String?,
    widgetHostId: Int = LauncherWidgetHolder.APPWIDGET_HOST_ID,
    layoutXml: String? = null,
    workspacePageId: Int = WorkspaceLayoutManager.FIRST_SCREEN_ID,
) : SandboxContext(base) {
    private val mPrefName: String

    private val mDbDir: File?

    init {
        val randomUid = UUID.randomUUID().toString()
        mPrefName = "preview-$randomUid"
        val prefs = ProxyPrefs(this, getSharedPreferences(mPrefName, MODE_PRIVATE))
        prefs.putOrRemove(LauncherPrefs.GRID_NAME, gridName)
        prefs.put(LauncherPrefs.FIXED_LANDSCAPE_MODE, false)

        val isTwoPanel =
            base.appComponent.idp.supportedProfiles.any { it.deviceProperties.isTwoPanels }
        val closestEvenPageId: Int = workspacePageId - (workspacePageId % 2)
        val selectionQuery =
            if (isTwoPanel) selectionForWorkspaceScreen(closestEvenPageId, closestEvenPageId + 1)
            else selectionForWorkspaceScreen(workspacePageId)

        val builder = DaggerPreviewContext_PreviewAppComponent.builder().bindPrefs(prefs)
        builder.bindLoaderParams(
            LoaderParams(
                workspaceSelection = selectionQuery,
                sanitizeData = false,
                loadNonWorkspaceItems = false,
            )
        )

        // Bind the LauncherApp's single QsbAppWidgetHost to PreviewComponent. This way same
        // AppWidgetHost is shared between the Preview and Launcher.
        // If the AppWidgetHost's are different, they will compete with each other for the same
        // AppWidgetHost Id and this will cause either launcher appcomponent or preview to app
        // component to go out of sync.
        builder.bindQsbAppWidgetHost(base.appComponent.qsbAppWidgetHost)

        if (layoutXml.isNullOrEmpty() || !Flags.extendibleThemeManager()) {
            mDbDir = null
            initDaggerComponent(builder.bindWidgetsFactory(base.appComponent.widgetHolderFactory))
        } else {
            mDbDir = File(base.filesDir, randomUid)
            emptyDbDir()
            mDbDir.mkdirs()
            initDaggerComponent(
                builder.bindWidgetsFactory { NonPrimaryWidgetHolder(it, widgetHostId) }
            )
            appComponent.layoutParserFactory.overrideXmlLayout(layoutXml)
        }

        if (!TextUtils.isEmpty(layoutXml)) {
            // Use null the DB file so that we use a new in-memory DB
            InvariantDeviceProfile.INSTANCE[this].dbFile = null
        }
    }

    fun <T : Any> LauncherPrefs.putOrRemove(item: Item, value: T?) {
        if (value != null) put(item, value) else remove(item)
    }

    private fun emptyDbDir() {
        if (mDbDir != null && mDbDir.exists()) {
            Arrays.stream(mDbDir.listFiles()).forEach { obj: File -> obj.delete() }
        }
    }

    override fun cleanUpObjects() {
        super.cleanUpObjects()
        deleteSharedPreferences(mPrefName)
        if (mDbDir != null) {
            emptyDbDir()
            mDbDir.delete()
        }
    }

    override fun getDatabasePath(name: String): File =
        if (mDbDir != null) File(mDbDir, name) else super.getDatabasePath(name)

    class NoOpWidgetSizeHandler
    @Inject
    constructor(@ApplicationContext context: Context, idp: InvariantDeviceProfile) :
        WidgetSizeHandler(context, idp) {

        override fun updateSizeRangesAsync(
            widgetId: Int,
            spanX: Int,
            spanY: Int,
            executor: Executor,
        ) {
            // Ignore
        }
    }

    private class NonPrimaryWidgetHolder(context: Context, hostId: Int) :
        LauncherWidgetHolder(context, hostId) {

        override fun startListeningForSharedUpdate() = startListening()
    }

    @Module
    abstract class PreviewModule {

        @Binds abstract fun bindWidgetSizeHandler(handler: NoOpWidgetSizeHandler): WidgetSizeHandler
    }

    @LauncherAppSingleton // Exclude widget module since we bind widget holder separately
    @Component(
        modules =
            [
                WindowManagerProxyModule::class,
                ApiWrapperModule::class,
                PluginManagerWrapperModule::class,
                StaticObjectModule::class,
                AppModule::class,
                PerDisplayModule::class,
                LauncherConcurrencyModule::class,
                ExecutorsModule::class,
                LauncherExecutorsModule::class,
                NoOpWidgetPickerModule::class,
                LauncherModelModule::class,
                PreviewModule::class,
                HomeScreenFilesModule::class,
                SettingsModule::class,
                SystemDragModule::class,
            ]
    )
    interface PreviewAppComponent : LauncherAppComponent {
        val model: LauncherModel
        val modelInitializer: ModelInitializer
        val localColorExtractor: LocalColorExtractor

        /** Builder for NexusLauncherAppComponent. */
        @Component.Builder
        interface Builder : LauncherAppComponent.Builder {
            @BindsInstance fun bindPrefs(prefs: LauncherPrefs): Builder

            @BindsInstance fun bindWidgetsFactory(holderFactory: WidgetHolderFactory): Builder

            @BindsInstance fun bindLoaderParams(params: LoaderParams): Builder

            @BindsInstance fun bindQsbAppWidgetHost(host: QsbAppWidgetHost): Builder

            override fun build(): PreviewAppComponent
        }
    }
}
