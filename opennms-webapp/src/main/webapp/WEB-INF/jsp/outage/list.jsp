<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Outages List" />
  <jsp:param name="headTitle" value="Outages" />
  <jsp:param name="breadcrumb" value="<a href='outage/index.jsp'>Outages</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<!-- We need the </script>, otherwise IE7 breaks -->
<script type="text/javascript" src="js/extremecomponents.js"></script>

<link rel="stylesheet" type="text/css"
      href="css/onms-extremecomponents.css" />
      
<script type="text/javascript">
function filterOnValue(param, value) {
    // Clear out all filtering options
    document.forms.outageForm.not_nodeid.value = '';
    document.forms.outageForm.nodeid.value = '';
    document.forms.outageForm.not_ipinterfaceid.value = '';
    document.forms.outageForm.ipinterfaceid.value = '';
    document.forms.outageForm.not_ifserviceid.value = '';
    document.forms.outageForm.ifserviceid.value = '';
    document.forms.outageForm.smaller_iflostservice.value = '';
    document.forms.outageForm.bigger_iflostservice.value = '';
    document.forms.outageForm.smaller_ifregainedservice.value = '';
    document.forms.outageForm.bigger_ifregainedservice.value = '';

    // Set the filtering option we care about
    document.forms.outageForm[param].value = value;

    // Reset the page count    
    document.forms.outageForm.tabledata_p.value = 1;
    
    document.forms.outageForm.setAttribute('action', '${relativeRequestPath}?${pageContext.request.queryString}');
    document.forms.outageForm.setAttribute('method', 'post');
    document.forms.outageForm.submit()
}
</script>

