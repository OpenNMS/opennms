/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareDatacollectionConfig;
import org.opennms.netmgt.dao.VmwareDatacollectionConfigDao;
import org.opennms.netmgt.model.RrdRepository;

import java.io.File;
import java.util.List;

/**
 * The Class DefaultVmwareDatacollectionConfigDao
 * <p/>
 * This class is used for accessing the Vmware Data Collection configuration file
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareDatacollectionConfigDaoJaxb extends AbstractJaxbConfigDao<VmwareDatacollectionConfig, VmwareDatacollectionConfig> implements VmwareDatacollectionConfigDao {

    /**
     * Default constructor
     */
    public VmwareDatacollectionConfigDaoJaxb() {
        super(VmwareDatacollectionConfig.class, "Vmware Data Collection Configuration");
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    @Override
    public VmwareDatacollectionConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * Used to transform the config object to a custom representation. This method is not modified in this class, it just
     * returns the config object itself.
     *
     * @param jaxbConfig a config object.
     * @return a custom object
     */
    @Override
    public VmwareDatacollectionConfig translateConfig(VmwareDatacollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

    /**
     * This method returns a subset of the configuration data for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the collection object
     */
    @Override
    public VmwareCollection getVmwareCollection(String collectionName) {
        VmwareCollection[] collections = getConfig().getVmwareCollection();
        VmwareCollection collection = null;
        for (VmwareCollection coll : collections) {
            if (coll.getName().equalsIgnoreCase(collectionName)) {
                collection = coll;
                break;
            }
        }
        if (collection == null) {
            throw new IllegalArgumentException("getVmwareCollection: collection name: "
                    + collectionName + " specified in collectd configuration not found in Vmware collection configuration.");
        }
        return collection;
    }

    /**
     * Returns the Rrd repository for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the repository
     */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
    }

    /**
     * Used to retrieve the Rrd's step parameter for a given collection name.
     *
     * @param cName the collection's name
     * @return the step value
     */
    private int getStep(String cName) {
        VmwareCollection collection = getVmwareCollection(cName);
        if (collection != null) {
            return collection.getRrd().getStep();
        } else {
            return -1;
        }
    }

    /**
     * Returns the RRAs for a given collection name.
     *
     * @param cName the collection's name
     * @return the RRAs list
     */
    private List<String> getRRAList(String cName) {
        VmwareCollection collection = getVmwareCollection(cName);
        if (collection != null) {
            return collection.getRrd().getRraCollection();
        } else {
            return null;
        }

    }

    /**
     * Returns the base Rrd's path.
     *
     * @return the Rrd's path
     */
    @Override
    public String getRrdPath() {
        String rrdPath = getConfig().getRrdRepository();
        if (rrdPath == null) {
            throw new RuntimeException("Configuration error, failed to "
                    + "retrieve path to RRD repository.");
        }

        if (rrdPath.endsWith(File.separator)) {
            rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
        }

        return rrdPath;
    }
}
