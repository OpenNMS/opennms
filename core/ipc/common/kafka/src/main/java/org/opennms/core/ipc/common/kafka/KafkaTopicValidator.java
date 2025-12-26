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
package org.opennms.core.ipc.common.kafka;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent. ExecutionException;
import java.util.stream.Collectors;

/**
 * Validates that required Kafka topics exist before attempting to use them.
 * This is particularly important in production environments where topic auto-creation is disabled.
 */

public class KafkaTopicValidator {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTopicValidator.class);

    private final Properties kafkaConfig;
    private final int timeoutMs;

    public KafkaTopicValidator(Properties kafkaConfig) {
        this(kafkaConfig, 5000); // 5 second default timeout
    }

    public KafkaTopicValidator(Properties kafkaConfig, int timeoutMs) {
        this.kafkaConfig = kafkaConfig;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Validates that required topics exist in Kafka.
     * Returns a validation result with details about missing topics.
     */
    public ValidationResult validateTopics(Set<String> requiredTopics) {
        ValidationResult result = new ValidationResult();

        if (requiredTopics == null || requiredTopics.isEmpty()) {
            result.setValid(true);
            return result;
        }

        try {
            // Get existing topics from Kafka
            Set<String> existingTopics = getExistingTopics();

            // Find missing topics
            Set<String> missingTopics = requiredTopics.stream()
                    .filter(topic -> !existingTopics.contains(topic))
                    .collect(Collectors.toSet());

            result.setRequiredTopics(requiredTopics);
            result.setExistingTopics(existingTopics);
            result.setMissingTopics(missingTopics);
            result.setValid(missingTopics.isEmpty());

        } catch (TopicAuthorizationException e) {
            result.setValid(false);
            result.setError("Authorization failure: " + e.getMessage());
        } catch (Exception e) {
            result.setValid(false);
            result.setError("Connection failure: " + e.getMessage());
        }

        return result;
    }

    /**
     * Helper method to log validation failures to the system log (for Services).
     */
    public void logValidationResult(ValidationResult result) {
        if (result.isValid()) {
            LOG.info("Kafka topic validation successful. All {} required topic(s) exist.", result.getRequiredTopics().size());
            return;
        }

        if (result.getError() != null) {
            LOG.error("Error validating Kafka topics: {}", result.getError());
            return;
        }

        Set<String> missingTopics = result.getMissingTopics();
        LOG.error("╔════════════════════════════════════════════════════════════════════════════╗");
        LOG.error("║  KAFKA TOPIC VALIDATION                                                    ║");
        LOG.error("╠════════════════════════════════════════════════════════════════════════════╣");
        LOG.error("║                                                                            ║");
        LOG.error("║  Missing Topics:                                                           ║");
        for (String topic : missingTopics) {
            LOG.error("║    • {}", String.format("%-68s", topic) + "║");
        }
        LOG.error("║                                                                            ║");
        LOG.error("╚════════════════════════════════════════════════════════════════════════════╝");
    }

    private Set<String> getExistingTopics() throws ExecutionException, InterruptedException {
        Properties adminConfig = new Properties();
        adminConfig.putAll(kafkaConfig);
        adminConfig.put("request.timeout.ms", timeoutMs);

        try (AdminClient adminClient = Utils.runWithGivenClassLoader(() ->
                AdminClient.create(adminConfig), AdminClient.class.getClassLoader())) {
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            return listTopicsResult.names().get();
        }
    }

    public static class ValidationResult {
        private boolean valid;
        private Set<String> requiredTopics = new HashSet<>();
        private Set<String> existingTopics = new HashSet<>();
        private Set<String> missingTopics = new HashSet<>();
        private String error;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public Set<String> getRequiredTopics() { return requiredTopics; }
        public void setRequiredTopics(Set<String> requiredTopics) { this.requiredTopics = requiredTopics; }
        public Set<String> getExistingTopics() { return existingTopics; }
        public void setExistingTopics(Set<String> existingTopics) { this.existingTopics = existingTopics; }
        public Set<String> getMissingTopics() { return missingTopics; }
        public void setMissingTopics(Set<String> missingTopics) { this.missingTopics = missingTopics; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}