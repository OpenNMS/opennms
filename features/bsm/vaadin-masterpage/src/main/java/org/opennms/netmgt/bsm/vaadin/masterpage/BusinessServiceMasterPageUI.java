/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.masterpage;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("opennms")
@Title("Business Service Master Page")
@SuppressWarnings("serial")
public class BusinessServiceMasterPageUI extends UI {

	private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

	private BusinessServiceManager businessServiceManager;

	public BusinessServiceMasterPageUI(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
		this.transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
	}

	@Override
	protected void init(VaadinRequest request) {
		Button b = new Button("Click me");
		b.addClickListener(event -> businessServiceManager.findAll());

		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(new Label("Welcome to the new Business Service Master Page *yay*"));
		layout.addComponent(b);

		setContent(layout);
	}

	public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
		Objects.requireNonNull(businessServiceManager);
		this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
//		this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
	}
}
