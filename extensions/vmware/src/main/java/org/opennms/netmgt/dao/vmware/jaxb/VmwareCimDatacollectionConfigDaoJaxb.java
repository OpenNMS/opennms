/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.vmware.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.dao.vmware.VmwareCimDatacollectionConfigDao;
import org.opennms.netmgt.rrd.RrdRepository;

import java.io.File;
import java.util.List;

/**
 * The Class VmwareCimDatacollectionConfigDaoJaxb
 * <p/>
 * This class is used for accessing the Vmware Cim Data Collection configuration file
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareCimDatacollectionConfigDaoJaxb extends AbstractJaxbConfigDao<VmwareCimDatacollectionConfig, VmwareCimDatacollectionConfig> implements VmwareCimDatacollectionConfigDao {


    /**
     * Default constructor
     */
    public VmwareCimDatacollectionConfigDaoJaxb() {
        super(VmwareCimDatacollectionConfig.class, "Vmware Cim Data Collection Configuration");
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    @Override
    public VmwareCimDatacollectionConfig getConfig() {
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
    public VmwareCimDatacollectionConfig translateConfig(VmwareCimDatacollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

    /**
     * This method returns a subset of the configuration data for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the Cim collection object
     */
    @Override
    public VmwareCimCollection getVmwareCimCollection(String collectionName) {
        VmwareCimCollection[] collections = getConfig().getVmwareCimCollection();
        VmwareCimCollection collection = null;
        for (VmwareCimCollection coll : collections) {
            if (coll.getName().equalsIgnoreCase(collectionName)) {
                collection = coll;
                break;
            }
        }
        if (collection == null) {
            throw new IllegalArgumentException("getVmwareCimCollection: collection name: "
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
        VmwareCimCollection collection = getVmwareCimCollection(cName);
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
        VmwareCimCollection collection = getVmwareCimCollection(cName);
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
