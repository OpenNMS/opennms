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
package org.opennms.smoketest.stacks;

import java.util.HashMap;
import java.util.Map;

public enum KafkaCompressionStrategy {
    GZIP("gzip"),
    SNAPPY("snappy"),
    LZ4("lz4"),
    ZSTD("zstd"),
    NONE("none");

    private String codec;

    KafkaCompressionStrategy(String codec) {
        this.codec = codec;
    }

    public String getCodec() {
        return codec;
    }

    private static final Map<String, KafkaCompressionStrategy> lookup = new HashMap<>();

    static
    {
        for(KafkaCompressionStrategy codec : KafkaCompressionStrategy.values())
        {
            lookup.put(codec.getCodec(), codec);
        }
    }

    public static KafkaCompressionStrategy get(String codec)
    {
        return lookup.get(codec);
    }
}
