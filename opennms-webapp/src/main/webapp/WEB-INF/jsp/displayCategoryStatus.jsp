<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
    
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<style type="text/css" media="screen">

body {
    background: #fff;
    font-family: Verdana, Arial, Helvetica, sans-serif;
    /*font-size: 75.5%; */
/*  margin: 0px 0px 0px 0px; */
    font-size: 8pt;
}

.sections {
}

.section {
    border-width:1px;
    border-color: #333;
    border-style: solid;
    margin-top: 4px;
}


.sectionHeading {
    background-color: #333;
    color: #ccc;
    font-weight: bold;
    font-size: 12pt;
    padding: 2px 2px 2px 2px;
}

.categorys {
}

.category {

    border-width:1px;
    border-color: #8fc80d;
    border-style: solid;
    margin-top: 6px;
    margin-left: 6px;
    margin-right: 6px;
    margin-bottom: 6px;
}


.categoryHeading {
    background-color: #8fc80d;
    cursor: pointer;
    font-weight: bold;
}

.nodes {
    background-color: #ccc;
    padding: 2px 2px 2px 2px;
}

.node {
        background-color: #ddd;
        border-color: #000;
        border-style: dashed;
        border-width:1px;
        margin: 4px 4px;
        
}

.nodeHeading {
        background-color: #eee;
        padding-left: 4px;
        padding-top: 1px;
        padding-bottom: 1px;    
}

.interfaces {
	padding-left: 6px;
}

.interfaceLabel {

}

.services {
        font-size: 7pt;
        font-style:italic;
        padding-left: 8px;

}

</style>

</head>


<body>

	<div style="width:500px">

		<div class="sections">
        	<c:forEach items="${statusTree}" var="section">
            	<div class="section">
                	<div class="sectionHeading"><c:out value="${section.name}" /></div>

	                <div class="categorys">
    	                <c:forEach items="${section.categories}" var="category">
        	                <div class="category">
            	                <div class="categoryHeading"><c:out value="${category.label}" /></div>
                	            <div class="nodes">
                    	            <c:forEach items="${category.nodes}" var="node">
                        	            <div class="node">
                            	            <div class="nodeHeading"><c:out value="${node.label}" /></div>
                                	        <div class="interfaces">
                                    	        <c:forEach items="${node.ipInterfaces}" var="ipinterface">
                                        	        <div class="interface">
                                            	        <div class="interfaceLabel"><c:out value="${ipinterface.ipAddress}" /></div>                                                    <div class="services">
                                                	        <c:forEach items="${ipinterface.services}" var="service">
                                                    	        <div class="service"><c:out value="${service.name}" /></div>
                                                        	</c:forEach>
                                                    	</div>
                                                	</div>
                                            	</c:forEach>
                                        	</div>
                                    	</div>
                                	</c:forEach>
                            	</div>
                        	</div>
                    	</c:forEach>
                	</div>
            	</div>
        	</c:forEach>
    	</div>
	</div>
</body>
