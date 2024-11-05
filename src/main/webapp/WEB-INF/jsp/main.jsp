<%@ page session="false" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <jsp:include page="widgets/headers.jsp"/>
    </head>
    <body>
        <div id="popup-container" onclick="App.closePopup()">
            <div id="popup" onclick="event.stopPropagation()">
                <div id="top-controls">
                    <a href="javascript: App.closePopup();" id="closeX" title="Close"></a>
                </div>
                <div id="popup-content"></div>
            </div>
        </div>

        <div id="main">
            <div>
                <a href="javascript: App.openPopup('hello-world')">Open popup that says "Hello world!"</a>
            </div>
            <div style="display: flex; justify-content: space-evenly; margin-top: 50px; width: 33%">
                <a href="/your_app/users">Users</a>
                <a href="/">Home</a>
                <a href="/logout">Log Out</a>
            </div>
        </div>

        <script defer>
            App.openPopupIfPopupState();
        </script>
    </body>
</html>