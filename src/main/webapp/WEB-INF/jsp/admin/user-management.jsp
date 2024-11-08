<%@ page session="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <jsp:include page="../widgets/headers.jsp"/>
        <jsp:include page="../widgets/csrf-token.jsp"/>

        <link rel="stylesheet" type="text/css" href="/public/assets/style/user-management.css">

        <style>
            h1 {
                text-align: center;
            }

            .horizontal-table tr td:last-of-type {
                border-width: 0 1px 1px 0;
                font-family: unset;
                font-size: 0;
                padding: 0;
                white-space: nowrap;
            }

            .horizontal-table tr td:last-of-type button:not(:last-of-type) {
                margin: 3px 0 3px 3px;
            }

            .horizontal-table tr td:last-of-type button:last-of-type {
                margin: 3px;
            }

            .horizontal-table tr td:nth-child(3),
            .horizontal-table tr th:nth-child(3),
            .horizontal-table tr td:nth-last-child(-n + 6),
            .horizontal-table tr th:nth-last-child(-n + 6) {
                display: none;
            }

            .vertical-table tr td input {
                width: 175px;
            }

            @media screen and (min-width: 1000px) {
                .horizontal-table tr td:nth-child(3),
                .horizontal-table tr th:nth-child(3),
                .horizontal-table tr td:nth-last-child(-n + 6),
                .horizontal-table tr th:nth-last-child(-n + 6) {
                    display: table-cell;
                }

                .vertical-table tr td input {
                    width: 250px;
                }
            }
        </style>

        <script>
            function createUser() {
                UserManagement.postNewUser({
                    "username":        document.getElementById("newUserName").value,
                    "password":        document.getElementById("newUserPassword").value,
                    "authorizedUntil": document.getElementById("newUserAuthUntil").value,
                    "enabled":         document.getElementById("newUserEnabled").checked,
                    "admin":           document.getElementById("newUserAdmin").checked,
                });
            }
        </script>
    </head>
    <body>
        <div class="top-container" role="main">
            <h1>User Management</h1>
            <nav class="bread-crumbs">
                <a href="/">Home</a>
                <a href="/your_app/">Your App</a> <!-- TODO - set your app's root path and name -->
            </nav>

            <fieldset>
                <legend>Create User</legend>
                <table class="vertical-table">
                    <tr>
                        <td>Username</td>
                        <td>
                            <input type="text" id="newUserName" autocomplete="off" aria-label="Enter new username"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Password</td>
                        <td>
                            <input type="text" id="newUserPassword" autocomplete="off" aria-label="Enter user's initial password"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Authorized Until</td>
                        <td><input type="date" id="newUserAuthUntil" aria-label="Select user's end of authorization date (or leave blank)"/></td>
                    </tr>
                </table>

                <div class="new-user-controls">
                    <div>
                        <input type="checkbox" id="newUserEnabled" checked/>
                        <label for="newUserEnabled" class="for-clickable">Enabled</label>
                    </div>

                    <div>
                        <input type="checkbox" id="newUserAdmin"/>
                        <label for="newUserAdmin" class="for-clickable">Admin</label>
                    </div>

                    <button onclick="createUser()">Create</button>
                </div>
            </fieldset>

            <table class="horizontal-table">
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Authorized Until</th>
                    <th>Last Successful Login</th>
                    <th>Failed Attempts</th>
                    <th>Locked On</th>
                    <th>Disabled</th>
                    <th>Admin</th>
                    <th>Granted Auths</th>
                    <th>Actions</th>
                </tr>
                <c:forEach items="${userList}" var="user">
                <c:set var="user" value="${user}" scope="request"/>
                <tr>
                    <td class="user-id">
                        <!-- TODO - set your app's root path -->
                        <a href="/your_app/users/${user.id}"
                           title="View user"
                           aria-label="View user ${user.id}"
                        >${user.id}</a>
                    </td>
                    <td>${user.username}</td>
                    <td>
                        <c:import url="../widgets/authorized-until-input.jsp"/>
                    </td>
                    <td>${user.getLastSuccessfulLoginTimestamp() == null ? "<i>never</i>" : user.getLastSuccessfulLoginTimestamp()}</td>
                    <td>${user.failedPasswordAttempts}</td>
                    <td>
                        ${user.lockedOn}
                        <c:if test="${user.lockedOn != null}">
                            <a href="javascript: UserManagement.unlockUser(${user.id}, '${user.username}')"
                               role="button"
                               class="emoji-silhouette"
                               title="Unlock"
                               aria-label="Unlock user ${user.id}"
                            >&#128275;</a>
                        </c:if>
                    </td>
                    <td onclick="UserManagement.setEnabled(${user.id}, ${!user.enabled})"
                        role="button"
                        class="binary-field"
                        title="${user.enabled ? 'Disable' : 'Enable'}"
                        aria-label="${user.enabled ? 'Disable' : 'Enable'} user ${user.id}"
                    >${!user.enabled ? "&cross;" : ""}</td>
                    <td onclick="UserManagement.setIsAdmin(${user.id}, ${!user.admin})"
                        role="button"
                        class="binary-field"
                        title="${user.admin ? 'Demote' : 'Promote'}"
                        aria-label="${user.admin ? 'Demote' : 'Promote'} user ${user.id}"
                    >${user.admin ? "&check;" : ""}</td>
                    <c:choose>
                        <c:when test="${user.appAuthorities.size() > 0}">
                            <td class="summation"
                                title="&#013;<c:forEach items='${user.appAuthorities}' var='authority'>${authority.pretty()}&#013;</c:forEach>">
                                <!-- TODO - set your app's root path -->
                                <a href="/your_app/users/${user.id}"
                                    title="View authorities"
                                    aria-label="View authorities for user ${user.id}"
                                >${user.appAuthorities.size()}</a>
                            </td>
                        </c:when>
                        <c:otherwise>
                            <td></td>
                        </c:otherwise>
                    </c:choose>
                    <td>
                        <button onclick="UserManagement.setPassword(${user.id}, '${user.username}')"
                                aria-label="Set password for user ${user.id}"
                        >Password</button>
                        <button onclick="UserManagement.deleteUser(${user.id}, '${user.username}')"
                                aria-label="Delete user ${user.id}"
                        >Delete</button>
                    </td>
                </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>