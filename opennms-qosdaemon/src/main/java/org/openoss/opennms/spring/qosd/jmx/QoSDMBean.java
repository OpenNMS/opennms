// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//





/*
 * Created on 08-Dec-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.openoss.opennms.spring.qosd.jmx;

/**
 * <p>QoSDMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface QoSDMBean {
    
    /**
     * <p>init</p>
     */
    public void init();
    /**
     * <p>start</p>
     */
    public void start();
    /**
     * <p>stop</p>
     */
    public void stop();
    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus();
    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String status();
	/**
	 * <p>getStats</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStats();

}
