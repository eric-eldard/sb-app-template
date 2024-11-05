<%@ page session="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <jsp:include page="widgets/headers.jsp"/>
        <meta name="robots" content="noindex">

        <style>
            a {
                color: #ddf;
                transition: color 0.75s;
            }

            a:hover {
                color: #ffd;
            }

            #main {
                background-color: #000;
                color: #fff;
                font-family: Helvetica, sans-serif;
                font-size: 20px;
                margin-top: 40%;
            }

            .heading {
                font-size: 60px;
                margin-bottom: 25px;
                margin-left: -4px;
                transition: font-size 1s;
            }

            @media screen and (min-width: 600px) {
                #main {
                    margin-top: 0; /* in desktop view, it'll be centered on screen, so top margin no longer needed */
                    padding: 30px;
                }

                .heading {
                    font-size: 120px;
                    margin-left: -8px;
                }
            }
        </style>
    </head>

    <body>

        <div id="main">
            <div>
                <div class="heading">HTTP ${status}</div>
                <p>
                    <c:choose>
                        <c:when test="${status == 401}">Hmmmmm, we don't seem to recognize you.</c:when>
                        <c:when test="${status == 403}">Hmmmmm, you don't seem to have access to that.</c:when>
                        <c:when test="${status == 404}">You got lost on a site with only one page?</c:when>
                        <c:otherwise>Hmmmmm, we're not sure what happened.</c:otherwise>
                    </c:choose>
                </p>
                <p>
                    <a href="/">Let's get you back to the app</a>
                </p>
            </div>
        </div>
    </body>
</html>