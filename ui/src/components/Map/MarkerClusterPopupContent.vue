<template>
  <div class="marker-cluster-popup" ref="clusterPopupContent" v-if="cluster">
    <div class="marker-cluster-popup-content">
      <h3>Nodes: {{ cluster.getChildCount() }}</h3>
      <br />
      <div class="marker-cluster-header-info">
        <div class="col">
          <span class="larger-icon"><FeatherIcon :icon="Location" /></span>
          <span>{{ latitude }}, {{ longitude }}</span>
        </div>
        <div class="col">
          <span><a :href="getTopologyLink()">View in Topology Map</a></span>
        </div>
      </div>
      <br />
      <div id="wrap">
        <table>
          <thead>
            <th>Node</th>
            <th>IP Address</th>
            <th>Status</th>
          </thead>
          <tbody>
            <template v-for="info of getItems()" :key="info.name">
              <tr>
                <td><a :href="info.link">{{ info.name }}</a></td>
                <td>{{ info.ipAddress }}</td>
                <td>
                  <div :class="['alarm-severity', `${info.severity || 'NORMAL'}`]">
                    <span>{{ info.severity || 'NORMAL' }}</span>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang ="ts">
import { PropType } from 'vue'
import { orderBy } from 'lodash'
import { Marker, MarkerCluster as Cluster } from 'leaflet'
import { FeatherIcon } from '@featherds/icon'
import Location from '@featherds/icon/action/Location'
import { useMapStore } from '@/stores/mapStore'
import { useMenuStore } from '@/stores/menuStore'
import { IpInterface, Node } from '@/types'
import { MainMenu } from '@/types/mainMenu'

interface ClusterInfo {
  description: string
  id: string
  ipAddress: string
  link: string
  name: string
  severity: string
}

const props = defineProps({
  cluster: { type: Object as PropType<Cluster>, default: () => { return }},
  ipListForNode: { type: Function as PropType<(node: Node | null) => IpInterface[]>, required: true }
})

const mapStore = useMapStore()
const menuStore = useMenuStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const baseNodeUrl = computed<string>(() => `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}`)
const nodes = computed<Node[]>(() => mapStore.getNodes())
const nodeLabelAlarmSeverityMap = computed(() => mapStore.getNodeAlarmSeverityMap())
const latitude = computed(() => props.cluster.getLatLng().lat.toFixed(6))
const longitude = computed(() => props.cluster.getLatLng().lng.toFixed(6))

const ipAddressesForNode = (node: Node | null) => {
  const ifList = props.ipListForNode(node)
  return ifList.map(ip => ip?.ipAddress).join(', ') || 'N/A'
}

const nodeFromMarker = (marker: Marker<any>) => {
  const name = (marker as any).options.name
  return nodes.value.find(n => n.label === name) || null
}

const getTopologyLink = () => {
  const children = props.cluster.getAllChildMarkers()
  const vertices = children
    .map(m => nodeFromMarker(m)?.id || '')
    .filter(id => id.length > 0)
    .join(',')

  return `${mainMenu.value.baseHref}topology?provider=Enhanced Linkd&focus-vertices=${vertices}`
}

const getItems = () => {
  const children = props.cluster.getAllChildMarkers()
  const currentNodes = children.map(m => nodeFromMarker(m)).filter(n => n !== null)

  const items = currentNodes.map(node => {
    const nodeId = node?.id || ''
    const nodeLink = (nodeId && `${baseNodeUrl.value}${nodeId}`) || '#'
    const description = node?.assetRecord?.description || 'N/A'
    const severity = nodeLabelAlarmSeverityMap.value[node?.label || '']
    const ipAddress = ipAddressesForNode(node)

    return {
      name: node?.label || '',
      description,
      id: nodeId,
      ipAddress,
      link: nodeLink,
      severity
    } as ClusterInfo
  })

  return orderBy(items, 'name', 'asc')
}
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/themes/variables";

.marker-cluster-header-info {
  display: flex;
  line-height: 1.5em;
}

.col {
  flex-grow: 1;
}

.larger-icon {
  font-size: 1.35em;
}

#wrap {
  /*height: calc(100vh - 310px);*/
  height: 340px;
  overflow: auto;
  white-space: nowrap;

  table {
    margin-top: 0px !important;
    font-size: 12px !important;
    @include table;
  }

  thead {
    z-index: 2;
    position: relative;
    background: var($surface);
  }

  td {
    div.alarm-severity {
      padding: 4px;
      text-align: center;
      font-weight: var($font-semibold);

      &.NORMAL {
        background: var($success);
        color: var($state-text-color-on-surface-dark); // --feather-state-text-color-on-surface-dark;
      }
      &.WARNING,
      &.MINOR,
      &.MAJOR {
        background: var($warning);
      }
      &.CRITICAL {
        background: var($error);
        color: var($state-text-color-on-surface-dark); // --feather-state-text-color-on-surface-dark;
      }
    }
   }
}
</style>
 