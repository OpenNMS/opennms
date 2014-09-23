<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="id"
              required="true"
              type="java.lang.String"
              description="Unique identifier of this tooltip. Must be unique in the whole page to show/hide the tooltip" %>

<%
    // only include the tooltip.js file once
    if (request.getAttribute("tooltip.js.included") == null) {
        %>
        <script type='text/javascript' src='js/tooltip.js'></script>
        <%
        request.setAttribute("tooltip.js.included", true);
    }
%>

<div id="${id}" class="tooltip">
    <p>
        <jsp:doBody/>
    </p>
</div>
<img src="css/images/ui-trans_1x1.png" class="info" onMouseOver="showTT('${id}')" onMouseOut="hideTT()"/>
