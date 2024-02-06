#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{$servers := getvs "/elastic/hosts/*"}}
{{if $servers}}
elasticUrl={{join $servers ","}}
connTimeout={{getv "/elastic/conn_timeout" "30000"}}
readTimeout={{getv "/elastic/read_timeout" "300000"}}
settings.index.number_of_replicas={{getv "/elastic/replicas" "1"}}
settings.index.number_of_shards={{getv "/elastic/shards" "1"}}
settings.index.refresh_interval={{getv "/elastic/refresh_interval" "10s"}}
elasticIndexStrategy={{getv "/elastic/index_strategy" "daily"}}
{{if exists "/opennms/instance_id"}}
indexPrefix={{getv "/opennms/instance_id" ""}}
{{end}}
{{end}}
