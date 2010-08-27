package org.opennms.netmgt.provision.detector.wmi;

import java.net.InetAddress;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.support.AbstractDetector;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class WmiDetector extends AbstractDetector {
    
    private final static String PROTOCOL_NAME = "WMI";

    private final static String DEFAULT_WMI_CLASS = "Win32_ComputerSystem";
    private final static String DEFAULT_WMI_OBJECT = "Status";
    private final static String DEFAULT_WMI_COMP_VAL = "OK";
    private final static String DEFAULT_WMI_MATCH_TYPE = "all";
    private final static String DEFAULT_WMI_COMP_OP = "EQ";
    private final static String DEFAULT_WMI_WQL = "NOTSET";

    private String m_matchType;

    private String m_compVal;

    private String m_compOp;

    private String m_wmiClass;

    private String m_wmiObject;

    private String m_wmiWqlStr;

    private String m_username;

    private String m_password;

    private String m_domain;
    
    public WmiDetector() {

    }
    
    
    @Override
    protected void onInit() {
        setServiceName(PROTOCOL_NAME);
        
        setMatchType(getMatchType() != null ? getMatchType() : DEFAULT_WMI_MATCH_TYPE);
        setCompVal(getCompVal() != null ? getCompVal() : DEFAULT_WMI_COMP_VAL);
        setCompOp(getCompOp() != null ? getCompOp() : DEFAULT_WMI_COMP_OP);
        setWmiClass(getWmiClass() != null ? getWmiClass() : DEFAULT_WMI_CLASS);
        setWmiObject(getWmiObject() != null ? getWmiObject() : DEFAULT_WMI_OBJECT);
        setWmiWqlStr(getWmiWqlStr() != null ? getWmiWqlStr() : DEFAULT_WMI_WQL);
    }

    @Override
    public boolean isServiceDetected(final InetAddress address, final DetectorMonitor detectMonitor) {
        WmiParams clientParams = null;

        if(getWmiWqlStr().equals(DEFAULT_WMI_WQL)) {
            // Create the check parameters holder.
            clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF,
                                         getCompVal(), getCompOp(), getWmiClass(), getWmiObject());
        } else {
            // Define the WQL Query.
            clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL,
                                         getCompVal(), getCompOp(), getWmiWqlStr(), getWmiObject());
        }


        // Perform the operation specified in the parameters.
        WmiResult result = isServer(address, getUsername(), getPassword(), getDomain(), getMatchType(),
                getRetries(), getTimeout(), clientParams);

        // Only fail on critical and unknown returns.
        if (result != null && result.getResultCode() != WmiResult.RES_STATE_CRIT
                && result.getResultCode() != WmiResult.RES_STATE_UNKNOWN) {

            return true;
        } else {
            return false;
        }
    }
    
    private WmiResult isServer(InetAddress host, String user, String pass,
            String domain, String matchType, int retries, int timeout,
            WmiParams params) {
        boolean isAServer = false;

        WmiResult result = null;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            WmiManager mgr = null;
            try {
                // Create the WMI Manager
                mgr = new WmiManager(host.getHostAddress(), user,
                        pass, domain, matchType);

                // Connect to the WMI server.
                mgr.init();

                // Perform the operation specified in the parameters.
                result = mgr.performOp(params);
                if(params.getWmiOperation().equals(WmiParams.WMI_OPERATION_WQL)) {
                    log().debug(
                        "WmiPlugin: "
                                + params.getWql()                               
                                + " : "
                                + WmiResult.convertStateToString(result
                                        .getResultCode()));
                } else {
                    log().debug(
                        "WmiPlugin: \\\\"
                                + params.getWmiClass()
                                + "\\"
                                + params.getWmiObject()
                                + " : "
                                + WmiResult.convertStateToString(result
                                        .getResultCode()));
                }

                isAServer = true;
            } catch (WmiException e) {
                StringBuffer message = new StringBuffer();
                message.append("WmiPlugin: Check failed... : ");
                message.append(e.getMessage());
                message.append(" : ");
                message.append((e.getCause() == null ? "" : e.getCause().getMessage()));
                log().info(message.toString());
                isAServer = false;
            } finally {
                if (mgr != null) {
                    try {
                        mgr.close();
                    } catch (WmiException e) {
                        log().warn("an error occurred closing the WMI Manager", e);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }


    public void setMatchType(String matchType) {
        m_matchType = matchType;
    }


    public String getMatchType() {
        return m_matchType;
    }


    public void setCompVal(String compVal) {
        m_compVal = compVal;
    }


    public String getCompVal() {
        return m_compVal;
    }


    public void setCompOp(String compOp) {
        m_compOp = compOp;
    }


    public String getCompOp() {
        return m_compOp;
    }


    public void setWmiClass(String wmiClass) {
        m_wmiClass = wmiClass;
    }


    public String getWmiClass() {
        return m_wmiClass;
    }


    public void setWmiObject(String wmiObject) {
        m_wmiObject = wmiObject;
    }


    public String getWmiObject() {
        return m_wmiObject;
    }


    public void setWmiWqlStr(String wmiWqlStr) {
        m_wmiWqlStr = wmiWqlStr;
    }


    public String getWmiWqlStr() {
        return m_wmiWqlStr;
    }


    public void setUsername(String username) {
        m_username = username;
    }


    public String getUsername() {
        return m_username;
    }


    public void setPassword(String password) {
        m_password = password;
    }


    public String getPassword() {
        return m_password;
    }


    public void setDomain(String domain) {
        m_domain = domain;
    }


    public String getDomain() {
        return m_domain;
    }

}
