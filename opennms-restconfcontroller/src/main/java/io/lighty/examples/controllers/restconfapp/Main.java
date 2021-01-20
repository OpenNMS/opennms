/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */
package io.lighty.examples.controllers.restconfapp;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lighty.core.common.models.YangModuleUtils;
import io.lighty.core.controller.api.LightyController;
import io.lighty.core.controller.api.LightyModule;
import io.lighty.core.controller.impl.LightyControllerBuilder;
import io.lighty.core.controller.impl.config.ConfigurationException;
import io.lighty.core.controller.impl.config.ControllerConfiguration;
import io.lighty.core.controller.impl.util.ControllerConfigUtils;
import io.lighty.modules.northbound.restconf.community.impl.CommunityRestConf;
import io.lighty.modules.northbound.restconf.community.impl.CommunityRestConfBuilder;
import io.lighty.modules.northbound.restconf.community.impl.config.RestConfConfiguration;
import io.lighty.modules.northbound.restconf.community.impl.util.RestConfConfigUtils;
// import io.lighty.modules.southbound.netconf.impl.NetconfTopologyPluginBuilder;
// import io.lighty.modules.southbound.netconf.impl.config.NetconfConfiguration;
// import io.lighty.modules.southbound.netconf.impl.util.NetconfConfigUtils;
import io.lighty.server.LightyServerBuilder;
import io.lighty.swagger.SwaggerLighty;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private ShutdownHook shutdownHook;

    private static final Set<YangModuleInfo> MY_MODELS = ImmutableSet.of(
            org.opendaylight.yang.gen.v1.urn.opennms.yang.vacuumd.rev200113.$YangModuleInfoImpl.getInstance()
    );

    public void start() {
        start(new String[] {}, false);
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void start(String[] args, boolean registerShutdownHook) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        LOG.info(".__  .__       .__     __              .__           _________________    _______");
        LOG.info("|  | |__| ____ |  |___/  |_ ___.__.    |__| ____    /   _____/\\______ \\   \\      \\");
        LOG.info("|  | |  |/ ___\\|  |  \\   __<   |  |    |  |/  _ \\   \\_____  \\  |    |  \\  /   |   \\");
        LOG.info("|  |_|  / /_/  >   Y  \\  |  \\___  |    |  (  <_> )  /        \\ |    `   \\/    |    \\");
        LOG.info("|____/__\\___  /|___|  /__|  / ____| /\\ |__|\\____/  /_______  //_______  /\\____|__  /");
        LOG.info("        /_____/     \\/      \\/      \\/                     \\/         \\/         \\/");
        LOG.info("Starting lighty.io RESTCONF example application ...");
        LOG.info("https://lighty.io/");
        LOG.info("https://github.com/PANTHEONtech/lighty");
        try {
                LOG.info("using default configuration with custom yang...");
                Set<YangModuleInfo> modelPaths = Stream.concat(RestConfConfigUtils.YANG_MODELS.stream(),
                        MY_MODELS.stream()).collect(Collectors.toSet());
                        // NetconfConfigUtils.NETCONF_TOPOLOGY_MODELS.stream()).collect(Collectors.toSet());
                ArrayNode arrayNode = YangModuleUtils
                        .generateJSONModelSetConfiguration(
                                Stream.concat(ControllerConfigUtils.YANG_MODELS.stream(), modelPaths.stream())
                                        .collect(Collectors.toSet())
                        );
                //0. print the list of schema context models
                LOG.info("JSON model config snippet: {}", arrayNode.toString());
                //1. get controller configuration
                ControllerConfiguration defaultSingleNodeConfiguration =
                        ControllerConfigUtils.getDefaultSingleNodeConfiguration(MY_MODELS);
                //2. get RESTCONF NBP configuration
                RestConfConfiguration restConfConfig =
                        RestConfConfigUtils.getDefaultRestConfConfiguration();
                startLighty(defaultSingleNodeConfiguration, restConfConfig, registerShutdownHook);
/*
            if (args.length > 0) {
                Path configPath = Paths.get(args[0]);
                LOG.info("using configuration from file {} ...", configPath);
                //1. get controller configuration
                ControllerConfiguration singleNodeConfiguration =
                        ControllerConfigUtils.getConfiguration(Files.newInputStream(configPath));
                //2. get RESTCONF NBP configuration
                RestConfConfiguration restConfConfiguration =
                        RestConfConfigUtils.getRestConfConfiguration(Files.newInputStream(configPath));
                //3. NETCONF SBP configuration
                NetconfConfiguration netconfSBPConfiguration =
                        NetconfConfigUtils.createNetconfConfiguration(Files.newInputStream(configPath));
                startLighty(singleNodeConfiguration, restConfConfiguration, netconfSBPConfiguration,
                        registerShutdownHook);
            } else {
                LOG.info("using default configuration ...");
                Set<YangModuleInfo> modelPaths = Stream.concat(RestConfConfigUtils.YANG_MODELS.stream(),
                        NetconfConfigUtils.NETCONF_TOPOLOGY_MODELS.stream()).collect(Collectors.toSet());
                ArrayNode arrayNode = YangModuleUtils
                        .generateJSONModelSetConfiguration(
                                Stream.concat(ControllerConfigUtils.YANG_MODELS.stream(), modelPaths.stream())
                                        .collect(Collectors.toSet())
                        );
                //0. print the list of schema context models
                LOG.info("JSON model config snippet: {}", arrayNode.toString());
                //1. get controller configuration
                ControllerConfiguration defaultSingleNodeConfiguration =
                        ControllerConfigUtils.getDefaultSingleNodeConfiguration(modelPaths);
                //2. get RESTCONF NBP configuration
                RestConfConfiguration restConfConfig =
                        RestConfConfigUtils.getDefaultRestConfConfiguration();
                //3. NETCONF SBP configuration
                NetconfConfiguration netconfSBPConfig = NetconfConfigUtils.createDefaultNetconfConfiguration();
                startLighty(defaultSingleNodeConfiguration, restConfConfig, netconfSBPConfig, registerShutdownHook);
            }
            */
            LOG.info("lighty.io and RESTCONF-NETCONF started in {}", stopwatch.stop());
        /* } catch (IOException cause) {
            LOG.error("Main RESTCONF-NETCONF application - error reading config file: ", cause); */
        } catch (ConfigurationException | ExecutionException | InterruptedException cause) {
            LOG.error("Main RESTCONF-NETCONF application exception: ", cause);
        }
    }

    private void startLighty(ControllerConfiguration controllerConfiguration,
                             RestConfConfiguration restConfConfiguration,
                             boolean registerShutdownHook)
            throws ConfigurationException, ExecutionException, InterruptedException {

        //1. initialize and start Lighty controller (MD-SAL, Controller, YangTools, Akka)
        LightyControllerBuilder lightyControllerBuilder = new LightyControllerBuilder();
        LightyController lightyController = lightyControllerBuilder.from(controllerConfiguration).build();
        lightyController.start().get();

        //2. start RestConf server
        LightyServerBuilder jettyServerBuilder = new LightyServerBuilder(new InetSocketAddress(
                restConfConfiguration.getInetAddress(), restConfConfiguration.getHttpPort()));
        CommunityRestConf communityRestConf = CommunityRestConfBuilder
                .from(RestConfConfigUtils.getRestConfConfiguration(restConfConfiguration,
                        lightyController.getServices()))
                .withLightyServer(jettyServerBuilder)
                .build();

        //3. start swagger
        /*
        SwaggerLighty swagger =
                new SwaggerLighty(restConfConfiguration, jettyServerBuilder, lightyController.getServices());
        swagger.start().get();
        */
        communityRestConf.start().get();
        communityRestConf.startServer();

        //4. start NetConf SBP
        /*
        LightyModule netconfSouthboundPlugin;
        netconfSBPConfiguration = NetconfConfigUtils.injectServicesToTopologyConfig(
                netconfSBPConfiguration, lightyController.getServices());
        netconfSouthboundPlugin = NetconfTopologyPluginBuilder
                .from(netconfSBPConfiguration, lightyController.getServices())
                .build();
        netconfSouthboundPlugin.start().get();
*/

        //5. Register shutdown hook for graceful shutdown.
        //shutdownHook = new ShutdownHook(lightyController, communityRestConf, netconfSouthboundPlugin, swagger);
        shutdownHook = new ShutdownHook(lightyController, communityRestConf);
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    public void shutdown() {
        shutdownHook.execute();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start(args, true);
    }

    private static class ShutdownHook extends Thread {

        private static final Logger LOG = LoggerFactory.getLogger(ShutdownHook.class);
        private final LightyController lightyController;
        private final CommunityRestConf communityRestConf;
        // private final LightyModule netconfSouthboundPlugin;
        // private final SwaggerLighty swagger;

        ShutdownHook(LightyController lightyController, CommunityRestConf communityRestConf) {
                     // LightyModule netconfSouthboundPlugin) {
                     //LightyModule netconfSouthboundPlugin, SwaggerLighty swagger) {
            this.lightyController = lightyController;
            this.communityRestConf = communityRestConf;
            // this.netconfSouthboundPlugin = netconfSouthboundPlugin;
            //this.swagger = swagger;
        }

        @Override
        public void run() {
            this.execute();
        }

        @SuppressWarnings({"checkstyle:illegalCatch", "VariableDeclarationUsageDistance"})
        public void execute() {
            LOG.info("lighty.io and RESTCONF shutting down ...");
            final Stopwatch stopwatch = Stopwatch.createStarted();
            /*try {
                swagger.shutdown().get();
            } catch (Exception e) {
                LOG.error("Exception while shutting down lighty.io swagger:", e);
            } */
            try {
                communityRestConf.shutdown().get();
            } catch (Exception e) {
                LOG.error("Exception while shutting down RESTCONF:", e);
            }
            /* try {
                netconfSouthboundPlugin.shutdown().get();
            } catch (Exception e) {
                LOG.error("Exception while shutting down NETCONF:", e);
            } */
            try {
                lightyController.shutdown().get();
            } catch (Exception e) {
                LOG.error("Exception while shutting down lighty.io controller:", e);
            }
            LOG.info("Lighty and OFP stopped in {}", stopwatch.stop());
        }
    }
}
