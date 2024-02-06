#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$servers := getvs "/opennms/kafka/consumer/bootstrap.servers/*"}}
{{if $servers }}
bootstrap.servers={{join $servers ","}}{{end}}
{{range $custom := gets "/opennms/kafka/consumer/config/*"}}{{base .Key}}={{.Value}}{{end}}
