<template>
  <div class="context-menu">
    <div class="menu-btn" @click="addContextNodeToFocus" v-if="!nodeIsFocused">Add To Focus</div>
    <div class="menu-btn" @click="removeContextNodeFromFocus" v-else>Remove From Focus</div>
    <div class="menu-btn" @click="setContextNodeAsFocus">Set As Focal Point</div>
    <div class="menu-btn" @click="openNodeInfoPage">Node Info</div>
    <div class="menu-btn" @click="openNodeResourcePage">Resource Graphs</div>
  </div>
</template>

<script setup lang="ts">
import { Node, SearchResultResponse } from '@/types'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()

const props = defineProps({
  nodeId: {
    required: true,
    type: String
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
    type: Function
  }
})

watchEffect(() => store.dispatch('nodesModule/getNodeById', props.nodeId))

const { x, y } = toRefs(props)
const compX = computed(() => x.value + 'px')
const compY = computed(() => y.value + 'px')
const node = computed<Node>(() => store.state.nodesModule.node)

const nodeIsFocused = computed(() => store.state.topologyModule.focusedNodeIds.includes(props.nodeId))

const addContextNodeToFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  store.dispatch('topologyModule/addFocusedSearchBarNode', results[0].results[0])
  store.dispatch('topologyModule/addContextNodeToFocus', props.nodeId)
  props.closeContextMenu()
}

const removeContextNodeFromFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  store.dispatch('topologyModule/removeFocusedSearchBarNode', results[0].results[0])
  store.dispatch('topologyModule/removeContextNodeFromFocus', props.nodeId)
  props.closeContextMenu()
}

const setContextNodeAsFocus = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  if (results) {
    store.dispatch('topologyModule/setFocusedSearchBarNodes', [results[0].results[0]])
    store.dispatch('topologyModule/addFocusedNodeIds', [props.nodeId])
  }
  props.closeContextMenu()
}

const openNodeInfoPage = () => {
  const route = router.resolve(`/node/${props.nodeId}`)
  window.open(route.href, '_blank')
  props.closeContextMenu()
}

const openNodeResourcePage = async () => {
  if (!node.value.label) return
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.value.label)

  if (results) {
    const route = router.resolve(`/resource-graphs/${results[0].results[0].identifier}`)
    window.open(route.href, '_blank')
  }
  props.closeContextMenu()
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
  min-height: 150px;
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
