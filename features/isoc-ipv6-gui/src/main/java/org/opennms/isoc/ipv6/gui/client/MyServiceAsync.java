package org.opennms.isoc.ipv6.gui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyServiceAsync {

    void myMethod(String string, AsyncCallback<String> callback);


}
