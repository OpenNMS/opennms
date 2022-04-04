<template>
  <FeatherDialog v-model="modalState" :labels="labels">
    <div class="flex">
      <div v-for="(value, key) in ICON_PATHS" :key="key">
        <img width="80" :src="value" />
      </div>
    </div>

    <template v-slot:footer>
      <FeatherButton secondary @click="changeIcon">Ok</FeatherButton>
      <FeatherButton secondary @click="cancel">Cancel</FeatherButton>
    </template>
  </FeatherDialog>
</template>
<script setup lang=ts>
import { useStore } from 'vuex'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import ICON_PATHS from './icons/iconPaths'

const store = useStore()
const selectedIcon = ref()

const modalState = computed({
  get: () => store.state.topologyModule.modalState,
  set: (value: boolean) => {
    store.dispatch('topologyModule/setModalState', value)
  },
})

const labels = {
  title: 'Change Icon',
  close: 'Close'
}

const changeIcon = () => store.dispatch('topologyModyle/changeIcon', selectedIcon.value)
const cancel = () => modalState.value = false
</script>

<style lang="scss" scoped>
.dialog {
  min-width: 500px;
  height: 300px;
}
.flex {
  display: flex;
  flex-wrap: wrap;
  max-width: 700px;
  max-height: 400px;
  overflow: auto;
}
</style>
