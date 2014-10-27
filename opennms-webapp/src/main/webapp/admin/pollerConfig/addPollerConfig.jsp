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

	String homeDir = Vault.getHomeDir();
        if( homeDir == null ) {
    throw new IllegalArgumentException( "Cannot take null parameters." );
        }
 
        props.load( new FileInputStream( ConfigFileConstants.getFile( ConfigFileConstants.POLLER_CONF_FILE_NAME )));
        protoMap = getQueries();
        String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
 
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
                                        if("true".equals(svcs.getUserDefined()))
                                        {
                                          scanableUserPlugin.put(svcs.getName(), svcs);
                                        }
                                        else
                                        {
                                          scanablePlugin.put(svcs.getName(), svcs);
                                        }

                                     		String status = svcs.getStatus();
                                     		if(status != null && "on".equals(status))
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
%><%!
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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Add New Custom Poller" />
  <jsp:param name="headTitle" value="Add New Custom Poller" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="Poller Config" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure Pollers" />
</jsp:include>

<script type="text/javascript" >

  function saveFile()
  {
      var port1 = document.poller.port1.value;

      if(port1 != '' && document.poller.name1.value == '')
      {
        alert('Name field cannot be empty');
        return false;
      }

      var newport = true;
      if(isNaN(port1))
      {
          colonIndex = port1.indexOf(':');
          if(colonIndex == -1)
          {
              alert("Please check the value of port again and retry later");
              return false;
          }

          while(colonIndex != -1)
          {
              var len = port1.length;
              var firsthalf = port1.substring(0, colonIndex);
              if(isNaN(firsthalf))
              {
                  newport = false;
                  alert("Please check the value of port again and retry later");
                  return false;
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
                      alert("Please check the value of port again and retry later");
                      return false;
                  }
              }
          }
      }

      if(newport)
      {
          document.poller.action="admin/pollerConfig/addPollerConfig";
          return true;
      }
  }

  function cancel()
  {
      document.poller.action="admin/pollerConfig/index.jsp";
      document.poller.method="get";
      document.poller.submit();
  }

</script>

<div id="contentleft">
  <h3>Add New Custom Poller</h3>

  <form method="post" name="poller" action="admin/pollerConfig/addPollerConfig" onsubmit="return saveFile();">
    <% int rowCounter = 0; %>
    <table class="standard">
      <tr>
        <td class="standardheader">Active</td>
        <td class="standardheader">Poller Name</td>
        <td class="standardheader">Protocol</td>
        <td class="standardheader">Port</td>
      </tr>

      <%-- XXX Not sure if this works.  Probably need to use classes. --%>
      <tr <% if(rowCounter % 2 == 0){ %>BGCOLOR="#cccccc"<% } %>>
	<td class="standard"><input type="checkbox" name="check1" /></td>
	<td class="standard"><input type="text" name="name1" /></td>
	<td class="standard">
	  <select name="protArray1" size="1">
	    <% for( int i=0; i < sortedProtocols.length; i++ ) { %>
	      <option><%= sortedProtocols[i] %></option>
	    <% } %>
	  </select>
	</td>
	<td class="standard"><input type="text" name="port1"/></td>
      </tr>
    </table>

    <br/>

    <input type="submit" value="Add"/>
    &nbsp;&nbsp;
    <input type="button" value="Cancel" onClick="cancel()" />
  </form>
</div>

<div id="contentright">
  <h3>Descriptions</h3>

  <p>
    The <b>Add New Custom Poller</b> page gives the administrator the
    ability to add a new poller that will poll services not covered by
    the default pollers.
  </p>

  <p>
    The <i><b>Active</b></i> column is the current status of the poller.
    If the active field is checked, the poller will be turned on and will
    scan the network during the next poller rescan.
  </p>

  <p>
    The <i><b>Poller Name</b></i> column is the name of the new service
    to be added.  Any name can be used; for instance, &quot;Company ABC
    Intranet HTTP&quot; or &quot;My Secret SSH Service&quot; are both
    valid.
  </p>

  <p>
    The <i><b>Protocol</b></i> column is the protocol used for polling
    the service. If you simply want to check to see if the target port
    is open, then choose &quot;TCP&quot; as the protocol.
  </p>

  <p>
    The <i><b>Port</b></i> column lists the ports at which the service
    will be polled. If there is more than one port where the service
    can be located, the values should be separated by colons (:).
  </p>

  <p>
    After you add the custom poller, click on the <b><i>Apply
    Changes</i></b> button on the <b>Configure Pollers</b> page to apply
    the settings.
  </p>
</div>

<jsp:include page="/includes/footer.jsp" flush="true"/>
