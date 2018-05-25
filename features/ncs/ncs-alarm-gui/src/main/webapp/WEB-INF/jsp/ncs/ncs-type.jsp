<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page import="org.opennms.web.servlet.XssRequestWrapper"%>
<%@page language="java"
    contentType="text/html"
    session="true" %>

<%@page import="java.util.Set" %>
<%@page import="org.opennms.netmgt.model.ncs.NCSComponent"%>
<%@page import="org.opennms.netmgt.model.ncs.NCSComponentRepository"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
XssRequestWrapper req = new XssRequestWrapper(request);
String treeView = (String) req.getAttribute("treeView");

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="NCS Component Hierarchy" />
  <jsp:param name="headTitle" value="Component Hierarchy" />
  <jsp:param name="breadcrumb" value="Component Hierarchy" />
</jsp:include>
<style type="text/css">
.TreeView 
{
    font: Verdana;
    line-height: 20px;
    cursor: pointer; 
    font-style: normal;
}

.TreeView LI
{
    /* The padding is for the tree view nodes */
    padding: 0 0 0 18px;
    float: left;
    width: 100%;
    list-style: none;
}

.TreeView, .TreeView ul
{
    margin: 0;
    padding: 0;
}

LI.Expanded 
{
    background: url(/opennms/images/minus.gif) no-repeat left top;
}

LI.Expanded ul
{
    display: block;
}

LI.Collapsed 
{
    background: url(/opennms/images/plus.gif) no-repeat left top;
}

LI.Collapsed ul
{
    display: none;
}

.Highlighted
{
    color: red;
}

.AlternateHighlight
{
    color: blue;
}
</style>
<script type="text/javascript">

//Toggles between two classes for an element
function ToggleClass(element, firstClass, secondClass, event)
{
    event.cancelBubble = true;
    
    var classes = element.className.split(" ");
    var firstClassIndex = classes.indexOf(firstClass);
    var secondClassIndex = classes.indexOf(secondClass);
    
    if (firstClassIndex == -1 && secondClassIndex == -1)
    {
        classes[classes.length] = firstClass;
    }
    else if (firstClassIndex != -1)
    {
        classes[firstClassIndex] = secondClass;
    }
    else
    {
        classes[secondClassIndex] = firstClass;
    }
    
    element.className = classes.join(" ");
    
}

//The toggle event handler for each expandable/collapsable node
//- Note that this also exists to prevent any IE memory leaks 
//(due to circular references caused by this)
function ToggleNodeStateHandler(event)
{
    ToggleClass(this, "Collapsed", "Expanded", (event == null) ? window.event : event);
}

//Prevents the onclick event from bubbling up to parent elements
function PreventBubbleHandler(event)
{
    if (!event) event = window.event;
    event.cancelBubble = true;
}

//Adds the relevant onclick handlers for the nodes in the tree view
function SetupTreeView(elementId)
{
    var tree = document.getElementById(elementId);
    var treeElements = tree.getElementsByTagName("li");
    
    for (var i=0; i < treeElements.length; i++)
    {
        if (treeElements[i].getElementsByTagName("ul").length > 0)
        {
            treeElements[i].onclick = ToggleNodeStateHandler; 
        }
        else
        {
            treeElements[i].onclick = PreventBubbleHandler; 
        }
    }
}

</script>
<div>
    <%=treeView%>

</div>
<script type="text/javascript">

SetupTreeView("TreeView");

</script>
<jsp:include page="/includes/footer.jsp" flush="false" />

