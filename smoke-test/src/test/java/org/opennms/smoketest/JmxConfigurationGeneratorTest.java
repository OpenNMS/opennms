package org.opennms.smoketest;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.io.ByteStreams;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Verifies that the Vaadin JMX Configuration Generator Application is deployed correctly.
 */
public class JmxConfigurationGeneratorTest extends OpenNMSSeleniumTestCase {

    private static final String MBEANS_VIEW_TREE_WAIT_NAME = "com.mchange.v2.c3p0";

    @BeforeClass
    public static void beforeClass() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setNormalize(true);
    }

    @Before
    public void before() {
        m_driver.get(BASE_URL + "opennms/admin/jmxConfigGenerator.jsp");
        switchToVaadinFrame();
    }

    @After
    public void after() {
        switchToDefaultFrame();
    }

    @Test
    public void testClickOnHeaderLinkWorks() {
        /*
         * In Vaadin 7 a Navigator was introduced to navigate within Vaadin applications
         * Somehow normal links cause a vaadin exception. This test verifies that no exception
         * is thrown when clicking the Header links
         */
        WebElement element = findElementByLink("1. Service Configuration");
        element.click();

        Collection<WebElement> errorPopups = m_driver.findElements(By.cssSelector("div[class=\"v-errormessage\"]"));
        // sometimes empty v-errormessage divs are present, we have to filter them out
        errorPopups = Collections2.filter(errorPopups, new Predicate<WebElement>() {
            public boolean apply(WebElement input) {
                return input.getText() != null && !input.getText().isEmpty();
            }
        });
        Assert.assertEquals("Error message is present.", 0, errorPopups.size());
    }

    @Test
    public void testNavigation() throws InterruptedException {
        // forwards
        findElementById("port").clear();
        findElementById("port").sendKeys("18980"); // Set OpenNMS JMX port
        findElementById("next").click();

        wait.until(pageContainsText(MBEANS_VIEW_TREE_WAIT_NAME));
        // on the 2nd page we have to deselect the PooledDataSource MBean,
        // because the name is too long and results in a validation error
        selectNodeByName("PooledDataSource", false);
        findElementById("next").click();

        // configuration summary
        wait.until(pageContainsText("collectd-configuration.xml"));

        // backwards
        findElementById("previous").click();
        wait.until(pageContainsText(MBEANS_VIEW_TREE_WAIT_NAME));

        findElementById("previous").click();
        wait.until(pageContainsText("Skip JVM MBeans"));
    }

    /*
     * Verifies that selected CompMembers do show up in the generated jmx-datacollection-config.xml snippet.
     */
    @Test
    public void verifyCompMemberSelection() throws InterruptedException, IOException, SAXException {
        // forwards
        findElementById("port").clear();
        findElementById("port").sendKeys("18980"); // set OpenNMS JMX port
        findElementByXpath("//span[@id='skipDefaultVM']/input").click(); // deselect

        // go to next page
        findElementById("next").click();
        wait.until(pageContainsText(MBEANS_VIEW_TREE_WAIT_NAME));

        selectNodeByName("PS MarkSweep", true);

        // go to last page
        findElementById("next").click();
        wait.until(pageContainsText("collectd-configuration.xml"));

        // switch to jmx-datacollection-config.xml tab
        findElementByXpath("//div[text()='jmx-datacollection-config.xml']").click();
        wait.until(pageContainsText("JMXMP protocol."));

        // verify output
        final String jmxDatacollectionConfigContent = findElementByXpath("//textarea").getAttribute("value");
        try ( ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ByteStreams.copy(getClass().getResourceAsStream("/expected-jmx-datacollection-config.xml"), outputStream);
            XMLAssert.assertXMLEqual(outputStream.toString(), jmxDatacollectionConfigContent);
        }
    }

    // switches to the embedded vaadin iframe
    private void switchToVaadinFrame() {
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
    }

    // go back to the content "frame"
    private void switchToDefaultFrame() {
        m_driver.switchTo().defaultContent();
    }

    private void selectNodeByName(final String name, boolean select) throws InterruptedException {
        final String treeNodeXpath = String.format("//span[contains(., '%s')]", name);
        final String deselectXpath = "//div[@id='PID_VAADIN_CM']//div[text()='deselect']";
        final String selectXpath = "//div[@id='PID_VAADIN_CM']//div[text()='select']";

        findElementByXpath(treeNodeXpath).click();

        // F**king Vaadin redraws the *entire* tree with new DOM so it turns out the new
        // PooledDataSource span usually isn't there yet after the last click, even if
        // we try waiting for it, since Selenium will return the cached one rather than
        // a new one. And guess what? You can't clear the cache.
        // So, ugh.  Sleep.  :/
        Thread.sleep(5000);

        new Actions(m_driver).contextClick(findElementByXpath(treeNodeXpath)).perform(); // right click
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(deselectXpath)));

        // deselect/select Element depending on the value of select.
        findElementByXpath(select ? selectXpath : deselectXpath).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("previous")));
    }
}
