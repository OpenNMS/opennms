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
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;

import org.opennms.poller.remote.AuthenticationBean;
import org.opennms.poller.remote.AuthenticationGui;

class LoginGui extends AbstractGui implements AuthenticationGui {
    def m_authenticationBean = new AuthenticationBean();
    
    /*
    def swing = new SwingBuilder();
    def gui;
    */
    CountDownLatch m_latch = new CountDownLatch(1);

    JTextField m_userTextField;
    JPasswordField m_passwordTextField;

    @Override
    protected Color getDetailColor() {
        return Color.GRAY;
    }

    public AuthenticationBean getAuthenticationBean() {
        m_latch.await();
        return m_authenticationBean;
    }

    public void setAuthenticationBean(AuthenticationBean bean) {
        m_authenticationBean = bean;
    }

    protected String getApplicationTitle() {
        return "Authentication"
    }

    public JPanel getMainPanel() {
        getGui().setResizable(false)

        return swing.panel(background:getBackgroundColor(), opaque:true, constraints:"grow") {
            migLayout(
                    layoutConstraints:"fill" + debugString,
                    columnConstraints:"[right,grow][left, grow]",
                    rowConstraints:""
                    )

            label(text:"Please authenticate with OpenNMS:", constraints:"left, spanx 2, wrap")

            label(text:"Username:")
            m_userTextField = textField(id:"username", columns: 20, constraints:"wrap")

            label(text:"Password:")
            m_passwordTextField = passwordField(id:"password", columns: 20, constraints:"wrap")
            def butt = button(text:"OK", defaultCapable: true, constraints:"spanx 2, right", font:getLabelFont(), foreground:getBackgroundColor(), background:getDetailColor(), opaque:true, action(name:"ok", closure:{
                m_authenticationBean.setUsername(m_userTextField.getText())
                m_authenticationBean.setPassword(m_passwordTextField.getText())
                m_latch.countDown()
                getGui().dispose()
            }))
            setDefaultButton(butt)
        }
    }

    public String toString() {
        return "Username = " + m_authenticationBean.getUsername() + ", Password = " + m_authenticationBean.getPassword();
    }

    public static void main(String[] args) {
        def configGui = new LoginGui()
        SwingUtilities.invokeLater({
            configGui.createAndShowGui()
        })
    }
}
