package org.opennms.isoc.ipv6.gui.server;

import org.opennms.isoc.ipv6.gui.client.MyService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MyServiceImpl extends RemoteServiceServlet implements MyService {

    public String myMethod(String string) {
        return string;
    }

}
