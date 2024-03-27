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
package org.opennms.netmgt.collection.persistence.newts;

import java.util.Objects;

import javax.inject.Inject;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.newts.NewtsWriter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.newts.api.Context;

/**
 * Factory for {@link org.opennms.netmgt.collection.persistence.newts.NewtsPersister}.
 *
 * @author jwhite
 */
public class NewtsPersisterFactory implements PersisterFactory {

    private final NewtsWriter m_newtsWriter;

    private final Context m_context;

    @Inject
    public NewtsPersisterFactory(Context context, NewtsWriter newtsWriter) {
        m_context = Objects.requireNonNull(context);
        m_newtsWriter = newtsWriter;
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        // We ignore the forceStoreByGroup flag since we always store by group, and we ignore
        // the dontReorderAttributes flag since attribute order does not matter
        NewtsPersister persister =  new NewtsPersister(params, repository, m_newtsWriter, m_context);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }
}
