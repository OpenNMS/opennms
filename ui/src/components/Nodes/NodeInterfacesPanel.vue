<template>
  <div
    class="node-interfaces-panel"
    data-test="node-interfaces-panel"
  >
    <span
      v-if="rows.length === 0"
      class="empty"
      data-test="no-interfaces"
    >No interfaces</span>
    <ul
      v-else
      class="interface-list"
    >
      <li
        v-for="row in rows"
        :key="row.key"
        data-test="interface-row"
      >
        <a :href="row.href">{{ row.label }}</a><span v-if="row.suffix"> : {{ row.suffix }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { PropType, computed } from 'vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { Node } from '@/types'
import { getInterfaceListMode, getInterfaceRowsForNode } from './hooks/useInterfaceListing'

const props = defineProps({
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const nodeStructureStore = useNodeStructureStore()

const rows = computed(() => {
  const mode = getInterfaceListMode(nodeStructureStore.queryFilter)
  const ipInterfaces = nodeStore.nodeToIpInterfaceMap.get(props.node.id) ?? []
  const snmpInterfaces = nodeStore.nodeToSnmpInterfaceMap.get(props.node.id) ?? []
  const baseHref = menuStore.mainMenu.baseHref

  return getInterfaceRowsForNode(props.node.id, mode, ipInterfaces, snmpInterfaces, baseHref)
})
</script>

<style lang="scss" scoped>
.node-interfaces-panel {
  padding: 0.5rem 1rem;

  .empty {
    font-style: italic;
  }

  .interface-list {
    list-style: none;
    margin: 0;
    padding: 0;

    li {
      padding: 0.15rem 0;
    }
  }
}
</style>
