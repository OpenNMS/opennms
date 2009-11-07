package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;


import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;

public interface MapsAdapterConfig {

    public int getMapElementDimension();
    public int getOperationNumberBeforeSync();
    public List<Cmap> getAllMaps();    
    public List<Csubmap> getSubMaps(String mapName);
    public Map<String,Csubmap> getContainerMaps(String submapName);
    public Map<String, List<Csubmap>> getsubMaps();
    public Map<String, Celement> getElementByAddress(String ipaddr);
    public Map<String, List<Celement>> getCelements();
    public void rebuildPackageIpListMap();
    public void update() throws IOException, MarshalException, ValidationException;
}
