/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus.types;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Superclass to all metrics. All metrics have name and labels.
 */
public abstract class Metric {

    public abstract static class Builder<B extends Builder<?>> {
        private String name;
        private Map<String, String> labels;
        private Instant timestamp;
        private Instant created;

        @SuppressWarnings("unchecked")
        public B setName(String name) {
            this.name = name;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addLabel(String name, String value) {
            if (labels == null) {
                labels = new LinkedHashMap<>(); // used linked hash map to retain ordering
            }
            labels.put(name, value);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addLabels(Map<String, String> map) {
            if (labels == null) {
                labels = new LinkedHashMap<>(); // used linked hash map to retain ordering
            }
            labels.putAll(map);
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        public B setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        public B setCreated(Instant created) {
            this.created = created;
            return (B) this;
        }

        public abstract <T extends Metric> T build();
    }

    private final String name;
    private final Map<String, String> labels;
    private final Instant timestamp;
    private final Instant created;

    protected Metric(Builder<?> builder) {
        if (builder.name == null) {
            throw new IllegalArgumentException("Need to set name");
        }

        this.name = builder.name;
        this.labels = builder.labels;
        this.timestamp = builder.timestamp;
        this.created = builder.created;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getLabels() {
        if (labels == null) {
            return Collections.emptyMap();
        }
        return labels;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Instant getCreated() {

        return created;
    }
    
    @Override
    public String toString() {

        return "Metric [name=" + name + ", labels=" + labels + ", timestamp=" + timestamp + ", created=" + created + "]";
    }

    public abstract void visit(MetricVisitor visitor);
}
