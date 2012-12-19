package org.opennms.netmgt.accesspointmonitor.poller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * Instance strategy for polling access-points: 1) Walks the configured OID
 * and uses the instance variable as the AP MAC address. 2) Verifies the
 * returned value against the configured criteria to determine if the AP is
 * ONLINE or NOT.
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class InstanceStrategy implements AccessPointPoller {

    /**
     * Constant for less-than operand
     */
    private static final String LESS_THAN = "<";

    /** Constant <code>GREATER_THAN=">"</code> */
    private static final String GREATER_THAN = ">";

    /** Constant <code>LESS_THAN_EQUALS="<="</code> */
    private static final String LESS_THAN_EQUALS = "<=";

    /** Constant <code>GREATER_THAN_EQUALS=">="</code> */
    private static final String GREATER_THAN_EQUALS = ">=";

    /** Constant <code>EQUALS="="</code> */
    private static final String EQUALS = "=";

    /** Constant <code>NOT_EQUAL="!="</code> */
    private static final String NOT_EQUAL = "!=";

    /** Constant <code>MATCHES="~"</code> */
    private static final String MATCHES = "~";

    private OnmsIpInterface m_iface;
    private Package m_package;
    private Map<String, String> m_parameters;
    private AccessPointDao m_accessPointDao;

    public InstanceStrategy() {

    }

    private SnmpAgentConfig getAgentConfig(InetAddress ipaddr) {
        // Retrieve this interface's SNMP peer object
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) {
            throw new IllegalStateException("SnmpAgentConfig object not available for interface " + ipaddr);
        }

        agentConfig.hashCode();

        // Set timeout and retries on SNMP peer object
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(m_parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(m_parameters, "retry", ParameterMap.getKeyedInteger(m_parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(m_parameters, "port", agentConfig.getPort()));

        return agentConfig;
    }

    public OnmsAccessPointCollection call() throws IOException {
        OnmsAccessPointCollection apsUp = new OnmsAccessPointCollection();
        InetAddress ipaddr = m_iface.getIpAddress();

        // Retrieve this interface's SNMP peer object
        SnmpAgentConfig agentConfig = getAgentConfig(ipaddr);
        final String hostAddress = InetAddressUtils.str(ipaddr);
        log().debug("poll: setting SNMP peer attribute for interface " + hostAddress);

        // Get configuration parameters
        String oid = ParameterMap.getKeyedString(m_parameters, "oid", null);
        if (oid == null) {
            throw new IllegalStateException("oid parameter is not set.");
        }
        String operator = ParameterMap.getKeyedString(m_parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(m_parameters, "operand", null);
        String matchstr = ParameterMap.getKeyedString(m_parameters, "match", "true");

        if (log().isDebugEnabled()) {
            log().debug("InstanceStrategy.poll: SnmpAgentConfig address= " + agentConfig);
        }

        // Establish SNMP session with interface
        try {
            SnmpObjId snmpObjectId = SnmpObjId.get(oid);

            Map<SnmpInstId, SnmpValue> map = SnmpUtils.getOidValues(agentConfig, "AccessPointMonitor::InstanceStrategy", snmpObjectId);

            if (map.size() <= 0) {
                throw new IOException("No entries found in table (possible timeout).");
            }

            for (Map.Entry<SnmpInstId, SnmpValue> entry : map.entrySet()) {
                boolean isUp = false;
                SnmpInstId instance = entry.getKey();
                SnmpValue value = entry.getValue();

                // Check the value against the configured criteria
                if (meetsCriteria(value, operator, operand)) {
                    if ("true".equals(matchstr)) {
                        isUp = true;
                    }
                } else if ("false".equals(matchstr)) {
                    isUp = true;
                }

                // If the criteria is met, find the AP and add it to the list
                // of online APs
                if (isUp) {
                    String physAddr = getPhysAddrFromInstance(instance);
                    log().debug("AP at instance '" + instance + "' with MAC '" + physAddr + "' is considered to be ONLINE on controller '" + m_iface.getIpAddress() + "'");
                    OnmsAccessPoint ap = m_accessPointDao.findByPhysAddr(physAddr);
                    if (ap != null) {
                        if (ap.getPollingPackage().compareToIgnoreCase(getPackage().getName()) == 0) {
                            // Save the controller's IP address
                            ap.setControllerIpAddress(ipaddr);
                            apsUp.add(ap);
                        } else {
                            log().info("AP with MAC '" + physAddr + "' is in a different package.");
                        }
                    } else {
                        log().info("No matching AP in database for instance '" + instance + "'.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            log().error("Number operator used on a non-number ", e);
        } catch (IllegalArgumentException e) {
            log().error("Invalid SNMP Criteria ", e);
        } catch (InterruptedException e) {
            log().error("Interrupted while polling " + hostAddress, e);
        }

        return apsUp;
    }

    public static String getPhysAddrFromInstance(SnmpInstId instance) {
        String[] elm;
        elm = instance.toString().split("\\.");

        if (elm.length != 6) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int val = Integer.parseInt(elm[i]);
            if (val < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(val));
            if (i < 5) {
                sb.append(':');
            }
        }

        return sb.toString().toUpperCase();
    }

    public static SnmpInstId getInstanceFromPhysAddr(String physAddr) {
        String[] elm;
        elm = physAddr.split(":");

        if (elm.length != 6) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(".");
            sb.append(Integer.parseInt(elm[i], 16));
        }

        return new SnmpInstId(sb.toString());
    }

    /**
     * Verifies that the result of the SNMP query meets the criteria specified
     * by the operator and the operand from the configuration file.
     * 
     * @param result
     *            a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     * @param operator
     *            a {@link java.lang.String} object.
     * @param operand
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    protected boolean meetsCriteria(SnmpValue result, String operator, String operand) {

        Boolean retVal = null;

        retVal = isCriteriaNull(result, operator, operand);

        if (retVal == null) {
            String value = result.toString();
            retVal = checkStringCriteria(operator, operand, value);

            if (retVal == null) {
                BigInteger val = BigInteger.valueOf(result.toLong());

                BigInteger intOperand = new BigInteger(operand);
                if (LESS_THAN.equals(operator)) {
                    return val.compareTo(intOperand) < 0;
                } else if (LESS_THAN_EQUALS.equals(operator)) {
                    return val.compareTo(intOperand) <= 0;
                } else if (GREATER_THAN.equals(operator)) {
                    return val.compareTo(intOperand) > 0;
                } else if (GREATER_THAN_EQUALS.equals(operator)) {
                    return val.compareTo(intOperand) >= 0;
                } else {
                    throw new IllegalArgumentException("operator " + operator + " is unknown");
                }
            }
        } else if (retVal.booleanValue()) {
            return true;
        }

        return retVal.booleanValue();
    }

    /**
     * @param operator
     * @param operand
     * @param retVal
     * @param value
     * @return
     */
    protected Boolean checkStringCriteria(final String operator, String operand, String value) {
        Boolean retVal = null;
        String effectiveOperand, effectiveValue;

        effectiveValue = value;
        if (value == null) {
            effectiveValue = "";
        } else if (value.startsWith(".")) {
            effectiveValue = value.substring(1);
        }

        // Bug 2178 -- if this is a regex match, a leading "." in the operand
        // should not be stripped
        effectiveOperand = operand;
        if (operand.startsWith(".") && !MATCHES.equals(operator)) {
            effectiveOperand = operand.substring(1);
        }

        if (EQUALS.equals(operator)) {
            retVal = Boolean.valueOf(effectiveOperand.equals(effectiveValue));
        } else if (NOT_EQUAL.equals(operator)) {
            retVal = Boolean.valueOf(!effectiveOperand.equals(effectiveValue));
        } else if (MATCHES.equals(operator)) {
            retVal = Boolean.valueOf(Pattern.compile(effectiveOperand).matcher(effectiveValue).find());
        }

        return retVal;
    }

    /**
     * @param result
     * @param operator
     * @param operand
     * @param retVal
     * @return
     */
    protected Boolean isCriteriaNull(Object result, String operator, String operand) {

        if (result == null)
            return Boolean.FALSE;
        if (operator == null || operand == null) {
            return Boolean.TRUE;
        } else {
            return null;
        }
    }

    @Override
    public void setInterfaceToPoll(OnmsIpInterface interfaceToPoll) {
        m_iface = interfaceToPoll;
    }

    @Override
    public OnmsIpInterface getInterfaceToPoll() {
        return m_iface;
    }

    @Override
    public void setPackage(Package pkg) {
        m_package = pkg;
    }

    @Override
    public Package getPackage() {
        return m_package;
    }

    @Override
    public void setPropertyMap(Map<String, String> parameters) {
        m_parameters = parameters;
    }

    @Override
    public Map<String, String> getPropertyMap() {
        return m_parameters;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    @Override
    public void setAccessPointDao(AccessPointDao accessPointDao) {
        m_accessPointDao = accessPointDao;
    }

    @Override
    public AccessPointDao getAccessPointDao() {
        return m_accessPointDao;
    }
};
