<%@ page session="false" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%-- An input field for AppUser#authorizedUntil which allows for both date and infinite-time selections. --%>

<div class="authorization-container">
    <div id="forever-auth-${user.id}" ${user.authorizedUntil == null ? "" : "style='display: none'"}>
        <i>forever</i>
        <a href="javascript: void(0)"
           onclick="document.getElementById('forever-auth-${user.id}').style.display='none';
                    document.getElementById('exp-date-${user.id}').style.display='inline-block'"
           role="button"
           class="emoji-silhouette clock"
           title="Set authorization end date"
           aria-label="Set authorization end date for user ${user.id}"
        >&#9202;</a>
    </div>
    <div id="exp-date-${user.id}" ${user.authorizedUntil == null ? "style='display: none'" : ""}>
        <fmt:formatDate pattern="yyyy-MM-dd" value="${user.authorizedUntil}" var="authorizedUntilFormatted"/>
        <input type="date"
               onchange="UserManagement.setAuthUntil(${user.id}, '${user.username}', this.value)"
               ${user.isAccountNonExpired() ? "" : "class='expired-account'"}
               ${user.authorizedUntil == null ? "" : "value='".concat(authorizedUntilFormatted).concat("'")}
        />
        <a href="javascript: UserManagement.setInfiniteAuth(${user.id}, '${user.username}')"
           role="button"
           class="infinity"
           title="Authorize forever"
           aria-label="Authorize user ${user.id} forever"
        >&infin;</a>
    </div>
</div>