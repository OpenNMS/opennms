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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

import com.google.protobuf.ByteString;

import groovy.lang.GroovyClassLoader;

public class GnmiLeafFlattenerTest {

    private GroovyClassLoader gcl;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
    }

    private static Gnmi.PathElem elem(String name) {
        return Gnmi.PathElem.newBuilder().setName(name).build();
    }

    private static Gnmi.PathElem elem(String name, String key, String value) {
        return Gnmi.PathElem.newBuilder().setName(name).putKey(key, value).build();
    }

    private static Gnmi.Path path(Gnmi.PathElem... elems) {
        return Gnmi.Path.newBuilder().addAllElem(Arrays.asList(elems)).build();
    }

    private static Gnmi.Update update(Gnmi.Path path, Gnmi.TypedValue value) {
        return Gnmi.Update.newBuilder().setPath(path).setVal(value).build();
    }

    private static Gnmi.SubscribeResponse response(Gnmi.Path prefix, Gnmi.Update... updates) {
        return Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder().setPrefix(prefix).addAllUpdate(Arrays.asList(updates)))
                .build();
    }

    private static Gnmi.TypedValue json(String text) {
        return Gnmi.TypedValue.newBuilder().setJsonVal(ByteString.copyFromUtf8(text)).build();
    }

    private static Gnmi.TypedValue jsonIetf(String text) {
        return Gnmi.TypedValue.newBuilder().setJsonIetfVal(ByteString.copyFromUtf8(text)).build();
    }

    private List<String> flatten(Gnmi.SubscribeResponse response) throws Exception {
        List<?> leaves = (List<?>) GnmiMappingScript.call(gcl, "LeafFlattener", "flatten", response);
        return leaves.stream().map(Object::toString).sorted().collect(Collectors.toList());
    }

    @Test
    public void joinsPrefixAndUpdatePath() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("interfaces"), elem("interface", "name", "E1")),
                update(path(elem("state"), elem("counters"), elem("in-octets")), Gnmi.TypedValue.newBuilder().setUintVal(5).build()));
        assertThat(flatten(r), contains("/interfaces/interface[name=E1]/state/counters/in-octets = 5 (Long)"));
    }

    @Test
    public void stripsModulePrefixes() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(),
                update(path(elem("openconfig-interfaces:interfaces"), elem("interface", "name", "E1"), elem("openconfig-interfaces:state"), elem("mtu")),
                        Gnmi.TypedValue.newBuilder().setUintVal(1500).build()));
        assertThat(flatten(r), contains("/interfaces/interface[name=E1]/state/mtu = 1500 (Long)"));
    }

    @Test
    public void convertsScalarTypes() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("v")),
                update(path(elem("a-int")), Gnmi.TypedValue.newBuilder().setIntVal(-3).build()),
                update(path(elem("b-uint-max")), Gnmi.TypedValue.newBuilder().setUintVal(-1L).build()),
                update(path(elem("c-bool")), Gnmi.TypedValue.newBuilder().setBoolVal(true).build()),
                update(path(elem("d-float")), Gnmi.TypedValue.newBuilder().setFloatVal(1.5f).build()),
                update(path(elem("e-decimal")), Gnmi.TypedValue.newBuilder()
                        .setDecimalVal(Gnmi.Decimal64.newBuilder().setDigits(1234).setPrecision(2)).build()),
                update(path(elem("f-string")), Gnmi.TypedValue.newBuilder().setStringVal("up").build()),
                update(path(elem("g-bytes")), Gnmi.TypedValue.newBuilder().setBytesVal(ByteString.copyFromUtf8("x")).build()));
        assertThat(flatten(r), contains(
                "/v/a-int = -3 (Long)",
                "/v/b-uint-max = 18446744073709551615 (BigInteger)",
                "/v/c-bool = true (Boolean)",
                "/v/d-float = 1.5 (Double)",
                "/v/e-decimal = 12.34 (BigDecimal)",
                "/v/f-string = up (String)"));
    }

    @Test
    public void flattensJsonIetfObjects() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(),
                update(path(elem("interfaces"), elem("interface", "name", "E1"), elem("state")),
                        jsonIetf("{\"openconfig-interfaces:counters\":{\"in-octets\":\"5\"},\"oper-status\":\"UP\"}")));
        assertThat(flatten(r), contains(
                "/interfaces/interface[name=E1]/state/counters/in-octets = 5 (String)",
                "/interfaces/interface[name=E1]/state/oper-status = UP (String)"));
    }

    @Test
    public void parsesJsonScalars() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("s")),
                update(path(elem("number")), json("15")),
                update(path(elem("text")), json("\"UP\"")));
        assertThat(flatten(r), contains("/s/number = 15 (Integer)", "/s/text = UP (String)"));
    }

    @Test
    public void skipsJsonArrays() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("i")),
                update(path(elem("state")), jsonIetf("{\"addresses\":[{\"ip\":\"10.0.0.1\"}],\"tags\":[\"a\"],\"mtu\":1500}")));
        assertThat(flatten(r), contains("/i/state/mtu = 1500 (Integer)"));
    }

    @Test
    public void malformedJsonSkipsOnlyThatUpdate() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("i")),
                update(path(elem("broken")), jsonIetf("{\"a\":")),
                update(path(elem("ok")), Gnmi.TypedValue.newBuilder().setUintVal(1).build()));
        assertThat(flatten(r), contains("/i/ok = 1 (Long)"));
    }

    @Test
    public void ignoresDeletes() throws Exception {
        Gnmi.SubscribeResponse r = Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder()
                        .setPrefix(path(elem("i")))
                        .addDelete(path(elem("gone")))
                        .addUpdate(update(path(elem("kept")), Gnmi.TypedValue.newBuilder().setUintVal(2).build())))
                .build();
        assertThat(flatten(r), contains("/i/kept = 2 (Long)"));
    }

    @Test
    public void syncResponseHasNoLeaves() throws Exception {
        assertThat(flatten(Gnmi.SubscribeResponse.newBuilder().setSyncResponse(true).build()), is(empty()));
    }
}
