/**
 * Functions supporting login and popup
 * @version 1.0-ts
 * @author  Eric Eldard
 */
export namespace App {
    const HASH_PATH_KEY: string = "hashPath";
    const POPUP_NAME_CONSOLE_STYLE = "color: light-dark(blue, cyan)";

    let historyPoppedProgrammatically: boolean = false;

    // Close popup on a history pop-state event (back button pressed)
    const POPSTATE_LISTENER = (e: PopStateEvent) => closePopup();


    window.addEventListener("DOMContentLoaded", () => {
        browserDetect();
        bindPopupCloseToEsc();
    });


    function bindPopupCloseToEsc() {
        window.addEventListener("keyup", function (e) {
            if (popupIsOpen() && e.keyCode == 27) {
                closePopup();
            }
        });
    }

    // Reinstate content if a popup name is found in the url's hash or in session storage
    export function openPopupIfPopupState(): void {
        if (locationIsHashPath()) {                               // If page loads with a popup hash param, then...
            sessionStorage.setItem(HASH_PATH_KEY, hashPath());    // store the requested popup name
            console.debug(`Found path %c${sessionStorage.getItem(HASH_PATH_KEY)}%c in url; storing and reloading...`,
                `${POPUP_NAME_CONSOLE_STYLE}`, "color: unset");
            reloadWithoutHash();                                  // reload w/o popup name so we have a clean back state
        }
        else if (sessionStorage.getItem(HASH_PATH_KEY)) {         // If the page loads with a stored popup name, then...
            const storedPath: string = sessionStorage.getItem(HASH_PATH_KEY)!;  // retrieve the stored name
            sessionStorage.removeItem(HASH_PATH_KEY);             // remove stored name so it doesn't trigger later
            console.debug(`Found path %c${storedPath}%c in session storage; navigating to this content...`,
                `${POPUP_NAME_CONSOLE_STYLE}`, "color: unset");
            openPopup(storedPath.substring(1));                   // trim "#" and navigate to corresponding content
        }
    }

    export function openPopup(path: string): void {
        retrieveContent(path, content => {
            window.addEventListener("popstate", POPSTATE_LISTENER); // listener for (mobile) back button
            showContentInPopup(path, content);
        });
    }

    function retrieveContent(
        path: string,
        successCallback: ((content: string) => void),
        failureCallback: ((error: Error) => void) = (error => console.error(error.message)),
    ): void {
        // Remove trailing slash if present, then always add it back (supports slash and no-slash paths)
        const basePath: string = window.location.pathname.replace(/\/+$/, "") + "/content/";

        fetch(basePath + path, {redirect: "manual"})
            .then(response => {
                // No access to the location response header here, but the only
                // redirect the app issues for a GET /content request is to login
                if (response.status === 401 || response.status === 403 || response.type === "opaqueredirect") {
                    window.location.assign("/login");
                    return;
                }
                else if (!response.ok) {
                    if (response.status === 502 || response.status === 503) {
                        alert("It looks like we're offline for maintenance.\n\nPlease try back shortly!");
                    }
                    throw new Error(`HTTP ${response.status} response returned for [${basePath}${path}]`);
                }
                return response.text();
            })
            .then(content => {
                if (!content) {
                    throw new Error(`Got null content back for path [${path}]`);
                }
                successCallback(content);
            })
            .catch(error => failureCallback(error));
    }

    // Show the content popup in a freshly opened or transitioned popup
    function showContentInPopup(path: string, content: string): void {
        const main         : HTMLElement        = document.getElementsByTagName("main")[0]!;
        const container    : HTMLElement        = document.getElementById("popup-container")!;
        const popupContent : HTMLElement        = document.getElementById("popup-content")!;
        const hashPath     : string             = `#${path}`;

        setInnerHTML(popupContent, content);

        // Tracking the path for the open popup solely for logging purposes when page is refreshed while popup is open
        setDataName(getPopup(), hashPath);

        // Changing effects behind the popup
        main.classList.add("blur");
        container.classList.add("open");

        window.setTimeout(() => {
            if (!popupIsOpen()) {
                setPopupIsOpen(true);
            }

            // Push an extra frame onto history, so the back button can be
            // used to close the popup without navigating away from the page
            history.pushState({ popupStatus : "open"}, "", hashPath);
            console.debug(`Popup %c${hashPath}%c opened and added to history`,
                `${POPUP_NAME_CONSOLE_STYLE}`, "color: unset");

            // Now that new content is loaded and set in the popup, recall it to the center of the screen
            getPopup().scrollTop = 0;

            historyPoppedProgrammatically = false;
        }, 100); // Buffer time for background animations
    }

    // Fully close the popup and return to the main page
    export function closePopup(): void {
        const main                  : HTMLElement = document.getElementsByTagName("main")[0]!;
        const container             : HTMLElement = document.getElementById("popup-container")!;
        const popupContent          : HTMLElement = document.getElementById("popup-content")!;
        const locationWasHashParam  : boolean     = locationIsHashPath();

        // If the popup's hash param is currently in browser history, we'll pop it off. It may not be, if the hash param
        // url was directly navigated to. I've attempted to push an event into history in this case, but Chrome & Safari
        // don't recognize it (though Firefox does), so a history.back() op would cause the user to leave the site.
        if (locationIsHashPath()) {
            historyPoppedProgrammatically = true;
            history.back();
        }

        // We have to retrieve the current popup's name from the popup element, because at this point the hash param has
        // been removed from the address and the popstate event doesn't contain info about the popped-state (it points
        // to the new history head)
        console.debug(`Popup %c${getDataName(getPopup())}%c removed from history`,
            `${POPUP_NAME_CONSOLE_STYLE}`, "color: unset");

        main.classList.remove("blur");

        toggleStyleForId("closeX", "on", false)

        window.removeEventListener("popstate", POPSTATE_LISTENER);

        setPopupIsOpen(false);
        console.debug(`Popup closed${locationWasHashParam ? "" : " with back button"}`);

        // Finish CSS animations before hiding
        window.setTimeout(() => {
            container.classList.remove("open");
            setInnerHTML(popupContent, "");
            setHideControls(false); // delay to prevent controls from appearing during close

            window.setTimeout(() =>
                    Array.from(document.querySelectorAll(".timeline .timeline-events .timeline-event"))
                        .forEach(elem => elem.classList.remove("focused"))
                , 700);
        }, 300);
    }

    function getPopup(): HTMLElement {
        return document.getElementById("popup")!;
    }

    function popupIsOpen(): boolean {
        return getPopup() && getPopup().classList.contains("open");
    }
    
    function setPopupIsOpen(isOpen: boolean): void {
        toggleStyle(getPopup(), "open", isOpen);
    }

    export function setHideControls(hideControls: boolean): void {
        toggleStyle(getPopup(), "hide-controls", hideControls);
    }

    export function togglePasswordVisibility(container: HTMLElement): void {
        const inputElem: HTMLInputElement = document.querySelector(`#${container} input`)!;
        inputElem.type = inputElem.type === "password" ? "text" : "password";

        const showElem: HTMLElement = document.querySelector(`#${container} .visibility-toggle .password-show`)!;
        const hideElem: HTMLElement = document.querySelector(`#${container} .visibility-toggle .password-hide`)!;

        if (showElem.style.display === "none") {
            showElem.style.display = "unset";
            hideElem.style.display = "none";
        }
        else {
            showElem.style.display = "none";
            hideElem.style.display = "unset";
        }
    }
}
