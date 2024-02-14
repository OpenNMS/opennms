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
package org.opennms.features.vaadin.jmxconfiggenerator;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

/**
 * Config class.
 *
 * @author Markus von Rüden
 */
public interface Config {

    int ATTRIBUTES_ALIAS_MAX_LENGTH = 19;
    int CONFIG_SNIPPET_MAX_LINES = 2500;

    /**
     * This class provides the application with icons. If any icon changes or new
     * icons are needed, please put them below.
     *
     * @author Markus von Rüden
     */
    interface Icons {
        Resource DUMMY = FontAwesome.SQUARE;
       	Resource BUTTON_SAVE = FontAwesome.FLOPPY_O;
        Resource BUTTON_NEXT = FontAwesome.CHEVRON_RIGHT;
        Resource BUTTON_PREVIOUS = FontAwesome.CHEVRON_LEFT;
		Resource SELECTED = FontAwesome.CHECK_SQUARE_O;
		Resource NOT_SELECTED = FontAwesome.SQUARE_O;
        Resource HELP = FontAwesome.QUESTION;
    }
}
