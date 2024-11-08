<%@ page session="false" %>
<div id="${param.id}-container">
    <input id="${param.id}"
           type="password"
           onkeydown="${param.onKeyDownAction}"
           autocomplete="current-password"
    >
    <a href="javascript: App.togglePasswordVisibility('${param.id}-container')"
       role="button"
       accesskey="v"
       class="visibility-toggle"
       aria-label="Toggle password visibility"
    >
        <img src="/public/assets/images/icons/visibility/show.png" class="password-show" title="Show password">
        <img src="/public/assets/images/icons/visibility/hide.png" class="password-hide" title="Hide password">
    </a>
</div>