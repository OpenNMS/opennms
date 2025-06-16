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
package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.ValidationException;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractForeignSourceRepository.class);

    /**
     * <p>Constructor for AbstractForeignSourceRepository.</p>
     */
    public AbstractForeignSourceRepository() {
    }

    /** {@inheritDoc} */
    @Override
    public Requisition importResourceRequisition(final Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);

        LOG.debug("importing requisition from {}", stripCredentials(resource));
        final Requisition requisition = JaxbUtils.unmarshal(Requisition.class, resource);
        requisition.setResource(resource);
        save(requisition);
        return requisition;
    }

    static String stripCredentials(final Object object) {
        if (object == null) {
            return null;
        } else {
            return String.valueOf(object).replaceAll("(username=)[^;&]*(;&)?", "$1***$2")
                                         .replaceAll("(password=)[^;&]*(;&)?", "$1***$2");
        }
    }

    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        Resource defaultForeignSource = new ClassPathResource("/default-foreign-source.xml");
        if (!defaultForeignSource.exists()) {
            defaultForeignSource = new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml");
        }
        final ForeignSource fs = JaxbUtils.unmarshal(ForeignSource.class, defaultForeignSource);
        fs.setDefault(true);
        return fs;
    }

    /** {@inheritDoc} */
    @Override
    public void putDefaultForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setName("default");

        final File outputFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        Writer writer = null;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            JaxbUtils.marshal(foreignSource, writer);
        } catch (final Throwable e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * <p>resetDefaultForeignSource</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        final File deleteFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.delete()) {
            LOG.warn("unable to remove {}", deleteFile.getPath());
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }

    @Override
    public void validate(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        final String name = foreignSource.getName();
        if (name.contains("/")) {
            throw new ForeignSourceRepositoryException("Foreign Source (" + name + ") contains invalid characters. ('/' is forbidden.)");
        }
    }

    @Override
    public void validate(final Requisition requisition) throws ForeignSourceRepositoryException {
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            throw new ForeignSourceRepositoryException(e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws ForeignSourceRepositoryException {
        for (final Requisition req : getRequisitions()) {
            if (req != null) delete(req);
        }
        for (final ForeignSource fs : getForeignSources()) {
            if (fs != null) delete(fs);
        }
    }
}
