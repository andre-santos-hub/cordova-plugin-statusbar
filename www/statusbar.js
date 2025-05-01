/*
 *
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

/* global cordova */

var exec = require('cordova/exec');

var StatusBar = {
    isVisible: true,

    overlaysWebView: function (doOverlay) {
        exec(null, null, 'StatusBar', 'overlaysWebView', [doOverlay]);
    },

    styleDefault: function () {
        // dark text ( to be used on a light background )
        exec(null, null, 'StatusBar', 'styleDefault', []);
    },

    styleLightContent: function (bgColour) {
        // light text ( to be used on a dark background )
        exec(null, null, 'StatusBar', 'styleLightContent', [bgColour]);
    },

    styleDarkContent: function (bgColour) {
        // dark text ( to be used on a light background )
        exec(null, null, 'StatusBar', 'styleDarkContent', [bgColour]);
    },

};

// prime it. setTimeout so that proxy gets time to init
window.setTimeout(function () {
    exec(
        function (res) {
            if (typeof res === 'object') {
                if (res.type === 'tap') {
                    cordova.fireWindowEvent('statusTap');
                }
            } else {
                StatusBar.isVisible = res;
            }
        },
        null,
        'StatusBar',
        '_ready',
        []
    );
}, 0);

module.exports = StatusBar;
