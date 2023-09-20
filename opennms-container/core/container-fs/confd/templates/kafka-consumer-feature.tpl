#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$servers := ls "/opennms/kafka/consumer/bootstrap_servers"}}
{{if $servers}}
opennms-kafka-consumer
{{end}}
