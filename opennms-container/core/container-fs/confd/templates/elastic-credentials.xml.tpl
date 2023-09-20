<!-- DON'T EDIT THIS FILE :: GENERATED WITH CONFD -->
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elastic-credentials>
{{range ls "/elastic/flows/hosts"}}{{ $index := (atoi . )}}{{$host := getv (print "/elastic/flows/hosts/" $index "/host")}}{{$user := getv (print "/elastic/flows/hosts/" $index "/username") ""}}{{$pass := getv (print "/elastic/flows/hosts/" $index "/password") ""}}
{{if $user}}{{if $pass}}
<credentials url="{{$host}}" username="{{$user}}" password="{{$pass}}" />
{{end}}{{end}}{{end}}
</elastic-credentials>
