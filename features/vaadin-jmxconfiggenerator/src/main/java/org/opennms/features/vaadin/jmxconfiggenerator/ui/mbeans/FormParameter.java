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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

/**
 * Is used to configure the {@link NameEditForm}.
 * 
 * @author Markus von Rüden
 * @see NameEditForm
 */
public interface FormParameter {
	/**
	 * Defines the caption for the {@link NameEditForm}.
	 * 
	 * @return the caption of the {@link NameEditForm}.
	 */
	String getCaption();

	/**
	 * Returns the name of the property which is editable.
	 * 
	 * @return the property name which is editable.
	 */
	String getEditablePropertyName();

	/**
	 * Returns the property name which is not editable.
	 * 
	 * @return the property name which is not editable.
	 */
	String getNonEditablePropertyName();

	String getEditablePropertyCaption();

	String getNonEditablePropertyCaption();
}