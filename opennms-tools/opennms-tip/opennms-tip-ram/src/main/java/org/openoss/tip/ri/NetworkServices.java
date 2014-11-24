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

package org.openoss.tip.ri;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.client.LocalInitialContext;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * See http://www.mail-archive.com/dev@openejb.apache.org/msg03800.html
 * Used to startup network services on openejb instance
 *
 */
public class NetworkServices {

    /**
     *  Apache Commons Log to use for logging messages
     *  Defaults to a basic value but can be set explicitly at class creation
     */
     protected Log log;

	 /**
	  *  setter for log
	  *  @param Log to use for log messages from this class
	  */
    @javax.annotation.Resource(name="tipInterfaceLog")
	 public void setLog(Log log){
 		if(log==null) throw new java.lang.IllegalArgumentException("ERROR: "+this.getClass().getSimpleName()+" method: setLog(Log log): Parameter log must not be null");
		this.log=log;
		if(log.isDebugEnabled()) log.debug("DEBUG: "+this.getClass().getSimpleName()+": Log set successfully for this class");
	 }

	 /**
	  *  getter for log
	  *  @param Log to use for log messages from this class
	  */
	 public Log getLog(){
	      if(log==null) { // returns a value for the logger if not set
	           log=LogFactory.getLog(this.getClass());
	      }
	      return log;
	 }

	private ServiceManager manager;

	@PostConstruct
	public void startNetworkServices() throws Exception {

		if (ServiceManager.get() != null) {
			return;
		}

		getLog().info("Starting network services");

		manager = ServiceManager.getManager();

		manager.init();
		manager.start(false);
	}

	@PreDestroy
	public void stopNetworkServices() throws Exception {

		getLog().info("Stopping network services");

		manager.stop();
		Thread.sleep(3000);
	}
}
