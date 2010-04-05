<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.admin.notification.noticeWizard.*,
		org.opennms.netmgt.config.notifications.*,
        org.opennms.netmgt.config.destinationPaths.*,
		org.opennms.netmgt.config.*
	"
%>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
    Notification newNotice = (Notification)user.getAttribute("newNotice");
    Varbind varbind=newNotice.getVarbind();
    String varbindName="";
    String varbindValue="";
    if(varbind!=null) {
        
        if(varbind.getVbname()!=null) {
            varbindName=varbind.getVbname();
		}
        if(varbind.getVbvalue()!=null) {
         	varbindValue=varbind.getVbvalue();
        }
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Path" />
  <jsp:param name="headTitle" value="Choose Path" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Choose Path" />
</jsp:include>

<script language="JAVASCRIPT" >
  
    function trimString(str) 
    {
        while (str.charAt(0)==" ")
        {
          str = str.substring(1);
        }
        while (str.charAt(str.length - 1)==" ")
        {
          str = str.substring(0, str.length - 1);
        }
        return str;
    }
    
    function finish()
    {
        trimmedName = trimString(document.info.name.value);
        trimmedText = trimString(document.info.textMsg.value);
        if (trimmedName=="")
        {
            alert("Please give this notification a name.");
        }
        else if (trimmedText=="")
        {
            alert("Please enter a text message for this notification.");
        }
        else if (document.info.path.selectedIndex==-1)
        {
            alert("Please select a destination path for this notification.");
        }
        else
        {
            document.info.submit();
        }
    }
  
</script>

<h2><%=(newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() + "<br/>" : "")%></h2>

<h3>Choose the destination path and enter the information to send via the notification</h3>

<form method="post" name="info"
      action="admin/notification/noticeWizard/notificationWizard">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH%>"/>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td width="10%" valign="top" align="left">
            Name:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="name" value='<%=(newNotice.getName()!=null ? newNotice.getName() : "")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            Description:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="description" value='<%=(newNotice.getDescription()!=null ? newNotice.getDescription() : "")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            Parameter:
          </td>
          <td valign="top" align="left">
            Name: <input type="text" size="30" name="varbindName" value='<%=varbindName%>'/>
			Value: <input type="text" size="30" name="varbindValue" value='<%=varbindValue%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            Choose A Path:
          </td>
          <td valign="top" align="left">
            <%=buildPathSelect(newNotice.getDestinationPath())%>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Text Message:
          </td>
          <td valign="top" align="left">
            <textarea rows="3" cols="100" name="textMsg"><%=(newNotice.getTextMessage()!=null ? newNotice.getTextMessage() : "")%></textarea>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Short Message:
          </td>
          <td valign="top" align="left">
            <textarea rows="1" cols="100" name="numMsg"><%=(newNotice.getNumericMessage()!=null ? newNotice.getNumericMessage() : "")%></textarea>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Email Subject:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="subject" value='<%=(newNotice.getSubject()!=null ? newNotice.getSubject() : "")%>'/>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Special Values:
          </td>
          <td valign="top" align="left">
            <table width="100%" border="0" cellspacing="0" cellpadding="1">
              <tr>
                <td colspan="3">Can be used in both the text message and email subject:</td>
              </tr>
              <tr>
                <td>%noticeid% = Notification ID number</td>
                <td>%time% = Time sent</td>
                <td>%severity% = Event severity</td>                          
              </tr>
              <tr>
                <td>%nodelabel% = May be IP address or empty</td>
                <td>%interface% = IP address, may be empty</td>
                <td>%service% = Service name, may be empty</td>
              </tr>
              <tr>
				<td>%eventid% = Event ID, may be empty</td>
				<td>%parm[a_parm_name]% = Value of a named event parameter</td>
				<td>%parm[#N]% = Value of the event parameter at index N</td>
			  </tr>
			  <tr>
			    <td>%ifalias% = SNMP ifAlias of affected interface</td>
			    <td>%interfaceresolve% = Reverse DNS name of interface IP address</td>
               <td>%operinstruct% = Operator instructions from event definition</td>		     
			  </tr>
            </table>
          </td>
         </tr>
         
        <tr>
          <td colspan="2">
            <a HREF="javascript:finish()">Finish</a>
          </td>
        </tr>
      </table>
      </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    public String buildPathSelect(String currentPath)
      throws ServletException
    {
         StringBuffer buffer = new StringBuffer("<select NAME=\"path\">");
         
         Map<String, Path> pathsMap = null;
         
         try {
            pathsMap = new TreeMap<String, Path>(DestinationPathFactory.getInstance().getPaths());
         Iterator iterator = pathsMap.keySet().iterator();
         while(iterator.hasNext())
         { 
                 String key = (String)iterator.next();
                 if (key.equals(currentPath))
                 {
                    buffer.append("<option SELECTED VALUE=" + key + ">" + key + "</option>");
                 }
                 else
                 {
                    buffer.append("<option VALUE=" + key + ">" + key + "</option>");
                 }
            }
         } catch (Exception e)
         {
            throw new ServletException("couldn't get destination path list.", e);
         }
         buffer.append("</select>");
         
         return buffer.toString();
    }
%>
