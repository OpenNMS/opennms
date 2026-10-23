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
package org.hawkular.agent.prometheus;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    
    private Util() {
        // prevent instantiation
    }

    public static double convertStringToDouble(String valueString) {
        double doubleValue;
        if (valueString.equalsIgnoreCase("NaN")) {
            doubleValue = Double.NaN;
        } else if (valueString.equalsIgnoreCase("+Inf")) {
            doubleValue = Double.POSITIVE_INFINITY;
        } else if (valueString.equalsIgnoreCase("-Inf")) {
            doubleValue = Double.NEGATIVE_INFINITY;
        } else {
            doubleValue = Double.valueOf(valueString).doubleValue();
        }
        return doubleValue;
    }

    public static String convertDoubleToString(double value) {
        // Prometheus spec requires positive infinity to be denoted as "+Inf" and negative infinity as "-Inf"
        if (Double.isInfinite(value)) {
            return (value < 0.0) ? "-Inf" : "+Inf";
        }
        return String.format("%f", value);
    }
    
    public static Instant tryConvertTimestamp(CharSequence timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }
        try {
            double timestampDouble = Double.parseDouble(timestamp.toString());
            long epochSecond = (long) timestampDouble;
            return Instant.ofEpochSecond(epochSecond);
        }
        catch (NumberFormatException e) {
            LOGGER.warn("Invalid timestamp: " + timestamp);
            return null;
        }
    }
}
