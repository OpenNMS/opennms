/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.vmware;

import org.opennms.netmgt.provision.service.dns.DnsRequisitionUrlConnection;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * <p>VMwareUrlFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class VMwareUrlFactory implements URLStreamHandlerFactory {

    /** {@inheritDoc} */
    //TODO Ronny: We didn't know if this is REALLY? the way to register different URLStreamHandler
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (VMwareRequisitionUrlConnection.PROTOCOL.equals(protocol)) {
            return new org.opennms.netmgt.provision.service.vmware.Handler();
        } else {
            if (DnsRequisitionUrlConnection.PROTOCOL.equals(protocol)) {
                return new org.opennms.netmgt.provision.service.dns.Handler();
            } else {
                return null;
            }
        }
    }

}