<form id="outageForm" action="${relativeRequestPath}?${pageContext.request.queryString}" method="post">
  <input type="hidden" name="nodeid"/>
  <input type="hidden" name="not_nodeid"/>
  <input type="hidden" name="ipinterfaceid"/>
  <input type="hidden" name="not_ipinterfaceid"/>
  <input type="hidden" name="ifserviceid"/>
  <input type="hidden" name="not_ifserviceid"/>
  <input type="hidden" name="smaller_iflostservice"/>
  <input type="hidden" name="bigger_iflostservice"/>
  <input type="hidden" name="smaller_ifregainedservice"/>
  <input type="hidden" name="bigger_ifregainedservice"/>
  
  <ec:table items="tabledata" var="tabledata"
            action="${relativeRequestPath}?${pageContext.request.queryString}"
            filterable="false" imagePath="images/table/compact/*.gif"
            title="Outages" retrieveRowsCallback="limit"
            filterRowsCallback="limit" sortRowsCallback="limit" rowsDisplayed="25"
            tableId="tabledata" form="outageForm"
            view="org.opennms.web.svclayer.etable.FixedRowCompact"
            showExports="true" showStatusBar="true" autoIncludeParameters="false">
  
    <ec:exportPdf fileName="Outages.pdf" tooltip="Export PDF"
                  headerColor="black" headerBackgroundColor="#b6c2da"
                  headerTitle="Outages" />
    <ec:exportXls fileName="Outages.xls" tooltip="Export Excel" />
  
    <ec:row highlightRow="false">
      <ec:column property="node" alias="Node"
                 interceptor="org.opennms.web.svclayer.outage.GroupColumnInterceptor">
        <a href="element/node.jsp?node=${tabledata.nodeid}">${tabledata.node}</a>
        <a href="javascript:filterOnValue('nodeid', '${tabledata.nodeid}')" title="Show only outages on this node">[+]</a>
        <a href="javascript:filterOnValue('not_nodeid','${tabledata.nodeid}')" title="Do not show outages for this node">[-]</a>
       </ec:column>
  
      <ec:column property="ipaddr" alias="Interface"
                 interceptor="org.opennms.web.svclayer.outage.GroupColumnInterceptor">
        <a href="element/interface.jsp?ipinterfaceid=${tabledata.interfaceid}">${tabledata.ipaddr}</a>
        <a href="javascript:filterOnValue('ipinterfaceid', '${tabledata.interfaceid}')" title="Show only outages on this interface">[+]</a>
        <a href="javascript:filterOnValue('not_ipinterfaceid', '${tabledata.interfaceid}')" title="Do not show outages for this interface">[-]</a>
      </ec:column>
  
      <ec:column property="service" alias="Service"
                 interceptor="org.opennms.web.svclayer.outage.GroupColumnInterceptor">
        <a href="element/service.jsp?ifserviceid=${tabledata.ifserviceid}">${tabledata.service}</a>
        <a href="javascript:filterOnValue('ifserviceid', '${tabledata.ifserviceid}')" title="Show only outages on this service">[+]</a>
        <a href="javascript:filterOnValue('not_ifserviceid', '${tabledata.ifserviceid}')" title="Do not show outages for this service">[-]</a>
      </ec:column>
  
      <c:choose>
        <c:when test="${!empty param.tabledata_ev}">
          <ec:column property="iflostservice" alias="Down" title="Time Down"
                     cell="date" format="yyyy-MM-dd HH:mm:ss">
            ${tabledata.iflostservice}
          </ec:column>
        </c:when>
        
        <c:otherwise>
          <ec:column property="iflostservice" alias="Down" title="Time Down">
            <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${tabledata.iflostservice}"/>
            <a href="javascript:filterOnValue('smaller_iflostservice', '${tabledata.iflostservicelong}')" title="Show only outages beginning after this one">[&lt;]</a>
            <a href="javascript:filterOnValue('bigger_iflostservice', '${tabledata.iflostservicelong}')" title="Show only outages beginning before this one">[&gt;]</a>
           </ec:column>
        </c:otherwise>
      </c:choose>
  
      <c:choose>
        <c:when test="${param.currentOutages == 'true'}">
          <!--  don't show ifregainedservice when currentOutages == true -->
        </c:when>
        
        <c:when test="${!empty param.tabledata_ev}">
          <ec:column property="ifregainedservice" alias="Up" title="Time Up"
                     cell="date"
                     format="yyyy-MM-dd HH:mm:ss"
                     interceptor="org.opennms.web.svclayer.outage.RedColumnInterceptor" >
            ${tabledata.iflostservice}
          </ec:column>
        </c:when>
        
        <c:otherwise>
          <ec:column property="ifregainedservice" alias="Up" title="Time Up"
                     interceptor="org.opennms.web.svclayer.outage.RedColumnInterceptor" >
            <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${tabledata.ifregainedservice}"/>
            <a href="javascript:filterOnValue('smaller_ifregainedservice', '${tabledata.ifregainedservicelong}')" title="Show only outages beginning after this one">[&lt;]</a>
            <a href="javascript:filterOnValue('bigger_ifregainedservice', '${tabledata.ifregainedservicelong}')" title="Show only outages beginning before this one">[&gt;]</a>
          </ec:column>
        </c:otherwise>
      </c:choose>
  
      <ec:column property="outageid" alias="ID" title="ID">
        <a href="outage/detail.jsp?id=${tabledata.outageid}">${tabledata.outageid}</a>
      </ec:column>
  
      <%--
      <ec:column alias="checkbox" title=" " width="5px" filterable="false"
        sortable="false"
        cell="org.opennms.web.svclayer.outage.SuppressOutageCheckBox" />
  
      <ec:column sortable="false" alias="droplist" title=" " width="5px"
        filterable="false">
        <select name="suppresstime_${tabledata.outageid}"
          id="suppresstime_${tabledata.outageid}">
          <option value="0" SELECTED></option>
          <option value="15">15m</option>
          <option value="30">30m</option>
          <option value="30">60m</option>
          <option value="180">3h</option>
          <option value="480">8h</option>
          <option value="720">12h</option>
          <option value="1440">1d</option>
          <option value="4320">3d</option>
          <option value="10080">1w</option>
          <option value="-1">Forever</option>
        </select>
      </ec:column>
      --%>
  
    </ec:row>
  </ec:table>
  
  <%--
  
  <p><input type="submit" name="sel" class="button"
  	value="Suppress Outage"
  	onclick="setFormAction('outageForm','displayCurrentOutages.htm', 'post');
                 document.outageForm.submit();" />
  </p>
  
  <script type="text/javascript">
                          function setOutageState(chkbx) {
                          		
                                  //make sure that always know the state of the checkbox
                                  if (chkbx.checked) {
                                  		
                                          eval('document.forms.outageForm.chkbx_' + chkbx.name).value='SELECTED';
                                  } else {
                                          eval('document.forms.outageForm.chkbx_' + chkbx.name).value='UNSELECTED';
                                  }
                          }
       </script></form>
  
  --%>

</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

