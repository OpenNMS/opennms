<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.io.File,java.util.*,org.opennms.netmgt.config.capsd.*,org.opennms.netmgt.config.poller.*,org.opennms.netmgt.config.PollerConfigFactory, org.opennms.netmgt.config.CapsdConfigFactory,org.opennms.core.resource.Vault,org.opennms.core.utils.BundleLists,org.opennms.netmgt.ConfigFileConstants,java.io.FileInputStream" %>
<%
	HashMap scanablePlugin = new HashMap();
	HashMap scanableUserPlugin = new HashMap();

	String homeDir = Vault.getHomeDir();
        if( homeDir == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
 
        props.load( new FileInputStream( ConfigFileConstants.getFile( ConfigFileConstants.POLLER_CONF_FILE_NAME )));
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

%><%!
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

<script language="Javascript" type="text/javascript" >

  function saveFile()
  {
      var port1 = document.poller.port1.value

      if(port1 != '' && document.poller.name1.value == '')
      {
	alert ('Name field cannot be empty')
	return;
      }

      var newport = true;
      if(isNaN(port1))
      {
	colonIndex = port1.indexOf(':');
	if(colonIndex == -1)
	{
		alert("Please check the value of port again and retry later")
		return
	}
        while(colonIndex != -1)
        {
		var len = port1.length;
		var firsthalf = port1.substring(0, colonIndex);
		if(isNaN(firsthalf))
		{
			newport = false;
			alert("Please check the value of port again and retry later")
			return;
		}
		else
		{
			if(colonIndex+1 < len)
			{
				port1 = port1.substring(colonIndex+1, len);
			}
		}
		colonIndex = port1.indexOf(':');
		if(colonIndex == -1){
			if(isNaN(port1))
			{
				alert("Please check the value of port again and retry later")
	                        return;
			}
		}
         }
      }
         if(newport)
         {
              document.poller.action="admin/pollerConfig/addPollerConfig";
              document.poller.submit();
         }
  }
  function cancel()
  {
      document.poller.action="admin/pollerConfig/index.jsp";
      document.poller.method="get";
      document.poller.submit();
  }

</script>
  <title>Add New Custom Poller | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<%
String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>";
String breadcrumb2 = "Configure Pollers";
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Add New Custom Poller" />
  <jsp:param name="location" value="Poller Config" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br />

<!-- Body -->
<form method="post" name="poller" action="admin/pollerConfig/addPollerConfig">
<table border="0" width="100%" cellspacing="0" cellpadding="1">
	<tr>
		<td>&nbsp;</td>
		<td width="50%" valign="top">
			<h3>Add New Custom Poller</h3>

			<% int rowCounter = 0; %>
			<table width="100%" border="1" cellspacing="0" cellpadding="1">
			<tr BGCOLOR="#999999">
				<td><b>Active</b></td>
				<td><b>Poller Name</b></td>
				<td><b>Protocol</b></td>
				<td><b>Port</b></td>
			</tr>
			<tr <% if(rowCounter % 2 == 0){ %>BGCOLOR="#cccccc"<% } %>>
				<td><input type="checkbox" name="check1" /></td>
				<td><input type="text" name="name1" /></td>
				<td>
					<select name="protArray1" size="1">
						<% for( int i=0; i < sortedProtocols.length; i++ ) { %>
							<option><%= sortedProtocols[i] %></option>
						<% } %>
					</select>
                		</td>
		                <td><input type="text" name="port1"/></td>
		        </tr>
			</table>
			<br />

			<input type="button" value="Add" onClick="saveFile()" />&nbsp;&nbsp;
			<input type="button" value="Cancel" onClick="cancel()" />
		</td>
		<td width="10">&nbsp;</td>
		<td valign="top">
			<h3>Descriptions</h3>
			<p>The <b>Add New Custom Poller</b> page gives the administrator the ability to add a new poller that will poll services not covered by the default pollers.</p>
			<p>The <i><b>Active</b></i> column is the current status of the poller. If the active field is checked, the poller will be turned on and will scan the network during the next poller rescan.</p>
			<p>The <i><b>Poller Name</b></i> column is the name of the new service to be added. Any name can be used; for instance, &quot;Company ABC Intranet HTTP&quot; or &quot;My Secret SSH Service&quot; are both valid.</p>
			<p>The <i><b>Protocol</b></i> column is the protocol used for polling the service. If you simply want to check to see if the target port is open, then choose &quot;TCP&quot; as the protocol.</p>
			<p>The <i><b>Port</b></i> column lists the ports at which the service will be polled. If there is more than one port where the service can be located, the values should be separated by colons (:).</p>
			<p>After you add the custom poller, click on the <b><i>Apply Changes</i></b> button on the <b>Configure Pollers</b> page to apply the settings.</p>
		</td>
		<td>&nbsp;</td>
	</tr>
</table>
</form>
<br />
<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="Poller Config" />
</jsp:include>
</body>
</html>
