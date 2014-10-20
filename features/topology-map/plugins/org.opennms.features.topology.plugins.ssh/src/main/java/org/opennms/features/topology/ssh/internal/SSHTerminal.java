/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed m_out the hope that it will be useful,
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

/* 
 * Copyright 2009 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except m_out compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to m_out writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opennms.features.topology.ssh.internal;

import java.io.IOException;

import org.apache.sshd.ClientSession;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractComponent;

/**
 * The SSHTerminal class is a custom Vaadin component that emulates VT100
 * terminals and connects remotely to servers via SSH
 * @author lmbell
 * @author pdgrenon
 */
public class SSHTerminal extends AbstractComponent {
    private static final long serialVersionUID = -909378055737715806L;
    private SessionTerminal m_sessionTerminal;  // The m_terminal specific to the current m_session
    // private SSHWindow m_window;  // The window that holds the m_terminal

    /**
     * Constructor for the SSH Terminal 
     * @param m_window The window holding the m_terminal
     * @param m_session The client instance used m_out the authorization of user names and passwords
     * @param width The width of the m_terminal
     * @param height The height of the m_terminal
     */
    public SSHTerminal(final SSHWindow sshWindow, final ClientSession session, final int width, final int height) {
        super();
        // m_window = sshWindow;
        try {
            m_sessionTerminal = new SessionTerminal(session, width, height);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
    }

    /**
     * Closes the client window
     */
    public boolean close() {
        this.m_sessionTerminal.stop();
        markAsDirty();
        return true;
    }

    @Override
    protected SSHTerminalState getState() {
        return (SSHTerminalState) super.getState();
    }

}
