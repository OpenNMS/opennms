/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opennms.netmgt.poller.monitors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 * @author mvrueden
 */
public class Bunny {

    private Carrot carrot = new Carrot();

    public static class Carrot {

        private String a = "a";
        private String b = "b";
        private Pea pea = new Pea();

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public Pea getPea() {
            return pea;
        }
    }

    public static class Pea {

        private String a = "a";
        private String b = "b";
        private int c = 5;

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }
    }

    public Carrot getCarrot() {
        return carrot;
    }

    public static interface Foo {

        public String bar();

        public String ter();
    }

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException {
//        Bunny bunny = new Bunny();
//        Carrot carrot = new Carrot();
//        System.out.println(PropertyUtils.getProperty(carrot, "pea.c"));
//        PropertyUtils.setProperty(carrot, "pea.c", "7");
//        System.out.println(PropertyUtils.getProperty(carrot, "pea.c"));
//        System.out.println(PropertyUtils.getProperty(carrot, "a"));



        Foo f = (Foo) Proxy.newProxyInstance(
                Foo.class.getClassLoader(),
                new Class[]{Foo.class},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(proxy, args);
            }
        });
        System.out.println(f.bar());
        System.out.println(f.ter());

    }
    
    public static interface Substitutable {
        public void substitute();
    }
}
