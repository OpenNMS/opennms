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
