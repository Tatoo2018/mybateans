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
            <div class="form-title">Update Todo</div>

            <div class="row"> 
                <div class="formlabel">ID:</div>
                <div class="forminput">
                    <input type="hidden" name="id" value="<c:out value='${todo.id}' />">
                    <c:out value="${todo.id}" />
                </div>
            </div>

            <div class="row"> 
                <div class="formlabel">Title:</div>
                <div class="forminput">
                    <input type="text" name="title" value="<c:out value='${todo.title}' />" required>  
                </div>
            </div>

            <div class="row"> 
                <div class="formlabel">Description:</div>
                <div class="forminput">
                    <textarea class="descriptiontext" name="description"><c:out value="${todo.description}" /></textarea>
                </div>
            </div>

            <div class="row"> 
                <div class="formlabel">Due Date:</div>
                <div class="forminput">
                    <input type="date" name="dueDate" value="<c:out value='${todo.dueDate}' />">
                </div>
            </div>

            <div class="row"> 
                <div class="formlabel">IsCompleted:</div>
                <div class="forminput">
                    <label>
                        <input type="checkbox" name="isCompleted" value="1" ${todo.isCompleted == 1 ? 'checked' : ''}>
                        Done
                    </label>
                </div>
            </div>

            <input type="submit" class="button savebutton" value="Update Data">
        </form>
    </body>
</html>