import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLEdgeStatus;

Map<String, String> style = new HashMap<String, String>();
OnmsSeverity severity = OnmsSeverity.NORMAL;

/*
Color/format of links:

- Solid thick green = Link is up -> solid thick line
- Link at 80% utilization -> solid thick yellow
- Link is in 'Build' status -> solid thin blue line
- Broken link -> thick dashed line
*/

thickGreen  = [ 'stroke' : 'green', 'stroke-width' : '6' ];
thickYellow = [ 'stroke' : 'yellow', 'stroke-width' : '6' ];
thinBlue    = [ 'stroke' : 'blue' ];
thickDashed = [ 'stroke-dasharray' : '5,5', 'stroke-width' : '6' ];

String interfaceName = edge.getProperties().get("interface");

if (interfaceName != null && sourceNode != null) {
    List<Double> inOut = measurements.computeUtilization(sourceNode, interfaceName);
    double inPercent = inOut.get(0);
    double outPercent = inOut.get(1);
    double maxPercent = Math.max(inPercent, outPercent);

    if (maxPercent >= 80.0) {
        style = thickYellow;
    }
}

return new GraphMLEdgeStatus(severity, style);
