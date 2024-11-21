/**
 * User management functions
 * @version 1.0-ts
 * @author  Eric Eldard
 */

import type { User } from "user";

export namespace UserManagement {
    const USER_ADMIN_PATH: string = "/your_app/users"; // TODO - set your app's root path
    const MIN_PASSWORD_CHARS: number = 10;

    export function postNewUser(newUser: User): void {
        if (!newUser.username || newUser.username.trim().length < 0) {
            alert("Username cannot be blank");
        }
        else if (!newUser.password || newUser.password.trim().length < MIN_PASSWORD_CHARS) {
            alert(`User ${newUser.username} not created\nPassword must be at least ${MIN_PASSWORD_CHARS} characters`);
        }
        else {
            fetch(USER_ADMIN_PATH, makeRequestOptions("POST", newUser))
                .then(response => handleResponse(response));
        }
    }

    export function deleteUser(id: number, username: string, successCallback?: ((r: Response) => void)): void {
        const confirmed: boolean = confirm(`Are you sure you want to delete user ${username}?`);

        if (confirmed) {
            fetch(USER_ADMIN_PATH + "/" + id, makeRequestOptions("DELETE"))
                .then(response => handleResponse(response, successCallback));
        }
        else {
            alert(`Deletion of user ${username} canceled`);
        }
    }

    export function unlockUser(id: number, username: string): void {
        const confirmed: boolean = confirm("Confirm unlocking account for " + username);

        if (confirmed) {
            fetch(`${USER_ADMIN_PATH}/${id}/unlock`, makeRequestOptions("PATCH"))
                .then(response => handleResponse(response));
        }
    }

    export function setPassword(id: number, username: string): void {
        const password: string | null = prompt("Set new password for user " + username);

        if (password == null) {
            alert(`Password change for user ${username} was canceled`);
        }
        else if (password.trim().length < MIN_PASSWORD_CHARS) {
            alert(`Password for user ${username} was not changed\nNew password must be at least ${MIN_PASSWORD_CHARS} characters`);
        }
        else {
            const user: User = {
                "password": password,
            }
            fetch(`${USER_ADMIN_PATH}/${id}/password`, makeRequestOptions("PATCH", user))
                .then(response => handleResponse(response, (() => {
                    alert("Password updated for user " + username);
                    window.location.reload(); // reload to refresh "failed attempts" count
                })));
        }
    }

    export function setAuthUntil(id: number, username: string, date: Date | null): void {
        let confirmed: boolean;

        if (date) {
            confirmed = confirm(`Confirm ${date} as new authorized-until date for ${username}`);
        }
        else {
            confirmed = confirm("Confirm infinite authorization for " + username);
        }

        if (confirmed) {
            const user: User = {
                "authorizedUntil": date,
            }
            fetch(`${USER_ADMIN_PATH}/${id}/authorized-until`, makeRequestOptions("PATCH", user))
                .then(response => handleResponse(response));
        }
        else {
            alert(`Authorized-until date for user ${username} not changed`);
            window.location.reload();
        }
    }

    export function setInfiniteAuth(id: number, username: string): void {
        setAuthUntil(id, username, null);
    }

    export function setEnabled(id: number, enabled: boolean): void {
        const user: User = {
            "enabled": enabled,
        }
        fetch(`${USER_ADMIN_PATH}/${id}/enabled`, makeRequestOptions("PATCH", user))
            .then(response => handleResponse(response));
    }

    export function setIsAdmin(id: number, isAdmin: boolean): void {
        const user: User = {
            "admin": isAdmin,
        }
        fetch(`${USER_ADMIN_PATH}/${id}/admin`, makeRequestOptions("PATCH", user))
            .then(response => handleResponse(response));
    }

    export function toggleAuth(id: number, authority: string): void {
        fetch(`${USER_ADMIN_PATH}/${id}/toggle-auth/${authority}`, makeRequestOptions("PATCH"))
            .then(response => handleResponse(response));
    }

    /**
     * @param successCallback action to run on success; omit for default behavior (reload page)
     */
    function handleResponse(response: Response,
        successCallback: ((r: Response) => void) = (() => window.location.reload())): void {
        if (response.ok) {
            successCallback(response);
        }
        else if (response.status == 403) {
            alert("Response from server: 403\nRefreshing for possible stale CSRF token...");
            window.location.reload();
        }
        else {
            alert("Response from server: " + response.status);
        }
    }
}