<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.element.NetworkElementFactory,org.opennms.web.event.*" %>

<%
    //get the service names, in alpha order
    Map serviceNameMap = new TreeMap(NetworkElementFactory.getServiceNameToIdMap());
    Set serviceNameSet = serviceNameMap.keySet();
    Iterator serviceNameIterator = serviceNameSet.iterator();
    
    //get the severity names, in severity order
    List severities = EventUtil.getSeverityList();
    Iterator severityIterator = severities.iterator();

    //get the current time
    Calendar now = Calendar.getInstance();
%>

<form action="event/query" method="GET">
  <table border="0" cellpadding="2" cellspacing="0">
    <tr>
      <td>Event Text:</td>
      <td>Time:</td>                  
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td><input type="text" name="msgmatchany" /></td>
      <td>
        <select name="relativetime" size="1">
          <option value="0" selected><%=EventUtil.ANY_RELATIVE_TIMES_OPTION%></option>
          <option value="1">Last hour</option>
          <option value="2">Last 4 hours</option>
          <option value="3">Last 8 hours</option>
          <option value="4">Last 12 hours</option>
          <option value="5">Last day</option>
          <option value="6">Last week</option>
          <option value="7">Last month</option>                
        </select>
      </td>
      <td><input type="submit" value="Search" /></td>            
    </tr>
  </table>
</form>



