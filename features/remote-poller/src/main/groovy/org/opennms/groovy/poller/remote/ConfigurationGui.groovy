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

package org.opennms.groovy.poller.remote;

import org.opennms.netmgt.poller.remote.PollerFrontEnd;

import groovy.swing.SwingBuilder;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.text.DefaultStyledDocument;

import org.opennms.poller.remote.AuthenticationBean;
import org.opennms.poller.remote.GroovyGui;

class ConfigurationGui implements GroovyGui {
	def m_authenticationBean = new AuthenticationBean();
	def swing = new SwingBuilder();
	def gui;
	CountDownLatch m_latch = new CountDownLatch(1);
	
	JTextField m_userTextField;
	JPasswordField m_passwordTextField;
	
	public AuthenticationBean getAuthenticationBean() {
		m_latch.await();
		return m_authenticationBean;
	}

	public void setAuthenticationBean(AuthenticationBean bean) {
		m_authenticationBean = bean;
	}

	public void createAndShowGui() {
		gui = swing.frame(title:'Authenticate', size:[380,150], defaultCloseOperation:JFrame.DISPOSE_ON_CLOSE) {
			gridBagLayout();

			label(text:"Please authenticate with OpenNMS:", constraints:gbc(gridx:0,gridy:0,gridwidth:GridBagConstraints.REMAINDER, fill:GridBagConstraints.HORIZONTAL))
			label(text:"Username:", constraints:gbc(gridx:0,gridy:1))
			m_userTextField = textField(id:"username", columns: 20, constraints:gbc(gridx:1,gridy:1))
			label(text:"Password:", constraints:gbc(gridx:0,gridy:2))
			m_passwordTextField = passwordField(id:"password", columns: 20, constraints:gbc(gridx:1,gridy:2))
			button(text:"OK", constraints:gbc(gridx:1,gridy:3,anchor:GridBagConstraints.EAST), defaultButton: true, action(name:"ok", closure:{
				m_authenticationBean.setUsername(m_userTextField.getText())
				m_authenticationBean.setPassword(m_passwordTextField.getText())
				m_latch.countDown()
				gui.dispose()
			}))
		}
		gui.show();
	}
	
	public String toString() {
		return "Username = " + m_authenticationBean.getUsername() + ", Password = " + m_authenticationBean.getPassword();
	}
	
	public static void main(String[] args) {
		def configGui = new ConfigurationGui();
		configGui.createAndShowGui();
	}
}
