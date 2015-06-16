package org.opennms.smoketest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

/**
 * Verifies that the Vaadin JMX Configuration Generator Application is deployed correctly.
 */
public class JmxConfigurationGeneratorTest extends OpenNMSSeleniumTestCase {

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
        WebElement element = m_driver.findElement(By.linkText("1. Introduction"));
        element.click();

        List<WebElement> errorPopups = m_driver.findElements(By.cssSelector("div[class=\"v-errormessage\"]"));
        Assert.assertEquals("Error message is present.", 0, errorPopups.size());
    }

    @Test
    public void testNavigation() {
        // forwards
        m_driver.findElement(By.id("next")).click();
        m_driver.findElement(By.id("next")).click();
        deselectNode();
        m_driver.findElement(By.id("next")).click();

        // backwards
        m_driver.findElement(By.id("previous")).click();
        m_driver.findElement(By.id("previous")).click();
        m_driver.findElement(By.id("previous")).click();
    }

    // switches to the embedded vaadin iframe
    private void switchToVaadinFrame() {
        List<WebElement> iframeElements = m_driver.findElements(By.xpath("/html/body/div/iframe"));
        if (iframeElements.isEmpty()) {
            Assert.fail("No iframe found. Cannot switch to Vaadin iframe.");
        }
        if (iframeElements.size() > 1) {
            Assert.fail("More than 1 iframe found. Cannot switch to Vaadin iframe.");
        }
        m_driver.switchTo().frame(iframeElements.get(0));
    }

    // go back to the content "frame"
    private void switchToDefaultFrame() {
        m_driver.switchTo().defaultContent();
    }


    // on the 3nd page we have to deselect the PooledDataSource mbean, because the name is too long and
    // resulted in a validation error
    private void deselectNode() {
        final WebElement treeNode = m_driver.findElement(By.xpath("//span[contains(., 'PooledDataSource')]"));
        new Actions(m_driver).contextClick(treeNode).perform(); // right click
        WebElement deselectItem = m_driver.findElement(By.xpath("//div[@id='PID_VAADIN_CM']//div[text()='deselect']"));
        deselectItem.click();
    }
}
