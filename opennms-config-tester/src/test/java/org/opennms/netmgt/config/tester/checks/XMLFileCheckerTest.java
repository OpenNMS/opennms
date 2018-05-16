package org.opennms.netmgt.config.tester.checks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

public class XMLFileCheckerTest {

    @Test
    public void shouldSucceedOnUnknownFileWithCorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><element></element>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }

    @Test(expected = ConfigCheckValidationException.class)
    public void shouldFailOnUnknownFileWithIncorrectSyntax() throws IOException {
        File file = saveXmltoFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><elementThatIsNotClosed>");
        XMLFileChecker.checkFile(file.toPath()).forSyntax();
    }


    private File saveXmltoFile(String xml) throws IOException{
        File file = File.createTempFile(this.getClass().getSimpleName(), ".xml");
        Writer writer = new FileWriter(file);
        writer.write(xml);
        writer.close();
        return file;
    }

}