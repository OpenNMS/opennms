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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonWriter;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BsonWriteTest {
    final SampleDatagramEnrichment sampleDatagramEnrichment = srcAddress -> Optional.empty();

    private ByteBuf getByteBuf() {
        return Unpooled.wrappedBuffer(new byte[1024]).setZero(0, 1024);
    }

    private BsonWriter getBsonWriter() {
        final BsonWriter bsonWriter = new BsonDocumentWriter(new BsonDocument());
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("BsonWriter");
        return bsonWriter;
    }

    private Object instanceOf(final Class clazz) {
        try {
            return clazz.getConstructor(ByteBuf.class).newInstance(getByteBuf());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            return null;
        }

        try {
            return clazz.getConstructor(int.class).newInstance(0);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
        }
        return null;
    }

    private Set<Class> getClassesForPackage(final String pkg) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        final Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(pkg);
        final Set<Class> classes = new HashSet<>();

        for (final BeanDefinition beanDefinition : beanDefinitions) {
            try {
                final Class clazz = Class.forName(beanDefinition.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    @Test
    public void testWriteBsonMethods() throws Exception {
        final Set<Class> classes = getClassesForPackage("org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows");

        for (final Class clazz : classes) {
            Method method = null;

            try {
                method = clazz.getMethod("writeBson", BsonWriter.class);
            } catch (NoSuchMethodException e) {
                // ignore
            }

            if (method != null) {
                final Object object = instanceOf(clazz);
                if (object != null) {
                    method.invoke(object, getBsonWriter(), sampleDatagramEnrichment);
                    System.out.println(clazz.getSimpleName() + " -> checked");
                } else {
                    System.out.println(clazz.getSimpleName() + " -> class not instantiatable");
                }
                continue;
            } else {
                try {
                    method = clazz.getMethod("writeBson", BsonWriter.class, SampleDatagramEnrichment.class);
                } catch (NoSuchMethodException e2) {
                    // ignore
                }

                if (method != null) {
                    final Object object = instanceOf(clazz);
                    if (object != null) {
                        method.invoke(instanceOf(clazz), getBsonWriter(), sampleDatagramEnrichment);
                        System.out.println(clazz.getSimpleName() + " -> checked");
                    } else {
                        System.out.println(clazz.getSimpleName() + " -> class not instantiatable");
                    }
                    continue;
                } else {
                    System.out.println(clazz.getSimpleName() + " -> no writeBson() method");
                }
            }
        }
    }
}
