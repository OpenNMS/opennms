#!/bin/sh
export LD_LIBRARY_PATH=/home/jesse/bin/protobuf-3.5.1/src/.libs
pushd /home/jesse/bin/protobuf-3.5.1/src
./protoc --java_out=/home/jesse/git/opennms/features/kafka/producer/src/main/java/ --proto_path=/home/jesse/git/opennms/features/kafka/producer/src/main/proto/  /home/jesse/git/opennms/features/kafka/producer/src/main/proto/opennms.proto
popd
