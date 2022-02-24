<template>
  <div class="context-menu">
    <div class="menu-btn" @click="addContextNodeToFocus" v-if="!nodeIsFocused">Add To Focus</div>
    <div class="menu-btn" @click="removeContextNodeFromFocus" v-else>Remove From Focus</div>
    <div class="menu-btn" @click="setContextNodeAsFocus">Set As Focal Point</div>
    <div class="menu-btn">Node Info</div>
    <div class="menu-btn">Resource Graphs</div>
    <div class="menu-btn">Change Icon</div>
    <div class="menu-btn">Reset Icon</div>
  </div>
</template>

<script setup lang="ts">
import { SearchResultResponse } from '@/types'
import { toRefs, computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

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

const { x, y } = toRefs(props)
const compX = computed(() => x.value + 'px')
const compY = computed(() => y.value + 'px')

const nodeIsFocused = computed(() => store.state.topologyModule.focusedNodeIds.includes(props.nodeId))

const addContextNodeToFocus = async () => {
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', props.nodeId)
  if (results) {
    store.dispatch('topologyModule/addFocusedSearchBarNode', results[0].results[0])
    store.dispatch('topologyModule/addContextNodeToFocus', props.nodeId)
  }
  props.closeContextMenu()
}

const removeContextNodeFromFocus = async () => {
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', props.nodeId)
  if (results) {
    store.dispatch('topologyModule/removeFocusedSearchBarNode', results[0].results[0])
    store.dispatch('topologyModule/removeContextNodeFromFocus', props.nodeId)
  }
  props.closeContextMenu()
}

const setContextNodeAsFocus = async () => { 
  const results: SearchResultResponse[] = await store.dispatch('searchModule/search', props.nodeId)
  if (results) {
    store.dispatch('topologyModule/setFocusedSearchBarNodes', [results[0].results[0]]) 
    store.dispatch('topologyModule/addFocusedNodeIds', [props.nodeId]) 
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
