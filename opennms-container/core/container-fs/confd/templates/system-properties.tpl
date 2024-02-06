#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{range $custom := gets "/opennms/properties/*"}}{{base .Key}}={{.Value}}{{end}}
