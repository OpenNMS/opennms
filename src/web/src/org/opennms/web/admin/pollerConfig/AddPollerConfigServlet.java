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
import org.opennms.web.Util;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a node
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AddPollerConfigServlet extends HttpServlet
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
	boolean errorflag = false;

	public void init() 
		throws ServletException
	{
		String homeDir = Vault.getHomeDir();
		ServletConfig config = this.getServletConfig(); 
		ServletContext context = config.getServletContext();
		Enumeration enum = context.getAttributeNames();
		try
		{
			props.load( new FileInputStream( ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME) ));
			String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
			PollerConfigFactory.init();
	                pollerFactory = PollerConfigFactory.getInstance();
                	pollerConfig = pollerFactory.getConfiguration();
 
			if(pollerConfig == null)
			{
				//response.sendRedirect( "error.jsp?error=2");
				errorflag = true;
				throw new ServletException("Poller Configuration file is empty");
			}
			CapsdConfigFactory.init();
	                capsdFactory = CapsdConfigFactory.getInstance();
                	capsdConfig = capsdFactory.getConfiguration();
 
			if(capsdConfig == null)
			{
				//response.sendRedirect( "error.jsp?error=3");
				errorflag = true;
				throw new ServletException("Capsd Configuration file is empty");
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
			props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)));
                        String[] protocols = BundleLists.parseBundleList( this.props.getProperty( "services" ));
                        PollerConfigFactory.init();
                        pollerFactory = PollerConfigFactory.getInstance();
                        pollerConfig = pollerFactory.getConfiguration();
 
                        if(pollerConfig == null)
                        {
				//response.sendRedirect( "error.jsp?error=2");
				errorflag = true;
				throw new ServletException("Poller Configuration file is empty");
                        }
                        CapsdConfigFactory.init();
                        capsdFactory = CapsdConfigFactory.getInstance();
                        capsdConfig = capsdFactory.getConfiguration();
 
                        if(capsdConfig == null)
                        {
				errorflag = true;
				//response.sendRedirect( "error.jsp?error=3");
                                throw new ServletException("Capsd Configuration file is empty");
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
		capsdProtocols = new HashMap();	
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
		pollerServices = new HashMap();
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
		ServletConfig config = this.getServletConfig(); 
		ServletContext context = config.getServletContext();
		String user_id = request.getRemoteUser();
		Enumeration enum = context.getAttributeNames();
	
		errorflag = false;
		reloadFiles();
		//String query = request.getQueryString();
		//if(query != null)
		{
			String check1 = request.getParameter("check1");
			String name1 = request.getParameter("name1");
			String protoArray1 = request.getParameter("protArray1");
			String port1 = request.getParameter("port1");

			java.util.List checkedList = new ArrayList();
			java.util.List deleteList = new ArrayList();
			if(name1 != null && !name1.equals(""))
			{
				addPollerInfo(check1, name1, port1, user_id, protoArray1, response, request);
				if(errorflag)
					return;
				checkedList.add(name1);
				addCapsdInfo(name1, port1, user_id, protoArray1, response, request);
				if(!errorflag)
				{
					props.setProperty("service."+name1+".protocol", protoArray1);
				}
				else
					return;
			}

			props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);
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

		if(!errorflag)
			response.sendRedirect(this.redirectSuccess);
	}

	public void addCapsdInfo(	String name, 
					String port, 
					String user, 
					String protocol, 
					HttpServletResponse response,
					HttpServletRequest request) throws ServletException, IOException
	{
		// Check to see if the name is duplicate of the already specified names first.
		Collection tmpCapsd  = capsdConfig.getProtocolPluginCollection();
		Iterator iter = tmpCapsd.iterator();
		Service pollersvc = null;
		while(iter.hasNext())
		{
			ProtocolPlugin svc = (ProtocolPlugin)iter.next();
			if(svc.getProtocol().equals(name))
			{
				// delete from the poller config.
				Collection tmpPollers = pkg.getServiceCollection();
				Iterator polleriter = tmpPollers.iterator();
				boolean removePoller = false;
				while(polleriter.hasNext())
				{
					pollersvc = (Service)polleriter.next();
					if(pollersvc.getName().equals(name))
					{
						removePoller = true;
						break;
					}
				} 
				if(removePoller)
				{
					Collection tmpPoller = pkg.getServiceCollection();
					if(tmpPoller.contains(pollersvc) && pollersvc.getName().equals(name))
					{
						errorflag = true;
						tmpPoller.remove(pollersvc);
						response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=1&name="+name );
						return;
					}
				}
				break;
				//throw new ServletException ("ProtocolPlugin name " + name + " is already defined.. Try assigning another unique name");
			}
		}
		ProtocolPlugin pluginAdd = new ProtocolPlugin();
		pluginAdd.setProtocol(name);
		String className = (String)props.get("service." + protocol + ".capsd-class");
		if(className != null){
			pluginAdd.setClassName(className);
			pluginAdd.setScan("on");
			pluginAdd.setUserDefined("true");
			org.opennms.netmgt.config.capsd.Property newprop = new org.opennms.netmgt.config.capsd.Property();
			String banner = "*";
			if(props.get("banner") != null)
				banner = (String) props.get("banner");
			newprop.setValue(banner);
			newprop.setKey("banner");
			pluginAdd.addProperty(newprop);

			newprop =new org.opennms.netmgt.config.capsd.Property(); 
			if(port != null && !port.equals(""))
			{
				newprop.setValue(port);
				if(port.indexOf(":") == -1)
					newprop.setKey("port");
				else
					newprop.setKey("ports");
				pluginAdd.addProperty(newprop);
			}
			else
			{	
				if(props.get("service."+protocol+".port") == null || ((String)props.get("service."+protocol+".port")).equals(""))
				{
					errorflag = true;
					response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=0&name="+"service." + protocol + ".port " );
					pluginAdd = null;
					return;
				}
				else
				{
					port = (String)props.get("service."+protocol+".port");
					newprop.setValue(port);
					if(port.indexOf(":") == -1)
						newprop.setKey("port");
					else
						newprop.setKey("ports");
					pluginAdd.addProperty(newprop);
				}
			}
			newprop =new org.opennms.netmgt.config.capsd.Property(); 
			String timeout = "3000";
			if(props.get("timeout") != null)
				timeout = (String)props.get("timeout");
			newprop.setValue(timeout);
			newprop.setKey("timeout");
			if(pluginAdd != null)
			pluginAdd.addProperty(newprop);
		
			newprop =new org.opennms.netmgt.config.capsd.Property(); 
			String retry = "3";
			if(props.get("retry") != null)
				retry = (String)props.get("retry");
			newprop.setValue(retry);
			newprop.setKey("retry");
			if(pluginAdd != null)
			{
			pluginAdd.addProperty(newprop);
			capsdProtocols.put(name, pluginAdd);
			pluginColl = capsdProtocols.values();
			capsdColl.add(pluginAdd);
			capsdConfig.addProtocolPlugin(pluginAdd);
		}
		}
		else
		{
			errorflag = true;
			response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=0&name="+"service." + protocol + ".capsd-class " );
			return;
		}
	}

	public void addPollerInfo(	String bPolled, 
					String name, 
					String port, 
					String user, 
					String protocol, 
					HttpServletResponse response,
					HttpServletRequest request) throws ServletException, IOException
	{
		// Check to see if the name is duplicate of the already specified names first.
		Collection tmpPollers = pkg.getServiceCollection();
		Iterator iter = tmpPollers.iterator();
		while(iter.hasNext())
		{
			Service svc = (Service)iter.next();
			if(svc.getName().equals(name))
			{
				errorflag = true;
				response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=1&name="+name );			
				return;
				//throw new ServletException ("Service name " + name + " is already defined.. Try assigning another unique name");
			}
		}

		if(pkg != null)
		{
			Service newService = new Service();
			newService.setName(name);
			if(bPolled != null)
				newService.setStatus(bPolled);
			else
				newService.setStatus("off");
			newService.setName(name);
			newService.setUserDefined("true");

			Collection monitorColl = pollerConfig.getMonitorCollection();
			Monitor newMonitor = new Monitor();
			String monitor = (String)props.get("service."+protocol+".monitor");
			if(monitor != null)
			{
				newMonitor.setService(name);
				newMonitor.setClassName(monitor);          
			}
			else
			{
				errorflag = true;
	                        response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=0&name="+"service." + protocol + ".monitor " );
        	                return; 
			}

			if(props.get("interval") != null)
				newService.setInterval((new Long((String)props.get("interval"))).longValue());
			else
				newService.setInterval(300000);
		
			org.opennms.netmgt.config.poller.Parameter newprop = new org.opennms.netmgt.config.poller.Parameter();
			String timeout = "3000";
			if(props.get("timeout") != null)
			{
				timeout = (String)props.get("timeout");
			}
			newprop.setValue(timeout);
			newprop.setKey("timeout");
			newService.addParameter(newprop);

			newprop = new org.opennms.netmgt.config.poller.Parameter();
			String banner = "*";
			if(props.get("banner") != null)
				banner = (String)props.get("banner");
			newprop.setValue(banner);
			newprop.setKey("banner");
			newService.addParameter(newprop);

			newprop = new org.opennms.netmgt.config.poller.Parameter();
			String retry = "3";
			if(props.get("retry") != null)
				retry = (String)props.get("retry");
			newprop.setValue(retry);
			newprop.setKey("retry");
			newService.addParameter(newprop);

			newprop = new org.opennms.netmgt.config.poller.Parameter();
			if(port == null || port.equals("")){
				if(props.get("service."+protocol+".port") == null || ((String)props.get("service."+protocol+".port")).equals("") )
				{
				errorflag = true;
					newMonitor = null;
					newService = null;
					response.sendRedirect(Util.calculateUrlBase(request)+ "/admin/error.jsp?error=0&name="+"service." + protocol + ".port" );
					return;
				}
				else
					port = (String)props.get("service."+protocol+".port");
			
			}

			newprop.setValue(port);
			if(port.indexOf(":") != -1)
				newprop.setKey("ports");
			else
				newprop.setKey("port");
			if(newMonitor != null && newService != null)
			{
				if(monitorColl == null)
					pollerConfig.addMonitor(0, newMonitor);
				else
				{
					pollerConfig.addMonitor(newMonitor);
				}
			newService.addParameter(newprop);
			pkg.addService(newService);
			}
		}
	}
}
