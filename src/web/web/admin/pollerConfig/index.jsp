<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.io.File,java.util.*,org.opennms.netmgt.config.capsd.*,org.opennms.netmgt.config.poller.*,org.opennms.netmgt.config.PollerConfigFactory, org.opennms.netmgt.config.CapsdConfigFactory,org.opennms.core.resource.Vault,org.opennms.core.utils.BundleLists,org.opennms.netmgt.ConfigFileConstants,java.io.FileInputStream" %>
<%
	HashMap scanablePlugin = new HashMap();
	HashMap scanableUserPlugin = new HashMap();
//	String protocols[] = {"SMTP", "FTP", "Postgres", "MSExchange", "MySQL", "IMAP", "POP3", "TCP", "HTTP"};

	String homeDir = Vault.getHomeDir();
        if( homeDir == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
 
        props.load( new FileInputStream( ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME )));
	protoMap = getQueries();
        String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
 
	java.util.List polledPlugins = new ArrayList();
	PollerConfigFactory pollerFactory = null;
	PollerConfiguration pollerConfig = null;
	try
	{
		PollerConfigFactory.init();
		pollerFactory = PollerConfigFactory.getInstance();
		pollerConfig = pollerFactory.getConfiguration();
	     	if(pollerConfig != null)
     		{
        		Collection packColl = pollerConfig.getPackageCollection();
        		if(packColl != null)
        		{
                		Iterator iter = (Iterator)packColl.iterator();
                		if(iter.hasNext())
                		{
                        		org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package)iter.next();
                        		if(pkg != null)
                        		{
                                		Collection svcCollection = pkg.getServiceCollection();
                                		if(svcCollection != null)
                                		{
                                        		Iterator svcIter = svcCollection.iterator();
                                        		while(svcIter.hasNext())
                                        		{
                                                		org.opennms.netmgt.config.poller.Service svcs = (org.opennms.netmgt.config.poller.Service)svcIter.next();
                                                		if(svcs != null)
                                                		{
									if(svcs.getUserDefined().equals("true"))
									{
										scanableUserPlugin.put(svcs.getName(), svcs);
									}
									else
									{
										scanablePlugin.put(svcs.getName(), svcs);
									}
                                                        		String status = svcs.getStatus();
                                                        		if(status != null && status.equals("on"))
                                                        		{
										polledPlugins.add(svcs.getName());
                                                        		}
                                                		}
                                        		}
                                		}
                        		}
                		}
        		}
		}

		if(pollerConfig == null)
		{
			throw new ServletException("Poller Configuration file is empty");
		}
	}
	catch(Exception e)
	{
		throw new ServletException(e);
	}

%>

<script language="Javascript" type="text/javascript" >
 
  function modifyPoller()
  {
      document.poller.redirect.value="finishPollerConfig.jsp";
      document.poller.action="admin/pollerConfig/pollerConfig";
      document.poller.submit();
  }

  function addPoller()
  {
      document.poller.redirect.value="addPollerConfig.jsp";
      document.poller.action="admin/pollerConfig/pollerConfig";
      document.poller.submit();
  }   
</script>
<%!
	Map protoMap;
        Properties props = new java.util.Properties();
	String[] sortedProtocols;
    	public Map getQueries() {
		Map queries = new HashMap();

        	if( this.protoMap == null ) {
            		String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
			sortedProtocols = new String[protocols.length];
            		this.protoMap = new TreeMap();
 
			TreeMap sortTmp = new TreeMap();
            		for( int i = 0; i < protocols.length; i++ )
            		{
                		this.protoMap.put(this.props.getProperty( "service." + protocols[i] + ".protocol" ), protocols[i]);
				sortTmp.put(protocols[i], "service." + protocols[i] + ".protocol");
            		}	
				
			Set keys = sortTmp.keySet();
			Iterator sortIter = keys.iterator();
			int i = 0;
			while(sortIter.hasNext())
			{
				String key = (String)sortIter.next();
				sortedProtocols[i++] = key;
			}
        	}
 
        	return( queries );
    	}

    	public String getProtocol(String servicename)
    	{
            	return (String)this.props.getProperty( "service." + servicename + ".protocol" );
    	}

    	public void setProtocol(String servicename, String protocol)
    	{
            	props.setProperty( "service." + servicename + ".protocol", protocol );
    	}

    	public void setMonitor(String servicename, String monitor)
    	{
            	props.setProperty( "service." + servicename + ".monitor", monitor );
    	}

    	public void setCapsdClass(String servicename, String capsdclass)
    	{
            	props.setProperty( "service." + servicename + ".capsd-class", capsdclass );
    	}
%>

<html>
<head>
  <title>Configure Pollers | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
</script>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Configure Pollers"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Pollers" />
  <jsp:param name="location" value="Poller Config" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<FORM METHOD="POST" name="poller" action="admin/pollerConfig/pollerConfig"  >
<input type="hidden" name="redirect" value=""/>
<!-- All the information that is in capsd is displayed on the form, if they are in poller they are checked -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >

<tr>
<td>&nbsp;</td>
<td width="50%" valign="top" >

