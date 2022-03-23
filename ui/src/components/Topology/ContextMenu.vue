<template>
  <div class="context-menu" v-if="contextMenu" @click="closeContextMenu">
    <div v-if="contextMenu === ContextMenuType.node">
      <div
        class="menu-btn"
        @click="() => groupClick ? addContextNodesToFocus() : addContextNodeToFocus()"
        v-if="!nodeIsFocused"
      >Add To Focus</div>
      <div
        class="menu-btn"
        @click="() => groupClick ? removeContextNodesFromFocus() : removeContextNodeFromFocus()"
        v-else
      >Remove From Focus</div>
      <div
        class="menu-btn"
        @click="() => groupClick ? setContextNodesAsFocus() : setContextNodeAsFocus()"
      >Set As Focal Point</div>
      <div class="menu-btn" @click="openNodeInfoPage" v-if="!groupClick">Node Info</div>
      <div class="menu-btn" @click="openNodeResourcePage" v-if="!groupClick">Resource Graphs</div>
    </div>

    <div v-if="contextMenu === ContextMenuType.background">
      <div class="menu-btn" @click="clearFocus">Clear Focus</div>
      <div class="menu-btn" @click="refreshNow">Refresh Now</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Node, SearchResultResponse } from '@/types'
import { PropType } from 'vue'
import { useStore } from 'vuex'
import { ContextMenuType } from './topology.constants'
import { Node as VNode } from 'v-network-graph'

const store = useStore()
const router = useRouter()

const props = defineProps({
  contextMenuType: {
    required: true,
    type: String as PropType<ContextMenuType>
  },
  refresh: {
    required: true,
    type: Function
  },
  nodeId: {
    type: String
  },
  selectedNodes: {
    type: Array as PropType<string[]>,
    default() {
      return []
    }
  },
  selectedNodeObjects: {
    type: Array as PropType<VNode>,
    default() {
      return []
    }
  },
  x: {
    required: true,
    type: Number
  },
  y: {
    required: true,
    type: Number
  },
  closeContextMenu: {
    required: true,
    type: Function as PropType<(payload: MouseEvent) => void>
  },
  groupClick: {
    type: Boolean,
    default: false
  }
})

watchEffect(() => {
  if (props.nodeId && !props.groupClick) {
    store.dispatch('nodesModule/getNodeById', props.nodeId)
  }
})

const { x, y, contextMenuType } = toRefs(props)
const compX = computed(() => x.value + 'px')
const compY = computed(() => y.value + 'px')
const contextMenu = computed<ContextMenuType>(() => contextMenuType.value)
const node = computed<Node>(() => store.state.nodesModule.node)

const nodeIsFocused = computed(() => {
  let idsToCheck

  if (props.groupClick) {
    idsToCheck = props.selectedNodes
  } else {
    idsToCheck = [props.nodeId]
  }

  for (const id of idsToCheck) {
    if (store.state.topologyModule.focusedNodeIds.includes(id)) {
      return true
    }
  }

  return false
})

// add single node to focus
const addContextNodeToFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  store.dispatch('topologyModule/addFocusedSearchBarNode', results[0].results[0])
  store.dispatch('topologyModule/addContextNodeToFocus', props.nodeId)
}

// add multiple nodes to focus
const addContextNodesToFocus = async () => {
  const promises: Promise<SearchResultResponse[]>[] = []

  for (const node of props.selectedNodeObjects as VNode[]) {
    promises.push(store.dispatch('searchModule/search', node.name))
  }

  const results = await Promise.all(promises)

  for (const result of results) {
    store.dispatch('topologyModule/addFocusedSearchBarNode', result[0].results[0])
  }

  for (const id of props.selectedNodes) {
    store.dispatch('topologyModule/addContextNodeToFocus', id)
  }

}

// remove single node
const removeContextNodeFromFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  store.dispatch('topologyModule/removeFocusedSearchBarNode', results[0].results[0])
  store.dispatch('topologyModule/removeContextNodeFromFocus', props.nodeId)
}

// remove multiple nodes
const removeContextNodesFromFocus = async () => {
  const promises: Promise<SearchResultResponse[]>[] = []

  for (const node of props.selectedNodeObjects as VNode[]) {
    promises.push(store.dispatch('searchModule/search', node.name))
  }

  const results = await Promise.all(promises)

  for (const result of results) {
    store.dispatch('topologyModule/removeFocusedSearchBarNode', result[0].results[0])
  }

  for (const id of props.selectedNodes) {
    store.dispatch('topologyModule/removeContextNodeFromFocus', id)
  }
}

// set single node as focus
const setContextNodeAsFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  if (results) {
    store.dispatch('topologyModule/setFocusedSearchBarNodes', [results[0].results[0]])
    store.dispatch('topologyModule/addFocusedNodeIds', [props.nodeId])
  }
}

// set multiple nodes as focus
const setContextNodesAsFocus = async () => {
  const promises: Promise<SearchResultResponse[]>[] = []

  for (const node of props.selectedNodeObjects as VNode[]) {
    promises.push(store.dispatch('searchModule/search', node.name))
  }

  const results = await Promise.all(promises)
  const resultsArr = results.map((result) => result[0].results[0])
  store.dispatch('topologyModule/setFocusedSearchBarNodes', resultsArr)
  store.dispatch('topologyModule/addFocusedNodeIds', props.selectedNodes)
}

const openNodeInfoPage = () => {
  const route = router.resolve(`/node/${props.nodeId}`)
  window.open(route.href, '_blank')
}

const openNodeResourcePage = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  if (results) {
    const route = router.resolve(`/resource-graphs/${results[0].results[0].identifier}`)
    window.open(route.href, '_blank')
  }
}

const clearFocus = () => {
  store.dispatch('topologyModule/setFocusedSearchBarNodes', [])
  store.dispatch('topologyModule/addFocusedNodeIds', [])
}

const refreshNow = () => {
  props.refresh()
}
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
.context-menu {
  @include body-small;
  @include elevation(1);
  padding: 10px;
  color: var($primary-text-on-surface);
  height: auto;
  width: auto;
  min-width: 100px;
  min-height: 20px;
  display: block;
  position: absolute;
  z-index: 3;
  left: v-bind(compX);
  top: v-bind(compY);

  .menu-btn {
    cursor: pointer;
    padding: 2px 5px 2px 5px;
  }
  .menu-btn:hover {
    color: var($primary-text-on-color);
    background: var($primary);
  }
}
</style>
