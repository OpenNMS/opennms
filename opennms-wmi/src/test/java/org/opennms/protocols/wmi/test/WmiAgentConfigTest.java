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
package org.opennms.protocols.wmi.test;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Sep 17, 2008
 * Time: 7:29:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class WmiAgentConfigTest  extends TestCase {
    	/*
	 * Create a placeholder mock object. We will reset() this in each test
	 * so that we can reuse it.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
        @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Tear down simply resets the mock object.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
        @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
	 * Test that the isValidMatchType object works for the expected values of
	 * "all", "none", "some" and "one" but does not work for other arbitrary values.
	 *
	 * Test method for
	 * {@link org.opennms.netmgt.config.wmi.WmiAgentConfig#isValidMatchType(java.lang.String)}.
	 */
	public final void testIsValidMatchType() {

    }

}
