<template>
  <FeatherButton text class="btn-drawer" @click="triggerDrawer">Help</FeatherButton>

  <FeatherDrawer
    id="drawer"
    v-model="visible"
    :labels="{ close: 'close', title: 'Snippets' }"
    width="40%"
  >
    <DrawerTabContainer>
      <DrawerTabContent class="first">
        <template v-slot:header>
          <span>Help</span>
        </template>
        <div class="snippets" v-html="snippets"></div>
      </DrawerTabContent>
    </DrawerTabContainer>
  </FeatherDrawer>
</template>

<script setup lang=ts>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherDrawer, DrawerTabContainer, DrawerTabContent } from "@featherds/drawer"
import { FeatherButton } from '@featherds/button'

const visible = ref(false)
const store = useStore()
const snippets = computed(() => store.state.fileEditorModule.snippets)

const triggerDrawer = () => visible.value = !visible.value
</script>

<style lang="scss" scoped>
.btn-drawer {
  position: absolute;
  right: 85px;
}
.snippets {
  padding: 15px;
  color: var(--feather-primary-text-on-surface)
}
</style>
