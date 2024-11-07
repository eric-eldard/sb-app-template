<%@ page session="false" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <jsp:include page="widgets/headers.jsp"/>
        <meta name="robots" content="noindex">

        <style>
            .content {
                max-width: 500px;
            }

            .error {
                color: #b00;
                display: none;
                font-size: 14px;
                font-style: italic;
                height: 32px;
                margin-bottom: 8px;
                margin-top: 4px;
                text-align: left;
                transition: height 1s;
            }

            .error#message-placeholder {
                display: block;
            }

            .login-form {
                margin-left: auto;
                margin-right: auto;
                transition: width 1s;
            }

            .login-form input {
                font-size: 16px;
            }

            .login-form form  {
                margin-left: auto;
                margin-right: auto;
            }

            .login-form #login-username {
                box-sizing: border-box;
                height: 24px; /* Use explicit height because browsers vary on input default height */
                width: 100%;
            }

            .login-form .buttons {
                display: flex;
                flex-direction: row-reverse; /* buttons are added in reverse order so Submit comes first in tab order */
                justify-content: space-between;
                margin-bottom: 20px;
            }

            .login-form .username {
                margin-top: 24px;
            }

            .login-form .password {
                margin-top: 12px;
            }


            @media screen and (min-width: 600px) {
                .content > div {
                    margin-left: 50px;
                    margin-right: 50px;
                }

                .error {
                    height: 20px;
                }
            }

            @media screen and (min-width: 1100px) {
                .content .intro-heading {
                    margin-bottom: 0;
                }
            }
        </style>
    </head>
    <body>
        <script>
            function login() {
                showErrorMessage("message-placeholder");

                const username = document.getElementById("login-username").value.trim();
                const password = document.getElementById("login-password").value.trim();

                if (username.length < 1) {
                    showErrorMessage("message-incomplete");
                    focusElement("login-username");
                }
                else if (password.length < 1) {
                    showErrorMessage("message-incomplete");
                    focusElement("login-password");
                }
                else {
                    const submit = document.getElementById("login-submit");
                    submit.disabled = true;

                    const body = {
                        "username": username,
                        "password": password
                    };

                    fetch("/login", makeRequestOptions("POST", body))
                        .then(response => response.status)
                        .then(status => {
                            if (status == 200) {
                                window.location.replace("/your_app"); // TODO - set your app's home page
                                return;
                            }
                            else if (status >= 400 && status < 500) {
                                showErrorMessage("message-unrecognized");
                            }
                            else if (status == 502 || status == 503) {
                                showErrorMessage("message-unavailable");
                            }
                            else {
                                showErrorMessage("message-unknown");
                            }

                            focusElement("login-password");
                            submit.disabled = false;
                        });
                }
            }

            function submitOnEnter(event) {
                if (event && event.keyCode == 13) {
                    login();
                }
            }

            function showErrorMessage(elemId) {
                document.querySelectorAll(".error").forEach(elem => elem.style.display = "none");

                if (typeof elemId !== "undefined") {
                    document.getElementById(elemId).style.display = "block";
                }
            }

            window.addEventListener("DOMContentLoaded", focusElement("login-username"));
        </script>

        <div id="main">
            <div class="content">
                <div class="login-form">
                    <div class="intro-heading">Login</div>
                    <form>
                        <div class="username">
                            <label for="login-username">Username</label>
                            <input id="login-username" autocomplete="username" onkeydown="submitOnEnter(event)">
                        </div>
                        <div class="password">
                            <label for="login-password">Password</label>
                            <jsp:include page="widgets/password-input.jsp">
                                <jsp:param name="id" value="login-password"/>
                                <jsp:param name="onKeyDownAction" value="submitOnEnter(event)"/>
                            </jsp:include>
                        </div>
                        <div id="message-placeholder"  class="error">&nbsp;</div>
                        <div id="message-incomplete"   class="error">Please provide both username and password</div>
                        <div id="message-unrecognized" class="error">Username and password combination not recognized</div>
                        <div id="message-unavailable"  class="error">Server not available</div>
                        <div id="message-unknown"      class="error">Unknown error; please try again</div>
                        <div class="buttons">
                            <input id="login-submit" type="button" onclick="login()" value="Submit">
                            <a href="/">Home</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>