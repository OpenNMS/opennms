/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.minion.container.karaft.ext;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.karaf.config.core.ConfigMBean;
import org.apache.karaf.features.management.FeaturesServiceMBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows Maven repositories, feature repositories and features to boot
 * to be extended using a .d style configuration format.
 *
 * @author jwhite
 */
public class KarafExtender implements BundleActivator, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(KarafExtender.class);
    private static final String KARAF_INSTANCE_NAME = "root";
    private static final String PAX_MVN_PID = "org.ops4j.pax.url.mvn";
    private static final String PAX_MVN_REPOSITORIES = "org.ops4j.pax.url.mvn.repositories";

    private Thread thread = null;

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        // We may need to wait for particular MBean to be available,
        // so we do this in a background thread order in to avoid
        // locking up any other features from loading
        thread = new Thread(this);
        thread.start();
        LOG.info("Started.");
    }

    @Override
    public void run() {
        FeaturesServiceMBean featuresService;
        ConfigMBean config;
        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted. Aborting.", e);
                return;
            }

            try {
                featuresService = getFeaturesServiceMBean();
                featuresService.getRepositories();
            } catch (Throwable t) {
                LOG.debug("FeaturesServiceMBean failed. Waiting to retry.", t);
                continue;
            }

            try {
                config = getConfigMBean();
                config.listProperties(PAX_MVN_PID);
            } catch (Throwable t) {
                LOG.debug("ConfigMBean failed. Waiting to retry.", t);
                continue;
            }

            // We were able to successfully query both MBeans
            break;
        }

        List<FeaturePack> featurePacks;
        try {
            featurePacks = getFeaturePacks();
        } catch (IOException e) {
            LOG.error("Failed to retrieve the list of feature packs. Aborting.", e);
            return;
        }

        List<String> featuresBoot;
        try {
            featuresBoot = getFeaturesBoot();
        } catch (IOException e) {
            LOG.error("Failed to retrieve the list of features to boot. Aborting.", e);
            return;
        }

        // Build a comma separated list of our Maven repositories
        StringBuilder mavenReposSb = new StringBuilder();
        for (FeaturePack featurePack : featurePacks) {
            if (mavenReposSb.length() != 0) {
                mavenReposSb.append(",");
            }
            mavenReposSb.append(featurePack.mavenRepo);
        }

        LOG.info("Updating Maven repositories to include: {}", mavenReposSb);
        try {
            Map<String, String> props = config.listProperties(PAX_MVN_PID);
            props.put(PAX_MVN_REPOSITORIES, mavenReposSb.toString());
            config.update(PAX_MVN_PID, props);
        } catch (MBeanException e) {
            LOG.error("Failed to update the list of Maven repositories to '{}'. Aborting.",
                    mavenReposSb, e);
            return;
        }

        for (FeaturePack featurePack : featurePacks) {
            for (String feature : featurePack.features) {
                try {
                    LOG.info("Adding feature repository: {}", feature);
                    featuresService.addRepository(feature);
                    featuresService.refreshRepository(feature);
                } catch (Throwable t) {
                    LOG.error("Failed to add feature repository '{}'. Skipping.", feature, t);
                }
            }
        }

        for (String featureBoot : featuresBoot) {
            LOG.info("Installing feature: {}", featureBoot);
            try {
                featuresService.installFeature(featureBoot);
            } catch (Throwable t) {
                LOG.error("Failed to install feature '{}'. Skipping.", featureBoot, t);
            }
            
        }
    }

    private static FeaturesServiceMBean getFeaturesServiceMBean() throws MalformedObjectNameException {
        return getKarafMbean("feature", KARAF_INSTANCE_NAME, FeaturesServiceMBean.class);
    }

    private static ConfigMBean getConfigMBean() throws MalformedObjectNameException {
        return getKarafMbean("config", KARAF_INSTANCE_NAME, ConfigMBean.class);
    }

    private static <T> T getKarafMbean(String type, String instance, Class<T> clazz) throws MalformedObjectNameException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ObjectName mbeanName = new ObjectName(String.format("org.apache.karaf:type=%s,name=%s", type, instance));
        return JMX.newMBeanProxy(mbs, mbeanName, clazz, true);
    }

    private static class FeaturePack {
        final String mavenRepo;
        final List<String> features;
        
        public FeaturePack(String l) {
            String[] components = l.split(",");
            mavenRepo = components[0];
            features = new ArrayList<>();
            for (int i = 1; i < components.length; i++) {
                features.add(components[i]);
            }
        }
    }

    private static List<FeaturePack> getFeaturePacks() throws IOException {
        return getLinesFromFiles("featurepacks.d").stream()
                .map(l -> new FeaturePack(l)).collect(Collectors.toList());
    }
    
    private static List<String> getFeaturesBoot() throws IOException {
        return getLinesFromFiles("featuresboot.d");
    }

    private static List<String> getLinesFromFiles(String folder) throws IOException {
        final List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(System.getProperty("karaf.etc"), folder))) {
            for (Path path : directoryStream) {
                if (path.getFileName().startsWith(".")) {
                    // Ignore dot files
                    continue;
                }
                paths.add(path);
            }
        }

        Collections.sort(paths);

        final List<String> lines = new ArrayList<>();
        for (Path path : paths) {
            lines.addAll(Files.readAllLines(path));
        }
        return lines;
    }
    
    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
