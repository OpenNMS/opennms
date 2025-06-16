/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
