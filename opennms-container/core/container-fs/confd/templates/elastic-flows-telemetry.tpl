#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$servers := ""}}
{{range ls "/elastic/flows/hosts"}}{{ $index := (atoi . )}}{{$host := getv (print "/elastic/flows/hosts/" $index "/host")}}{{if $index = 0}}{{$servers := $host}}{{else}}{{$servers = print $host "," $servers}}{{end}}{{end}}
{{$servers := trimSuffix $servers ","}}
{{if $servers}}
ENABLE_TELEMETRYD=TRUE
{{else}}
ENABLE_TELEMETRYD=FALSE
{{end}}
