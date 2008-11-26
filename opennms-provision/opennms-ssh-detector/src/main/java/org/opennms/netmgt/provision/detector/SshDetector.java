package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;

public class SshDetector extends BasicDetector<SshRequest, SshResponse>{

    protected SshDetector() {
        super(22, 3000, 0);
    }

    @Override
    protected Client<SshRequest, SshResponse> getClient() {
        return new SshClient();
    }

    @Override
    protected void onInit() {
        
    }
	
}