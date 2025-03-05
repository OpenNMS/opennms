package org.opennms.netmgt.provision.persist.requisition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.ValidationException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class RequisitionValidationTest {
    private String name;
    private boolean expected;

    @Parameterized.Parameters
    public static Collection<Object[]> names() {
        return Arrays.asList(new Object[][]{
                {"aaabbb", true},
                {"aaa bbb", true},
                {"aaa-bbb", true},
                {"aaa#bbb", true},
                {"aaa:bbb", false},
                {"aaa/bbb", false},
                {"aaa\\bbb", false},
                {"aaa?bbb", false},
                {"aaa&bbb", false},
                {"aaa*bbb", false},
                {"aaa'bbb", false},
                {"aaa\"bbb", false}
        });
    }

    public RequisitionValidationTest(String name, boolean expected) {
        this.name = name;
        this.expected = expected;
    }

    @Test
    public void validateName() {
        final Requisition req = new Requisition();
        req.setForeignSource(name);
        boolean result = true;
        try {
            req.validate();
        } catch (ValidationException e) {
            result = false;
        }
        System.out.println("Validating requisition '" + name + "' --> " + expected + "/" + result);
        Assert.assertEquals(expected, result);
    }
}
