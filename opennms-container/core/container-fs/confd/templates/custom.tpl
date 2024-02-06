#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$elastic_servers := ""}}
{{range ls "/elastic/flows/hosts"}}{{ $index := (atoi . )}}{{$host := getv (print "/elastic/flows/hosts/" $index "/host")}}{{if $index = 0}}{{$elastic_servers := $host}}{{else}}{{$elastic_servers = print $host "," $elastic_servers}}{{end}}{{end}}
{{if $elastic_servers}}
ENABLE_TELEMETRYD=true
{{else}}
ENABLE_TELEMETRYD=false
{{end}}

{{range $custom := gets "/opennms/custom/*"}}
{{base .Key}}={{.Value}}{{end}}
{{range $custom := gets "/opennms/daemons/*"}}
ENABLE_{{toUpper (base .Key)}}={{.Value}}{{end}}
