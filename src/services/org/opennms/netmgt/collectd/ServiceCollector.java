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
//      http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.Map;

import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.utils.EventProxy;

/**
 * <P>The Collector class...</P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public interface ServiceCollector
{
	/** 
	 * Status of the collector object.
	 */
	public static final int		COLLECTION_UNKNOWN 	= 0;
	public static final int 	COLLECTION_SUCCEEDED	= 1;
	public static final int		COLLECTION_FAILED = 2;
	
	public static final String[]  statusType = {"Unknown", "COLLECTION_SUCCEEDED", "COLLECTION_FAILED"};
	
	public void initialize(Map parameters);
	public void release();
	
	public void initialize(NetworkInterface iface, Map parameters);
	public void release(NetworkInterface iface);
	
	/**
	 * <P>Invokes a collection on the object.</P>
	 */
	public int collect(NetworkInterface iface, EventProxy eproxy, Map parameters);
}

