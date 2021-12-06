<template>
  <table class="tl1 tl2 tl3" summary="Nodes">
    <thead>
      <tr>
        <th scope="col">ID</th>
        <th scope="col">FOREIGN SOURCE</th>
        <th scope="col">FOREIGN ID</th>
        <th scope="col">LABEL</th>
        <th scope="col">LABEL SOURCE</th>
        <th scope="col">LAST CAPABILITIES SCAN</th>
        <th scope="col">PRIMARY INTERFACE</th>
        <th scope="col">SYS OBJECT ID</th>
        <th scope="col">SYS NAME</th>
        <th scope="col">SYS DESCRIPTION</th>
        <th scope="col">SYS CONTACT</th>
        <th scope="col">SYS LOCATION</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="node in nodes" :key="node.id">
        <td>{{ node.id }}</td>
        <td>{{ node.foreignSource }}</td>
        <td>{{ node.foreignId }}</td>
        <td>{{ node.label }}</td>
        <td>{{ node.labelSource }}</td>
        <td>{{ node.lastCapabilitiesScan }}</td>
        <td>{{ node.primaryInterface }}</td>
        <td>{{ node.sysObjectid }}</td>
        <td>{{ node.sysName }}</td>
        <td>{{ node.sysDescription }}</td>
        <td>{{ node.sysContact }}</td>
        <td>{{ node.sysLocation }}</td>
      </tr>
    </tbody>
  </table>
</template>
<script setup lang="ts">
import { useStore } from "vuex"
import { computed } from 'vue'
import { Coordinates, Node, FeatherSortObject } from "@/types"
import { FeatherSortHeader, SORT } from "@featherds/table"

const store = useStore()
const nodes = computed<Node[]>(() => store.getters['mapModule/getNodes'])

const rowDoubleClicked = (node: Node) => {
  const coordinate: Coordinates = { latitude: node.assetRecord.latitude, longitude: node.assetRecord.longitude }
  store.dispatch("mapModule/setMapCenter", coordinate)
}
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
table {
  @include table();
  @include table-condensed();
  background: var(--feather-surface);
  color: var(--feather-primary-text-on-surface);
  display: block;
  height: calc(100% - 58px);
  width: 100%;
  overflow-y: scroll;
  padding-top: 4px;
  margin-top: 15px;
}
</style>
