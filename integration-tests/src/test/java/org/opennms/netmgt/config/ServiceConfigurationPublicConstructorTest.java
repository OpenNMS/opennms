/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.runner.RunWith;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
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
public class ServiceConfigurationPublicConstructorTest extends PublicConstructorTest {
	@Override
	protected List<Class<? extends Object>> getClasses() throws MarshalException, ValidationException, IOException, ClassNotFoundException {
		List<Class<? extends Object>> retval = new ArrayList<Class<? extends Object>>();
		ServiceConfigFactory.init();
		Service[] services = ServiceConfigFactory.getInstance().getServices();
		for (Service service : services) {
			retval.add(Class.forName(service.getClassName()));
		}
		return retval;
	}
}
