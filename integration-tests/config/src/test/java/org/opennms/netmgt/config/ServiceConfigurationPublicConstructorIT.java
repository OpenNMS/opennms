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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.opennms.netmgt.config.service.Service;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.OpenNMSConfigurationExecutionListener;
import org.opennms.test.PublicConstructorTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class uses the ServiceConfigFactory to fetch the list of reflection-instantiated
 * classes that are started by the OpenNMS daemon process. It inspects each class to make
 * sure that it has a public constructor so that OpenNMS startup will work properly.
 * 
 * @author Seth
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
@JUnitConfigurationEnvironment
public class ServiceConfigurationPublicConstructorIT extends PublicConstructorTest {
	@Override
	protected List<Class<? extends Object>> getClasses() throws IOException, ClassNotFoundException {
		List<Class<? extends Object>> retval = new ArrayList<Class<? extends Object>>();
		Service[] services = new ServiceConfigFactory().getServices();
		for (Service service : services) {
			retval.add(Class.forName(service.getClassName()));
		}
		return retval;
	}
}
