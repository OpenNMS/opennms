package org.opennms.features.topology.plugins.topo.asset.layers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Each layer which should be included in the generated Asset Topology must be configured in {@link org.opennms.features.topology.plugins.topo.asset.GeneratorConfig#layerHierarchy}.
 * In order to map it back to an actual {@link Layers} the {@link Key} annotation indicates the key name of the layer in the configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Key {
    String value();
}
