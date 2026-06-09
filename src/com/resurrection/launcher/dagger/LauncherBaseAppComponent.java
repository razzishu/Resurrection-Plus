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

package com.resurrection.launcher.dagger;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.resurrection.launcher.InvariantDeviceProfile;
import com.resurrection.launcher.LauncherAppState;
import com.resurrection.launcher.LauncherPrefs;
import com.resurrection.launcher.MainProcessInitializer;
import com.resurrection.launcher.RemoveAnimationSettingsTracker;
import com.resurrection.launcher.backuprestore.LauncherRestoreEventLogger;
import com.resurrection.launcher.compose.core.widgetpicker.WidgetPickerComposeWrapper;
import com.resurrection.launcher.dragndrop.SystemDragController;
import com.resurrection.launcher.folder.FolderNameSuggestionLoader;
import com.resurrection.launcher.graphics.GridCustomizationsProxy;
import com.resurrection.launcher.graphics.ThemeManager;
import com.resurrection.launcher.graphics.theme.ThemePreference;
import com.resurrection.launcher.homescreenfiles.HomeScreenFilesProvider;
import com.resurrection.launcher.icons.IconChangeTracker;
import com.resurrection.launcher.icons.LauncherIcons.IconPool;
import com.resurrection.launcher.logging.DumpManager;
import com.resurrection.launcher.logging.StatsLogManager;
import com.resurrection.launcher.model.GridSizeMigrationLogic;
import com.resurrection.launcher.model.ItemInstallQueue;
import com.resurrection.launcher.model.LayoutParserFactory;
import com.resurrection.launcher.model.LoaderCursor.LoaderCursorFactory;
import com.resurrection.launcher.model.TestableModelState;
import com.resurrection.launcher.notification.NotificationRepository;
import com.resurrection.launcher.pm.InstallSessionHelper;
import com.resurrection.launcher.pm.UserCache;
import com.resurrection.launcher.popup.PopupDataRepository;
import com.resurrection.launcher.qsb.OSEManager;
import com.resurrection.launcher.qsb.OseWidgetManager;
import com.resurrection.launcher.qsb.QsbAppWidgetHost;
import com.resurrection.launcher.testing.TestInformationHandler;
import com.resurrection.launcher.util.ApiWrapper;
import com.resurrection.launcher.util.DaggerSingletonTracker;
import com.resurrection.launcher.util.DisplayController;
import com.resurrection.launcher.util.DynamicResource;
import com.resurrection.launcher.util.InstantAppResolver;
import com.resurrection.launcher.util.LayoutImportExportHelper;
import com.resurrection.launcher.util.LockedUserState;
import com.resurrection.launcher.util.MSDLPlayerWrapper;
import com.resurrection.launcher.util.PackageManagerHelper;
import com.resurrection.launcher.util.PluginManagerWrapper;
import com.resurrection.launcher.util.ScreenOnTracker;
import com.resurrection.launcher.util.SettingsCache;
import com.resurrection.launcher.util.TaskbarModeUtil;
import com.resurrection.launcher.util.VibratorWrapper;
import com.resurrection.launcher.util.WallpaperColorHints;
import com.resurrection.launcher.util.window.RefreshRateTracker;
import com.resurrection.launcher.util.window.WindowManagerProxy;
import com.resurrection.launcher.widget.LauncherWidgetHolder.WidgetHolderFactory;
import com.resurrection.launcher.widget.custom.CustomWidgetManager;
import com.resurrection.launcher.widget.util.WidgetSizeHandler;

import dagger.BindsInstance;

import javax.inject.Named;

/**
 * Launcher base component for Dagger injection.
 *
 * This class is not actually annotated as a Dagger component, since it is not used directly as one.
 * Doing so generates unnecessary code bloat.
 *
 * See {@link LauncherAppComponent} for the one actually used by AOSP.
 */
public interface LauncherBaseAppComponent {
    DaggerSingletonTracker getDaggerSingletonTracker();
    ApiWrapper getApiWrapper();
    CustomWidgetManager getCustomWidgetManager();
    DynamicResource getDynamicResource();
    InstallSessionHelper getInstallSessionHelper();
    ItemInstallQueue getItemInstallQueue();
    RefreshRateTracker getRefreshRateTracker();
    ScreenOnTracker getScreenOnTracker();
    SettingsCache getSettingsCache();
    PackageManagerHelper getPackageManagerHelper();
    PluginManagerWrapper getPluginManagerWrapper();
    VibratorWrapper getVibratorWrapper();
    MSDLPlayerWrapper getMSDLPlayerWrapper();
    WindowManagerProxy getWmProxy();
    LauncherPrefs getLauncherPrefs();
    ThemeManager getThemeManager();
    UserCache getUserCache();
    DisplayController getDisplayController();
    WallpaperColorHints getWallpaperColorHints();
    LockedUserState getLockedUserState();
    InvariantDeviceProfile getIDP();
    IconPool getIconPool();
    RemoveAnimationSettingsTracker getRemoveAnimationSettingsTracker();
    LauncherAppState getLauncherAppState();
    LauncherRestoreEventLogger getLauncherRestoreEventLogger();
    GridCustomizationsProxy getGridCustomizationsProxy();
    FolderNameSuggestionLoader getFolderNameSuggestionLoader();
    LoaderCursorFactory getLoaderCursorFactory();
    WidgetHolderFactory getWidgetHolderFactory();
    RefreshRateTracker getFrameRateProvider();
    InstantAppResolver getInstantAppResolver();
    DumpManager getDumpManager();
    StatsLogManager.StatsLogManagerFactory getStatsLogManagerFactory();
    ActivityContextComponent.Builder getActivityContextComponentBuilder();
    WidgetPickerComposeWrapper getWidgetPickerComposeWrapper();
    WidgetSizeHandler getWidgetSizeHandler();
    MainProcessInitializer getMainProcessInitializer();
    OseWidgetManager getOseWidgetManager();
    OSEManager getOseManager();
    QsbAppWidgetHost getQsbAppWidgetHost();
    TestInformationHandler getTestInformationHandler();
    TaskbarModeUtil getTaskbarModeUtil();
    SystemDragController getSystemDragController();

    /** Utility class for importing/exporting launcher layout */
    LayoutImportExportHelper getLayoutImportExportHelper();
    /** Returns the layout parser factory for default layout parsing */
    LayoutParserFactory getLayoutParserFactory();

    @VisibleForTesting
    GridSizeMigrationLogic createNewGridSizeMigrationLogic();
    /** Returns reference to various model objects used for test verification */
    TestableModelState getTestableModelState();

    PopupDataRepository getPopupDataRepository();
    NotificationRepository getNotificationRepository();
    HomeScreenFilesProvider getHomeScreenFilesProvider();

    /** Preferences for icon theme */
    ThemePreference getThemePreference();

    /** Tracker for any app icon changes */
    IconChangeTracker getIconChangeTracker();

    /** Builder for LauncherBaseAppComponent. */
    interface Builder {
        @BindsInstance Builder appContext(@ApplicationContext Context context);
        @BindsInstance Builder iconsDbName(@Nullable @Named("ICONS_DB") String dbFileName);
        @BindsInstance Builder setSafeModeEnabled(@Named("SAFE_MODE") boolean safeModeEnabled);
        LauncherBaseAppComponent build();
    }
}
