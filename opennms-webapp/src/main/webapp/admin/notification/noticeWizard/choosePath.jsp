<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Path" />
  <jsp:param name="headTitle" value="Choose Path" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Choose Path" />
</jsp:include>

<script type="text/javascript" >
  
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

<form method="post" name="info"
      action="admin/notification/noticeWizard/notificationWizard">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH%>"/>

<div class="row">
  <div class="col-md-7">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Choose the destination path and enter the information to send via the notification</h3>
      </div>
      <table class="table table-condensed">
        <tr>
          <td width="10%" valign="top" align="left">
            <label>Name:</label>
          </td>
          <td valign="top" align="left">
            <input type="text" class="form-control" name="name" value='<%=(newNotice.getName()!=null ? newNotice.getName() : "")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            <label>Description:</label>
          </td>
          <td valign="top" align="left">
            <input type="text" class="form-control" name="description" value='<%=newNotice.getDescription().orElse("")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            <label>Parameter:</label>
          </td>
          <td valign="top" align="left">
            <div class="row">
              <div class="col-md-6">
                <label>Name:</label> <input type="text" class="form-control" size="30" name="varbindName" value='<%=varbindName%>'/>
              </div>
              <div class="col-md-6">
                <label>Value:</label> <input class="form-control" type="text" size="30" name="varbindValue" value='<%=varbindValue%>'/>
              </div>
            </div>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            <label>Choose A Path:</label>
          </td>
          <td valign="top" align="left">
            <%=buildPathSelect(newNotice.getDestinationPath())%>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            <label>Text Message:</label>
          </td>
          <td valign="top" align="left">
            <textarea rows="3" class="form-control" name="textMsg"><%=(newNotice.getTextMessage()!=null ? newNotice.getTextMessage() : "")%></textarea>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            <label>Short Message:</label>
          </td>
          <td valign="top" align="left">
            <textarea rows="1" class="form-control" name="numMsg"><%=newNotice.getNumericMessage().orElse("")%></textarea>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            <label>Email Subject:</label>
          </td>
          <td valign="top" align="left">
            <input type="text" class="form-control" name="subject" value='<%=newNotice.getSubject().orElse("")%>'/>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            <label>Special Values:</label>
          </td>
          <td valign="top" align="left">
            <table class="table table-condensed">
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
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    public String buildPathSelect(String currentPath)
      throws ServletException
    {
         StringBuffer buffer = new StringBuffer("<select class=\"form-control\" NAME=\"path\">");
         
         Map<String, Path> pathsMap = null;
         
         try {
             pathsMap = new TreeMap<String, Path>(DestinationPathFactory.getInstance().getPaths());
             for (String key : pathsMap.keySet()) {
                 if (key.equals(currentPath))
                 {
                    buffer.append("<option SELECTED VALUE=" + key + ">" + key + "</option>");
                 }
                 else
                 {
                    buffer.append("<option VALUE=" + key + ">" + key + "</option>");
                 }
             }
         } catch (Throwable e)
         {
            throw new ServletException("couldn't get destination path list.", e);
         }
         buffer.append("</select>");
         
         return buffer.toString();
    }
%>
