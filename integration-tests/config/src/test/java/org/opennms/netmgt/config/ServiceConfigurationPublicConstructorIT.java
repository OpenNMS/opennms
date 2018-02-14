/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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
