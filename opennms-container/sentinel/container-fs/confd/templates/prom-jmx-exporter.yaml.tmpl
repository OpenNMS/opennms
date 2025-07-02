#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$promJmxPath := "/java/agent/prom-jmx-exporter" -}}
startDelaySeconds: {{getv (print $promJmxPath "/startDelaySeconds") "0"}}
lowercaseOutputName: {{getv (print $promJmxPath "/lowerCaseOutputName") "true"}}
lowercaseOutputLabelNames: {{getv (print $promJmxPath "/lowercaseOutputLabelNames") "true"}}
autoExcludeObjectNameAttributes: {{getv (print $promJmxPath "/autoExcludeObjectNameAttributes") "true"}}

{{$woSize := len (getvs (print $promJmxPath "/includeObjectNames/*")) -}}
{{if gt $woSize 0 -}}
includeObjectNames:
{{range getvs (print $promJmxPath "/includeObjectNames/*") -}}
- "{{.}}"
{{end -}}
{{end -}}

{{$boSize := len (getvs (print $promJmxPath "/excludeObjectNames/*")) -}}
{{if gt $boSize 0 -}}
excludeObjectNames:
{{range getvs (print $promJmxPath "/excludeObjectNames/*") -}}
- "{{.}}"
{{end -}}
{{end -}}

rules:
- pattern: org\.opennms\..+\.(.+)<name=(.+)><>Value
  name: minion_$1_$2
  type: GAUGE

- pattern: org\.opennms\..+\.(.+)<name=(.+)><>Count
  name: minion_$1_$2_count
  type: COUNTER

- pattern: org\.opennms\..+\.(.+)<name=(.+)><>(\d+)thPercentile
  name: minion_$1_$2
  type: GAUGE
  labels:
    quantile: "0.$3"