<h3>Default Pollers</h3>
<table border="1" width="100%" cellspacing="0" cellpadding="0" valign="top">
      <tr BGCOLOR="#999999">
	<td><b>Active</b></td>
	<td><b>Poller Name</b></td>
	<td><b>Protocol</b></td>
	<td><b>Port</b></td>
      </tr>
      <%
	Set scanned = (Set) scanablePlugin.keySet();
	Iterator iterator = scanned.iterator();
	int rowCounter = 0;
	while(iterator.hasNext())
	{
		String servicename = (String)iterator.next();
		Service svc = (Service)scanablePlugin.get(servicename);
		if(svc != null)
		{
			String user = request.getRemoteUser();
			String status = svc.getStatus();
			String port = "<b>-</b>";

			Enumeration param = svc.enumerateParameter();
			while(param.hasMoreElements())
			{
				Parameter parameter = (Parameter)param.nextElement();
				if(parameter != null)
				{
					if(parameter.getKey().equals("port") || parameter.getKey().equals("ports"))
					{
						port = parameter.getValue();
					}
				}
			}
       %>
			<tr <% if(rowCounter % 2 == 0){ %>
				 BGCOLOR="#cccccc"
			    <% } %>

			><td><input type="checkbox"
		<%
				if(polledPlugins != null && polledPlugins.contains(servicename)){%>
					checked
		<% 		} %>
			name="activate" value="<%= servicename %>" ></td>
			<td><%= servicename %></td>
			<td><% if(getProtocol(servicename) == null){ %>
				<%= servicename %>
			    <% }else{ %>
				<%= getProtocol(servicename) %>
			    <% } %>
			</td>
			<td><%= port %></td></tr>
	  <%
 		}
		rowCounter++;
	}
	%>
	</table>

	<%
        Set userscanned = (Set) scanableUserPlugin.keySet();
	if(userscanned.size() > 0)
	{
	%>

	<br /><br />
	<table width="100%"><tr>
	<td>
	<h3>Custom Pollers</h3>
	<table border="1" cellspacing="0" cellpadding="0" valign="top" width="100%">
      <tr BGCOLOR="#999999">
	<td><b>Active</b></td>
	<td><b>Poller Name</b></td>
	<td><b>Protocol</b></td>
	<td><b>Port</b></td>
	<td><b>Delete</b></td>
      </tr>
	<%

        iterator = userscanned.iterator();
        while(iterator.hasNext())
        {
                String servicename = (String)iterator.next();
                Service svc = (Service)scanableUserPlugin.get(servicename);
                if(svc != null)
                {
                        String user = request.getRemoteUser();
                        String status = svc.getStatus();
                        String port = "<b>-</b>";

                        Enumeration param = svc.enumerateParameter();
                        while(param.hasMoreElements())
                        {
                                Parameter parameter = (Parameter)param.nextElement();
                                if(parameter != null)
                                {
                                        if(parameter.getKey().equals("port") || parameter.getKey().equals("ports"))
                                        {
                                                port = parameter.getValue();
                                        }
                                }
                        }
       %>
                        <tr <%=(rowCounter % 2 == 0) ? "BGCOLOR='#cccccc'" : "" %>>
                          <td><input type="checkbox"
                <%      if(polledPlugins != null && polledPlugins.contains(servicename)){%>
				checked
                <%      } %>
                        name="activate" value="<%= servicename %>" ></td>
			
                        <td><%= servicename %></td>
                        <td><% if(getProtocol(servicename) == null){ %>
                                <%= servicename %>
                            <% }else{ %>
                                <%= getProtocol(servicename) %>
                            <% } %>
                        </td>
                        <td><%= port %></td>
                        <td><input type="checkbox" name="delete" value="<%= servicename %>"></td>
                      </tr>
          <%
                }
                rowCounter++;
        }%>
	</table></td></tr>
</table>
	<% } %>

	<br>
	<input type="button" value="Apply Changes" onClick="modifyPoller()" />&nbsp;&nbsp;
	<input type="button" value="Add Custom Poller" onClick="addPoller()" />
</td>
<td width="10">&nbsp;</td>
<td valign="top">
            <h3>Descriptions</h3>

		<p>This page provides a list of all of the pollers that the system uses to check the status of services on the network. On this page, administrators can enable or disable specific pollers and define new custom pollers.</p>
		<p>The <i><b>Active</b></i> column shows the current status of the poller. If the active field is checked, the poller will be used to scan the network during the next poller rescan.</p>
		<p>The <i><b>Poller Name</b></i> column shows the name of each service in the poller configuration.</p>
		<p>The <i><b>Protocol</b></i> column shows the protocol used for polling each service.</p>
		<p>The <i><b>Port</b></i> column shows the ports at which the service will be polled. If there is more than one port, the values should be seperated by colons (:).</p>
		<p>If you add custom pollers by using the <b>Add New Custom Pollers</b> page, a table with the heading <b><i>Custom Pollers</i></b> will be present to list them. There are checkboxes on each line to allow you to enable, disable, or delete each custom poller.</p>

</td>
<td>&nbsp;</td>
</tr>
</table>
</FORM>
<br>
<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="Poller Config" />
</jsp:include>
</body>
</html>
