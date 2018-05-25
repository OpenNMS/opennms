/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.smoketest;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

@Ignore("Flapping. See NMS-10129.")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ThresholdEditorIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        thresholdsPage();
    }
    protected void thresholdsPage() {
        m_driver.get(getBaseUrl() + "opennms/admin/thresholds/index.htm");
    }
    @Test
    public void a_testAllTextIsPresent() throws Exception {
        // main threshold page
        assertEquals(1, countElementsMatchingCss("h3.panel-title"));
        findElementByXpath("//h3[text()='Threshold Configuration']");
        findElementByXpath("//button[@type='button' and text()='Request a reload threshold packages configuration']");
        // threshold group page
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(3, countElementsMatchingCss("h3.panel-title"));
        findElementByXpath("//h3[text()='Basic Thresholds']");
        findElementByXpath("//a[text()='Create New Threshold']");
        findElementByXpath("//h3[text()='Expression-based Thresholds']");
        findElementByXpath("//a[text()='Create New Expression-based Threshold']");
        findElementByXpath("//h3[text()='Help']");
    }
    @Test
    public void b_testCreateNewThreshold() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(5, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
        clickElement(By.xpath("//a[text()='Create New Threshold']"));
        // "type" select field
        final Select threshType = new Select(findElementByName("type"));
        threshType.selectByVisibleText("rearmingAbsoluteChange");
        // "datasource" input field
        enterText(By.name("dsName"), "foo");
        // "datasource type" select field
        final Select datasourceType = new Select(findElementByName("dsType"));
        datasourceType.selectByVisibleText("BGP Peer");
        // "datasource label" input field
        enterText(By.name("dsLabel"), "bar");
        // "value" input field
        enterText(By.name("value"), "0.1");
        // "re-arm" input field
        enterText(By.name("rearm"), "0.2");
        // "trigger" input field
        enterText(By.name("trigger"), "2");
        // "description" input field
        enterText(By.name("description"), "baz");
        // "triggered uei" field
        enterText(By.name("triggeredUEI"), "uei.opennms.org/triggered");
        // "re-armed uei" field
        enterText(By.name("rearmedUEI"), "uei.opennms.org/rearmed");
        // save the threshold
        clickElement(By.xpath(("//input[@value='Save']")));
        findElementByXpath("//h3[text()='Basic Thresholds']");
        // make sure all the visible fields on the threshold page match
        assertEquals(6, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
        assertEquals("rearmingAbsoluteChange", findElementByCss("td[name='threshold.4.type'").getText());
        assertEquals("baz", findElementByCss("td[name='threshold.4.description'").getText());
        assertEquals("foo", findElementByCss("td[name='threshold.4.dsName'").getText());
        assertEquals("bgpPeerEntry", findElementByCss("td[name='threshold.4.dsType'").getText());
        assertEquals("bar", findElementByCss("td[name='threshold.4.dsLabel'").getText());
        assertEquals("0.1", findElementByCss("td[name='threshold.4.value'").getText());
        assertEquals("0.2", findElementByCss("td[name='threshold.4.rearm'").getText());
        assertEquals("2", findElementByCss("td[name='threshold.4.trigger'").getText());
        assertEquals("uei.opennms.org/triggered", findElementByCss("td[name='threshold.4.triggeredUEI'").getText());
        assertEquals("uei.opennms.org/rearmed", findElementByCss("td[name='threshold.4.rearmedUEI'").getText());
    }
    @Test
    public void c_testEditThreshold() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(6, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
        clickElement(By.xpath("//tr[@name='threshold.4']//a[text()='Edit']"));
        // "triggered uei" field
        enterText(By.name("triggeredUEI"), "uei.opennms.org/so-very-triggered");
        // "rearmed uei" field
        enterText(By.name("rearmedUEI"), "uei.opennms.org/you-call-that-rearmed");
        // create a field
        enterText(By.name("filterField"), "a");
        enterText(By.name("filterRegexp"), "anchor");
        clickElement(By.xpath(("//input[@value='Add']")));
        // create a second field
        enterText(By.name("filterField"), "b");
        enterText(By.name("filterRegexp"), "boat");
        clickElement(By.xpath(("//input[@value='Add']")));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("a", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("b", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // move boat up
        clickElement(By.xpath("//tr[@name='filter.2']//input[@value='Up']"));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("b", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("a", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // move boat back down
        clickElement(By.xpath("//tr[@name='filter.1']//input[@value='Down']"));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("a", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("b", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // delete boat
        clickElement(By.xpath("//tr[@name='filter.2']//input[@value='Delete']"));
        assertElementDoesNotExist(By.xpath("//tr[@name='filter.2']"));
        clickElement(By.xpath(("//input[@value='Save']")));
        findElementByXpath("//h3[text()='Basic Thresholds']");
        assertEquals(6, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
        assertEquals("rearmingAbsoluteChange", findElementByCss("td[name='threshold.4.type'").getText());
        assertEquals("baz", findElementByCss("td[name='threshold.4.description'").getText());
        assertEquals("foo", findElementByCss("td[name='threshold.4.dsName'").getText());
        assertEquals("bgpPeerEntry", findElementByCss("td[name='threshold.4.dsType'").getText());
        assertEquals("bar", findElementByCss("td[name='threshold.4.dsLabel'").getText());
        assertEquals("0.1", findElementByCss("td[name='threshold.4.value'").getText());
        assertEquals("0.2", findElementByCss("td[name='threshold.4.rearm'").getText());
        assertEquals("2", findElementByCss("td[name='threshold.4.trigger'").getText());
        assertEquals("uei.opennms.org/so-very-triggered", findElementByCss("td[name='threshold.4.triggeredUEI'").getText());
        assertEquals("uei.opennms.org/you-call-that-rearmed", findElementByCss("td[name='threshold.4.rearmedUEI'").getText());
    }
    @Test
    public void d_testDeleteThreshold() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(6, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
        clickElement(By.xpath("//tr[@name='threshold.4']//a[text()='Delete']"));
        Thread.sleep(50);
        waitForElement(By.xpath("//h3[text()='Basic Thresholds']"));
        assertEquals(5, countElementsMatchingCss(".edit-group-basic-thresholds tr"));
    }
    @Test
    public void e_testCreateNewExpression() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(1, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
        clickElement(By.xpath("//a[text()='Create New Expression-based Threshold']"));
        // "type" select field
        final Select threshType = new Select(findElementByName("type"));
        threshType.selectByVisibleText("rearmingAbsoluteChange");
        // "expression" input field
        enterText(By.name("expression"), "foo");
        // "datasource type" select field
        final Select datasourceType = new Select(findElementByName("dsType"));
        datasourceType.selectByVisibleText("BGP Peer");
        // "datasource label" input field
        enterText(By.name("dsLabel"), "bar");
        // "value" input field
        enterText(By.name("value"), "0.1");
        // "re-arm" input field
        enterText(By.name("rearm"), "0.2");
        // "trigger" input field
        enterText(By.name("trigger"), "2");
        // "description" input field
        enterText(By.name("description"), "baz");
        // "triggered uei" field
        enterText(By.name("triggeredUEI"), "uei.opennms.org/triggered");
        // "re-armed uei" field
        enterText(By.name("rearmedUEI"), "uei.opennms.org/rearmed");
        // save the threshold
        clickElement(By.xpath(("//input[@value='Save']")));
        findElementByXpath("//h3[text()='Basic Thresholds']");
        // make sure all the visible fields on the threshold page match
        assertEquals(2, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
        assertEquals("rearmingAbsoluteChange", findElementByCss("td[name='expression.0.type'").getText());
        assertEquals("baz", findElementByCss("td[name='expression.0.description'").getText());
        assertEquals("foo", findElementByCss("td[name='expression.0.expression'").getText());
        assertEquals("bgpPeerEntry", findElementByCss("td[name='expression.0.dsType'").getText());
        assertEquals("bar", findElementByCss("td[name='expression.0.dsLabel'").getText());
        assertEquals("0.1", findElementByCss("td[name='expression.0.value'").getText());
        assertEquals("0.2", findElementByCss("td[name='expression.0.rearm'").getText());
        assertEquals("2", findElementByCss("td[name='expression.0.trigger'").getText());
        assertEquals("uei.opennms.org/triggered", findElementByCss("td[name='expression.0.triggeredUEI'").getText());
        assertEquals("uei.opennms.org/rearmed", findElementByCss("td[name='expression.0.rearmedUEI'").getText());
    }
    @Test
    public void f_testEditExpression() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(2, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
        clickElement(By.xpath("//tr[@name='expression.0']//a[text()='Edit']"));
        // "triggered uei" field
        enterText(By.name("triggeredUEI"), "uei.opennms.org/so-very-triggered");
        // "rearmed uei" field
        enterText(By.name("rearmedUEI"), "uei.opennms.org/you-call-that-rearmed");
        // create a field
        enterText(By.name("filterField"), "a");
        enterText(By.name("filterRegexp"), "anchor");
        clickElement(By.xpath(("//input[@value='Add']")));
        // create a second field
        enterText(By.name("filterField"), "b");
        enterText(By.name("filterRegexp"), "boat");
        clickElement(By.xpath(("//input[@value='Add']")));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("a", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("b", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // move boat up
        clickElement(By.xpath("//tr[@name='filter.2']//input[@value='Up']"));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("b", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("a", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // move boat back down
        clickElement(By.xpath("//tr[@name='filter.1']//input[@value='Down']"));
        // make sure we have 2 rows with a=anchor and b=boat
        assertEquals("a", findElementByXpath("(//tr[@name='filter.1']//input)[1]").getAttribute("value"));
        assertEquals("anchor", findElementByXpath("(//tr[@name='filter.1']//input)[2]").getAttribute("value"));
        assertEquals("b", findElementByXpath("(//tr[@name='filter.2']//input)[1]").getAttribute("value"));
        assertEquals("boat", findElementByXpath("(//tr[@name='filter.2']//input)[2]").getAttribute("value"));
        // delete boat
        clickElement(By.xpath("//tr[@name='filter.2']//input[@value='Delete']"));
        assertElementDoesNotExist(By.xpath("//tr[@name='filter.2']"));
        clickElement(By.xpath(("//input[@value='Save']")));
        findElementByXpath("//h3[text()='Basic Thresholds']");
        assertEquals(2, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
        assertEquals("rearmingAbsoluteChange", findElementByCss("td[name='expression.0.type'").getText());
        assertEquals("baz", findElementByCss("td[name='expression.0.description'").getText());
        assertEquals("foo", findElementByCss("td[name='expression.0.expression'").getText());
        assertEquals("bgpPeerEntry", findElementByCss("td[name='expression.0.dsType'").getText());
        assertEquals("bar", findElementByCss("td[name='expression.0.dsLabel'").getText());
        assertEquals("0.1", findElementByCss("td[name='expression.0.value'").getText());
        assertEquals("0.2", findElementByCss("td[name='expression.0.rearm'").getText());
        assertEquals("2", findElementByCss("td[name='expression.0.trigger'").getText());
        assertEquals("uei.opennms.org/so-very-triggered", findElementByCss("td[name='expression.0.triggeredUEI'").getText());
        assertEquals("uei.opennms.org/you-call-that-rearmed", findElementByCss("td[name='expression.0.rearmedUEI'").getText());
    }
    @Test
    public void g_testDeleteExpression() throws Exception {
        clickElement(By.xpath("//a[contains(@href, 'groupName=cisco&editGroup')]"));
        assertEquals(2, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
        clickElement(By.xpath("//tr[@name='expression.0']//a[text()='Delete']"));
        Thread.sleep(50);
        waitForElement(By.xpath("//h3[text()='Basic Thresholds']"));
        assertEquals(1, countElementsMatchingCss(".edit-group-expression-based-thresholds tr"));
    }
}
