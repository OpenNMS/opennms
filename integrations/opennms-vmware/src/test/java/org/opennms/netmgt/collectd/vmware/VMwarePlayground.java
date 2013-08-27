package org.opennms.netmgt.collectd.vmware;

import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.service.vmware.VmwareRequisitionUrlConnection;

import com.vmware.vim25.CustomFieldDef;
import com.vmware.vim25.CustomFieldStringValue;
import com.vmware.vim25.CustomFieldValue;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class VMwarePlayground {

    @Before
    @SuppressWarnings("unused")
    public void setUp() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public boolean isServerTrusted(X509Certificate[] certs) {
                return true;
            }
            public boolean isClientTrusted(X509Certificate[] certs) {
                return true;
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception exception) {
            Assert.fail("Error setting relaxed SSL policy " + exception.getMessage());
        }
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }

    @Test
    public void testApi() throws Exception {
        ServiceInstance serviceInstance = new ServiceInstance(new URL("https://192.168.32.103/sdk"), "opennms", "opennms");
        ManagedEntity[] hosts = getEntities(serviceInstance, "HostSystem");
        for (ManagedEntity entity : hosts) {
            System.out.println("ESX: " + entity.getName());
            printAttributes(entity);
        }
        ManagedEntity[] vms = getEntities(serviceInstance, "VirtualMachine");
        for (ManagedEntity entity : vms) {
            System.out.println("VM: " + entity.getName());
            printAttributes(entity);
        }
        serviceInstance.getServerConnection().logout();
    }
    
    @Test
    public void testProvisioner() throws Exception {
        GenericURLFactory.initialize();
        VmwareRequisitionUrlConnection c = new VmwareRequisitionUrlConnection(new URL("vmware://192.168.32.103/VMWare-JUnit"));
        Requisition req = JaxbUtils.unmarshal(Requisition.class, new InputStreamReader(c.getInputStream()));
        System.out.println(req);
    }

    private void printAttributes(ManagedEntity entity) throws Exception {
        Map<String,String> attribMap = getCustomAttributes(entity);
        
        
        String attrib = "_onmsMonitoring";
        String value = "true";
        boolean ok = true;
        for (Entry<String,String> entry : attribMap.entrySet()) {
            System.out.println("    " + entry.getKey() + " = " + entry.getValue());
            String attribValue = attribMap.get(StringUtils.removeStart(attrib, "_"));
            if (attribValue != null) {
                System.out.println("checking if " + attribValue + " matches " + StringUtils.removeStart(value, "~"));
                if (value.startsWith("~")) {
                    System.out.println("match? " + attribValue.matches(StringUtils.removeStart(value, "~")));
                    ok = ok && attribValue.matches(StringUtils.removeStart(value, "~"));
                } else {
                    ok = ok && attribValue.equals(value);
                }
            }
        }
        System.out.println("> ok? " + ok);
    }

    private ManagedEntity[] getEntities(ServiceInstance serviceInstance, String type) throws Exception {
        final ManagedEntity[] hosts = (new InventoryNavigator(serviceInstance.getRootFolder())).searchManagedEntities(type);
        return hosts;
    }

    private Map<String,String> getCustomAttributes(ManagedEntity entity) throws Exception {
        final Map<String,String> attributes = new TreeMap<String,String>();
        CustomFieldDef[] defs = entity.getAvailableField();
        CustomFieldValue[] values = entity.getCustomValue();
        for (int i = 0; defs != null && i < defs.length; i++) {
            String key = defs[i].getName();
            int targetIndex = defs[i].getKey();
            for (int j = 0; j < values.length; j++) {
                if (targetIndex == values[j].getKey()) {
                    attributes.put(key, ((CustomFieldStringValue) values[j]).getValue());
                }
            }
        }
        return attributes;
    }
}
