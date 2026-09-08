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
package org.opennms.protocols.json.collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.Pointer;
import org.junit.Assert;
import org.junit.Test;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * The Class JsonXpathRewriterTest.
 *
 * @author <a href="mailto:christian@opennms.com">Christian Pape</a>
 */
public class JsonXpathRewriterTest {

    /**
     * Asserts that the given expression is returned untouched, as the very same instance.
     *
     * @param xpath the expression
     */
    private static void assertUnchanged(final String xpath) {
        Assert.assertSame(xpath, JsonXpathRewriter.rewrite(xpath));
    }

    /**
     * Asserts that the given expression is rewritten as expected.
     *
     * @param expected the expected result
     * @param xpath the expression
     */
    private static void assertRewritten(final String expected, final String xpath) {
        Assert.assertEquals(expected, JsonXpathRewriter.rewrite(xpath));
    }

    @Test
    public void testNullAndEmpty() {
        Assert.assertNull(JsonXpathRewriter.rewrite(null));
        assertUnchanged("");
    }

    /**
     * Every expression used by the collections shipped in src/main/etc and by the existing JSON
     * tests has to come back untouched.
     */
    @Test
    public void testExistingExpressionsAreUnchanged() {
        final String[] expressions = new String[] {
                "/",
                "/zones/zone",
                "@name",
                "parameter[@key='nproc']/@value",
                "/elements",
                "@it",
                "val",
                "/records",
                "input",
                "read",
                "result",
                "totalNumberOfConnections",
                "connectionLimit",
                "/indices/shards/index/primaries",
                "/nodes/process/open_file_descriptors",
                "/nodes/jvm/mem",
                "available_in_bytes",
                "cluster_name",
                "count",
                "max",
                "min",
                "total",
                "version",
                "vm_name",
                "r[@p=1]",
                "@measObjLdn",
                "/measCollecFile/fileFooter/measCollec/@endTime",
                "/measCollecFile/measData/measInfo[@measInfoId='gb|ns']/measValue",
                "/measCollecFile/measData/measInfo[@measInfoId='platform-network|Network']/measValue"
        };
        for (final String expression : expressions) {
            assertUnchanged(expression);
        }
    }

    @Test
    public void testDigitLeadingKeys() {
        assertRewritten("/stats/*[name()='2xx']/count", "/stats/2xx/count");
        assertRewritten("*[name()='2xx']", "2xx");
        assertRewritten("/*[name()='1st']/*[name()='2nd']/*[name()='3rd']/v", "/1st/2nd/3rd/v");
        assertRewritten("/*[name()='5xx']", "/5xx");
    }

    @Test
    public void testAbsoluteRelativeAndDoubleSlash() {
        assertUnchanged("/");
        assertRewritten("/*[name()='2xx']", "/2xx");
        assertRewritten("//*[name()='2xx']", "//2xx");
        assertRewritten("a/*[name()='2b']", "a/2b");
        assertRewritten("/*[name()='2a']/", "/2a/");
    }

    @Test
    public void testBareNumberStep() {
        assertRewritten("/*[name()='2']", "/2");
        assertRewritten("/a/*[name()='200']", "/a/200");
    }

    @Test
    public void testPredicatesArePreserved() {
        assertRewritten("/*[name()='2list'][1]/v", "/2list[1]/v");
        assertRewritten("*[name()='2a'][@b='c'][2]", "2a[@b='c'][2]");
        assertRewritten("/*[name()='2a'][b/c='x']", "/2a[b/c='x']");
        assertRewritten("/*[name()='2a'][@n=']']", "/2a[@n=']']");
    }

    /**
     * Nothing inside a predicate, inside a function call or inside a string literal may be treated
     * as a location step.
     */
    @Test
    public void testPredicateContentsAreNotTokenized() {
        assertRewritten("/a[b/c='x']/*[name()='2d']", "/a[b/c='x']/2d");
        assertRewritten("/x[substring(a/b,1)]/*[name()='2c']", "/x[substring(a/b,1)]/2c");
        assertUnchanged("/a[@id='2xx']");
        assertUnchanged("/a[@n='2/3']");
        assertRewritten("/measInfo[@measInfoId='gb|ns']/*[name()='2vals']", "/measInfo[@measInfoId='gb|ns']/2vals");
    }

    @Test
    public void testAttributeAxis() {
        assertRewritten("@*[name()='2xx']", "@2xx");
        assertRewritten("/a/@*[name()='2b']", "/a/@2b");
        assertUnchanged("@it");
    }

