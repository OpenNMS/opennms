/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.trapd.TrapSinkConsumer;

/**
 * <p>Trapd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Trapd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.trapd.Trapd> implements TrapdMBean {
    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.trapd.Trapd.LOG4J_CATEGORY;
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "trapDaemonContext";
    }
    
    /** {@inheritDoc} */
    @Override
    public long getTrapsReceived() {
        return getTrapdInstrumentation().getTrapsReceived();
    }

    /** {@inheritDoc} */
    @Override
    public long getV1TrapsReceived() {
        return getTrapdInstrumentation().getV1TrapsReceived();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getV2cTrapsReceived() {
        return getTrapdInstrumentation().getV2cTrapsReceived();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getV3TrapsReceived() {
        return getTrapdInstrumentation().getV3TrapsReceived();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getVUnknownTrapsReceived() {
        return getTrapdInstrumentation().getVUnknownTrapsReceived();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getTrapsDiscarded() {
        return getTrapdInstrumentation().getTrapsDiscarded();
    }
    
    /** {@inheritDoc} */
    @Override
    public long getTrapsErrored() {
        return getTrapdInstrumentation().getTrapsErrored();
    }
    
    private TrapdInstrumentation getTrapdInstrumentation() {
        return TrapSinkConsumer.trapdInstrumentation;
    }
}
