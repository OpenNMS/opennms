#!/bin/sh
/home/jesse/bin/protobuf-3.11.4/bin/protoc -I=$(pwd) --java_out ../java transport.proto


