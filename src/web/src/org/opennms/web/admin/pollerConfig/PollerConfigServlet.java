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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.admin.pollerConfig;

import java.io.IOException;
import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.resource.*;
import org.opennms.netmgt.*;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.poller.*;
import org.opennms.netmgt.config.capsd.*;
import org.opennms.core.utils.BundleLists;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a node
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class PollerConfigServlet extends HttpServlet
{
	PollerConfiguration pollerConfig = null;
	CapsdConfiguration capsdConfig = null;
	protected String redirectSuccess;
	HashMap pollerServices = new HashMap();
	HashMap capsdProtocols = new HashMap();
	java.util.List capsdColl = new ArrayList();
	org.opennms.netmgt.config.poller.Package pkg = null;
	Collection pluginColl = null;
	Properties props = new Properties();
	PollerConfigFactory pollerFactory = null;
	CapsdConfigFactory capsdFactory = null;

	public void init() 
		throws ServletException
	{
		String homeDir = Vault.getHomeDir();
		ServletConfig config = this.getServletConfig(); 
		ServletContext context = config.getServletContext();
		Enumeration enum = context.getAttributeNames();
		try
		{
			props.load( new FileInputStream( ConfigFileConstants.getFile( ConfigFileConstants.POLLER_CONF_FILE_NAME )));
			String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
			PollerConfigFactory.init();
	                pollerFactory = PollerConfigFactory.getInstance();
                	pollerConfig = pollerFactory.getConfiguration();
 
			if(pollerConfig == null)
			{
				throw new ServletException("Poller Configuration file is empty");
			}
			CapsdConfigFactory.init();
	                capsdFactory = CapsdConfigFactory.getInstance();
                	capsdConfig = capsdFactory.getConfiguration();
 
			if(capsdConfig == null)
			{
				throw new ServletException("Poller Configuration file is empty");
			}
		}
		catch(Exception e)
		{
			throw new ServletException (e.getMessage());
		}
		initPollerServices();
		initCapsdProtocols();
		this.redirectSuccess = config.getInitParameter("redirect.success");
		if( this.redirectSuccess == null ) 
		{
            		throw new ServletException("Missing required init parameter: redirect.success");
        	}
	}

	public void reloadFiles() throws ServletException
	{
		String homeDir = Vault.getHomeDir();
                ServletConfig config = this.getServletConfig();
                ServletContext context = config.getServletContext();
                Enumeration enum = context.getAttributeNames();
                try
                {
                        props.load( new FileInputStream( ConfigFileConstants.getFile( ConfigFileConstants.POLLER_CONF_FILE_NAME )));
                        String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
                        PollerConfigFactory.init();
                        pollerFactory = PollerConfigFactory.getInstance();
                        pollerConfig = pollerFactory.getConfiguration();
 
                        if(pollerConfig == null)
                        {
                                throw new ServletException("Poller Configuration file is empty");
                        }
                        CapsdConfigFactory.init();
                        capsdFactory = CapsdConfigFactory.getInstance();
                        capsdConfig = capsdFactory.getConfiguration();
 
                        if(capsdConfig == null)
                        {
                                throw new ServletException("Poller Configuration file is empty");
                        }
                }
                catch(Exception e)
                {
                        throw new ServletException (e.getMessage());
                }
                initPollerServices();
                initCapsdProtocols();
                this.redirectSuccess = config.getInitParameter("redirect.success");
                if( this.redirectSuccess == null )
                {
                        throw new ServletException("Missing required init parameter: redirect.success");
                }
	}

	public void initCapsdProtocols()
	{
                pluginColl = capsdConfig.getProtocolPluginCollection();
                if(pluginColl != null)
                {
                        Iterator pluginiter = pluginColl.iterator();
                        while(pluginiter.hasNext())
                        {
                                ProtocolPlugin plugin = (ProtocolPlugin)pluginiter.next();
				capsdColl.add(plugin);
				capsdProtocols.put(plugin.getProtocol(), plugin);
			}
		}
	}

	public void initPollerServices() 
	{
                Collection packageColl = pollerConfig.getPackageCollection();
                if(packageColl != null)
                {
                        Iterator pkgiter = packageColl.iterator();
                        if(pkgiter.hasNext())
                        {
                                pkg = (org.opennms.netmgt.config.poller.Package)pkgiter.next();
                                Collection svcColl = pkg.getServiceCollection();
                                Iterator svcIter = svcColl.iterator();
                                Service svcProp = null;
                                while(svcIter.hasNext())
                                {
                                        svcProp = (Service)svcIter.next();
					pollerServices.put(svcProp.getName(), svcProp);
				}
			}
		}
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
		//ServletConfig config = this.getServletConfig(); 
		//ServletContext context = config.getServletContext();
		//String user_id = request.getRemoteUser();
		//Enumeration enum = context.getAttributeNames();
		reloadFiles();
	
		//String query = request.getQueryString();
                //System.out.println("query string = " + query);
		//if(query != null)
		{
			java.util.List checkedList = new ArrayList();
			java.util.List deleteList = new ArrayList();

			props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null );
			
                        String[] requestActivate = request.getParameterValues("activate");
                        String[] requestDelete   = request.getParameterValues("delete");
                        
                        if (requestActivate!=null)
                        {
                                for (int i = 0; i < requestActivate.length; i++)
                                {
                                        modifyPollerInfo("on", requestActivate[i]);
                                        checkedList.add(requestActivate[i]);
                                }
                        }
                        
                        if (requestDelete!=null)
                        {
                                for (int j = 0; j < requestDelete.length; j++)
                                {
                                        deleteList.add(requestDelete[j]);
                                }
                        }
                        /*
                        StringTokenizer strTok = new StringTokenizer(query, "&");
			while(strTok.hasMoreTokens())
			{
				String token = strTok.nextToken();
				if(token != null)
				{
					StringTokenizer keyTokens = new StringTokenizer(token, "=");
					String name = null;
					if(keyTokens.hasMoreTokens())
					{
						name =  (String)keyTokens.nextToken();
					}
					if(keyTokens.hasMoreTokens())
					{
						String checked =  (String)keyTokens.nextToken();
						if( name != null )
						{
							if(name.indexOf("delete") == -1)	// Not to be deleted
							{
								modifyPollerInfo(checked, name);
								checkedList.add(name);
							}
							else	// Deleted
							{
								String deleteService = name.substring(0, name.indexOf("delete"));
								System.out.println("deleting " + deleteService);
                                                                deleteList.add(deleteService);
							}
						}
					}
				}
			}
                        */
			adjustNonChecked(checkedList);
			deleteThese(deleteList);

			StringWriter stringWriter = new StringWriter();
			FileWriter poller_fileWriter = new FileWriter(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME));
			FileWriter capsd_fileWriter = new FileWriter(ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME));
			try{
				Marshaller.marshal( pollerConfig, poller_fileWriter );
				Marshaller.marshal( capsdConfig, capsd_fileWriter );
			}
			catch(MarshalException e)
			{
				e.printStackTrace();
				throw new ServletException(e.getMessage());
			}
			catch(ValidationException e)
			{
				e.printStackTrace();
				throw new ServletException(e.getMessage());
			}
		}
                
                String redirectPage = request.getParameter("redirect");
                if (redirectPage == null)
                {
                        redirectPage = this.redirectSuccess;
                }
		response.sendRedirect(redirectPage);
	}

	public void deleteCapsdInfo(String name)
	{
		if(capsdProtocols.get(name) != null)
		{
			ProtocolPlugin tmpproto = (ProtocolPlugin)capsdProtocols.get(name);
			capsdProtocols.remove(name);
			pluginColl = capsdProtocols.values();
			capsdColl.remove(tmpproto);
			capsdConfig.setProtocolPluginCollection(pluginColl);
		}
	}

        public void adjustNonChecked(java.util.List checkedList)
        {
		if(pkg != null)
		{
			Collection svcColl = pkg.getServiceCollection();
			Service svc  = null;
			if(svcColl != null)
			{
				Iterator svcIter = svcColl.iterator();
				while(svcIter.hasNext())
				{
					svc = (Service)svcIter.next();
					if(svc != null)
					{
						if(!checkedList.contains(svc.getName()))
						{
							if(svc.getStatus().equals("on"))
							{
								svc.setStatus("off");
							}
						}
					}
				}
			}
		}
        } 

	public void deleteThese(java.util.List deleteServices) throws IOException 
        {
		ListIterator lstIter = deleteServices.listIterator();
		while(lstIter.hasNext())
		{
			String svcname = (String)lstIter.next();

			if(pkg != null)
			{
				boolean flag = false;
				Collection svcColl = pkg.getServiceCollection();
				if(svcColl != null)
				{
					Iterator svcIter = svcColl.iterator();
					Service svc = null;
					while(svcIter.hasNext())
					{
						svc = (Service)svcIter.next();
						if(svc != null)
						{
							if(svc.getName().equals(svcname))
							{
								flag = true;
								break;
							}
						}
					}
					if(flag)
					{
						pkg.removeService(svc);
						System.out.println("Package removed " + svc.getName());
						removeMonitor(svc.getName());
						deleteCapsdInfo(svc.getName());
						props.remove("service."+svc.getName()+".protocol");
						props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null );
					}
				}
			}
		}
	}

	public void removeMonitor(String service)
	{
		// Add the new monitor with the protocol.
		Collection monitorColl = pollerConfig.getMonitorCollection();
		Monitor newMonitor = new Monitor();
		if(monitorColl != null)
		{
			Iterator monitoriter = monitorColl.iterator();
			while(monitoriter.hasNext())
			{
				Monitor mon = (Monitor)monitoriter.next();
				if(mon != null)
				{
					if(mon.getService().equals(service))
					{
						newMonitor.setService(service);
						newMonitor.setClassName(mon.getClassName());
						newMonitor.setParameterCollection(mon.getParameterCollection());
						break;
					}
				}
			}
			monitorColl.remove( newMonitor );
		}
        }

	public void modifyPollerInfo(String bPolled, String protocol)
	{
		if(pkg != null)
		{
			Collection svcColl = pkg.getServiceCollection();
			if(svcColl != null)
			{
				Iterator svcIter = svcColl.iterator();
				while(svcIter.hasNext())
				{
					Service svc = (Service)svcIter.next();
					if(svc != null)
					{
						if(svc.getName().equals(protocol))
						{
							svc.setStatus(bPolled);
							break;
						}
					}
				}
			}
		}
	}
}
