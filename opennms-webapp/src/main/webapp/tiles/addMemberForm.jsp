<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
</head>
<body>
<form action="addMember.htm"  method="POST">
First Name: <input name="firstName" type="text"/>
Last Name: <input name="lastName" type="text"/>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset">
</form>
</body>
</html>
