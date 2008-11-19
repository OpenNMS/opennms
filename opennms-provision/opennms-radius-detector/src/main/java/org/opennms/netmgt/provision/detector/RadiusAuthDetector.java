package org.opennms.netmgt.provision.detector;

import net.sourceforge.jradiusclient.RadiusAttribute;
import net.sourceforge.jradiusclient.RadiusAttributeValues;
import net.sourceforge.jradiusclient.RadiusPacket;
import net.sourceforge.jradiusclient.util.ChapUtil;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;

public class RadiusAuthDetector extends BasicDetector<RadiusPacket, RadiusPacket>{
    
    /**
     * Default radius authentication port
     */
    public static final int DEFAULT_AUTH_PORT = 1812;

    /**
     * Default radius accounting port
     */
    public static final int DEFAULT_ACCT_PORT = 1813;

    /**
     * Default radius authentication type
     */
    public static final String DEFAULT_AUTH_TYPE = "pap";

    /**
     * Default user
     */
    public static final String DEFAULT_USER = "OpenNMS";

    /**
     * Default password
     */
    public static final String DEFAULT_PASSWORD = "OpenNMS";

    /**
     * Default secret
     */
    public static final String DEFAULT_SECRET = "secret";

    /**
     * 
     * Default NAS_ID
     */
    public static final String DEFAULT_NAS_ID = "opennms";
    
    private int m_authport = DEFAULT_AUTH_PORT;
    private int m_acctport = DEFAULT_ACCT_PORT;
    private String m_secret = DEFAULT_SECRET;
    private String m_authType = DEFAULT_AUTH_TYPE;
    private String m_nasid = DEFAULT_NAS_ID;
    private String m_user = DEFAULT_USER;
    private String m_password = DEFAULT_PASSWORD;
    
    protected RadiusAuthDetector() {
        super(1812, 5000, 0);
    }

    @Override
    protected void onInit() {
        send(request(getNasID(), getUser(), getPassword()), expectValidResponse(RadiusPacket.ACCESS_ACCEPT, RadiusPacket.ACCESS_CHALLENGE, RadiusPacket.ACCESS_REJECT));
    }
    
    /**
     * @return
     */
    private ResponseValidator<RadiusPacket> expectValidResponse(final int accept, final int challenge, final int reject) {
        
        return new ResponseValidator<RadiusPacket>() {

            public boolean validate(RadiusPacket response) {
                
                return (response.getPacketType() == accept || response.getPacketType() == challenge || response.getPacketType() == reject);
            }
            
        };
    }

    private RequestBuilder<RadiusPacket> request(final String nasID, final String user, final String password){
        return new RequestBuilder<RadiusPacket>() {

            public RadiusPacket getRequest() throws Exception {
                ChapUtil chapUtil = new ChapUtil();
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                RadiusAttribute nasIdAttribute;
                nasIdAttribute = new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER,nasID.getBytes());
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,user.getBytes());
                accessRequest.setAttribute(userNameAttribute);
                accessRequest.setAttribute(nasIdAttribute);
                if(getAuthType().equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(password, chapChallenge, chapUtil)));
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,password.getBytes()));
                }
                return accessRequest;
            }
            
        };
    }
    
    @Override
    protected Client<RadiusPacket, RadiusPacket> getClient() {
        RadiusDetectorClient rdc = new RadiusDetectorClient();
        rdc.setAuthport(getAuthPort());
        rdc.setAcctPort(getAcctPort());
        rdc.setSecret(getSecret());
        return rdc;
    }
    
    private static byte[] chapEncrypt(final String plainText, final byte[] chapChallenge, final ChapUtil chapUtil){
        byte chapIdentifier = chapUtil.getNextChapIdentifier();
        byte[] chapPassword = new byte[17];
        chapPassword[0] = chapIdentifier;
        System.arraycopy(ChapUtil.chapEncrypt(chapIdentifier, plainText.getBytes(),chapChallenge), 0, chapPassword, 1, 16);
        return chapPassword;
    }
    
    public void setAuthPort(int authport) {
        m_authport = authport;
    }

    public int getAuthPort() {
        return m_authport;
    }

    public void setAcctPort(int acctport) {
        m_acctport = acctport;
    }

    public int getAcctPort() {
        return m_acctport;
    }

    public void setSecret(String secret) {
        m_secret = secret;
    }

    public String getSecret() {
        return m_secret;
    }

    public void setAuthType(String authType) {
        m_authType = authType;
    }

    public String getAuthType() {
        return m_authType;
    }

    public void setNasID(String nasid) {
        m_nasid = nasid;
    }

    public String getNasID() {
        return m_nasid;
    }

    public void setUser(String user) {
        m_user = user;
    }

    public String getUser() {
        return m_user;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }
	
}