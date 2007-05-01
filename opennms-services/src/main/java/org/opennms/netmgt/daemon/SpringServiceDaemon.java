package org.opennms.netmgt.daemon;

import org.springframework.beans.factory.InitializingBean;

public interface SpringServiceDaemon extends InitializingBean {
    public void start() throws Exception;
}
