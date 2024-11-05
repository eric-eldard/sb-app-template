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
            <div class="bread-crumbs">
                <a href="/">Home</a>
                <a href="/your_app/">Your App</a> <%-- TODO - set your app's root path and name --%>
                <a href="/your_app/users">Users</a> <%-- TODO - set your app's root path --%>
            </div>

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
                            <a href="javascript: UserManagement.unlockUser(${user.id}, '${user.username}')" class="emoji-silhouette" title="Unlock">&#128275;</a>
                        </c:if>
                    </td>
                </tr>
                </c:if>
                <tr>
                    <td>Disabled</td>
                    <td class="binary-field" onclick="UserManagement.setEnabled(${user.id}, ${!user.enabled})" title="${user.enabled ? 'Disable' : 'Enable'}">
                        ${!user.enabled ? "&cross;" : ""}
                    </td>
                </tr>
                <tr>
                    <td>Is Admin</td>
                    <td class="binary-field" onclick="UserManagement.setIsAdmin(${user.id}, ${!user.admin})" title="${user.admin ? 'Demote' : 'Promote'}">
                        ${user.admin ? "&check;" : ""}
                    </td>
                </tr>
                <c:forEach items="<%=com.your_namespace.your_app.model.user.enumeration.AppAuthority.values()%>" var="authority">
                <tr>
                    <td>${authority.pretty()}</td>
                    <td class="binary-field" onclick="UserManagement.toggleAuth(${user.id}, '${authority}')" title="${user.hasAuthority(authority) ? 'Remove' : 'Grant'}">
                        ${user.hasAuthority(authority) ? "&check;" : ""}
                    </td>
                </tr>
                </c:forEach>
                <tr>
                    <td colspan="2">
                        <div class="button-cell">
                            <button onclick="UserManagement.setPassword(${user.id}, '${user.username}')">Set Password</button>
                            <button onclick="UserManagement.deleteUser(${user.id}, '${user.username}', (() => window.location = '/your_app/users'));">Delete</button> <%-- TODO - set your app's root path --%>
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