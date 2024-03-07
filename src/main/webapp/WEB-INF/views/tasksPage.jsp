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

<div class="container">
    <div class="row justify-content-center">
        <div class="col-sm-8">
            <div class="card m-3">
                <div class="card-header">
                    New Task
                </div>
    
                <div class="card-body">
    
                    <!-- New Task Form -->
                    <form action="create" method="post">
    
                        <!-- Task Name -->
                        <div class="form-group row">
                            <label for="name" class="col-sm-2 col-form-label">Task</label>
    
                            <div class="col-sm-7">
                                <input type="text" name="name" class="form-control" placeholder="Enter task description">
                            </div>
                        </div>
    
                        <!-- Add Task Button -->
                        <div class="form-group row mt-3">
                            <div class="offset-sm-2 col-sm-7">
                                <button type="submit" class="btn btn-primary">Add Task</button>        
                            </div>
                        </div>
                    </form>
                </div>
            </div>
    
            <!-- Current Tasks -->
            <c:if test="${not empty taskRecords}">
                
                <div class="card m-3">
                    <div class="card-header">
                        Current Tasks
                    </div>
    
                    <div class="card-body">
                        <table class="table table-striped">
                            <thead>
                                <th>&nbsp;</th>
                            </thead>
                            <tbody>
                                <c:forEach items="${taskRecords}" var="record">  
                                    <tr>
                                        <td class="table-text"><div>${record.getName()}</div></td>
                                        <!-- Task Delete Button -->
                                        <td>
                                            <form action="delete" method="post">
                                                <input type="hidden" name="id" value="${record.getId()}">
                                                <button type="submit" class="btn btn-danger">Delete</button>
                                            </form>
                                        </td>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>
                
        </div>
    
    </div>
</div>
</body>