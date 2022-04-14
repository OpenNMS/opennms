<template>
  <div class="context-menu" v-if="contextMenu" @click="closeContextMenu">
    <div v-if="contextMenu === ContextMenuType.node">
      <!-- Navigate to object sublayer -->
      <div v-if="node.subLayer" class="menu-btn" @click="() => navigateToSubLayer(node.subLayer.namespace)">Navigate to {{
        node.subLayer.label
      }}</div>

      <!-- Add object/s to focus -->
      <div class="menu-btn" @click="() => groupClick ? addFocusObjects(selectedNodeObjects) : addFocusObject(node)"
        v-if="!nodeIsFocused">Add To Focus</div>

      <!-- Remove object/s from focus -->
      <div class="menu-btn" @click="() => groupClick ? removeFocusObjectsByIds(selectedNodes) : removeFocusObjectsByIds([node.id])" v-else>
        Remove From Focus</div>

      <!-- Change single object icon -->
      <div class="menu-btn" @click="openIconModal" v-if="!groupClick">Change Icon</div>

      <!-- Replace focus with object/s -->
      <div class="menu-btn" @click="() => groupClick ? replaceFocusObjects(selectedNodeObjects) : replaceFocusObjects([node])">Set As Focal
        Point</div>

      <!-- Open node info page -->
      <div class="menu-btn" @click="openNodeInfoPage" v-if="!groupClick && node.namespace === 'nodes'">Node Info</div>

      <!-- Open node resource graphs -->
      <div class="menu-btn" @click="openNodeResourcePage" v-if="!groupClick && node.namespace === 'nodes'">Resource
        Graphs</div>

    </div>

    <div v-if="contextMenu === ContextMenuType.background">
      <div class="menu-btn" @click="replaceFocusObjects([])">Clear Focus</div>
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
import { useTopologyFocus } from './topology.composables'

const store = useStore()
const router = useRouter()
const { addFocusObject, addFocusObjects, replaceFocusObjects, removeFocusObjectsByIds } = useTopologyFocus()

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
const containerId = computed<string>(() => store.state.topologyModule.container)

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

const navigateToSubLayer = (namespace: string) => {
  store.dispatch('topologyModule/getTopologyGraphByContainerAndNamespace', { containerId: containerId.value, namespace })
}

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
