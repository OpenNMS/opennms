package org.opennms.web.svclayer.support;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;

public interface ForeignSourceService {

    void setActiveForeignSourceRepository(ForeignSourceRepository repo);
    void setPendingForeignSourceRepository(ForeignSourceRepository repo);

    Set<ForeignSource> getAllForeignSources();

    ForeignSource getForeignSource(String name);
    ForeignSource saveForeignSource(String name, ForeignSource fs);
    ForeignSource deployForeignSource(String name);
    ForeignSource deleteForeignSource(String name);
    ForeignSource cloneForeignSource(String name, String target);

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
