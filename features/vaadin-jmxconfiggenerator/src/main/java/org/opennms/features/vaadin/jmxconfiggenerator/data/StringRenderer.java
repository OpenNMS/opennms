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
 * StringRenderer which renders any object of type <code>T</code> to a String
 * representation. Usually a {@linkplain StringRenderer} is used when a specific
 * representation of an object is needed and toString() cannot be overwritten or
 * does not fulfill the requirements.<br/>
 * <br/>
 * 
 * @author Markus von Rüden
 * @param <T>
 *            the type of the object which needs to be rendered as a String
 */
public interface StringRenderer<T> {

	/**
	 * Transforms the input-object to a String.
	 * 
	 * @param input
	 *            The input object.
	 * @return The formatted string of the input object.
	 */
	String render(T input);
}
