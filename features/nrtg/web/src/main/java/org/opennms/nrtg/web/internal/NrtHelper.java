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
package org.opennms.nrtg.web.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.model.PrefabGraph;

/**
 * @author Markus@OpenNMS.org
 */
public class NrtHelper {

    public static final List<String> RRD_KEYWORDS = Arrays.asList(
            "--",
            "DEF",
            "CDEF",
            "LINE",
            "GPRINT");

    public String cleanUpRrdGraphStringForWebUi(final PrefabGraph prefabGraph, final Map<String,String> externalPropertyAttributes, final Map<String,String> stringPropertyAttributes) {
        String graphString = prefabGraph.getCommand();

        //Overwrite height and width by cinematic ration 1x2.40
        graphString = "--height=400 " + graphString;
        graphString = "--width=960 " + graphString;

        if (!graphString.contains("--slope-mode")) {
            graphString = "--slope-mode " + graphString;
        }
        if (!graphString.contains("--watermark")) {
            graphString = "--watermark=\"NRTG Alpha 1.0\" " + graphString;
        }

        // Escaping colons in rrd-strings rrd in javascript in java...
        graphString = graphString.replace("\\:", "\\\\\\\\:");
        graphString = graphString.replace("\\n", "\\\\\\\\n");

        // Escaping quotes in javascript in java
        graphString = graphString.replace("\"", "\\\\\"");

        for (final String key : externalPropertyAttributes.keySet()) {
            graphString = graphString.replace("{" + key + "}", externalPropertyAttributes.get(key));
        }

        for (final String key : stringPropertyAttributes.keySet()) {
            graphString = graphString.replace("{" + key + "}", stringPropertyAttributes.get(key));
        }

        return graphString;
    }

    public String generateJsMappingObject(String rrdCommand, final Map<String, String> rrdGraphAttributesToMetricIds) {
        final StringBuilder stringBuilder = new StringBuilder();
        final String command = rrdCommand;

        final Pattern pattern = Pattern.compile("DEF:.*?=(\\{.*?\\}):(.*?):");
        final Matcher matcher = pattern.matcher(command);

        final Map<String, String> rrdFileMapping = new HashMap<String, String>();
        while (matcher.find()) {
            rrdFileMapping.put(matcher.group(2), matcher.group(1));
        }

        for (final Map.Entry<String,String> entry : rrdGraphAttributesToMetricIds.entrySet()) {
            final String row = String.format("\"%s\": \"%s:%s\", %n", entry.getValue(), rrdFileMapping.get(entry.getKey()), entry.getKey());
            stringBuilder.append(row);
        }

        return stringBuilder.toString().substring(0,stringBuilder.toString().length() - ", \n".length());
    }
}
