package org.opennms.isoc.ipv6.gui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("myService")
public interface MyService extends RemoteService {
    public String myMethod(String string);
}
