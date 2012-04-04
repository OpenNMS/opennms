
package org.opennms.netmgt.netsuite;

import java.io.IOException;
import java.util.List;

public interface NetsuiteConfigDao {

    public String getUsername();
    public String getPassword();
    public List<String> getValidClosedStatus();
    public List<Integer> getValidOpenStatus();
    public List<String> getValidCancelledStatus();
    public String getOpenStatus();
    public String getClosedStatus();
    public String getCancelledStatus();
    public String getRequestor();
    public String getBaseURL();
    public int getTimeout();
    public int getRetry();

    public void save() throws IOException;
}
