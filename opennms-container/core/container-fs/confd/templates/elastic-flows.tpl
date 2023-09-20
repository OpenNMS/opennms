#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$servers := ""}}
{{range ls "/elastic/flows/hosts"}}{{ $index := (atoi . )}}{{$host := getv (print "/elastic/flows/hosts/" $index "/host")}}{{if $index = 0}}{{$servers := $host}}{{else}}{{$servers = print $host "," $servers}}{{end}}{{end}}
{{$servers := trimSuffix $servers ","}}
{{if $servers}}
elasticUrl={{$servers}}
{{if exists "/opennms/instance_id"}}
indexPrefix={{getv "/opennms/instance_id" ""}}{{end}}
{{range $custom := gets "/elastic/flows/config/*"}}
{{base .Key}}={{.Value}}{{end}}
{{end}}
