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

import org.opennms.features.vaadin.jmxconfiggenerator.data.MetaMBeanItem;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectionChangedListener;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

import com.vaadin.v7.data.Validator;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 *
 * @author Markus von Rüden
 */
public class MBeansContentPanel extends VerticalLayout implements SelectionChangedListener {

	private static final FormParameter MBEAN_FORM_PARAMETER = new FormParameter() {
		@Override
		public String getCaption() {
			return "MBean details";
		}

		@Override
		public String getEditablePropertyName() {
			return MetaMBeanItem.NAME;
		}

		@Override
		public String getNonEditablePropertyName() {
			return MetaMBeanItem.OBJECTNAME;
		}

		@Override
		public String getEditablePropertyCaption() {
			return "Name";
		}

		@Override
		public String getNonEditablePropertyCaption() {
			return "Objectname";
		}
	};

	private static final FormParameter COMPOSITE_FORM_PARAMETER = new FormParameter() {
		@Override
		public String getCaption() {
			return "Composite details";
		}

		@Override
		public String getEditablePropertyName() {
			return "alias";
		}

		@Override
		public String getNonEditablePropertyName() {
			return "name";
		}

		@Override
		public String getEditablePropertyCaption() {
			return "Alias";
		}

		@Override
		public String getNonEditablePropertyCaption() {
			return "Name";
		}
	};

	private static final String CAPTION_FORMAT = "<b>%s</b>";

	private final AttributesTable attributesTable;

	private final NameEditForm nameEditForm;

	private final MBeansController controller;

	private final VerticalLayout contentLayout;

	private final VerticalLayout emptyLayout;

	private final Label captionLabel;

	public MBeansContentPanel(final MBeansController controller) {
		this.controller = controller;
		this.attributesTable = new AttributesTable(controller, controller);

		emptyLayout = new VerticalLayout();
		emptyLayout.setSpacing(true);
		emptyLayout.setMargin(true);
		emptyLayout.addComponent(new Label("No MBean or Composite selected. Please select one to modify data."));

		nameEditForm = new NameEditForm(controller);

		captionLabel = new Label();
		captionLabel.setContentMode(ContentMode.HTML);
		VerticalLayout attributeLayout = new VerticalLayout();
		attributeLayout.addComponent(captionLabel);
		attributeLayout.addComponent(attributesTable);
		attributeLayout.setSizeFull();
		attributeLayout.setExpandRatio(attributesTable, 1);

		contentLayout = new VerticalLayout();
		contentLayout.setSpacing(true);
		contentLayout.setMargin(true);
		contentLayout.addComponent(nameEditForm);
		contentLayout.addComponent(attributeLayout);
		contentLayout.setSizeFull();
		contentLayout.setExpandRatio(attributeLayout, 1);

		// by default we do not show details
		addComponent(emptyLayout);
		addComponent(contentLayout);
		setSizeFull();

		// we have to listen for "selection" value changed events
		controller.registerSelectionValueChangedListener(nameEditForm);
		controller.registerSelectionValueChangedListener(attributesTable);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object selectedBean = event.getSelectedBean();

		if (selectedBean instanceof Mbean) {
			captionLabel.setValue(String.format(CAPTION_FORMAT, "MBean Attributes"));
			nameEditForm.setParameter(MBEAN_FORM_PARAMETER);
			nameEditForm.selectionChanged(event);
			attributesTable.modelChanged(event.getSelectedItem(), event.getSelectedBean(), controller.getContainer(Attrib.class, selectedBean));
			setContent(contentLayout);
			return;
		}

		if (selectedBean instanceof CompAttrib) {
			captionLabel.setValue(String.format(CAPTION_FORMAT, "Composite Members"));
			nameEditForm.setParameter(COMPOSITE_FORM_PARAMETER);
			nameEditForm.selectionChanged(event);
			attributesTable.modelChanged(event.getSelectedItem(), event.getSelectedBean(), controller.getContainer(CompMember.class, selectedBean));
			setContent(contentLayout);
			return;
		}
		setContent(emptyLayout);
	}

	private void setContent(Component component) {
		emptyLayout.setVisible(false);
		contentLayout.setVisible(false);
		component.setVisible(true);
	}

	/**
	 * Indicates wheter the panel has uncommitted changes.
	 * @return True if there are uncommitted changes, False otherwise.
	 */
	boolean isDirty() {
		if (contentLayout.isVisible()) {
			boolean dirtyFlag = nameEditForm.isDirty() || attributesTable.isDirty();
			return dirtyFlag;
		}
		return false;
	}

	private void validate() throws Validator.InvalidValueException {
		if (contentLayout.isVisible()) {
			nameEditForm.validate();
			attributesTable.validate();
		}
	}

	boolean isValid() {
		try {
			validate();
			return true;
		} catch (Validator.InvalidValueException ex) {
			return false;
		}
	}

	void reset() {
		setContent(emptyLayout);
	}

	void discard() {
		if (contentLayout.isVisible()) {
			nameEditForm.discard();
			attributesTable.discard();
		}
	}
}
