#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$promJmxPath := "/java/agent/prom-jmx-exporter" -}}
startDelaySeconds: {{getv (print $promJmxPath "/startDelaySeconds") "0"}}
lowercaseOutputName: {{getv (print $promJmxPath "/lowerCaseOutputName") "true"}}
lowercaseOutputLabelNames: {{getv (print $promJmxPath "/lowercaseOutputLabelNames") "true"}}
autoExcludeObjectNameAttributes: {{getv (print $promJmxPath "/autoExcludeObjectNameAttributes") "true"}}

# The specific list of scheduled objects for Pollerd and Collectd are not exposed to Prometheus.
excludeObjectNameAttributes:
  "OpenNMS:Name=Pollerd":
    - "Schedule"
  "OpenNMS:Name=Collectd":
    - "Schedule"
{{$ioSize := len (getvs (print $promJmxPath "/includeObjectNames/*")) -}}
{{if gt $ioSize 0 -}}
includeObjectNames:
{{range getvs (print $promJmxPath "/includeObjectNames/*") -}}
  - "{{.}}"
{{end -}}
{{else -}}
includeObjectNames:
  - "OpenNMS:*"
  - "org.opennms.*:*"
  - "org.opennms.newts:name=ring-buffer*"
  - "org.opennms.newts:name=repository.samples-inserted*"
  - "org.opennms.core.ipc.rpc:*"
  - "com.zaxxer.hikari:*"
{{end -}}

# Exclude noisy Kafka metrics
excludeObjectNames:
  - "kafka.consumer:*"
  - "kafka.consumer.*:*"
  - "org.apache.karaf:*"
  - "org.apache.karaf.*:*"

# The specific list of scheduled objects for Pollerd and Collectd are not exposed to Prometheus.
excludeObjectNameAttributes:
  "OpenNMS:Name=Pollerd":
    - "Schedule"
  "OpenNMS:Name=Collectd":
    - "Schedule"

rules:
  # IMPORTANT: Exclude kafka.consumer first - rules are processed in order
  - pattern: "kafka\\.consumer.*"
    name: ""
  - pattern: ".*kafka\\.consumer.*"
    name: ""
  - pattern: "apache\\.karaf.*"
    name: ""
  - pattern: ".*apache\\.karaf.*"
    name: ""
  - pattern: "OpenNMS.*"
  - pattern: "org.opennms.*"
  - pattern: "com.zaxxer.hikari.*"
  - pattern: "org.opennms.core.ipc.rpc<name=([^.,]*).([^.,]*).([^.,]*).*Count*"
    name: org_opennms_core_ipc_rpc_count
    labels:
      destination: $1
      type: $2
      metric: $3
  - pattern: "org.opennms.core.ipc.rpc<name=([^.,]*).([^.,]*).([^.,]*).*Mean*"
    name: org_opennms_core_ipc_rpc_mean
    labels:
      destination: $1
      type: $2
      metric: $3
