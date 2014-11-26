/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.BasePersister;
import org.opennms.netmgt.collection.persistence.rrd.GroupPersister;
import org.opennms.netmgt.collection.persistence.rrd.OneToOnePersister;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.FileAnticipator;

public abstract class CollectorTestUtils {

    static CollectionSpecification createCollectionSpec(String svcName, ServiceCollector svcCollector, String collectionName) {
        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        Parameter collectionParm = new Parameter();
        collectionParm.setKey("collection");
        collectionParm.setValue(collectionName);
        service.addParameter(collectionParm);
        pkg.addService(service);

        CollectionSpecification spec = new CollectionSpecification(pkg, svcName, svcCollector, new DefaultCollectdInstrumentation());
        return spec;
    }

    public static void persistCollectionSet(CollectionSpecification spec, CollectionSet collectionSet) {
        RrdRepository repository=spec.getRrdRepository("default");
        System.err.println("repository = " + repository);
        ServiceParameters params = spec.getServiceParameters();
        System.err.println("service parameters = " + params);
        BasePersister persister;
        if (Boolean.getBoolean("org.opennms.rrd.storeByGroup")) {
            persister=new GroupPersister(params, repository);
        } else {
            persister=new OneToOnePersister(params, repository);
        }
        System.err.println("persister = " + persister);
        collectionSet.visit(persister);
    }

    public static void collectNTimes(CollectionSpecification spec, CollectionAgent agent, int numUpdates)
    throws InterruptedException, CollectionException {
        for(int i = 0; i < numUpdates; i++) {
    
            // now do the actual collection
            CollectionSet collectionSet = spec.collect(agent);
            assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
            
            persistCollectionSet(spec, collectionSet);
        
            System.err.println("COLLECTION "+i+" FINISHED");
        
            //need a one second time elapse to update the RRD
            Thread.sleep(1010);
        }
    }

    public static void failToCollectNTimes(CollectionSpecification spec, CollectionAgent agent, int numUpdates)
    throws InterruptedException, CollectionException {
        for(int i = 0; i < numUpdates; i++) {

            // now do the actual collection
            CollectionSet collectionSet = spec.collect(agent);
            assertEquals("collection status", ServiceCollector.COLLECTION_FAILED, collectionSet.getStatus());

            persistCollectionSet(spec, collectionSet);

            System.err.println("COLLECTION "+i+" FINISHED");

            //need a one second time elapse to update the RRD
            Thread.sleep(1010);
        }
    }

    public static File anticipatePath(FileAnticipator fa, File rootDir, String... pathElements) {
        File parent = rootDir;
        assertTrue(pathElements.length > 0);
        for (String pathElement : pathElements) {
            parent = fa.expecting(parent, pathElement);
        }
        return parent;
    }

    public static String rrd(String file) {
        return file + RrdUtils.getExtension();
    }
}
