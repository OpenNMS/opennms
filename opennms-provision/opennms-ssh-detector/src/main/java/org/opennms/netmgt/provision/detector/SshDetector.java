package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.ssh.Ssh;

public class SshDetector extends BasicDetector<NullRequest, SshResponse>{
    
    private String m_banner = null;
    private String m_match = null;
    private String m_clientBanner = Ssh.DEFAULT_CLIENT_BANNER;
    
    protected SshDetector() {
        super(22, 3000, 0);
        setServiceName("SSH");
    }

    @Override
    protected Client<NullRequest, SshResponse> getClient() {
        SshClient client = new SshClient();
        client.setBanner(getBanner());
        client.setMatch(getMatch());
        client.setClientBanner(getClientBanner());
        return client;
    }

    @Override
    protected void onInit() {
        expectBanner(sshIsAvailable());
    }

    /**
     * @return
     */
    private ResponseValidator<SshResponse> sshIsAvailable() {
        
        return new ResponseValidator<SshResponse>(){

            public boolean validate(SshResponse response) throws Exception {
                return response.isAvailable();
            }
            
        };
    }

    public void setBanner(String banner) {
        m_banner = banner;
    }

    public String getBanner() {
        return m_banner;
    }

    public void setMatch(String match) {
        m_match = match;
    }

    public String getMatch() {
        return m_match;
    }

    public void setClientBanner(String clientBanner) {
        m_clientBanner = clientBanner;
    }

    public String getClientBanner() {
        return m_clientBanner;
    }
	
}