/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.openoss.test.server;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class SoapRiManager {

	private ConfigurableApplicationContext context = null;

	public static final String RI_APPLICTION_CONTEXT_FILE="soapImplAppContext.xml";

	private String riAppContext=RI_APPLICTION_CONTEXT_FILE;

	public String getRiAppContext() {
		return riAppContext;
	}

	public void setRiAppContext(String riAppContext) {
		this.riAppContext = riAppContext;
	}

	public void setContext(ConfigurableApplicationContext context) {
		this.context = context;
	}



	/**
	 * starts the reference implementation in openejb
	 */
	public void start() {
		try {
			this.getContext();
			System.out.println("SoapRiManager start(): Started: waiting for connections ");
		} catch (Exception e) {
			System.out.println("SoapRiManager start(): Exception thrown " + e);
		}
	}

	/**
	 * stops the reference implementation in openejb
	 */
	public void stop() {
		try {
			this.getContext().close();
			System.out.println("SoapRiManager stop(): RI closed ");
		} catch (Exception e) {
			System.out.println("SoapRiManager stop(): Exception thrown " + e);
		}
	}

	public synchronized ConfigurableApplicationContext getContext() {
		if (context == null) {
			try {
				System.out.println("SoapRiManager getContext(): loading application context from '"+getRiAppContext()+"'");
				GenericApplicationContext ctx = new GenericApplicationContext();
				XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
				// TODO - need to check that file actually exists

				xmlReader.loadBeanDefinitions(new ClassPathResource(riAppContext));
				ctx.refresh();
				context = ctx;
				System.out.println("SoapRiManager getContext(): CONTEXT LOADED");

			} catch (Exception ex) {
				System.out.println("SoapRiManager getContext():problem looking up the Spring ApplicationContext: "+ ex);
				context = null;
			}
		}
		return context;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SoapRiManager m = new SoapRiManager();

		System.out.println("SoapRiManager main(): starting RI");
		try {
			m.start();

			System.out.println("SoapRiManager main(): RI waiting for connections ");
			for (int i = 0; i <10; i++) { // waits
				// System.out.print(i);
				Thread.sleep(3000);
			}
			m.getContext().close();
		} catch (Exception e) {
			System.out.println("SoapRiManager main(): Exception thrown " + e);
		}
		System.out.println("SoapRiManager main(): STOPPING RI");
	}





}
