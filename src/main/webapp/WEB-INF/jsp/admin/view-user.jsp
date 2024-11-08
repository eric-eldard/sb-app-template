<%@ page session="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="user" value="${user}" scope="request"/>

<!DOCTYPE html>
<html lang="en">
    <head>
        <jsp:include page="../widgets/csrf-token.jsp"/>
        <jsp:include page="../widgets/headers.jsp"/>

        <link rel="stylesheet" type="text/css" href="/public/assets/style/user-management.css">

        <style>
            .button-cell {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                justify-content: space-around;
                justify-items: center;
                width: 100%;
            }

            .button-cell button {
                width: 100px;
            }

            .vertical-table tr td {
                font-size: 12px;
                height: 22px;
                padding: 5px;
            }

            .vertical-table tr td:last-of-type {
                width: 120px;
            }
        </style>
    </head>
    <body>
        <div class="top-container">
            <h1>View User: <b>${user.username}</b></h1>
            <nav class="bread-crumbs">
                <a href="/">Home</a>
                <a href="/your_app/">Your App</a> <!-- TODO - set your app's root path and name -->
                <a href="/your_app/users">Users</a> <!-- TODO - set your app's root path -->
            </nav>

            <table class="vertical-table">
                <tr>
                    <td>Authorized Until</td>
                    <td>
                        <c:import url="../widgets/authorized-until-input.jsp"/>
                    </td>
                </tr>
                <tr>
                    <td>Failed Password Attempts</td>
                    <td>${user.failedPasswordAttempts}</td>
                </tr>
                <c:if test="${user.lockedOn != null}">
                <tr>
                    <td>Locked On</td>
                    <td>
                        ${user.lockedOn}
                        <c:if test="${user.lockedOn != null}">
                            <a href="javascript: UserManagement.unlockUser(${user.id}, '${user.username}')"
                               role="button"
                               class="emoji-silhouette"
                               title="Unlock"
                               aria-label="Unlock user"
                            >&#128275;</a>
                        </c:if>
                    </td>
                </tr>
                </c:if>
                <tr>
                    <td>Disabled</td>
                    <td onclick="UserManagement.setEnabled(${user.id}, ${!user.enabled})"
                        role="button"
                        class="binary-field"
                        title="${user.enabled ? 'Disable' : 'Enable'}"
                        aria-label="${user.enabled ? 'Disable' : 'Enable'} user"
                    >${!user.enabled ? "&cross;" : ""}</td>
                </tr>
                <tr>
                    <td>Is Admin</td>
                    <td onclick="UserManagement.setIsAdmin(${user.id}, ${!user.admin})"
                        role="button"
                        class="binary-field"
                        title="${user.admin ? 'Demote' : 'Promote'}"
                        aria-label="${user.admin ? 'Demote' : 'Promote'} user"
                    >${user.admin ? "&check;" : ""}</td>
                </tr>
                <c:forEach items="<%=com.your_namespace.your_app.model.user.enumeration.AppAuthority.values()%>" var="authority">
                <tr>
                    <td>${authority.pretty()}</td>
                    <td onclick="UserManagement.toggleAuth(${user.id}, '${authority}')"
                        role="button"
                        class="binary-field"
                        title="${user.hasAuthority(authority) ? 'Remove' : 'Grant'}"
                        aria-label="${user.hasAuthority(authority) ? 'Remove' : 'Grant'} authority ${authority.pretty()}"
                    >${user.hasAuthority(authority) ? "&check;" : ""}</td>
                </tr>
                </c:forEach>
                <tr>
                    <td colspan="2">
                        <div class="button-cell">
                            <button onclick="UserManagement.setPassword(${user.id}, '${user.username}')"
                                    aria-label="Set password for user"
                            >Set Password</button>
                            <!-- TODO - set your app's root path -->
                            <button onclick="UserManagement.deleteUser(${user.id}, '${user.username}', (() => window.location = '/your_app/users'))"
                                    aria-label="Delete user"
                            >Delete</button>
                        </div>
                    </td>
                </tr>
            </table>

            <c:choose>
                <c:when test="${empty user.loginAttempts}">
                    <h2>No Login Attempts</h2>
                </c:when>
                <c:otherwise>
                    <h2>Login Attempts</h2>
                    <table class="horizontal-table">
                        <tr>
                            <th>Timestamp</th>
                            <th>Outcome</th>
                            <th>Failure Reason</th>
                        </tr>
                        <c:forEach items="${user.getLoginAttemptsSorted()}" var="loginAttempt">
                            <tr>
                                <td style="${loginAttempt.outcome == 'FAILURE' ? 'color: #b00;' : ''}">${loginAttempt.timestamp}</td>
                                <td style="${loginAttempt.outcome == 'FAILURE' ? 'color: #b00;' : ''}">${loginAttempt.outcome}</td>
                                <td style="${loginAttempt.outcome == 'FAILURE' ? 'color: #b00;' : ''}">${loginAttempt.failureReason != null ? loginAttempt.failureReason : "&nbsp;"}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </body>
</html>