<!DOCTYPE html>  
  
<html>  
<head>  
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">  
<title>View Users</title>
<base href="${pageContext.request.contextPath}/"/>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" crossorigin="anonymous">
</head>  
<body>  
  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Students</h1>  
  
<table class="table">  
    <thead>
        <tr>
            <th>Student ID</th>
            <th>Name</th>
            <th>Std</th>
            <th>Edit</th>
            <th>Delete</th>
        </tr>  
    </thead>
    <tbody>
        <c:forEach items="${studentRecords}" var="record">  
            <tr>
                <td>${record.getId()}</td>
                <td>${record.getName()}</td>
                <td>${record.getStd()}</td>  
                <td><form action="edit" method="get">
                    <input type="hidden" name="id" value="${record.getId()}">
                    <button type="submit" class="btn btn-primary">Edit</button>
                </form></td>                 <td></td>  
                <td><form action="delete" method="post">
                    <input type="hidden" name="id" value="${record.getId()}">
                    <button type="submit" class="btn btn-danger">Delete</button>
                </form></td> 
        </c:forEach>

        <a class="btn btn-primary" href="create" role="button">New Student</a>
    </tbody>
</table>
</body>