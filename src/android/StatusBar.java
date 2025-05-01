/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.cordova.statusbar;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

public class StatusBar extends CordovaPlugin {
    private static final String TAG = "StatusBar";

    private static final String ACTION_READY = "_ready";
    private static final String ACTION_OVERLAYS_WEB_VIEW = "overlaysWebView";
    private static final String ACTION_STYLE_DEFAULT = "styleDefault";
    private static final String ACTION_STYLE_LIGHT_CONTENT = "styleLightContent";
    private static final String ACTION_STYLE_DARK_CONTENT = "styleDarkContent";

    private static final String STYLE_DEFAULT = "default";
    private static final String STYLE_LIGHT_CONTENT = "lightcontent";
    private static final String STYLE_DARK_CONTENT = "darkcontent";

    private AppCompatActivity activity;
    private Window window;

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        LOG.v(TAG, "StatusBar: initialization");
        super.initialize(cordova, webView);

        activity = this.cordova.getActivity();
        window = activity.getWindow();

        activity.runOnUiThread(() -> {
            // Clear flag FLAG_FORCE_NOT_FULLSCREEN which is set initially by Cordova
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            setStatusBarTransparent(preferences.getBoolean("StatusBarOverlaysWebView", true));
            setStatusBarStyle(preferences.getString("StatusBarStyle", STYLE_LIGHT_CONTENT).toLowerCase());

            String navBarStyle = preferences.getString("NavigationBarStyle", STYLE_LIGHT_CONTENT).toLowerCase();
            String navBarColor = preferences.getString("NavigationBarColor", "#000000");
            setNavigationBarStyle(navBarStyle, navBarColor);
        });
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) {
        LOG.v(TAG, "Executing action: " + action);

        switch (action) {
            case ACTION_READY:
                boolean statusBarVisible = (window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, statusBarVisible));
                return true;

            case ACTION_OVERLAYS_WEB_VIEW:
                activity.runOnUiThread(() -> {
                    try {
                        setStatusBarTransparent(args.getBoolean(0));
                    } catch (JSONException ignore) {
                        LOG.e(TAG, "Invalid boolean argument");
                    }
                });
                return true;

            case ACTION_STYLE_DEFAULT:
                activity.runOnUiThread(() -> setStatusBarStyle(STYLE_DEFAULT));
                return true;

            case ACTION_STYLE_LIGHT_CONTENT:
                activity.runOnUiThread(() -> setStatusBarStyle(STYLE_LIGHT_CONTENT));
                activity.runOnUiThread(() -> {
                    try {
                        setNavigationBarStyle(STYLE_LIGHT_CONTENT, args.getString(0));
                    } catch (JSONException ignore) {
                        LOG.e(TAG, "Missing or invalid color argument for light content; using default colour", e);
                    }
                });
                return true;


            case ACTION_STYLE_DARK_CONTENT:
                activity.runOnUiThread(() -> setStatusBarStyle(STYLE_DARK_CONTENT));
                activity.runOnUiThread(() -> {
                    try {
                        setNavigationBarStyle(STYLE_DARK_CONTENT, args.getString(0));
                    } catch (JSONException ignore) {
                        LOG.e(TAG, "Missing or invalid color argument for light content; using default colour", e);
                    }
                });
                return true;

            default:
                return false;
        }
    }

    private void setStatusBarTransparent(final boolean isTransparent) {
        final Window window = cordova.getActivity().getWindow();
        int visibility = isTransparent
            ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            : View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE;

        window.getDecorView().setSystemUiVisibility(visibility);

        if (isTransparent) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setStatusBarStyle(final String style) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !style.isEmpty()) {

            View decorView = window.getDecorView();
            WindowInsetsControllerCompat windowInsetsControllerCompat = WindowCompat.getInsetsController(window, decorView);

            if (style.equals(STYLE_LIGHT_CONTENT)) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
            } else if (style.equals(STYLE_DEFAULT) || style.equals(STYLE_DARK_CONTENT)) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(false);
            } else {
                LOG.e(TAG, "Invalid style, must be either 'default', 'darkcontent' or 'lightcontent'");
            }
        }
    }

    private void setNavigationBarStyle(final String style, final String colorHex) {
        // Check API level for setting background color (Lollipop = 21) <<<
        // Icon appearance control requires M (23), handled conditionally below.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
             LOG.w(TAG, "Setting navigation bar appearance requires Android L (API 21) for color and M (API 23) for icon style.");
             // Still need to check if style or colorHex are empty
             if (style.isEmpty() || colorHex.isEmpty()) {
                  LOG.w(TAG, "Style or colorHex is empty, exiting setNavigationBarStyle.");
             }
             return; // Cannot proceed on older APIs
        }

        // Ensure window is initialized
        if (this.window == null) {
             LOG.e(TAG, "Window member variable is not initialized in setNavigationBarStyle!");
             // Also check if style or colorHex are empty before returning
             if (style.isEmpty() || colorHex.isEmpty()) {
                  LOG.w(TAG, "Style or colorHex is empty, exiting setNavigationBarStyle.");
             }
             return;
        }

        int backgroundColor;
        try {
            backgroundColor = Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            LOG.e(TAG, "Invalid hexString argument for navigation bar background: " + colorHex, e);
            // If execute method wraps this call in try-catch and sends callback error, re-throwing is appropriate.
            // Otherwise, just logging and returning here is safer to prevent crashes.
            throw e; // Keep re-throw assuming execute method handles it
            // Alternative if execute does NOT catch:
            // return;
        }

        // These flags ensure the system draws the color behind the navigation bar area.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION); // Clear translucent flag if set (SDK 19-30)

        // Note: This method is deprecated in API 35 - deal with it later
        window.setNavigationBarColor(backgroundColor);
        LOG.v(TAG, "Navigation bar background color set to: " + colorHex);

        // Only attempt to set icon appearance if API level is sufficient (M=23) and style string is not empty
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !style.isEmpty()) {
            View decorView = window.getDecorView();
            WindowInsetsControllerCompat windowInsetsControllerCompat = WindowCompat.getInsetsController(window, decorView);

            if (windowInsetsControllerCompat == null) {
                 LOG.e(TAG, "Could not get WindowInsetsControllerCompat to set navigation bar icon appearance.");
                 // Continue, color is set, but icon style won't be applied.
            } else {
                // Determine icon lightness based on the 'style' parameter
                if (style.equals(STYLE_LIGHT_CONTENT)) {
                    windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
                    LOG.v(TAG, "Navigation bar icon appearance set to LIGHT based on style: " + style);
                } else if (style.equals(STYLE_DEFAULT) || style.equals(STYLE_DARK_CONTENT)) {
                    windowInsetsControllerCompat.setAppearanceLightNavigationBars(false);
                    LOG.v(TAG, "Navigation bar icon appearance set to DARK based on style: " + style);
                } else {
                     LOG.e(TAG, "Invalid style for icon appearance, must be 'default', 'darkcontent' or 'lightcontent'");
                     // Continue, color is set, but icon style won't be applied based on invalid style string.
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !style.isEmpty()) { // API 21/22 with non-empty style
             LOG.w(TAG, "Setting navigation bar icon appearance style is only supported on Android M (API 23) and above. Style '" + style + "' ignored for icon appearance.");
        }
        // If style is empty, icon appearance logic is skipped, which is fine.
    }
}
