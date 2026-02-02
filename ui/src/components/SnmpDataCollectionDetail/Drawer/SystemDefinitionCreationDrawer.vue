<template>
  <FeatherDrawer
    id="drawer"
    data-test="system-definition-drawer"
    v-model="store.systemDefDrawerState.visible"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    width="40rem"
    class="system-definition-drawer"
  >
    <div class="container">
      <div class="drawer-header">
        <h2>{{ drawerTitle }}</h2>
      </div>
      <div class="spacer"></div>
      <div class="drawer-content">
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherInput
          label="Name"
          v-model.trim="name"
          data-test="system-def-name-input"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherRadioGroup
          :label="'OID Type'"
          v-model.trim="oidType"
        >
          <FeatherRadio
            v-for="item in OID_TYPE_OPTIONS"
            :value="item.value"
            :key="item.name"
          >
            {{ item.name }}
          </FeatherRadio>
        </FeatherRadioGroup>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherInput
          label="OID Value"
          v-model.trim="oidValue"
          data-test="system-def-oid-value-input"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherAutocomplete
          class="my-autocomplete"
          label="Mib Groups"
          type="multi"
          v-model="value"
          :loading="loading"
          :results="results"
          @search="search"
        ></FeatherAutocomplete>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherRadioGroup
          :label="'Status'"
          v-model="status"
        >
          <FeatherRadio
            v-for="item in STATUS_OPTIONS"
            :value="item.value"
            :key="item.name"
          >
            {{ item.name }}
          </FeatherRadio>
        </FeatherRadioGroup>
      </div>
      <div class="spacer"></div>
      <div class="drawer-footer">
        <FeatherButton
          secondary
          data-test="cancel-button"
          @click="store.closeSystemDefDrawer"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          data-test="save-button"
          @click="store.closeSystemDefDrawer"
        >
          Save
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { DEFAULT_OID_TYPE, DEFAULT_STATUS, OID_TYPE_OPTIONS, STATUS_OPTIONS } from '../utils'

const store = useSnmpDataCollectionDetailStore()
const oidType = ref<string>(DEFAULT_OID_TYPE)
const status = ref<boolean>(DEFAULT_STATUS)
const oidValue = ref<string>('')
const name = ref<string>('')
const timeout = ref<any>(null)
const loading = ref<boolean>(false)
const results = ref<Array<IAutocompleteItemType>>([])
const value = ref<Array<IAutocompleteItemType>>([])

const drawerTitle = computed(() =>
  store.systemDefDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create System Definition'
    : 'Edit System Definition'
)

const loadInitialData = () => {
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
    console.log('Loading data for edit mode', store.selectedSystemDef)
    
    const def = store.selectedSystemDef
    if (def) {
      name.value = def.name
      oidType.value = def.sysoidMask ? 'mask' : ''
      oidType.value = def.sysoid ? 'single' : ''
      oidValue.value = def.sysoid || def.sysoidMask || ''
      status.value = def.enabled
      value.value = JSON.parse(def.mibGroupNames).map((x: string) => ({ _text: x, _value: x }))
    } else {
      // Reset values if no definition is selected
      name.value = ''
      oidType.value = DEFAULT_OID_TYPE
      oidValue.value = ''
      status.value = DEFAULT_STATUS
      value.value = []
    }
  }
}

const search = (q: string) => {
  loading.value = true
  if (timeout.value) {
    clearTimeout(timeout.value)
  }
  timeout.value = setTimeout(() => {
    results.value = store.resourceTypeNames
      .filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map((x) => ({
        _text: x,
        _value: x
      }))
    loading.value = false
  }, 500)
}

watch(
  () => store.systemDefDrawerState.visible,
  (visible) => {
    if (visible) {
      loadInitialData()
    }
  }
)
</script>

<style scoped lang="scss">
.system-definition-drawer {
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

