package org.opennms.netmgt.provision;

public interface AsyncDetectorFactory <T extends AsyncServiceDetector> {
    T createDetector();
}
