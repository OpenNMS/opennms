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
package org.opennms.netmgt.provision.service;

import org.apache.http.client.utils.URIBuilder;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.Assert;

import java.net.URI;

/**
 * Wrapper object for the doImport method of the Provisioner
 *
 * @author ranger
 * @version $Id: $
 */
public class ImportJob implements Job {
    
    private Provisioner provisioner;

    /** Constant <code>URL="url"</code> */
    protected static final String URL = "url";

    public static final String URI_SCHEME = "requisition";
    
    /** Constant <code>RESCAN_EXISTING="rescanExisting"</code> */
    protected static final String RESCAN_EXISTING = "rescanExisting";

    /** Constant <code>MONITOR="monitor"</code> */
    protected static final String MONITOR = "monitor";

    private ProvisionMonitor provisionMonitor;

    private EntityScopeProvider entityScopeProvider;

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String url = Interpolator.interpolate(context.getJobDetail().getJobDataMap().getString(URL), entityScopeProvider.getScopeForScv()).output;
            Assert.notNull(url);
            Assert.notNull(provisionMonitor);
            String rescanExisting = context.getJobDetail().getJobDataMap().getString(RESCAN_EXISTING);
            URI inputUri = new URI(url);
            String scheme = inputUri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                url = new URIBuilder()
                        .setScheme(URI_SCHEME)
                        .setHost(scheme)
                        .addParameter(URL, url)
                        .build()
                        .toString();
            }
            getProvisioner().doImport(url, rescanExisting == null ? Boolean.TRUE.toString() : rescanExisting, provisionMonitor);
        } catch (Exception t) {
            throw new JobExecutionException(t);
        }
    }
    
    /**
     * <p>setProvisioner</p>
     *
     * @param provisioner a {@link org.opennms.netmgt.provision.service.Provisioner} object.
     */
    public void setProvisioner(Provisioner provisioner) {
        this.provisioner = provisioner;
    }

    Provisioner getProvisioner() {
        return provisioner;
    }

    public void setMonitor(ProvisionMonitor provisionMonitor) {
        this.provisionMonitor = provisionMonitor;
    }

    public EntityScopeProvider getEntityScopeProvider() {
        return entityScopeProvider;
    }

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }
}
