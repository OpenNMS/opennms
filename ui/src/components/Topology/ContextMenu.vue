<template>
  <div class="context-menu" v-if="contextMenu" @click="closeContextMenu">
    <div v-if="contextMenu === ContextMenuType.node">
      <div class="menu-btn" @click="() => groupClick ? addContextNodesToFocus() : addFocusObject()"
        v-if="!nodeIsFocused">Add To Focus</div>
      <div class="menu-btn" @click="() => groupClick ? removeContextNodesFromFocus() : removeFocusObject()" v-else>
        Remove From Focus</div>
      <div class="menu-btn" @click="openIconModal" v-if="!groupClick">Change Icon</div>
      <div class="menu-btn" @click="() => groupClick ? setContextNodesAsFocus() : setContextNodeAsFocus()">Set As Focal
        Point</div>
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
import { IdLabelProps, SearchResultResponse } from '@/types'
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
  node: {
    required: true,
    type: Object as PropType<VNode>
  },
  selectedNodes: {
    type: Array as PropType<string[]>,
    default() {
      return []
    }
  },
  selectedNodeObjects: {
    type: Array as PropType<VNode[]>,
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

const { x, y, contextMenuType } = toRefs(props)
const compX = computed(() => x.value + 'px')
const compY = computed(() => y.value + 'px')
const contextMenu = computed<ContextMenuType>(() => contextMenuType.value)

const nodeIsFocused = computed(() => {
  let idsToCheck

  if (props.groupClick) {
    idsToCheck = props.selectedNodes
  } else {
    idsToCheck = [props.node.id]
  }

  for (const id of idsToCheck) {
    if (store.state.topologyModule.focusObjects.map((obj: IdLabelProps) => obj.id).includes(id)) {
      return true
    }
  }

  return false
})

// add single node to focus
const addFocusObject = async () => store.dispatch('topologyModule/addFocusObject', props.node)

// add multiple nodes to focus
const addContextNodesToFocus = async () => {
  for (const obj of props.selectedNodeObjects) {
    store.dispatch('topologyModule/addFocusObject', obj)
  }
}

// remove single node
const removeFocusObject = async () => store.dispatch('topologyModule/removeFocusObject', props.node.id)

// remove multiple nodes
const removeContextNodesFromFocus = async () => {
  for (const id of props.selectedNodes) {
    store.dispatch('topologyModule/removeFocusObject', id)
  }
}

// set single node as focus
const setContextNodeAsFocus = async () => store.dispatch('topologyModule/addFocusObjects', [props.node])

// set multiple nodes as focus
const setContextNodesAsFocus = async () => store.dispatch('topologyModule/addFocusObjects', props.selectedNodeObjects)

const openNodeInfoPage = () => {
  const route = router.resolve(`/node/${props.node.id}`)
  window.open(route.href, '_blank')
}

const openNodeResourcePage = async () => {
  if (!props.node.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', props.node.label)

  if (results) {
    const route = router.resolve(`/resource-graphs/${results[0].results[0].identifier}`)
    window.open(route.href, '_blank')
  }
}

const clearFocus = () => store.dispatch('topologyModule/addFocusObjects', [])
const refreshNow = () => props.refresh()
const openIconModal = () => store.dispatch('topologyModule/setModalState', true)
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
