<template>
  <div class="event-config">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage Event Configurations</h1>
      </div>
      <div class="action">
        <Button
          label="Create New Event Source"
          @click="store.showCreateEventConfigSourceDialog"
        />
        <Button
          label="Create New Event Config"
          @click="goToCreateEventConfig()"
        />
      </div>
    </div>
    <div class="tabs">
      <EventConfigTabContainer />
    </div>
  </div>
  <CreateEventConfigurationDialog />
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { getDefaultEventConfigEvent } from '@/stores/eventConfigDetailStore'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb, CreateEditMode } from '@/types'
import Button from 'primevue/button'

const store = useEventConfigStore()
const router = useRouter()
const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Event Configurations', to: '#', position: 'last' }
  ]
})

const goToCreateEventConfig = () => {
  const modificationStore = useEventModificationStore()
  modificationStore.openCreateWithoutSource(CreateEditMode.Create, getDefaultEventConfigEvent())
  router.push({ name: 'Event Configuration Create' })
}
</script>

<style lang="scss" scoped>
.event-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .action {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
}
</style>
