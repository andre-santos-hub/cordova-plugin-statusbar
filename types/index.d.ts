// Type definitions for cordova-plugin-statusbar
// Project: https://github.com/apache/cordova-plugin-statusbar
// Definitions by: Xinkai Chen <https://github.com/Xinkai>
//                 Tim Brust <https://github.com/timbru31>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped

/**
* Global object StatusBar.
*/
interface Window {
    StatusBar: StatusBar;
    addEventListener(type: "statusTap", listener: (ev: Event) => any, useCapture?: boolean): void;
}

/**
* The StatusBar object provides some functions to customize the iOS and Android StatusBar.
*/
interface StatusBar {
    /**
    * On iOS 7, make the statusbar overlay or not overlay the WebView.
    * 
    * @param isOverlay - On iOS 7, set to false to make the statusbar appear like iOS 6.
    *                    Set the style and background color to suit using the other functions.
    */
    overlaysWebView(isOverlay: boolean): void;

    /**
    * Use the default statusbar (dark text, for light backgrounds).
    */
    styleDefault(): void;

    /**
    * Use the lightContent statusbar (light text, for dark backgrounds).
    */
    styleLightContent(): void;

    /**
    * Use the darkContent statusbar (dark text, for light backgrounds).
    */
    styleDarkContent(): void;

    /**
    * Read this property to see if the statusbar is visible or not.
    */
    isVisible: boolean;
}

declare var StatusBar: StatusBar;