<template>
  <FeatherDrawer
    id="drawer"
    data-test="mib-group-drawer"
    v-model="store.mibGroupDrawerState.visible"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    width="40rem"
    class="mib-group-drawer"
  >
    <div class="container">
      <div class="drawer-header">
        <h2>{{ drawerTitle }}</h2>
      </div>
      <div class="spacer"></div>
      <div class="drawer-content">
        <!-- Content for Mib Group Creation/Edit goes here -->
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { FeatherDrawer } from '@featherds/drawer'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'

const store = useSnmpDataCollectionDetailStore()
const drawerTitle = computed(() =>
  store.mibGroupDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create Mib Group'
    : 'Edit Mib Group'
)
</script>

<style lang="scss" scoped>
.mib-group-drawer {
  .container {
    .drawer-header {
      padding: 40px 20px;
    }

    .spacer {
      min-height: 0.5em;
    }

    .drawer-content {
      padding: 0 20px;
    }

    .drawer-footer {
      padding: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}
</style>

