<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
    <head>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css" /> 
    </head>

    <body>

        <form action="${pageContext.request.contextPath}/" method="GET">
            Title: <input type="text" class="search-text" name="title" value="<c:out value='${param.title}' />">
            <button type="submit" class="button">Search</button>     
        </form>

        <a href="${pageContext.request.contextPath}/add" class="editbutton addbutton button">＋Add</a>

        <table border="1" class="table">
            <tr>
                <th></th>
                <th>ID</th>
                <th>Title</th>
                <th>Description</th>
                <th>DueDate</th>
                <th>IsCompleted</th>
                <th style="min-width: 150px;text-align: center;">CreatedAt</th>
            </tr>

            <c:choose>
                <c:when test="${not empty todos}">
                    <c:forEach var="c" items="${todos}">
                        <tr>
                            <td class="idcolumn">
                                <a class="editbutton button" href="${pageContext.request.contextPath}/edit?id=${c.id}">Edit</a>
                                <form style="margin:0" action="${pageContext.request.contextPath}/delete" method="POST" >
                                    <button type="submit" class="delbutton button">Delete</button>
                                    <input type="hidden" name="id" value="${c.id}" />
                                </form>
                            </td>
                            <td><c:out value="${c.id}" /></td>
                            <td><c:out value="${c.title}" /></td>
                            <td class="prewrapcell"><c:out value="${c.description}" /></td>
                            <td>
                                <fmt:formatDate value="${c.dueDate}" pattern="yyyy/MM/dd" />
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${c.isCompleted == 1}">
                                        <span style='color:green;'>完了</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style='color:red;'>未完了</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <fmt:formatDate value="${c.createdAt}" pattern="yyyy/MM/dd HH:mm:ss" />
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr><td colspan="7">No data found.</td></tr>
                </c:otherwise>
            </c:choose>
        </table>

        <c:if test="${isExistDbServer == 'false'}">
            <div class="messagebox">
                <div class="messageboxmessage">Can't connect Database Server</div>
                <div class="messageboxsql">     
                    Connection failed.<br>
                    The database server may be offline,<br>
                    or the connection settings might be incorrect.<br>
                    Please verify your configuration in <strong>/src/main/resources/mybatis-config.xml</strong> <br>
                    or ensure the DB server is running.

                    <div style="margin:20px 0 0 0;font-weight: bold;">DB server on NetBeans</div>
                    <img src='images/image2.png' style="width:400px;display:block;margin:0 0 20px;border:1px solid gray;"/>

                    <div style="font-weight:bold;">Configuration of MyBatis on [/src/main/resources/mybatis-config.xml] </div>
                    <img src='images/image1.png' style="width:400px;display:block;border:1px solid gray;"/>
                </div>
            </div>
        </c:if>

        <c:if test="${isExistTable == 'false'}">
            <div class="messagebox">
                <div class="messageboxmessage">
                    TODOS TABLE doesn't exist.<br>
                    Please create this table on database.
                </div>
                <div class="messageboxsql">     
                    CREATE TABLE todos (<br>
                    id INT PRIMARY KEY NOT NULL,<br>
                    title VARCHAR(255),<br>
                    description VARCHAR(1000),<br>
                    due_date DATE,<br>
                    is_completed SMALLINT DEFAULT 0 NOT NULL,<br>
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP<br>
                    );
                </div>
            </div>
        </c:if>
    </body>
</html>