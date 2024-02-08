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
package org.opennms.netmgt.dao.jaxb.collector;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.jaxb.InvocationAnticipator;
import org.opennms.netmgt.dao.jaxb.collector.DataCollectionConfigFile;
import org.opennms.netmgt.dao.jaxb.collector.DataCollectionVisitor;
import org.springframework.core.io.ClassPathResource;

public class DataCollectionConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private DataCollectionVisitor m_visitor;
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        InvocationHandler noNullsAllowed = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                assertNotNull(args);
                assertEquals(1, args.length);
                assertNotNull(args[0]);
                return null;
            }
            
        };
        m_invocationAnticipator = new InvocationAnticipator(DataCollectionVisitor.class);
        m_invocationAnticipator.setInvocationHandler(noNullsAllowed);
        
        m_visitor = (DataCollectionVisitor)m_invocationAnticipator.getProxy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisit() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/datacollectionconfigfile-testdata.xml");
        DataCollectionConfigFile configFile = new DataCollectionConfigFile(resource.getFile());
        
        anticipateVisits(1, "DataCollectionConfig");
        anticipateVisits(1, "SnmpCollection");
        anticipateVisits(1, "Rrd");
        anticipateVisits(4, "Rra");
        anticipateVisits(26, "SystemDef");
        anticipateVisits(4, "SysOid");
        anticipateVisits(22, "SysOidMask");
        anticipateVisits(0, "IpList");
        anticipateVisits(26, "Collect");
        anticipateVisits(69, "IncludeGroup");
        anticipateVisits(57, "Group");
        anticipateVisits(0, "SubGroup");
        anticipateVisits(809, "MibObj");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

    private void anticipateVisits(int count, String visited) {
        m_invocationAnticipator.anticipateCalls(count, "visit"+visited);
        m_invocationAnticipator.anticipateCalls(count, "complete"+visited);
    }

}
