//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This software is Proprietary and Confidental.
// 
// For more information contact: 
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
package org.opennms.netmgt.eventd.adaptors.udp;

import java.lang.*;

import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;

/**
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 *
 */
public interface UdpEventReceiverMBean
{
	public void init();
	public void destroy();

	public void start();
	public void stop();

	public void setPort(Integer port);
	public Integer getPort();
	
	public int getStatus();

	public void addEventHandler(String name)
		throws MalformedObjectNameException,
			InstanceNotFoundException;
	
	public void removeEventHandler(String name)
		throws MalformedObjectNameException,
			InstanceNotFoundException;
	
	public void setLogPrefix(String prefix);
}
