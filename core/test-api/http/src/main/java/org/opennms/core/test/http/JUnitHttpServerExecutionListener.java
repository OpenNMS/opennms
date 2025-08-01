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
package org.opennms.core.test.http;

import org.opennms.core.test.OpenNMSAbstractTestExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitHttpServer} annotation
 * and uses attributes on it to launch a temporary HTTP server for use during unit tests.
 *
 * @author ranger
 */
public class JUnitHttpServerExecutionListener extends OpenNMSAbstractTestExecutionListener {
    private static JUnitServer m_junitServer;

    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        
        final JUnitHttpServer config = findTestAnnotation(JUnitHttpServer.class, testContext);
        if (config == null) return;

        m_junitServer = new JUnitServer(config);
        m_junitServer.start();
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        if (m_junitServer != null) m_junitServer.stop();
        m_junitServer = null;
    }

    public static int getPort() {
        return m_junitServer == null? -1 : m_junitServer.getPort();
    }
}
