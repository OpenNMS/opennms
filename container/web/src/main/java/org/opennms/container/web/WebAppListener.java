/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.karaf.main.Main;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationHook;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class WebAppListener implements ServletContextListener, RegistrationHook, ServiceListener {
	
	private static final String ONMS_SOURCE = "onms";
	private static final String OSGI_SOURCE = "osgi";
	private static final String REGISTRATION_EXPORT = "registration.export";
	private static final String REGISTRATION_SOURCE = "registration.source";
	private Main main;
	private BundleContext m_framework;
	private ServiceRegistry m_registry = DefaultServiceRegistry.INSTANCE;
	private Map<Registration, ServiceRegistration> m_onmsRegistration2osgiRegistrationMap = new HashMap<Registration, ServiceRegistration>();
	private Map<ServiceReference, Registration> m_osgiReference2onmsRegistrationMap = new HashMap<ServiceReference, Registration>();
	private ServletContext m_servletContext;

	public void contextInitialized(ServletContextEvent sce) {

		try {
			m_servletContext = sce.getServletContext();
			
			System.err.println("contextInitialized");
			String root = new File(m_servletContext.getRealPath("/") + "/WEB-INF/karaf").getAbsolutePath();
			System.err.println("Root: " + root);
			System.setProperty("karaf.home", root);
            System.setProperty("karaf.base", root);
            System.setProperty("karaf.data", root + "/data");
            System.setProperty("karaf.history", root + "/data/history.txt");
            System.setProperty("karaf.instances", root + "/instances");
			System.setProperty("karaf.startLocalConsole", "false");
			System.setProperty("karaf.startRemoteShell", "true");
            System.setProperty("karaf.lock", "false");
            System.setProperty("karaf.framework.factory", OnmsFelixFrameworkFactory.class.getName());
			main = new Main(new String[0]);
            main.launch();
            
            // get bundle context for registering service
            m_framework = main.getFramework().getBundleContext();
            
            // add bundle context to servlet context for Proxy Servlet
            m_servletContext.setAttribute(BundleContext.class.getName(), m_framework);

            
            // register for ONMS registrations to forward registrations to OSGi service registry
            m_registry.addRegistrationHook(this, true);
            

            // register service listener for export osgi services to forward to ONMS registry
            String exportFilter = "("+REGISTRATION_EXPORT+"=*)";
			m_framework.addServiceListener(this, exportFilter);

			// forward any existing exported OSGi services with ONMS service registry
            ServiceReference[] osgiServices = m_framework.getAllServiceReferences(null, exportFilter);

            if (osgiServices != null) {
	            for(ServiceReference reference : osgiServices) {
	            	registerWithOnmsRegistry(reference);
	            }
            }

		} catch (final Exception e) {
			main = null;
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		try {
			
			// TODO unregister services form both registries with the osgi container stops
			
			System.err.println("contextDestroyed");
			if (main != null) {
                main.destroy();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registrationAdded(Registration onmsRegistration) {
		
		Map<String, String> onmsProperties = onmsRegistration.getProperties();
		if (OSGI_SOURCE.equals(onmsProperties.get(REGISTRATION_SOURCE))) return;
		
		
		Class<?>[] providerInterfaces = onmsRegistration.getProvidedInterfaces();
		String[] serviceClasses = new String[providerInterfaces.length];
		

		for(int i = 0; i < providerInterfaces.length; i++) {
			serviceClasses[i] = providerInterfaces[i].getName();
		}
		
		Properties props = new Properties();
		for(Entry<String, String> entry : onmsProperties.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		props.put(REGISTRATION_SOURCE, ONMS_SOURCE);
		
		ServiceRegistration osgiRegistration = m_framework.registerService(serviceClasses, onmsRegistration.getProvider(), props);
		m_onmsRegistration2osgiRegistrationMap.put(onmsRegistration, osgiRegistration);
	}

	@Override
	public void registrationRemoved(Registration onmsRegistration) {
		ServiceRegistration osgiRegistration = m_onmsRegistration2osgiRegistrationMap.remove(onmsRegistration);
		if (osgiRegistration == null) return;
		osgiRegistration.unregister();		
	}

	@Override
	public void serviceChanged(ServiceEvent serviceEvent) {
		switch(serviceEvent.getType()) {
		case ServiceEvent.REGISTERED:
			registerWithOnmsRegistry(serviceEvent.getServiceReference());
			break;
		case ServiceEvent.MODIFIED:
			registerWithOnmsRegistry(serviceEvent.getServiceReference());
			break;
		case ServiceEvent.MODIFIED_ENDMATCH:
			unregisterWithOnmsRegistry(serviceEvent.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			unregisterWithOnmsRegistry(serviceEvent.getServiceReference());
			break;
		}
	}
	
	private void registerWithOnmsRegistry(ServiceReference reference) {
				
		// skip this service if this should not be exported
		if (!isOnmsExported(reference)) return;
		
		// skip this service if its came from the opennms registry originally
		if (isOnmsSource(reference)) return;
		
		// if this service is already registered then skip it
		if (m_osgiReference2onmsRegistrationMap.containsKey(reference)) return;
		
		
		String[] classNames = (String[]) reference.getProperty(Constants.OBJECTCLASS);
		
		try {
			Class<?>[] providerInterfaces = findClasses(classNames);
			
			Object provider = m_framework.getService(reference);
			
			Map<String, String> properties = new LinkedHashMap<String, String>();
			
			for(String key : reference.getPropertyKeys()) {
				Object val = reference.getProperty(key);
				StringBuilder buf = new StringBuilder();
				if (val instanceof Object[]) {
					Object[] a = (Object[])val;
					for(int i = 0; i < a.length; i++) {
						if (i != 0) buf.append(',');
						buf.append(a[i]);
					}
				} else {
					buf.append(val);
				}
				properties.put(key, buf.toString());
			}
			
			properties.put(REGISTRATION_SOURCE, OSGI_SOURCE);
			
			
			Registration onmsRegistration = m_registry.register(provider, properties, providerInterfaces);
			
			
			m_osgiReference2onmsRegistrationMap.put(reference, onmsRegistration);
			
		
		} catch (ClassNotFoundException e) {
			m_servletContext.log("Unable to find class used by exported OSGi service", e);
		}
	}
	
	private boolean isOnmsExported(ServiceReference reference) {
		return Arrays.asList(reference.getPropertyKeys()).contains(REGISTRATION_EXPORT);
	}

	private boolean isOnmsSource(ServiceReference reference) {
		return ONMS_SOURCE.equals(reference.getProperty(REGISTRATION_SOURCE));
	}

	private Class<?>[] findClasses(String[] classNames) throws ClassNotFoundException {
		Class<?>[] providerInterfaces = new Class<?>[classNames.length];
		

		for(int i = 0; i < classNames.length; i++) {
			providerInterfaces[i] = Class.forName(classNames[i]);
		}
		
		return providerInterfaces;

	}

	private void unregisterWithOnmsRegistry(ServiceReference reference) {
		
		Registration onmsRegistration = m_osgiReference2onmsRegistrationMap.remove(reference);
		
		if (onmsRegistration == null) return;
		
		onmsRegistration.unregister();
		
	}



}
