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
package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;

import junit.framework.TestCase;

import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class JdbcDataCollectionConfigDaoJaxbTest extends TestCase {
    
    public void testAfterPropertiesSetWithNoConfigSet() {
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
        dao.setConfigResource(resource);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("jdbc-datacollection-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        assertNotNull("jdbc data collection should not be null", dao.getConfig());
    }

}
