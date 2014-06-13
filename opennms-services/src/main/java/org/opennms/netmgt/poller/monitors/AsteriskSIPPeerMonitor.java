/*******************************************************************************
* This file is part of OpenNMS(R).
*
* Copyright (C) 2006-2012 The OpenNMS Group, Inc.
* OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
*
* OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
*
* OpenNMS(R) is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published
* by the Free Software Foundation, either version 3 of the License,
* or (at your option) any later version.
*
* OpenNMS(R) is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with OpenNMS(R).  If not, see:
*      http://www.gnu.org/licenses/
*
* For more information contact:
*     OpenNMS(R) Licensing <license@opennms.org>
*     http://www.opennms.org/
*     http://www.opennms.com/
*******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.apache.log4j.Level;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.SipShowPeerAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.config.AmiPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.config.ami.AmiAgentConfig;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of Asterisk SIP Peers by executing a "sip show peers" over AMI. 
 * It gets the AMI parameters from the AMI configuration and needs the parameter 
 * sip-peer to be set in the poller configuration.
 * </P>
 *
 * @author <A HREF="mailto:michael.batz@nethinks.com">Michael Batz</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AsteriskSIPPeerMonitor extends AbstractServiceMonitor {

	/**
     	* Default retries.
     	*/
    	private static final int DEFAULT_RETRY = 0;

	/**
     	* Default timeout. Specifies how long (in milliseconds) to block waiting for data from the
     	* monitored interface.
     	*/
	private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on read()

	/**
     	* Default sip peer. Specifies the sip peer to get information from the Asterisk server.
     	*/
	private static final String DEFAULT_SIPPEER = ""; 

	/**
	* {@inheritDoc}
     	*
     	* <P>
     	* Initialize the service monitor.
     	* </P>
     	* @exception RuntimeException
     	*                Thrown if an unrecoverable error occurs that prevents the
     	*                plug-in from functioning.
     	*/
    	public void initialize(Map<String, Object> parameters) 
	{
		try
		{
			AmiPeerFactory.init();
		}
		catch(Exception e)
		{
			log().fatal("Initalize: Failed to load AMI configuration", e);
			throw new UndeclaredThrowableException(e);
		}
		return;
	}

	/**
        * {@inheritDoc}
        *
        * <P>
        * Run the service monitor and return the poll status
        * </P>
        */
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters)
	{
		//check, if interface type is supported
		final NetworkInterface<InetAddress> iface = svc.getNetInterface();
        	if (iface.getType() != NetworkInterface.TYPE_INET) 
		{
            		throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");
        	}

		//read configuration parameters
		String sipPeer = ParameterMap.getKeyedString(parameters, "sip-peer", DEFAULT_SIPPEER);
		if(sipPeer.equals(DEFAULT_SIPPEER))
		{
			log().fatal("AsteriskMonitor: No sip-peer parameter in poller configuration");
			throw new RuntimeException("AsteriskMonitor: required parameter 'sip-peer' is not present in supplied properties.");

		}
		TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
		AmiPeerFactory amiPeerFactory = AmiPeerFactory.getInstance();
		AmiAgentConfig amiConfig = amiPeerFactory.getAgentConfig(svc.getAddress());

		//setting up AMI connection	
		log().debug(svc.getSvcName() + ": Creating new AMI-Connection: " + svc.getIpAddr() + ":" + amiConfig.getPort() + ", " + amiConfig.getUsername() + "/" + amiConfig.getPassword());
		ManagerConnectionFactory factory = new ManagerConnectionFactory(svc.getIpAddr(), amiConfig.getPort(), amiConfig.getUsername(), amiConfig.getPassword());
		ManagerConnection managerConnection;
		if(amiConfig.getUseTls())
		{
                	managerConnection = factory.createSecureManagerConnection();
		}
		else
		{
                	managerConnection = factory.createManagerConnection();
		}
		managerConnection.setSocketTimeout(new Long(timeoutTracker.getTimeoutInMillis()).intValue());

		//start with polling
		while(timeoutTracker.shouldRetry())
		{
			timeoutTracker.nextAttempt();
			log().debug(svc.getSvcName() + ": Attempt " + timeoutTracker.getAttempt());
			try
			{
				log().debug(svc.getSvcName() + ": AMI login");
	                	managerConnection.login();

				log().debug(svc.getSvcName() + ": AMI sendAction SipShowPeer");
                		ManagerResponse response = managerConnection.sendAction(new SipShowPeerAction(sipPeer));
				if(response.getAttribute("Status") == null)
				{
					log().debug(svc.getSvcName() + ": service status down");
					return PollStatus.decode("Down", "State of SIP Peer is unknown, because it was not found on the Asterisk server");

				}
				log().debug(svc.getSvcName() + ": Response: " + response.getAttribute("Status"));

				log().debug(svc.getSvcName() + ": AMI logoff");
	               		managerConnection.logoff();

                		if (response.getAttribute("Status").startsWith("OK"))
                		{
					log().debug(svc.getSvcName() + ": service status up");
					return PollStatus.decode("Up", "OK");
	                	}
        	        	else
                		{
					log().debug(svc.getSvcName() + ": service status down");
					return PollStatus.decode("Down", "State of SIP Peer is " + response.getAttribute("Status") + " and not OK");
	                	}
			}
			catch(AuthenticationFailedException e)
			{
				log().error(svc.getSvcName() + ": AMI AuthenticationError: " + e.toString());
				return PollStatus.decode("Down", "Could not get the state of SIP Peer: AMI AuthenticationError");
			}
			catch(TimeoutException e)
			{
				log().debug(svc.getSvcName() + ": TimeOut reached: " + e.toString());
			}

			catch(SocketTimeoutException e)
			{
				log().debug(svc.getSvcName() + ": TimeOut reached: " + e.toString());
			}

			catch(Exception e)
			{	
				log().error(svc.getSvcName() + ": Exception: " + e.toString());
				return PollStatus.decode("Down", "Could not get the state of SIP Peer: " + e.toString());
			}
		}
		//If none of the retries worked
		return PollStatus.decode("Down", "Could not get the state of SIP Peer: Timeout exceeded");
	}
}


