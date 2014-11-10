<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.netmgt.config.poller.*,
		org.opennms.netmgt.config.PollerConfigFactory,
		org.opennms.netmgt.config.PollerConfig,
		org.opennms.core.resource.Vault,
		org.opennms.core.utils.BundleLists,
		org.opennms.core.utils.ConfigFileConstants,
		java.io.FileInputStream
	"
%>

<%
    HashMap<String,Service> scanablePlugin = new HashMap<String,Service>();
	HashMap<String,Service> scanableUserPlugin = new HashMap<String,Service>();
//	String protocols[] = {"SMTP", "FTP", "Postgres", "MSExchange", "MySQL", "IMAP", "POP3", "TCP", "HTTP"};

	String homeDir = Vault.getHomeDir();
        if( homeDir == null ) {
    throw new IllegalArgumentException( "Cannot take null parameters." );
        }
 
        props.load( new FileInputStream( ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME )));
	protoMap = getQueries();
 
	java.util.List<String> polledPlugins = new ArrayList<String>();
	PollerConfig pollerFactory = null;
	PollerConfiguration pollerConfig = null;
	try
	{
		PollerConfigFactory.init();
		pollerFactory = PollerConfigFactory.getInstance();
		pollerConfig = pollerFactory.getConfiguration();
	     	if(pollerConfig != null)
     		{
        		Collection<org.opennms.netmgt.config.poller.Package> packColl = pollerConfig.getPackages();
        		if(packColl != null)
        		{
        		Iterator<org.opennms.netmgt.config.poller.Package> iter = packColl.iterator();
        		if(iter.hasNext())
        		{
        		org.opennms.netmgt.config.poller.Package pkg = iter.next();
        		if(pkg != null)
        		{
                		Collection<org.opennms.netmgt.config.poller.Service> svcCollection = pkg.getServices();
                		if(svcCollection != null)
                		{
                				for (Service svcs : svcCollection) {
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
	catch(Throwable e)
	{
		throw new ServletException(e);
	}
%>

<%!
	Map<String,String> protoMap;
        Properties props = new java.util.Properties();
	String[] sortedProtocols;
    	public Map<String,String> getQueries() {
		Map<String,String> queries = new HashMap<String,String>();

        	if( this.protoMap == null ) {
            		String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
			sortedProtocols = new String[protocols.length];
            		this.protoMap = new TreeMap<String,String>();
 
			TreeMap<String,String> sortTmp = new TreeMap<String,String>();
            		for( int i = 0; i < protocols.length; i++ )
            		{
                		this.protoMap.put(this.props.getProperty( "service." + protocols[i] + ".protocol" ), protocols[i]);
				sortTmp.put(protocols[i], "service." + protocols[i] + ".protocol");
            		}	
				
			Set<String> keys = sortTmp.keySet();
			int i = 0;
			for (String key : keys) {
				sortedProtocols[i++] = key;
			}
        	}
 
        	return queries;
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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Pollers" />
  <jsp:param name="headTitle" value="Configure Pollers" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="Poller Config" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure Pollers" />
</jsp:include>

<script type="text/javascript" >
 
  function modifyPoller()
  {
      document.poller.redirect.value="finishPollerConfig.jsp";
      document.poller.action="admin/pollerConfig/pollerConfig";
      return true;
  }

  function addPoller()
  {
      document.poller.redirect.value="addPollerConfig.jsp";
      document.poller.action="admin/pollerConfig/pollerConfig";
      document.poller.submit();
  }   
</script>

<div id="contentleft">
  <h3>Default Pollers</h3>

  <form method="post" name="poller" action="admin/pollerConfig/pollerConfig" onsubmit="return modifyPoller();">
    <input type="hidden" name="redirect" value=""/>
    <!-- All the information that is in capsd is displayed on the form, if
         they are in poller they are checked -->

    <table class="standard">
      <tr>
	<td class="standardheader">Active</td>
	<td class="standardheader">Poller Name</td>
	<td class="standardheader">Protocol</td>
	<td class="standardheader">Port</td>
      </tr>
      <%
	Set<String> scanned = scanablePlugin.keySet();
	int rowCounter = 0;
	for (String servicename : scanned) {
		Service svc = (Service)scanablePlugin.get(servicename);
		if(svc != null)
		{
			String port = "<b>-</b>";

			for (final Parameter parameter : svc.getParameters()) {
				if(parameter != null)
				{
					if(parameter.getKey().equals("port") || parameter.getKey().equals("ports"))
					{
						port = parameter.getValue();
					}
				}
			}
       %>
		<%-- XXX not sure if this will work with the CSS classes --%>
			<tr <% if(rowCounter % 2 == 0){ %>
				 bgcolor="#cccccc"
			    <% } %>

			><td class="standard"><input type="checkbox"
		<%
				if(polledPlugins != null && polledPlugins.contains(servicename)){%>
					checked
		<% 		} %>
			name="activate" value="<%= servicename %>" ></td>
			<td class="standard"><%= servicename %></td>
			<td class="standard"><% if(getProtocol(servicename) == null){ %>
				<%= servicename %>
			    <% }else{ %>
				<%= getProtocol(servicename) %>
			    <% } %>
			</td>
			<td class="standard"><%= port %></td></tr>
	  <%
 		}
		rowCounter++;
	}
	%>
	</table>

	<%
        Set<String> userscanned = scanableUserPlugin.keySet();
	if(userscanned.size() > 0)
	{
	%>

    <h3>Custom Pollers</h3>

    <table class="standard">
      <tr>
	<td class="standardheader">Active</td>
	<td class="standardheader">Poller Name</td>
	<td class="standardheader">Protocol</td>
	<td class="standardheader">Port</td>
	<td class="standardheader">Delete</td>
      </tr>
	<%

        for (String servicename : userscanned) {
                Service svc = (Service)scanableUserPlugin.get(servicename);
                if(svc != null)
                {
                        String port = "<b>-</b>";

                        for (final Parameter parameter : svc.getParameters()) {
                                if(parameter != null)
                                {
                                        if(parameter.getKey().equals("port") || parameter.getKey().equals("ports"))
                                        {
                                                port = parameter.getValue();
                                        }
                                }
                        }
       %>
		<%-- XXX not sure if this will work with the CSS classes --%>
                        <tr <%=(rowCounter % 2 == 0) ? "bgcolor='#cccccc'" : "" %>>
                          <td class="standard"><input type="checkbox"
                <%      if(polledPlugins != null && polledPlugins.contains(servicename)){%>
				checked
                <%      } %>
                        name="activate" value="<%= servicename %>" ></td>
			
                        <td class="standard"><%= servicename %></td>
                        <td class="standard"><% if(getProtocol(servicename) == null){ %>
                                <%= servicename %>
                            <% }else{ %>
                                <%= getProtocol(servicename) %>
                            <% } %>
                        </td>
                        <td class="standard"><%= port %></td>
                        <td class="standard"><input type="checkbox" name="delete" value="<%= servicename %>"></td>
                      </tr>
          <%
                }
                rowCounter++;
        }%>
	</table>
	<% } %>

    <br/>

    <input type="submit" value="Apply Changes"/>
    &nbsp;&nbsp;
    <input type="button" value="Add Custom Poller" onclick="addPoller()" />
  </form>
</div>

<div id="contentright">
  <h3>Descriptions</h3>

  <p>
    This page provides a list of all of the pollers that the system uses
    to check the status of services on the network. On this page,
    administrators can enable or disable specific pollers and define new
    custom pollers.
  </p>

  <p>
    The <i><b>Active</b></i> column shows the current status of the
    poller. If the active field is checked, the poller will be used to
    scan the network during the next poller rescan.
  </p>

  <p>
    The <i><b>Poller Name</b></i> column shows the name of each service
    in the poller configuration.
  </p>

  <p>
    The <i><b>Protocol</b></i> column shows the protocol used for
    polling each service.
  </p>

  <p>
    The <i><b>Port</b></i> column shows the ports at which the service
    will be polled. If there is more than one port, the values should
    be separated by colons (:).
  </p>

  <p>
    If you add custom pollers by using the <b>Add New Custom Pollers</b>
    page, a table with the heading <b><i>Custom Pollers</i></b> will be
    present to list them. There are checkboxes on each line to allow you
    to enable, disable, or delete each custom poller.
  </p>
</div>

<jsp:include page="/includes/footer.jsp" flush="true"/>
