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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

/**
 * Meta interface to address all properties of an ConfigModel bean in vaadin
 * framework. In this way we do not need use strings!
 * 
 * @author Markus von Rüden
 * @see ServiceConfig
 */
public interface MetaConfigModel {

	String SERVICE_NAME = "serviceName";
	String PASSWORD = "password";
	String AUTHENTICATE = "authenticate";
	String USER = "user";
	String SKIP_DEFAULT_VM = "skipDefaultVM";
	String SKIP_NON_NUMBER = "skipNonNumber";
	String CONNECTION = "connection";
}
