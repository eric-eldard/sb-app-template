<%@ page session="false" %>
<div id="${param.id}-container">
    <input type="password" id="${param.id}" onkeydown="${param.onKeyDownAction}" autocomplete="current-password">
    <a href="javascript: App.togglePasswordVisibility('${param.id}-container')" accesskey="v" class="visibility-toggle">
        <img src="/public/assets/images/icons/visibility/show.png" class="password-show" title="Show password">
        <img src="/public/assets/images/icons/visibility/hide.png" class="password-hide" title="Hide password">
    </a>
</div>