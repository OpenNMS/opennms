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
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opennms.container.daemon.KarafContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Talks to the embedded Karaf container through the framework's own
 * BundleContext. The Karaf feature and KAR services live inside OSGi bundles
 * whose packages are not exported to the webapp class loader, so they are
 * driven reflectively through the interfaces loaded by the service's own loader.
 */
public class OsgiKarafBridge implements KarafBridge {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiKarafBridge.class);

    static final String FEATURES_SERVICE = "org.apache.karaf.features.FeaturesService";
    static final String FEATURE = "org.apache.karaf.features.Feature";
    static final String DEPENDENCY = "org.apache.karaf.features.Dependency";
    static final String KAR_SERVICE = "org.apache.karaf.kar.KarService";
    static final String BUNDLE_INFO = "org.apache.karaf.features.BundleInfo";
    static final String REPOSITORY = "org.apache.karaf.features.Repository";
    static final String FEATURES_OPTION = "org.apache.karaf.features.FeaturesService$Option";
    static final String BUNDLE_SERVICE = "org.apache.karaf.bundle.core.BundleService";
    static final String HEALTH_CHECK = "org.opennms.core.health.api.HealthCheck";
    static final String HEALTH_CONTEXT = "org.opennms.core.health.api.Context";
    static final String HEALTH_RESPONSE = "org.opennms.core.health.api.Response";
    /** plugins built on the Integration API register this one; the API layer's core wrapper belongs to another bundle */
    static final String OIA_HEALTH_CHECK = "org.opennms.integration.api.v1.health.HealthCheck";
    static final String OIA_HEALTH_CONTEXT = "org.opennms.integration.api.v1.health.Context";
    static final String OIA_HEALTH_RESPONSE = "org.opennms.integration.api.v1.health.Response";
    static final String KARAF_BUNDLE_INFO = "org.apache.karaf.bundle.core.BundleInfo";
    /** per health check; the checks themselves usually answer in milliseconds */
    static final long HEALTH_CHECK_TIMEOUT_MS = 5_000L;
    /** runs plugin health checks so a check that blocks cannot pin the request or watchdog thread */
    private static final java.util.concurrent.ExecutorService HEALTH_EXECUTOR = java.util.concurrent.Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r, "plugin-management-health");
        thread.setDaemon(true);
        return thread;
    });
    static final String PACKAGE_NAMESPACE = "osgi.wiring.package";
    static final String OIA_BUNDLE_SYMBOLIC_NAME = "org.opennms.integration.api";
    static final String OIA_FEATURE_NAME = "opennms-integration-api";
    static final String API_LAYER_FEATURE_NAME = "opennms-api-layer";
    private static final int MAX_DEPENDENCY_DEPTH = 3;

    private interface ServiceCall<T> {
        T apply(Object service) throws Exception;
    }

    private final Supplier<BundleContext> contextSupplier;
    private final Supplier<Set<String>> bootRepositories;
    /** data/kar, where Karaf's KarService keeps each installed KAR's list of feature repositories; null when unknown. */
    private final Path karStorageDir;

    /**
     * @param etcDir the container's etc directory, holding org.apache.karaf.features.cfg
     * @param karStorageDir the container's data/kar directory
     */
    public OsgiKarafBridge(final Path etcDir, final Path karStorageDir) {
        this(OsgiKarafBridge::defaultContext, () -> BootRepositories.load(etcDir == null ? null : etcDir.resolve(BootRepositories.CFG_FILE_NAME)), karStorageDir);
    }

    OsgiKarafBridge(final Supplier<BundleContext> contextSupplier, final Supplier<Set<String>> bootRepositories, final Path karStorageDir) {
        this.contextSupplier = contextSupplier;
        this.bootRepositories = bootRepositories;
        this.karStorageDir = karStorageDir;
    }

    private static BundleContext defaultContext() {
        return KarafContext.getBundleContextIfAvailable().orElse(null);
    }

    @Override
    public boolean isAvailable() {
        return context() != null;
    }

    @Override
    public List<InstalledFeature> installedFeatures() {
        return withService(FEATURES_SERVICE, Collections.emptyList(), service -> {
            final List<InstalledFeature> result = new ArrayList<>();
            final Object[] features = (Object[]) invoke(service, FEATURES_SERVICE, "listInstalledFeatures");
            for (final Object feature : features) {
                result.add(toInstalledFeature(service, feature));
            }
            return result;
        });
    }

    @Override
    public List<InstalledFeature> features() {
        return withService(FEATURES_SERVICE, Collections.emptyList(), service -> {
            final List<InstalledFeature> result = new ArrayList<>();
            for (final Object feature : (Object[]) invoke(service, FEATURES_SERVICE, "listFeatures")) {
                result.add(toInstalledFeature(service, feature));
            }
            return result;
        });
    }

    @Override
    public List<String> installedKars() {
        return withService(KAR_SERVICE, Collections.emptyList(), service -> {
            final List<String> result = new ArrayList<>();
            for (final Object name : (List<?>) invoke(service, KAR_SERVICE, "list")) {
                result.add(String.valueOf(name));
            }
            return result;
        });
    }

    @Override
    public Map<String, List<String>> frameworkExports() {
        final Map<String, List<String>> exports = new TreeMap<>();
        final BundleContext ctx = context();
        if (ctx == null) {
            return exports;
        }
        try {
            for (final Bundle bundle : ctx.getBundles()) {
                final int state = bundle.getState();
                if (state != Bundle.ACTIVE && state != Bundle.RESOLVED && state != Bundle.STARTING) {
                    continue;
                }
                final BundleWiring wiring = bundle.adapt(BundleWiring.class);
                if (wiring == null) {
                    continue;
                }
                final List<BundleCapability> capabilities = wiring.getCapabilities(PACKAGE_NAMESPACE);
                if (capabilities == null) {
                    continue;
                }
                for (final BundleCapability capability : capabilities) {
                    final Map<String, Object> attributes = capability.getAttributes();
                    final Object pkg = attributes.get(PACKAGE_NAMESPACE);
                    if (pkg == null) {
                        continue;
                    }
                    final Object version = attributes.get("version");
                    final List<String> versions = exports.computeIfAbsent(pkg.toString(), k -> new ArrayList<>());
                    final String v = version == null ? "0.0.0" : version.toString();
                    if (!versions.contains(v)) {
                        versions.add(v);
                    }
                }
            }
        } catch (final Throwable t) {
            LOG.warn("Failed to enumerate the packages exported by the Karaf container: {}", t.toString());
        }
        return exports;
    }

    @Override
    public String javaVersion() {
        return System.getProperty("java.specification.version");
    }

    @Override
    public String oiaVersion() {
        final BundleContext ctx = context();
        if (ctx == null) {
            return null;
        }
        try {
            for (final Bundle bundle : ctx.getBundles()) {
                if (OIA_BUNDLE_SYMBOLIC_NAME.equals(bundle.getSymbolicName())) {
                    return bundle.getVersion().toString();
                }
            }
        } catch (final Throwable t) {
            LOG.warn("Failed to look up the OpenNMS Integration API bundle: {}", t.toString());
        }
        return null;
    }

    @Override
    public List<InstalledFeature> pluginFeatures() {
        return withService(FEATURES_SERVICE, Collections.emptyList(), service -> {
            final Map<String, Set<String>> dependencyGraph = new HashMap<>();
            for (final Object feature : (Object[]) invoke(service, FEATURES_SERVICE, "listFeatures")) {
                final String name = string(invoke(feature, FEATURE, "getName"));
                dependencyGraph.computeIfAbsent(name, k -> new LinkedHashSet<>()).addAll(dependencyNames(feature));
            }
            final Set<String> oiaDependent = new HashSet<>();
            final Set<String> coreRepositories = bootRepositories.get();
            final List<InstalledFeature> result = new ArrayList<>();
            for (final Object feature : (Object[]) invoke(service, FEATURES_SERVICE, "listInstalledFeatures")) {
                final String name = string(invoke(feature, FEATURE, "getName"));
                if (BootRepositories.isCore(repositoryUrl(feature), coreRepositories)) {
                    continue;
                }
                if (dependsOnOia(name, dependencyGraph, oiaDependent, new HashSet<>(), 0)) {
                    result.add(toInstalledFeature(service, feature));
                }
            }
            return result;
        });
    }

    @Override
    public List<String> karRepositories(final String karName) {
        final List<String> repositories = new ArrayList<>();
        if (karStorageDir == null || karName == null) {
            return repositories;
        }
        final Path cfg = karStorageDir.resolve(karName).resolve(PluginRegistry.KAR_FEATURES_CFG);
        if (!Files.isRegularFile(cfg)) {
            return repositories;
        }
        try {
            for (final String line : Files.readAllLines(cfg, StandardCharsets.UTF_8)) {
                final String uri = line.trim();
                if (!uri.isEmpty() && !repositories.contains(uri)) {
                    repositories.add(uri);
                }
            }
        } catch (final IOException e) {
            LOG.warn("Cannot read {}: {}", cfg, e.toString());
        }
        return repositories;
    }

    @Override
    public void addRepository(final String uri) throws KarafOperationException {
        operate(FEATURES_SERVICE, service -> {
            final URI target = URI.create(uri.trim());
            for (final Object repository : (Object[]) invoke(service, FEATURES_SERVICE, "listRepositories")) {
                if (target.equals(invoke(repository, REPOSITORY, "getURI"))) {
                    return null;
                }
            }
            invoke(service, FEATURES_SERVICE, "addRepository", new Class<?>[] { URI.class }, target);
            return null;
        });
    }

    @Override
    public void installFeatures(final Set<String> features) throws KarafOperationException {
        operate(FEATURES_SERVICE, service -> {
            invoke(service, FEATURES_SERVICE, "installFeatures", new Class<?>[] { Set.class, EnumSet.class }, new LinkedHashSet<>(features), noOptions(service));
            return null;
        });
    }

    @Override
    public void uninstallFeatures(final Set<String> features) throws KarafOperationException {
        operate(FEATURES_SERVICE, service -> {
            invoke(service, FEATURES_SERVICE, "uninstallFeatures", new Class<?>[] { Set.class, EnumSet.class }, new LinkedHashSet<>(features), noOptions(service));
            return null;
        });
    }

    @Override
    public Map<String, String> featureDiagnostics(final Set<String> features) {
        return withService(FEATURES_SERVICE, Collections.emptyMap(), service -> {
            final Map<String, String> result = new LinkedHashMap<>();
            final BundleContext ctx = context();
            final Map<String, Bundle> byLocation = new HashMap<>();
            for (final Bundle bundle : ctx.getBundles()) {
                if (bundle.getLocation() != null) {
                    byLocation.put(bundle.getLocation(), bundle);
                }
            }
            for (final String name : features) {
                final Object feature = invoke(service, FEATURES_SERVICE, "getFeature", new Class<?>[] { String.class }, name);
                if (feature == null) {
                    result.put(name, "The feature is not declared by any repository the container knows");
                    continue;
                }
                final Object state = invoke(service, FEATURES_SERVICE, "getState", new Class<?>[] { String.class }, string(invoke(feature, FEATURE, "getId")));
                final String stateName = state == null ? "Uninstalled" : state.toString();
                if (InstalledFeature.STATE_STARTED.equals(stateName)) {
                    continue;
                }
                final StringBuilder reason = new StringBuilder("Feature state: ").append(stateName);
                for (final Bundle bundle : featureBundles(ctx, service, Collections.singleton(name))) {
                    describeBundle(ctx, bundle, reason);
                }
                result.put(name, reason.toString());
            }
            return result;
        });
    }

    @Override
    public Set<String> bundleNames(final Set<String> features) {
        return withService(FEATURES_SERVICE, Collections.<String>emptySet(), service -> {
            final Set<String> names = new LinkedHashSet<>();
            for (final Bundle bundle : featureBundles(context(), service, features)) {
                names.add(bundle.getSymbolicName());
            }
            return names;
        });
    }

    @Override
    public boolean refreshBundles(final Set<String> symbolicNames, final java.time.Duration timeout) {
        final BundleContext ctx = context();
        if (ctx == null || symbolicNames.isEmpty()) {
            return false;
        }
        try {
            final FrameworkWiring wiring = ctx.getBundle(0).adapt(FrameworkWiring.class);
            if (wiring == null) {
                return false;
            }
            final List<Bundle> pending = new ArrayList<>();
            for (final Bundle bundle : wiring.getRemovalPendingBundles()) {
                if (symbolicNames.contains(bundle.getSymbolicName())) {
                    pending.add(bundle);
                }
            }
            if (pending.isEmpty()) {
                return true;
            }
            LOG.info("Refreshing {} removal-pending bundle(s) of the plugin: {}", pending.size(), pending.stream().map(Bundle::getSymbolicName).collect(Collectors.joining(", ")));
            final CountDownLatch done = new CountDownLatch(1);
            wiring.refreshBundles(pending, (FrameworkEvent event) -> done.countDown());
            return done.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final Throwable t) {
            LOG.warn("Refreshing the plugin's bundles after an uninstall failed: {}", t.toString());
            return false;
        }
    }

    @Override
    public List<PluginHealth> health(final Set<String> features) {
        return withService(FEATURES_SERVICE, Collections.<PluginHealth>emptyList(), service -> {
            final BundleContext ctx = context();
            final Set<Bundle> bundles = featureBundles(ctx, service, features);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Health of {}: bundles {}", features, bundles.stream().map(b -> b.getSymbolicName() + "#" + b.getBundleId()).collect(Collectors.joining(", ")));
            }
            final List<PluginHealth> result = new ArrayList<>();
            for (final Bundle bundle : bundles) {
                final PluginHealth container = containerState(ctx, bundle);
                if (container != null) {
                    result.add(container);
                }
            }
            for (final String[] api : new String[][] { { HEALTH_CHECK, HEALTH_CONTEXT, HEALTH_RESPONSE }, { OIA_HEALTH_CHECK, OIA_HEALTH_CONTEXT, OIA_HEALTH_RESPONSE } }) {
                // the system bundle cannot load the plugin-facing interfaces, so the class-space check would hide every reference
                final ServiceReference<?>[] references = ctx.getAllServiceReferences(api[0], null);
                if (references == null) {
                    continue;
                }
                for (final ServiceReference<?> reference : references) {
                    LOG.debug("Health check {} registered by bundle {}", reference, reference.getBundle() == null ? null : reference.getBundle().getSymbolicName() + "#" + reference.getBundle().getBundleId());
                    if (reference.getBundle() == null || !bundles.contains(reference.getBundle())) {
                        continue;
                    }
                    result.add(runHealthCheck(ctx, reference, api[0], api[1], api[2]));
                }
            }
            result.sort((a, b) -> String.valueOf(a.getDescription()).compareTo(String.valueOf(b.getDescription())));
            return result;
        });
    }

    /**
     * The installed bundles of the features and, recursively, of the non-core features
     * they depend on, matched by (original) location as the extender records them. A
     * plugin's boot feature often lists only sub-features and no bundle of its own.
     */
    private Set<Bundle> featureBundles(final BundleContext ctx, final Object service, final Set<String> features) throws Exception {
        final Map<String, Bundle> byLocation = new HashMap<>();
        for (final Bundle bundle : ctx.getBundles()) {
            if (bundle.getLocation() != null) {
                byLocation.put(bundle.getLocation(), bundle);
            }
        }
        final Set<String> coreRepositories = bootRepositories.get();
        final Set<Bundle> bundles = new LinkedHashSet<>();
        final Set<String> visited = new HashSet<>();
        for (final String name : features) {
            collectBundles(service, invoke(service, FEATURES_SERVICE, "getFeature", new Class<?>[] { String.class }, name), byLocation, coreRepositories, visited, bundles);
        }
        return bundles;
    }

    private void collectBundles(final Object service, final Object feature, final Map<String, Bundle> byLocation, final Set<String> coreRepositories,
                                final Set<String> visited, final Set<Bundle> bundles) throws Exception {
        if (feature == null || !visited.add(string(invoke(feature, FEATURE, "getId"))) || BootRepositories.isCore(repositoryUrl(feature), coreRepositories)) {
            return;
        }
        final Object infos = invoke(feature, FEATURE, "getBundles");
        if (infos instanceof List) {
            for (final Object info : (List<?>) infos) {
                final String location = string(invoke(info, BUNDLE_INFO, "getLocation"));
                Bundle bundle = byLocation.get(location);
                if (bundle == null) {
                    final String original = string(invokeIfPresent(info, BUNDLE_INFO, "getOriginalLocation"));
                    bundle = original == null ? null : byLocation.get(original);
                }
                if (bundle != null) {
                    bundles.add(bundle);
                } else {
                    LOG.debug("No installed bundle at {} for feature {}", location, string(invoke(feature, FEATURE, "getName")));
                }
            }
        }
        final Object dependencies = invoke(feature, FEATURE, "getDependencies");
        if (dependencies instanceof List) {
            for (final Object dependency : (List<?>) dependencies) {
                final String name = string(invoke(dependency, DEPENDENCY, "getName"));
                final String version = string(invokeIfPresent(dependency, DEPENDENCY, "getVersion"));
                final Object child = version == null || version.isBlank() || "0.0.0".equals(version)
                        ? invoke(service, FEATURES_SERVICE, "getFeature", new Class<?>[] { String.class }, name)
                        : invoke(service, FEATURES_SERVICE, "getFeature", new Class<?>[] { String.class, String.class }, name, version);
                collectBundles(service, child == null && version != null ? invoke(service, FEATURES_SERVICE, "getFeature", new Class<?>[] { String.class }, name) : child,
                        byLocation, coreRepositories, visited, bundles);
            }
        }
    }

    /**
     * Karaf's view of a bundle's blueprint or declarative container: GracePeriod,
     * Waiting and Failure mean an Active bundle whose services never came up, which
     * the framework state alone does not show. Null when the bundle is fine.
     */
    private static PluginHealth containerState(final BundleContext ctx, final Bundle bundle) {
        if (bundle.getState() != Bundle.ACTIVE || bundle.getHeaders().get("Fragment-Host") != null) {
            return null;
        }
        ServiceReference<?> reference = null;
        try {
            reference = ctx.getServiceReference(BUNDLE_SERVICE);
            if (reference == null) {
                return null;
            }
            final Object bundleService = ctx.getService(reference);
            if (bundleService == null) {
                return null;
            }
            final Object info = invoke(bundleService, BUNDLE_SERVICE, "getInfo", new Class<?>[] { Bundle.class }, bundle);
            final String state = info == null ? null : string(invoke(info, KARAF_BUNDLE_INFO, "getState"));
            if (state == null || "Active".equals(state) || "Starting".equals(state) || "Resolved".equals(state) || "Unknown".equals(state)) {
                return null;
            }
            final String diag = bundleDiag(ctx, bundle);
            final String status = "Failure".equals(state) ? PluginHealth.FAILURE : PluginHealth.STARTING;
            return new PluginHealth("Bundle " + bundle.getSymbolicName(), status, state + (diag == null || diag.isBlank() ? "" : ": " + diag.trim().replaceAll("\\s+", " ")));
        } catch (final Throwable t) {
            LOG.debug("Container state of {} is not available: {}", bundle, t.toString());
            return null;
        } finally {
            if (reference != null) {
                try {
                    ctx.ungetService(reference);
                } catch (final Throwable ignored) {
                    // the service may already be gone
                }
            }
        }
    }

    /** Runs one health check, core or Integration API flavour; the two share method names and status names. */
    private static PluginHealth runHealthCheck(final BundleContext ctx, final ServiceReference<?> reference, final String checkInterface, final String contextInterface, final String responseInterface) {
        Object check = null;
        String description = String.valueOf(reference.getProperty("service.id"));
        try {
            check = ctx.getService(reference);
            if (check == null) {
                return new PluginHealth(description, PluginHealth.UNKNOWN, "the health check service is gone");
            }
            description = String.valueOf(invoke(check, checkInterface, "getDescription"));
            final ClassLoader loader = loaderOf(check);
            final Class<?> contextClass = loader.loadClass(contextInterface);
            final Object context = healthContext(contextClass);
            final Object target = check;
            final Object response;
            try {
                response = HEALTH_EXECUTOR.submit(() -> invoke(target, checkInterface, "perform", new Class<?>[] { contextClass }, context))
                        .get(HEALTH_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (final java.util.concurrent.TimeoutException e) {
                return new PluginHealth(description, PluginHealth.TIMEOUT, "no answer within " + (HEALTH_CHECK_TIMEOUT_MS / 1000) + " seconds");
            } catch (final java.util.concurrent.ExecutionException e) {
                throw e.getCause() == null ? e : e.getCause();
            }
            if (response == null) {
                return new PluginHealth(description, PluginHealth.UNKNOWN, "the health check returned nothing");
            }
            final String status = string(invoke(response, responseInterface, "getStatus"));
            final String message = string(invoke(response, responseInterface, "getMessage"));
            return new PluginHealth(description, status == null ? PluginHealth.UNKNOWN : status, message);
        } catch (final Throwable t) {
            final Throwable cause = t instanceof InvocationTargetException && t.getCause() != null ? t.getCause() : t;
            return new PluginHealth(description, PluginHealth.FAILURE, cause.toString());
        } finally {
            if (check != null) {
                try {
                    ctx.ungetService(reference);
                } catch (final Throwable ignored) {
                    // the service may already be gone
                }
            }
        }
    }

    /** The core Context is a bean with setTimeout; the Integration API one is an interface with getTimeout, answered by a proxy. */
    private static Object healthContext(final Class<?> contextClass) throws Exception {
        if (contextClass.isInterface()) {
            return java.lang.reflect.Proxy.newProxyInstance(contextClass.getClassLoader(), new Class<?>[] { contextClass }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getTimeout": return HEALTH_CHECK_TIMEOUT_MS;
                    case "toString": return "PluginManagement health context";
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == args[0];
                    default: return null;
                }
            });
        }
        final Object context = contextClass.getDeclaredConstructor().newInstance();
        contextClass.getMethod("setTimeout", long.class).invoke(context, HEALTH_CHECK_TIMEOUT_MS);
        return context;
    }

    private void describeBundle(final BundleContext ctx, final Bundle bundle, final StringBuilder reason) throws Exception {
        if (bundle.getState() == Bundle.ACTIVE || bundle.getHeaders().get("Fragment-Host") != null) {
            return;
        }
        reason.append("; bundle ").append(bundle.getSymbolicName()).append('/').append(bundle.getVersion()).append(" is ").append(bundleState(bundle.getState()));
        final String diag = bundleDiag(ctx, bundle);
        if (diag != null && !diag.isBlank()) {
            reason.append(": ").append(diag.trim().replaceAll("\\s+", " "));
        }
    }

    /** The resolver's explanation from Karaf's BundleService, which is optional; null when it is absent or silent. */
    private static String bundleDiag(final BundleContext ctx, final Bundle bundle) {
        ServiceReference<?> reference = null;
        try {
            reference = ctx.getServiceReference(BUNDLE_SERVICE);
            if (reference == null) {
                return null;
            }
            final Object bundleService = ctx.getService(reference);
            if (bundleService == null) {
                return null;
            }
            for (final Method m : bundleService.getClass().getClassLoader().loadClass(BUNDLE_SERVICE).getMethods()) {
                if ("getDiag".equals(m.getName()) && m.getParameterCount() == 1) {
                    return string(m.invoke(bundleService, bundle));
                }
            }
            return null;
        } catch (final Throwable t) {
            LOG.debug("BundleService diagnosis of {} failed: {}", bundle, t.toString());
            return null;
        } finally {
            if (reference != null) {
                try {
                    ctx.ungetService(reference);
                } catch (final Throwable ignored) {
                    // the service may already be gone
                }
            }
        }
    }

    static String bundleState(final int state) {
        switch (state) {
            case Bundle.UNINSTALLED: return "Uninstalled";
            case Bundle.INSTALLED: return "Installed";
            case Bundle.RESOLVED: return "Resolved";
            case Bundle.STARTING: return "Starting";
            case Bundle.STOPPING: return "Stopping";
            case Bundle.ACTIVE: return "Active";
            default: return "state " + state;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EnumSet<?> noOptions(final Object featuresService) throws Exception {
        final Class option = loaderOf(featuresService).loadClass(FEATURES_OPTION);
        return EnumSet.noneOf(option);
    }

    private static boolean dependsOnOia(final String name, final Map<String, Set<String>> graph, final Set<String> memo, final Set<String> visiting, final int depth) {
        if (OIA_FEATURE_NAME.equals(name) || API_LAYER_FEATURE_NAME.equals(name)) {
            return false;
        }
        if (memo.contains(name)) {
            return true;
        }
        final Set<String> dependencies = graph.getOrDefault(name, Collections.emptySet());
        if (dependencies.contains(OIA_FEATURE_NAME)) {
            memo.add(name);
            return true;
        }
        if (depth >= MAX_DEPENDENCY_DEPTH || !visiting.add(name)) {
            return false;
        }
        try {
            for (final String dependency : dependencies) {
                if (dependsOnOia(dependency, graph, memo, visiting, depth + 1)) {
                    memo.add(name);
                    return true;
                }
            }
        } finally {
            visiting.remove(name);
        }
        return false;
    }

    private InstalledFeature toInstalledFeature(final Object featuresService, final Object feature) throws Exception {
        final String name = string(invoke(feature, FEATURE, "getName"));
        final String version = string(invoke(feature, FEATURE, "getVersion"));
        final String id = string(invoke(feature, FEATURE, "getId"));
        final Object state = invoke(featuresService, FEATURES_SERVICE, "getState", new Class<?>[] { String.class }, id);
        final InstalledFeature installed = new InstalledFeature(name, version, state == null ? null : state.toString(), repositoryUrl(feature));
        installed.getDependencies().addAll(dependencyNames(feature));
        return installed;
    }

    private static String repositoryUrl(final Object feature) throws Exception {
        return string(invoke(feature, FEATURE, "getRepositoryUrl"));
    }

    private static List<String> dependencyNames(final Object feature) throws Exception {
        final List<String> names = new ArrayList<>();
        final Object dependencies = invoke(feature, FEATURE, "getDependencies");
        if (dependencies instanceof List) {
            for (final Object dependency : (List<?>) dependencies) {
                names.add(string(invoke(dependency, DEPENDENCY, "getName")));
            }
        }
        return names;
    }

    private BundleContext context() {
        try {
            return contextSupplier.get();
        } catch (final Throwable t) {
            LOG.warn("The Karaf container is not reachable from the web application: {}", t.toString());
            return null;
        }
    }

    private <T> T withService(final String className, final T fallback, final ServiceCall<T> call) {
        final BundleContext ctx = context();
        if (ctx == null) {
            return fallback;
        }
        ServiceReference<?> reference = null;
        try {
            reference = ctx.getServiceReference(className);
            if (reference == null) {
                LOG.warn("No {} service is registered in the Karaf container", className);
                return fallback;
            }
            final Object service = ctx.getService(reference);
            if (service == null) {
                return fallback;
            }
            return call.apply(service);
        } catch (final Throwable t) {
            LOG.warn("Call to {} failed: {}", className, t.toString());
            return fallback;
        } finally {
            if (reference != null) {
                try {
                    ctx.ungetService(reference);
                } catch (final Throwable ignored) {
                    // the service may already be gone
                }
            }
        }
    }

    /** Like {@link #withService} for an operation whose failure the caller must see. */
    private void operate(final String className, final ServiceCall<?> call) throws KarafOperationException {
        final BundleContext ctx = context();
        if (ctx == null) {
            throw new KarafOperationException("The Karaf container is not reachable from the web application");
        }
        ServiceReference<?> reference = null;
        try {
            reference = ctx.getServiceReference(className);
            if (reference == null) {
                throw new KarafOperationException("No " + className + " service is registered in the Karaf container");
            }
            final Object service = ctx.getService(reference);
            if (service == null) {
                throw new KarafOperationException("The " + className + " service is gone");
            }
            call.apply(service);
        } catch (final KarafOperationException e) {
            throw e;
        } catch (final Throwable t) {
            final Throwable cause = t instanceof InvocationTargetException && t.getCause() != null ? t.getCause() : t;
            LOG.warn("Call to {} failed: {}", className, cause.toString());
            throw new KarafOperationException(cause.getMessage() == null || cause.getMessage().isBlank() ? cause.toString() : cause.getMessage(), cause);
        } finally {
            if (reference != null) {
                try {
                    ctx.ungetService(reference);
                } catch (final Throwable ignored) {
                    // the service may already be gone
                }
            }
        }
    }

    private static Object invokeIfPresent(final Object target, final String interfaceName, final String method) throws Exception {
        try {
            return invoke(target, interfaceName, method);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    private static ClassLoader loaderOf(final Object target) {
        final ClassLoader loader = target.getClass().getClassLoader();
        return loader == null ? ClassLoader.getSystemClassLoader() : loader;
    }

    private static Object invoke(final Object target, final String interfaceName, final String method) throws Exception {
        return invoke(target, interfaceName, method, new Class<?>[0]);
    }

    /** Resolves the interface through the target's own class loader so class spaces match. */
    private static Object invoke(final Object target, final String interfaceName, final String method, final Class<?>[] parameterTypes, final Object... args) throws Exception {
        final Class<?> iface = loaderOf(target).loadClass(interfaceName);
        final Method m = iface.getMethod(method, parameterTypes);
        return m.invoke(target, args);
    }

    private static String string(final Object value) {
        return value == null ? null : value.toString();
    }
}
