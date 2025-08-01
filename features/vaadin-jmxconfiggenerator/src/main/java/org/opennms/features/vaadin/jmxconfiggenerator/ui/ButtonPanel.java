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
package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.HorizontalLayout;

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.Config;

public class ButtonPanel extends HorizontalLayout {
	private final Button next;
	private final Button previous;

	public ButtonPanel(ClickListener listener) {
		next = UIHelper.createButton("", "next", Config.Icons.BUTTON_NEXT, listener);
		previous = UIHelper.createButton("", "previous", Config.Icons.BUTTON_PREVIOUS, listener);

		next.setId("next");
		previous.setId("previous");

		setMargin(true);
		setSpacing(true);

		addComponent(previous);
		addComponent(next);
	}

	public Button getNext() {
		return next;
	}
	
	public Button getPrevious() {
		return previous;
	}
}
