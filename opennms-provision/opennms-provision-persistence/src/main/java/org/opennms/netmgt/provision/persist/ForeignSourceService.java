package org.opennms.netmgt.provision.persist;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;

public interface ForeignSourceService {

    void setDeployedForeignSourceRepository(ForeignSourceRepository repo);
    void setPendingForeignSourceRepository(ForeignSourceRepository repo);

    Set<ForeignSource> getAllForeignSources();

    ForeignSource getForeignSource(String name);
    ForeignSource saveForeignSource(String name, ForeignSource fs);
    ForeignSource cloneForeignSource(String name, String target);
    void          deleteForeignSource(String name);

    ForeignSource deletePath(String foreignSourceName, String dataPath);
    ForeignSource addParameter(String foreignSourceName, String dataPath);

    ForeignSource addDetectorToForeignSource(String foreignSource, String name);
    ForeignSource deleteDetector(String foreignSource, String name);

    ForeignSource addPolicyToForeignSource(String foreignSource, String name);
    ForeignSource deletePolicy(String foreignSource, String name);

    Map<String,String> getDetectorTypes();
    Map<String,String> getPolicyTypes();
    Map<String,PluginWrapper> getWrappers();
    
}
