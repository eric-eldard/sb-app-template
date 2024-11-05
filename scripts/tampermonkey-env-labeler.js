// TODO set your domain(s) in @include
// ==UserScript==
// @name         Localhost-Prod Labeler
// @namespace    https://eric-eldard.com
// @author       Eric Eldard
// @version      1.1
// @description  Stop spending 10 minutes trying to figure out why your changes aren't showing up...
// @include      /^https?:\/\/((local\.)?eric-eldard.com|localhost:[0-9]{2,5})((?!assets).)*$/
// ==/UserScript==

(function() {
    'use strict';

    function isLocalHost() {
        return window.location.hostname.startsWith("local");
    }

    let label;
    let background;
    let borderColor;
    let color;

    if (isLocalHost()) {
        label = "Local";
        background = "rgba(128, 128, 255, .7)";
        borderColor = "#00f";
        color = "#fff";
    }
    else {
        label = "Prod";
        background = "rgba(255, 0, 0, .7)";
        borderColor = "#f00";
        color = "#fff";
    }

    const versionTag = document.querySelector("meta[name='version']");
    const version = versionTag == null ? "" : " (" + versionTag.content + ")"

    const elem = document.createElement("pre");

    elem.innerText = "Env: " + label + version;
    elem.title = "click to remove";
    elem.style.backgroundColor = background;
    elem.style.border = `1px solid ${borderColor}`;
    elem.style.borderRadius = "4px";
    elem.style.color = color;
    elem.style.cursor = "pointer";
    elem.style.display = "block";
    elem.style.fontSize = "13px";
    elem.style.margin = "5px";
    elem.style.padding = "5px 15px";
    elem.style.position = "fixed";
    elem.style.right = "0";
    elem.style.top = "0";
    elem.style.zIndex = "9999999";

    elem.addEventListener("click", () => document.body.removeChild(elem));

    document.body.appendChild(elem);

    console.log(`%cEnvironment labeled as %c${label}%c by "Localhost-Prod Labeler" Tampermonkey script`, "color: #aaa", `color: ${borderColor}`, "color: #aaa");
})();