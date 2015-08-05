package org.opennms.smoketest;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Verifies that the Vaadin JMX Configuration Generator Application is deployed correctly.
 */
public class JmxConfigurationGeneratorTest extends OpenNMSSeleniumTestCase {

    private static final String MBEANS_VIEW_TREE_WAIT_NAME = "com.mchange.v2.c3p0";

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
        // we have to wait, until the field is really cleared, otherwise
        // the port might not have been cleared
        wait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return "".equals(findElementById("port").getText());
            }
        });
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
        JmxDatacollectionConfig config = JAXB.unmarshal(new ByteArrayInputStream(jmxDatacollectionConfigContent.getBytes()), JmxDatacollectionConfig.class);

        Assert.assertNotNull(config);
        Assert.assertFalse(config.getJmxCollection().isEmpty());
        Assert.assertNotNull(config.getJmxCollection().get(0).getMbeans());
        Assert.assertEquals(1, config.getJmxCollection().get(0).getMbeans().getMbean().size());

        final Mbean mbean = config.getJmxCollection().get(0).getMbeans().getMbean().get(0);
        Assert.assertEquals(1, mbean.getCompAttrib().size());
        Assert.assertEquals(5, mbean.getCompAttrib().get(0).getCompMember().size());
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
