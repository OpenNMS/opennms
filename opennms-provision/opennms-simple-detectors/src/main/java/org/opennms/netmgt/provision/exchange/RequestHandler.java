package org.opennms.netmgt.provision.exchange;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestHandler {
    public void doRequest(OutputStream out) throws IOException;
}
