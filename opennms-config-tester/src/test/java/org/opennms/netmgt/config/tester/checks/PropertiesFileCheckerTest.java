package org.opennms.netmgt.config.tester.checks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PropertiesFileCheckerTest {


    @Test
    public void shouldSucceedOnValidConfig() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        testProperty(properties, Lists.newArrayList(new NotEmptyConfigEntryDefinition("key", true)));
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnMissingMandatoryConfig() throws IOException {
        Properties properties = new Properties();
        testProperty(properties, Lists.newArrayList(new NotEmptyConfigEntryDefinition("key", true)));
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnDefinitionThatIsNotMet() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key", ""); // empty value
        testProperty(properties, Lists.newArrayList(new NotEmptyConfigEntryDefinition("key", true)));
    }

    @Test
    public void shouldSucceedOnUnknownFileWithCorrectSyntax() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key", "abc");
        testProperty(properties);
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnUnknownFileWithIncorrectSyntax() throws IOException {
        // we need to create the properties file manually to be able to make a corrupt one:
        String properties="key=\\u005";
        File file = createPropertiesFile();
        FileWriter writer = new FileWriter(file);
        writer.write(properties);
        writer.close();
        PropertiesFileChecker.checkFile(file.toPath()).forSyntax();
    }

    private void testProperty(Properties properties) throws IOException {
        File file = savePropertiesToFile(properties);
        PropertiesFileChecker.checkFile(file.toPath()).forSyntax();
    }

    private void testProperty(Properties properties, Collection<ConfigEntryDefinition> definitions) throws IOException {

        File file = savePropertiesToFile(properties);
        PropertiesFileChecker.checkFile(file.toPath()).against(new ConfigCheck() {
            @Override
            public String getFilename() {
                return file.getName();
            }

            @Override
            public Collection<ConfigEntryDefinition> getChecks() {
                return definitions;
            }
        });
    }

    private File savePropertiesToFile(Properties properties) throws IOException{
        File file = createPropertiesFile();
        properties.store(new FileOutputStream(file), "");
        return file;
    }

    private File createPropertiesFile() throws IOException{
        return File.createTempFile(this.getClass().getSimpleName(), ".properties");
    }


}