package org.opennms.netmgt.config;

import java.io.IOException;

import java.util.List;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * <p>SnmpInterfacePollerConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpInterfacePollerConfig {
    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads();
    /**
     * <p>getService</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getService();
    /**
     * <p>getCriticalServiceIds</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getCriticalServiceIds();
    /**
     * <p>getAllPackageMatches</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> getAllPackageMatches(String ipaddr);
    /**
     * <p>getPackageName</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName(String ipaddr);
    /**
     * <p>getInterfaceOnPackage</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getInterfaceOnPackage(String pkgName);
    /**
     * <p>getStatus</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean getStatus(String pkgName,String pkgInterfaceName);
    /**
     * <p>getInterval</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a long.
     */
    public long getInterval(String pkgName,String pkgInterfaceName);
    /**
     * <p>getCriteria</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getCriteria(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasPort</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasPort(String pkgName,String pkgInterfaceName);
    /**
     * <p>getPort</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getPort(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasTimeout</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasTimeout(String pkgName,String pkgInterfaceName);
    /**
     * <p>getTimeout</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getTimeout(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasRetries</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasRetries(String pkgName,String pkgInterfaceName);
    /**
     * <p>getRetries</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getRetries(String pkgName,String pkgInterfaceName);
    /**
     * <p>hasMaxVarsPerPdu</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    /**
     * <p>getMaxVarsPerPdu</p>
     *
     * @param pkgName a {@link java.lang.String} object.
     * @param pkgInterfaceName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    /**
     * <p>rebuildPackageIpListMap</p>
     */
    public void rebuildPackageIpListMap();
    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException;
}