    @Test
    public void testSkippedConstructsAndIdempotence() {
        final String[] expressions = new String[] {
                ".",
                "..",
                "*",
                "node()",
                "text()",
                "count(/a/b)",
                "child::foo",
                "/self::node()[@name='2xx']",
                "/descendant-or-self::node()/a",
                "$var",
                "/a | /b",
                "/*[name()='2xx']",
                "/stats/*[name()='2xx']/count"
        };
        for (final String expression : expressions) {
            assertUnchanged(expression);
        }
        // rewriting twice must not change anything a second time
        final String once = JsonXpathRewriter.rewrite("/stats/2xx/count");
        Assert.assertSame(once, JsonXpathRewriter.rewrite(once));
    }

    @Test
    public void testQuotingOfKeys() {
        assertRewritten("/*[name()=\"o'brien\"]", "/o'brien");
        assertRewritten("/*[name()='say\"hi\"']", "/say\"hi\"");
        assertRewritten("/*[name()=concat('q', \"'\", 'd\"x')]", "/q'd\"x");
        Assert.assertEquals("'plain'", JsonXpathRewriter.toXPathLiteral("plain"));
        Assert.assertEquals("\"o'brien\"", JsonXpathRewriter.toXPathLiteral("o'brien"));
        Assert.assertEquals("concat('q', \"'\", 'd\"x')", JsonXpathRewriter.toXPathLiteral("q'd\"x"));
    }

    @Test
    public void testIsNcName() {
        for (final String valid : new String[] { "a", "_a", "a-b", "a.b", "a_b1", "count", "div", "or", "not", "gr\u00f6\u00dfe" }) {
            Assert.assertTrue(valid, JsonXpathRewriter.isNcName(valid));
        }
        for (final String invalid : new String[] { "", "2xx", "2", "-a", ".a", "a b", "a:b", "o'brien", "1min" }) {
            Assert.assertFalse(invalid, JsonXpathRewriter.isNcName(invalid));
        }
    }

    /**
     * The reason the rewritten form is <code>*[name()='...']</code> and not JXPath's own
     * <code>.[@name='...']</code> pointer syntax: only the wildcard form fans an array out into one
     * pointer per element, which is what a multi-instance resource-xpath depends on. If this test
     * ever fails because someone changed the replacement, the collector would silently create a
     * single resource where it used to create one per array entry.
     */
    @Test
    public void testRewrittenExpressionsBehaveLikePlainSteps() {
        final JSONObject json = (JSONObject) JSONSerializer.toJSON(
                "{\"records\":[{\"v\":1},{\"v\":2},{\"v\":3}],\"2records\":[{\"v\":1},{\"v\":2},{\"v\":3}]}");
        final JXPathContext context = JXPathContext.newContext(json);
        context.setLenient(true);

        try {
            context.iteratePointers("/2records").hasNext();
            Assert.fail("JXPath is expected to reject a location step starting with a digit");
        } catch (final JXPathInvalidSyntaxException e) {
            // expected, that is the bug being worked around
        }

        Assert.assertEquals(values(context, "/records"), values(context, JsonXpathRewriter.rewrite("/2records")));
        Assert.assertEquals(3, values(context, JsonXpathRewriter.rewrite("/2records")).size());
    }

    /**
     * A rewritten expression must keep returning null instead of throwing when the key is absent,
     * which is what the collection handler relies on. That is what the lenient context is for.
     */
    @Test
    public void testMissingKeysAreLenient() {
        final JSONObject json = (JSONObject) JSONSerializer.toJSON("{\"a\":1}");
        final JXPathContext context = JXPathContext.newContext(json);
        context.setLenient(true);
        Assert.assertNull(context.getValue("/missing"));
        Assert.assertNull(context.getValue(JsonXpathRewriter.rewrite("/2missing")));
    }

    /**
     * Collects the "v" value of every resource the given resource-xpath selects.
     *
     * @param context the JXPath context
     * @param xpath the resource xpath
     * @return the values, in order
     */
    private static List<Object> values(final JXPathContext context, final String xpath) {
        final List<Object> values = new ArrayList<>();
        final Iterator<?> itr = context.iteratePointers(xpath);
        while (itr.hasNext()) {
            values.add(context.getRelativeContext((Pointer) itr.next()).getValue("v"));
        }
        return values;
    }
}
