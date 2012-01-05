package org.opennms.netmgt.provision.service.vmware;

import com.vmware.vim25.CustomFieldDef;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.CustomFieldValue;
import com.vmware.vim25.mo.*;
import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: indigo
 * Date: 1/4/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class VMwareRequisitionUrlConnection extends URLConnection
{
    private Logger logger = LoggerFactory.getLogger(VMwareRequisitionUrlConnection.class);
    
    private static final String QUERY_ARG_SEPARATOR = "&";

    /** Constant <code>URL_SCHEME="vmware://"</code> */
    public static final String URL_SCHEME = "vmware://";

    /** Constant <code>PROTOCOL="dns"</code> */
    public static final String PROTOCOL = "vmware";
    
    private String m_hostname = null;
    
    private String m_foreignSource = null;

    private static Map<String, String> m_args = null;

    private ServiceInstance m_serviceInstance = null;

    private Requisition m_requisition = null;

    protected VMwareRequisitionUrlConnection(URL url) throws MalformedURLException, RemoteException {
        super(url);
        m_args = getUrlArgs(url);
        m_hostname = url.getHost();
        // TODO indigo: not so nice ;)
        String username = url.getAuthority().split("@")[0].split(":")[0];
        String password = url.getAuthority().split("@")[0].split(":")[1];

        m_serviceInstance = new ServiceInstance(new URL("https://" + m_hostname + "/sdk"), username, password, true);

        m_foreignSource = "vmware-" + m_hostname;
    }

    @Override
    public void connect() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Map<String, String> getUrlArgs(URL url) {

        if (url.getQuery() == null) {
            return new HashMap<String, String>();
        }

        //TODO: need to throw exception if query is null
        String query = decodeQueryString(url);

        //TODO: need to handle exception
        List<String> queryArgs = tokenizeQueryArgs(query);
        Map<String, String> args = new HashMap<String, String>();
        for (String queryArg : queryArgs) {
            String[] argTokens = StringUtils.split(queryArg, '=');

            if (argTokens.length < 2) {
                logger.warn("getUrlArgs: syntax error in URL query string, missing '=' in query argument: '{}'", queryArg);
            } else {
                logger.debug("adding arg tokens '{}', '{}'", argTokens[0].toLowerCase(), argTokens[1]);
                args.put(argTokens[0].toLowerCase(), argTokens[1]);
            }
        }
        return args;
    }
    
    private RequisitionNode createRequisitionNode (String ip, ManagedEntity managedEntity) {
        RequisitionNode rnode = new RequisitionNode();
        rnode.setNodeLabel(managedEntity.getName());
        rnode.setForeignId(m_hostname+"/"+managedEntity.getMOR().getVal());
        RequisitionInterface rint = new RequisitionInterface();
        rint.setIpAddr(ip);
        rint.setSnmpPrimary("P");
        rint.setManaged(Boolean.TRUE);
        rint.setStatus(Integer.valueOf(1));
        rnode.putInterface(rint);
        return rnode;
    }

    private Requisition buildVMwareRequisition () throws UnknownHostException, RemoteException {
        //for now, set the foreign source to the specified vcenter host
        m_requisition = new Requisition(getForeignSource());

        importHostSystems();
        importVirtualMachines();

        return m_requisition;
    }

    /**
     * {@inheritDoc}
     *
     * Creates a ByteArrayInputStream implementation of InputStream of the XML marshaled version
     * of the Requisition class.  Calling close on this stream is safe.
     */
    @Override
    public InputStream getInputStream() throws IOException {

        InputStream stream = null;

        try {
            Requisition r = buildVMwareRequisition();
            stream = new ByteArrayInputStream(jaxBMarshal(r).getBytes());
        } catch (IOException e) {
            logger.warn("getInputStream: Problem getting input stream: '{}'", e);
            throw e;
        } catch (Throwable e) {
            logger.warn("Problem getting input stream: '{}'", e);
            throw new IOExceptionWithCause("Problem getting input stream: "+e,e );
        }

        return stream;
    }

    /**
     * Utility to marshal the Requisition class into XML.
     *
     * @param r
     * @return a String of XML encoding the Requisition class
     *
     * @throws javax.xml.bind.JAXBException
     */
    private String jaxBMarshal(Requisition r) throws JAXBException {
        return JaxbUtils.marshal(r);
    }


    /**
     * <p>decodeQueryString</p>
     *
     * @param url a {@link java.net.URL} object.
     * @return a {@link java.lang.String} object.
     */
    protected String decodeQueryString(URL url) {
        if (url == null || url.getQuery() == null) {
            throw new IllegalArgumentException("The URL or the URL query is null: "+url);
        }

        String query = null;
        try {
            query = URLDecoder.decode(url.getQuery(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("decodeQueryString: '{}'", e);
        }

        return query;
    }

    private List<String> tokenizeQueryArgs(String query) throws IllegalArgumentException {

        if (query == null) {
            throw new IllegalArgumentException("The URL query is null");
        }

        return Arrays.asList(StringUtils.split(query, QUERY_ARG_SEPARATOR));
    }

    public Map<String, String> getArgs() {
        return m_args;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    private void importHostSystems() throws RemoteException, UnknownHostException {
        ManagedEntity[] hostSystems;

        hostSystems = new InventoryNavigator(m_serviceInstance.getRootFolder()).searchManagedEntities("HostSystem");

        if (hostSystems != null) {

            for (int i = 0; i < hostSystems.length; i++) {
                HostSystem hostSystem = (HostSystem) hostSystems[i];

                if (checkForAttribute(hostSystem)) {
                    logger.debug("Adding Host System '{}'", hostSystem.getName());
                    RequisitionNode node = createRequisitionNode(hostSystem.getName(), hostSystem);
                    m_requisition.insertNode(node);
                }
            }
        }
    }

    private void importVirtualMachines() throws RemoteException, UnknownHostException {
        ManagedEntity[] virtualMachines;

        virtualMachines = new InventoryNavigator(m_serviceInstance.getRootFolder()).searchManagedEntities("VirtualMachine");

        if (virtualMachines != null) {
            for (int i = 0; i < virtualMachines.length; i++) {
                VirtualMachine virtualMachine = (VirtualMachine) virtualMachines[i];
                if (checkForAttribute(virtualMachine)) {
                    logger.debug("Adding Virtual Machine '{}'", virtualMachine.getName());
                    RequisitionNode node = createRequisitionNode(virtualMachine.getGuest().getIpAddress(), virtualMachine);
                    m_requisition.insertNode(node);
                }
            }
        }
    }

    private boolean checkForAttribute(ManagedEntity managedEntity) throws RemoteException {
        String key = getArgs().get("key");
        String value = getArgs().get("value");

        if (key == null && value == null)
            return true;

        if (key == null || value == null)
            return false;

        CustomFieldValue[] values = managedEntity.getCustomValue();
        CustomFieldDef[] defs = managedEntity.getAvailableField();

        for (int i = 0; defs != null && i < defs.length; i++) {
            if (key.equals(defs[i].getName())) {

                int targetIndex = defs[i].getKey();

                for (int j = 0; j < values.length; j++) {
                    if (targetIndex == values[j].getKey()) {
                        return value.equals(((CustomFieldStringValue)values[j]).value);
                    }
                }
            }
        }
        return false;
    }
}
