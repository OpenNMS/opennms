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
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.vmmgr;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;

/**
 * <p>The Manager is reponsible for launching/starting all services in the VM that
 * it is started for. The Manager operates in two modes, normal and server</p>
 *
 * <p>normal mode: In the normal mode, the Manager starts all services configured
 * for its VM in the service-configuration.xml and starts listening for control events
 * on the 'control-broadcast' JMS topic for stop control messages for itself</p>
 *
 * <p>server mode: In the server mode, the Manager starts up and listens on the 
 * 'control-broadcast' JMS topic for 'start' control messages for services in its
 * VM and a stop control messge for itself. When a start for a service is received,
 * it launches only that service and sends a successful 'running' or an 'error'
 * response to the Controller</p>
 *
 * <p><strong>Note:</strong>The Manager is NOT intelligent - if it receives a stop
 * control event, it will exit - does not check to see if the services its started
 * are all stopped<p>
 *
 * @author 	<A HREF="mailto:weave@oculan.com">Brian Weaver</A>
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public class Manager
	implements ManagerMBean
{
	/** 
	 * The log4j category used to log debug messsages
	 * and statements.
	 */
	private static final String LOG4J_CATEGORY	= "OpenNMS.Manager";

	public static Attribute getAttribute(org.opennms.netmgt.config.service.Attribute attrib)
		throws Exception
	{
		Class attribClass = Class.forName(attrib.getValue().getType());
		Constructor construct = attribClass.getConstructor(new Class[] { String.class });

		Object value = construct.newInstance(new Object[] { attrib.getValue().getContent() });

		return new Attribute(attrib.getName(), value);
	}

	public static Object getArgument(Argument arg)
		throws Exception
	{
		Class argClass = Class.forName(arg.getType());
		Constructor construct = argClass.getConstructor(new Class[] { String.class });

		return construct.newInstance(new Object[] { arg.getContent() });
	}

	public static void start(MBeanServer server)
	{
		ServiceConfigFactory sfact = null;
		try
		{
			ServiceConfigFactory.init();
			sfact = ServiceConfigFactory.getInstance();
		}
		catch(Exception e)
		{
			throw new java.lang.reflect.UndeclaredThrowableException(e);
		}

		// allocate some storage locations
		//
		Service[] services = sfact.getServices();
		ObjectInstance[] mbeans = new ObjectInstance[services.length];
		BitSet badSvcs = new BitSet(services.length);

		Category log = ThreadCategory.getInstance(Manager.class);
		boolean isTracing = log.isDebugEnabled();

		// preload the classes and register a new instance
		// with the MBeanServer
		//
		for(int i = 0; i < services.length; i++)
		{
			try
			{
				// preload the class
				//
				if(isTracing)
					log.debug("loading class " + services[i].getClassName());

				Class cinst = Class.forName(services[i].getClassName());

				// get a new instance of the
				// class
				//
				if(isTracing)
					log.debug("create new instance of " + services[i].getClassName());

				Object bean = cinst.newInstance();

				// register the mbean
				//
				if(isTracing)
					log.debug("registering mbean instance " + services[i].getName());

				ObjectName name = new ObjectName(services[i].getName());
				mbeans[i] = server.registerMBean(bean, name);

				org.opennms.netmgt.config.service.Attribute[] attribs = services[i].getAttribute();
				if(attribs != null)
				{
					for(int j = 0; j < attribs.length; j++)
					{
						if(isTracing)
							log.debug("setting attribute " + attribs[j].getName());

						server.setAttribute(name, getAttribute(attribs[j]));
					}
				}
			}
			catch(Throwable t)
			{
				log.error("An error occured loading the mbean " + services[i].getName() + " of type " + 
					  services[i].getClassName() + " it will be skipped", t);
				badSvcs.set(i);
			}
		}

		// now that everything is initialized, invoke the 
		// methods
		//
		int pass = 0; 
		int end = 0;
		while(pass <= end)
		{
			if(isTracing)
				log.debug("starting pass " + pass);
			for(int i = 0; i < services.length && !badSvcs.get(i); i++)
			{
				Invoke[] todo = services[i].getInvoke();
				for(int j = 0; todo != null && j < todo.length; j++)
				{
					if(todo[j].getPass() == pass && (todo[j].getAt() == null || todo[j].getAt().equals("start")))
					{
						// invoke!
						//
						try
						{
							// get the arguments
							//
							Argument[] args = todo[j].getArgument();
							Object[] parms = new Object[0];
							String[] sig = new String[0];
							if(args != null && args.length > 0)
							{
								parms = new Object[args.length];
								sig = new String[args.length];
								for(int k = 0; k < parms.length; k++)
								{
									parms[k] = getArgument(args[k]);
									sig[k] = parms[k].getClass().getName();
								}
							}

							if(isTracing)
								log.debug("Invoking " + todo[j].getMethod() + " on object " + mbeans[i].getObjectName());

							server.invoke(mbeans[i].getObjectName(),
								      todo[j].getMethod(),
								      parms,
								      sig);
						}
						catch(Throwable t)
						{
							log.error("An error occured invoking operation "
								  + todo[j].getMethod()
								  + " on MBean "
								  + mbeans[i].getObjectName(), t);
						}
					} // end if this pass

					end = (end <= todo[j].getPass() ? todo[j].getPass() : end);

				} // end invoke loop

			} // end services loop

			++pass;

		} // end passes
		if(isTracing)
			log.debug("Startup complete");
	}

	public void stop()
	{
		List servers = MBeanServerFactory.findMBeanServer(null);
		Iterator i = servers.iterator();
		while(i.hasNext())
			stop((MBeanServer)i.next());
	}

	public static void stop(MBeanServer server)
	{
		ServiceConfigFactory sfact = null;
		try
		{
			ServiceConfigFactory.init();
			sfact = ServiceConfigFactory.getInstance();
		}
		catch(Exception e)
		{
			throw new java.lang.reflect.UndeclaredThrowableException(e);
		}

		// allocate some storage locations
		//
		Service[] services = sfact.getServices();
		ObjectInstance[] mbeans = new ObjectInstance[services.length];
		BitSet badSvcs = new BitSet(services.length);

		Category log = ThreadCategory.getInstance(Manager.class);
		boolean isTracing = log.isDebugEnabled();

		// preload the classes and register a new instance
		// with the MBeanServer
		//
		for(int i = 0; i < services.length; i++)
		{
			try
			{
				// find the mbean
				//
				if(isTracing)
					log.debug("finding mbean instance " + services[i].getName());

				ObjectName name = new ObjectName(services[i].getName());
				mbeans[i] = server.getObjectInstance(name);
			}
			catch(Throwable t)
			{
				log.error("An error occured loading the mbean " + services[i].getName() + " of type " + 
					  services[i].getClassName() + " it will be skipped", t);
				badSvcs.set(i);
			}
		}

		// now that everything is initialized, invoke the 
		// methods that match start
		//
		int pass = 0; 
		int end = 0;
		while(pass <= end)
		{
			if(isTracing)
				log.debug("starting pass " + pass);
			for(int i = services.length-1; i >= 0 && !badSvcs.get(i); i--)
			{
				Invoke[] todo = services[i].getInvoke();
				for(int j = 0; todo != null && j < todo.length; j++)
				{
					if(todo[j].getPass() == pass && (todo[j].getAt() != null && todo[j].getAt().equals("stop")))
					{
						// invoke!
						//
						try
						{
							// get the arguments
							//
							Argument[] args = todo[j].getArgument();
							Object[] parms = new Object[0];
							String[] sig = new String[0];
							if(args != null && args.length > 0)
							{
								parms = new Object[args.length];
								sig = new String[args.length];
								for(int k = 0; k < parms.length; k++)
								{
									parms[k] = getArgument(args[k]);
									sig[k] = parms[k].getClass().getName();
								}
							}

							if(isTracing)
								log.debug("Invoking " + todo[j].getMethod() + " on object " + mbeans[i].getObjectName());

							server.invoke(mbeans[i].getObjectName(),
								      todo[j].getMethod(),
								      parms,
								      sig);
						}
						catch(Throwable t)
						{
							log.error("An error occured invoking operation "
								  + todo[j].getMethod()
								  + " on MBean "
								  + mbeans[i].getObjectName(), t);
						}
					} // end if this pass

					end = (end <= todo[j].getPass() ? todo[j].getPass() : end);

				} // end invoke loop

			} // end services loop

			++pass;

		} // end passes
		if(isTracing)
			log.debug("Shutdown complete");
	}

	public List status()
	{
		List servers = MBeanServerFactory.findMBeanServer(null);
		List result = new ArrayList();
		Iterator i = servers.iterator();
		while(i.hasNext())
			result.addAll(status((MBeanServer)i.next()));
		return result;
	}

	public static List status(MBeanServer server)
	{
		ServiceConfigFactory sfact = null;
		try
		{
			ServiceConfigFactory.init();
			sfact = ServiceConfigFactory.getInstance();
		}
		catch(Exception e)
		{
			throw new java.lang.reflect.UndeclaredThrowableException(e);
		}

		// allocate some storage locations
		//
		Service[] services = sfact.getServices();
		ObjectInstance[] mbeans = new ObjectInstance[services.length];
		BitSet badSvcs = new BitSet(services.length);

		Category log = ThreadCategory.getInstance(Manager.class);
		boolean isTracing = log.isDebugEnabled();

		// preload the classes and register a new instance
		// with the MBeanServer
		//
		for(int i = 0; i < services.length; i++)
		{
			try
			{
				// find the mbean
				//
				if(isTracing)
					log.debug("finding mbean instance " + services[i].getName());

				ObjectName name = new ObjectName(services[i].getName());
				mbeans[i] = server.getObjectInstance(name);
			}
			catch(Throwable t)
			{
				log.error("An error occured loading the mbean " + services[i].getName() + " of type " + 
					  services[i].getClassName() + " it will be skipped", t);
				badSvcs.set(i);
			}
		}

		List statusInfo = new ArrayList(15);
		// now that everything is initialized, invoke the 
		// methods that match start
		//
		int pass = 0; 
		int end = 0;
		while(pass <= end)
		{
			if(isTracing)
				log.debug("starting pass " + pass);
			for(int i = 0; i < services.length && !badSvcs.get(i); i++)
			{
				Invoke[] todo = services[i].getInvoke();
				for(int j = 0; todo != null && j < todo.length; j++)
				{
					if(todo[j].getPass() == pass && (todo[j].getAt() != null && todo[j].getAt().equals("status")))
					{
						// invoke!
						//
						try
						{
							// get the arguments
							//
							Argument[] args = todo[j].getArgument();
							Object[] parms = new Object[0];
							String[] sig = new String[0];
							if(args != null && args.length > 0)
							{
								parms = new Object[args.length];
								sig = new String[args.length];
								for(int k = 0; k < parms.length; k++)
								{
									parms[k] = getArgument(args[k]);
									sig[k] = parms[k].getClass().getName();
								}
							}

							if(isTracing)
								log.debug("Invoking " + todo[j].getMethod() + " on object " + mbeans[i].getObjectName());

							Object result = server.invoke(mbeans[i].getObjectName(),
								      		      todo[j].getMethod(),
										      parms,
										      sig);
							statusInfo.add("Status: " + mbeans[i].getObjectName() + " = " + result);
						}
						catch(Throwable t)
						{
							log.error("An error occured invoking operation "
								  + todo[j].getMethod()
								  + " on MBean "
								  + mbeans[i].getObjectName(), t);
							statusInfo.add("Status: " + mbeans[i].getObjectName() + " = STATUS_CHECK_ERROR");
						}
					} // end if this pass

					end = (end <= todo[j].getPass() ? todo[j].getPass() : end);

				} // end invoke loop

			} // end services loop

			++pass;

		} // end passes
		if(isTracing)
			log.debug("status check complete");

		return statusInfo;
	}

	public void doSystemExit()
	{
		System.exit(1);
	}

	public static void main(String[] args)
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance(Manager.class);

		// set up the JMX logging
		//
		mx4j.log.Log.redirectTo(new mx4j.log.Log4JLogger());

		if(args.length == 0 || "start".equals(args[0]))
		{
			MBeanServer server = MBeanServerFactory.createMBeanServer("OpenNMS");
			start(server);
		}
		else if(args.length != 0 && "stop".equals(args[0]))
		{
			try
			{
				URL invoke = new URL("http://127.0.0.1:8181/invoke?objectname=OpenNMS%3AName=FastExit&operation=stop");
				InputStream in = invoke.openStream();
				int ch;
				while((ch = in.read()) != -1)
					System.out.write((char)ch);
				in.close();
				System.out.println("");
				System.out.flush();
			}
			catch(Throwable t)
			{
				log.error("error invoking stop command", t);
			}
		}
		else if(args.length != 0 && "status".equals(args[0]))
		{
			try
			{
				URL invoke = new URL("http://127.0.0.1:8181/invoke?objectname=OpenNMS%3AName=FastExit&operation=status");
				InputStream in = invoke.openStream();
				int ch;
				while((ch = in.read()) != -1)
					System.out.write((char)ch);
				in.close();
				System.out.println("");
				System.out.flush();
			}
			catch(Throwable t)
			{
				log.error("error invoking status command", t);
			}
		}
	}
}

