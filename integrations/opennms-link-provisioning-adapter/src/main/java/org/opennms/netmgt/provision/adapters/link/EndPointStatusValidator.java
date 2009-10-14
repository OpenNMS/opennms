package org.opennms.netmgt.provision.adapters.link;

import java.net.UnknownHostException;

public interface EndPointStatusValidator{
   public boolean validate() throws UnknownHostException;
}