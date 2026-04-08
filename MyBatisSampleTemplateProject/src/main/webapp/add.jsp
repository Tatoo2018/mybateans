<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css" /> 
    </head>

    <body>
        <div>
            <a class="link" href="${pageContext.request.contextPath}/">&lt;&lt; Go To Search Screen</a>
        </div>

        <form action="${pageContext.request.contextPath}/" method="POST" class="formbox">
            <div class="form-title">Register Todo</div>

            <div class="row"> 
                <div class="formlabel">ID:</div>
                <div class="forminput">
                    <c:choose>
                        <c:when test="${not empty todo.id}">
                            <c:out value="${todo.id}" /> (Next ID)
                        </c:when>
                        <c:otherwise>
                            Auto-increment
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <%-- Title --%>
            <div class="row"> 
                <div class="formlabel">Title:</div>
                <div class="forminput">
                    <input type="text" name="title" value="<c:out value='${todo.title}' />" required>  
                </div>
            </div>

            <%-- Description --%>
            <div class="row"> 
                <div class="formlabel">Description:</div>
                <div class="forminput">
                    <textarea class="descriptiontext" name="description"><c:out value="${todo.description}" /></textarea>
                </div>
            </div>

            <%-- Due Date --%>
            <div class="row">
                <div class="formlabel">Due Date:</div>
                <div class="forminput">
                    <input type="date" style="font-size:16px;" name="dueDate" value="<c:out value='${todo.dueDate}' />">
                </div>
            </div>

            <input type="submit" class="button savebutton" value="Save Data">
        </form>
    </body>
</html>