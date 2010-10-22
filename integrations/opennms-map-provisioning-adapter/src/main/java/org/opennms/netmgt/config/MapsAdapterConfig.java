package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;

/**
 * <p>MapsAdapterConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface MapsAdapterConfig {

    /**
     * <p>getMapElementDimension</p>
     *
     * @return a int.
     */
    public int getMapElementDimension();
    /**
     * <p>getAllMaps</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Cmap> getAllMaps();    
    /**
     * <p>getSubMaps</p>
     *
     * @param mapName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Csubmap> getSubMaps(String mapName);
    /**
     * <p>getContainerMaps</p>
     *
     * @param submapName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String,Csubmap> getContainerMaps(String submapName);
    /**
     * <p>getsubMaps</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, List<Csubmap>> getsubMaps();
    /**
     * <p>getElementByAddress</p>
     *
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Celement> getElementByAddress(String ipaddr);
    /**
     * <p>getCelements</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, List<Celement>> getCelements();
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

    public Lock getReadLock();
    
    public Lock getWriteLock();
}
