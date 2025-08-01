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
package org.opennms.netmgt.model;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>ResourceTypeUtils class.</p>
 */
public abstract class ResourceTypeUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypeUtils.class);

    /**
     * Default folder where RRD files are stored.
     *
     * Working directory defaults to $OPENNMS_HOME, so we can find these in $OPENNMS_HOME/share/rrd.
     */
    public static final File DEFAULT_RRD_ROOT = Paths.get("share", "rrd").toAbsolutePath().toFile();

    /**
     * Directory name of where latency data is stored.
     */
    public static final String RESPONSE_DIRECTORY = "response";

    /**
     * Directory name of where status data is stored.
     */
    public static final String STATUS_DIRECTORY = "status";

    /**
     * Directory name of where all other collected data is stored.
     */
    public static final String SNMP_DIRECTORY = "snmp";

    /**
     * Directory name of where stored-by-foreign-source data is stored.
     */
    public static final String FOREIGN_SOURCE_DIRECTORY = "fs";

    private static final Pattern s_responseDirectoryPattern =  Pattern.compile("^" + RESPONSE_DIRECTORY + ".+$");
    private static final Pattern s_statusDirectoryPattern =  Pattern.compile("^" + STATUS_DIRECTORY + ".+$");

    /**
     * <p>isStoreByGroup</p>
     *
     * @return a boolean.
     */
    public static boolean isStoreByGroup() {
        return Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }

    /**
     * <p>isStoreByForeignSource</p>
     *
     * @return a boolean.
     */
    public static boolean isStoreByForeignSource() {
        return Boolean.getBoolean("org.opennms.rrd.storeByForeignSource");
    }

    /**
     * <p>isResponseTime</p>
     *
     * @param relativePath a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isResponseTime(String relativePath) {
        return s_responseDirectoryPattern.matcher(relativePath).matches();
    }

    public static boolean isStatus(String relativePath) {
        return s_statusDirectoryPattern.matcher(relativePath).matches();
    }

    /**
     * 
     * @param nodeSource a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getRelativeNodeSourceDirectory(String nodeSource) {
        String[] ident = getFsAndFidFromNodeSource(nodeSource);
        return new File(FOREIGN_SOURCE_DIRECTORY, File.separator + ident[0] + File.separator + ident[1]);
    }

    public static String[] getFsAndFidFromNodeSource(String nodeSource) {
        final String[] ident = nodeSource.split(":", 2);
        if (!(ident.length == 2)) {
            LOG.warn("'{}' is not in the format foreignSource:foreignId.", nodeSource);
            throw new IllegalArgumentException("Node definition '" + nodeSource + "' is invalid, it should be in the format: 'foreignSource:foreignId'.");
        }
        return ident;
    }

    /**
     * Convenience method for retrieving the OnmsNode entity from
     * an abstract resource.
     *
     * @throws ObjectRetrievalFailureException on failure
     */
    public static OnmsNode getNodeFromResource(OnmsResource resource) {
        // Null check
        if (resource == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource must be non-null.");
        }

        // Grab the entity
        final OnmsEntity entity = resource.getEntity();
        if (entity == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource entity must be non-null: " + resource);
        }

        // Type check
        if (!(entity instanceof OnmsNode)) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource entity must be an instance of OnmsNode: " + resource);
        }

        return (OnmsNode)entity;
    }

    /**
     * Convenience method for retrieving the OnmsNode entity from
     * an abstract resource's ancestor.
     *
     * @throws ObjectRetrievalFailureException on failure
     */
    public static OnmsNode getNodeFromResourceRoot(final OnmsResource resource) {
        OnmsResource res = resource;
        while (res != null && res.getParent() != null) {
            res = res.getParent();
        }

        // Null check
        if (res == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource must be non-null.");
        }

        // Grab the entity
        final OnmsEntity entity = res.getEntity();
        if (entity == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource entity must be non-null: " + resource);
        }

        // Type check
        if (!(entity instanceof OnmsNode)) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, "Resource entity must be an instance of OnmsNode: " + resource);
        }

        return (OnmsNode)entity;
    }

    /**
     * Retrieves the ResourcePath relative to rrd.base.dir.
     */
    public static ResourcePath getResourcePathWithRepository(RrdRepository repository, ResourcePath resource) {
        // Here we just assume that the repository dir is of the form ${rrd.base.dir}/snmp or ${rrd.base.dir}/response
        // since all of operations in the ResourceDao assume that the resources are stored in these paths
        return ResourcePath.get(ResourcePath.get(repository.getRrdBaseDir().getName()), resource);
    }

    /**
     * Returns the number of elements that correspond to the "node resource" for the given path.
     * @param path resource path
     * @return number of elements or -1 if the given path does not map to a node
     */
    public static int getNumPathElementsToNodeLevel(ResourcePath path) {
        final String[] elements = path.elements();
        if (elements == null) return -1;
        if (elements.length >= 2
                && ResourceTypeUtils.SNMP_DIRECTORY.equals(elements[0])
                && !ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(elements[1])) {
            return 2;
        } else if (elements.length >= 4
                && ResourceTypeUtils.SNMP_DIRECTORY.equals(elements[0])
                && ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(elements[1])) {
            return 4;
        }
        return -1;
    }
}
