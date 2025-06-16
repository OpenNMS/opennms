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

import junit.framework.TestCase;

import org.opennms.netmgt.dao.jaxb.InvocationAnticipator;
import org.opennms.netmgt.dao.jaxb.collector.CollectdConfigFile;
import org.opennms.netmgt.dao.jaxb.collector.CollectdConfigVisitor;
import org.springframework.core.io.ClassPathResource;

public class CollectdConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private CollectdConfigVisitor m_visitor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_invocationAnticipator = new InvocationAnticipator(CollectdConfigVisitor.class);
        m_visitor = (CollectdConfigVisitor)m_invocationAnticipator.getProxy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/collectdconfiguration-testdata.xml");
        CollectdConfigFile configFile = new CollectdConfigFile(resource.getFile());
        
        m_invocationAnticipator.anticipateCalls(1, "visitCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(1, "completeCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(2, "visitCollectorCollection");
        m_invocationAnticipator.anticipateCalls(2, "completeCollectorCollection");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

}
