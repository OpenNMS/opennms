/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;

/**
 * @author Donald Desloge
 *
 */
public class MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    protected Socket m_socket;
    private OutputStream m_out;
    private BufferedReader m_in;
    
    public void close() {
        LogUtils.infof(this, "Closing Socket");
        Socket socket = m_socket;
        m_socket = null;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }       
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        setOutput(socket.getOutputStream());
        m_socket = socket;
    }

    public MultilineOrientedResponse receiveBanner() throws IOException {
        return receiveResponse();
    }

    public MultilineOrientedResponse sendRequest(LineOrientedRequest request) throws IOException {
        request.send(getOutput());
        return receiveResponse();
    }
    
    /**
     * @return
     * @throws IOException
     */
    private MultilineOrientedResponse receiveResponse() throws IOException {
        MultilineOrientedResponse response = new MultilineOrientedResponse();
        response.receive(getInput());
        return response;
    }

    public void setInput(BufferedReader in) {
        m_in = in;
    }

    public BufferedReader getInput() {
        return m_in;
    }

    public void setOutput(OutputStream out) {
        m_out = out;
    }

    public OutputStream getOutput() {
        return m_out;
    }

}
