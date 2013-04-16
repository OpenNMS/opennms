/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.data;

/**
 * Meta interface to address all properties of an ConfigModel bean in vaadin
 * framework. In this way we do not need use strings!
 * 
 * @author Markus von Rüden
 * @see ServiceConfig
 */
public interface MetaConfigModel {

	String SERVICE_NAME = "serviceName";
	String PACKAGE_NAMES = "packageNames";
	String JMXMP = "jmxmp";
	String HOST = "host";
	String PASSWORD = "password";
	String PORT = "port";
	String AUTHENTICATE = "authenticate";
	String USER = "user";
	String SKIP_DEFAULT_VM = "skipDefaultVM";
	String RUN_WRITABLE_MBEANS = "runWritableMBeans";
}
