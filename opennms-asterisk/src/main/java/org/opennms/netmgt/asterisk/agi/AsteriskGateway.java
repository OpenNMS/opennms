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
package org.opennms.netmgt.asterisk.agi;

import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.ClassNameMappingStrategy;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * <p>AsteriskGateway class.</p>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 */
public class AsteriskGateway extends AbstractServiceDaemon {

    AgiServerThread m_agiServerThread;
    private int m_port = 4573;
    private int m_maxPoolSize = 10;
    
    /**
     * <p>Constructor for AsteriskGateway.</p>
     */
    protected AsteriskGateway() {
        super("asterisk-gateway");
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        int port = SystemProperties.getInteger("org.opennms.netmgt.asterisk.agi.listenPort", m_port);
        int maxPoolSize = SystemProperties.getInteger("org.opennms.netmgt.asterisk.agi.maxPoolSize", m_maxPoolSize);
        
        DefaultAgiServer agiServer = new DefaultAgiServer(new ClassNameMappingStrategy(false));
        
        agiServer.setPort(port);
        agiServer.setMaximumPoolSize(maxPoolSize);
        
        m_agiServerThread = new AgiServerThread(agiServer);
        
        // This is the default, but be explicit
        m_agiServerThread.setDaemon(true);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        m_agiServerThread.startup();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        m_agiServerThread.shutdown();
    }

}
