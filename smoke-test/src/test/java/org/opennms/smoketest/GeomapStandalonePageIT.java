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
package org.opennms.smoketest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.time.Duration;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * End-to-end check that {@code /opennms/geomap/standalone.jsp} works
 * end-to-end. Three regressions live in this page; each test pins one.
 *
 * <ol>
 *   <li>{@code map.jsp}'s inline script used
 *       {@code $('<id>').ready(handler)}, which expanded to
 *       {@code $('map').ready(handler)} -- a tag selector matching the
 *       HTML {@code <map>} element (none on the page) combined with the
 *       jQuery 3.0 removal of {@code $(selector).ready()}, so the
 *       handler never fired and {@code geomap.render()} was never
 *       called. Caught by {@link #leafletMapMounts()}.</li>
 *   <li>{@code standalone.jsp}'s {@code refresh()} subtracted
 *       {@code $("#header").outerHeight()} from the window height to
 *       size {@code #map-container}. The {@code #header} element was
 *       removed when the top navbar moved to the side menu, so
 *       {@code outerHeight()} returned {@code undefined}, the
 *       arithmetic produced {@code NaN}, and the container collapsed
 *       to zero pixels -- leaflet then mounted a zero-size map. The
 *       DOM check alone passes in that state, so caught by the size
 *       assertion in {@link #mapContainerHasNonZeroHeight()}.</li>
 *   <li>The JSP emitted the literal string {@code "null"} for missing
 *       {@code strategy}/{@code severity} query parameters, so the
 *       client-side {@code isUndefinedOrNull} default substitution
 *       did not fire and {@code /api/v2/geolocation/config?strategy=null}
 *       broke marker loading. Caught by
 *       {@link #emitsJsNullLiteralWhenQueryParamsAreMissing()} and
 *       {@link #emitsQuotedJsStringWhenQueryParamsArePresent()},
 *       which inspect the rendered page source -- the broken value
 *       affects the marker POST that fires AFTER initMap, so a
 *       DOM-presence check passes even when the param contract is
 *       wrong.</li>
 * </ol>
 */
public class GeomapStandalonePageIT extends OpenNMSSeleniumIT {

    private static final int MIN_VISIBLE_HEIGHT_PX = 200;

    /**
     * Confirms the rendered page actually contains the post-fix inline
     * script before we wait on a runtime side-effect (the leaflet DOM).
     * Without this, a stale opennms image fails with a vague 30-second
     * timeout instead of a clear "fix not present" message.
     */
    private void assertGeomapJspIsPatched() {
        final String source = getDriver().getPageSource();
        assertThat(
                "Rendered page does not contain the post-fix inline-script form '$(function() {'."
                        + " Either the opennms image under test is older than this PR, or the JSP"
                        + " was not rebuilt. URL=" + getDriver().getCurrentUrl(),
                source,
                containsString("$(function() {"));
    }

    @Test
    public void leafletMapMounts() {
        getDriver().get(getBaseUrlInternal() + "opennms/geomap/standalone.jsp");
        assertGeomapJspIsPatched();

        // Leaflet's _initLayout adds the 'leaflet-container' class to
        // the existing #map div (it does NOT create a child) -- the
        // selector is the compound form '#map.leaflet-container' (same
        // element, no space), not the descendant form. The class is
        // only added after geomap.render() -> loadConfig() -> initMap
        // -> L.map(mapId, ...). If render never runs, the class is
        // never added.
        new WebDriverWait(getDriver(), Duration.ofSeconds(30))
                .until(d -> !d.findElements(By.cssSelector("#map.leaflet-container")).isEmpty());

        // .leaflet-tile-pane IS a descendant -- created by
        // L.tileLayer(...).addTo(map), which only runs after the GET
        // /api/v2/geolocation/config call succeeds. Its presence
        // proves the full init path ran.
        assertThat(
                getDriver().findElements(By.cssSelector("#map .leaflet-tile-pane")).size(),
                greaterThan(0));
    }

    @Test
    public void mapContainerHasNonZeroHeight() {
        getDriver().get(getBaseUrlInternal() + "opennms/geomap/standalone.jsp");
        assertGeomapJspIsPatched();

        // Wait for leaflet to add its class to #map so we know
        // geomap.render ran before we measure size. Compound selector
        // matches the same element; descendant form (with a space)
        // would never match because leaflet annotates #map in place.
        new WebDriverWait(getDriver(), Duration.ofSeconds(30))
                .until(d -> !d.findElements(By.cssSelector("#map.leaflet-container")).isEmpty());

        final WebElement mapContainer = getDriver().findElement(By.id("map-container"));
        final int containerHeight = mapContainer.getRect().getHeight();
        // Bug #2 collapsed this to 0 because refresh() did
        // window.height - footer.outerHeight() - $("#header").outerHeight()
        // and $("#header") returns nothing on a page with the modern
        // side-menu chrome, so .outerHeight() was undefined, the math
        // produced NaN, and .height(NaN) is a no-op. Without #2 fixed,
        // this assertion fails with "expected greater than 200 but was 0"
        // even though leafletMapMounts() succeeds.
        assertThat(
                "Bug #2 regression: #map-container collapsed to zero height -- height-calc subtraction produced NaN",
                containerHeight,
                greaterThan(MIN_VISIBLE_HEIGHT_PX));

        // Also assert leaflet's own container inherited a usable size,
        // so a future regression that decouples #map-container from
        // its descendants gets caught too.
        final WebElement leafletContainer = getDriver().findElement(By.cssSelector("#map.leaflet-container"));
        assertThat(
                "leaflet container collapsed to zero height",
                leafletContainer.getRect().getHeight(),
                greaterThan(MIN_VISIBLE_HEIGHT_PX));
    }

    @Test
    public void emitsJsNullLiteralWhenQueryParamsAreMissing() {
        // Regression target: previously the JSP rendered
        //   strategy: "null", severity: "null"
        // (literal four-character JS strings) when the page was loaded
        // without query parameters, because <%= null %> writes the
        // word "null" inside surrounding double quotes. The geomap-js
        // client only treats the actual JS null value as "absent" via
        // isUndefinedOrNull(...), so the string "null" was forwarded
        // to /api/v2/geolocation/config?strategy=null, breaking marker
        // loading. The fix renders the bare JS null literal (no quotes)
        // when the parameter is missing.
        //
        // We assert this by inspecting the rendered page source rather
        // than runtime behavior because the broken value first
        // surfaces in the marker POST that fires AFTER initMap, so a
        // DOM-presence check passes even when the param contract is
        // wrong (initMap and .leaflet-control are created during
        // config load, not marker load).
        getDriver().get(getBaseUrlInternal() + "opennms/geomap/standalone.jsp");
        final String source = getDriver().getPageSource();

        assertThat(
                "JSP must emit the JS null literal (no quotes) for missing strategy",
                source,
                containsString("strategy: null"));
        assertThat(
                "JSP must emit the JS null literal (no quotes) for missing severity",
                source,
                containsString("severity: null"));
        assertThat(
                "Regression: JSP emitted the literal string \"null\" for strategy",
                source.contains("strategy: \"null\""),
                is(false));
        assertThat(
                "Regression: JSP emitted the literal string \"null\" for severity",
                source.contains("severity: \"null\""),
                is(false));
    }

    @Test
    public void emitsQuotedJsStringWhenQueryParamsArePresent() {
        // The complement of the test above: when params ARE supplied,
        // they must arrive at geomap.render() as quoted JS strings.
        getDriver().get(getBaseUrlInternal()
                + "opennms/geomap/standalone.jsp"
                + "?strategy=Outages&severity=Warning");
        final String source = getDriver().getPageSource();

        assertThat(source, containsString("strategy: \"Outages\""));
        assertThat(source, containsString("severity: \"Warning\""));
    }
}
