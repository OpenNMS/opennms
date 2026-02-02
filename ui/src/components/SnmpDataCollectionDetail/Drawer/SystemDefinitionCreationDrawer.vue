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
          :error="error.name"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherRadioGroup
          :label="'OID Type'"
          v-model.trim="oidType"
          data-test="system-def-oid-type-input"
          :error="error.oidType"
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
          :error="error.oidValue"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherAutocomplete
          class="my-autocomplete"
          label="Mib Groups"
          type="multi"
          v-model="mibGroupNames"
          :loading="loading"
          :results="results"
          @search="search"
          data-test="system-def-mib-groups-input"
          :error="error.mibGroupNames"
        ></FeatherAutocomplete>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherRadioGroup
          :label="'Status'"
          v-model="status"
          data-test="system-def-status-input"
          :error="error.enabled"
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
          :disabled="isSaveDisabled"
          @click="saveSystemDef"
        >
          Save Definition
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { mapSnmpDataCollectionSystemDefPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createSystemDefinition, updateSystemDefinition } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SystemDefErrors } from '@/types/snmpDataCollection'
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
const mibGroupNames = ref<Array<IAutocompleteItemType>>([])
const snackbar = useSnackbar()
const error = ref<SystemDefErrors>({})
const isSaveDisabled = ref<boolean>(true)
const drawerTitle = computed(() =>
  store.systemDefDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create System Definition'
    : 'Edit System Definition'
)

const loadInitialData = () => {
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
    const def = store.selectedSystemDef
    if (def) {
      name.value = def.name
      oidType.value = def.sysoidMask ? 'mask' : def.sysoid ? 'single' : ''
      oidValue.value = def.sysoid || def.sysoidMask || ''
      status.value = def.enabled
      mibGroupNames.value = JSON.parse(def.mibGroupNames).map((x: string) => ({ _text: x, _value: x }))
    } else {
      name.value = ''
      oidType.value = DEFAULT_OID_TYPE
      oidValue.value = ''
      status.value = DEFAULT_STATUS
      mibGroupNames.value = []
    }
  }
}

const validateDefinition = (): SystemDefErrors => {
  const validationErrors: SystemDefErrors = {}
  if (!name.value.trim()) {
    validationErrors['name'] = 'Name is required.'
  }
  if (!oidType.value) {
    validationErrors['oidType'] = 'OID Type is required.'
  }
  if (!oidValue.value.trim()) {
    validationErrors['oidValue'] = 'OID Value is required.'
  }
  if (mibGroupNames.value.length === 0) {
    validationErrors['mibGroupNames'] = 'At least one MIB Group must be selected.'
  }
  return validationErrors
}

const search = (q: string) => {
  loading.value = true
  if (timeout.value) {
    clearTimeout(timeout.value)
  }
  timeout.value = setTimeout(() => {
    results.value = store.mibGroupNames
      .filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map((x) => ({
        _text: x,
        _value: x
      }))
    loading.value = false
  }, 500)
}

const saveSystemDef = async () => {
  error.value = validateDefinition()
  if (Object.keys(error.value).length > 0) {
    return
  }

  if (!store.selectedCollectionSource?.id) {
    snackbar.showSnackBar({ msg: 'Please select a Collection Source first.', error: true })
    return
  }

  try {
    const payload = mapSnmpDataCollectionSystemDefPayloadToServer(
      name.value,
      oidType.value === 'single' ? oidValue.value : '',
      oidType.value === 'mask' ? oidValue.value : '',
      '',
      '',
      JSON.stringify(mibGroupNames.value.map((x) => x._value)),
      status.value,
      store.selectedSystemDef?.id || 0,
      store.systemDefDrawerState.isEditMode
    )

    let response
    if (store.systemDefDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createSystemDefinition(payload, store.selectedCollectionSource.id)
      snackbar.showSnackBar({ msg: 'System Definition created successfully.' })
    }
    if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateSystemDefinition(payload, store.selectedCollectionSource.id)
      snackbar.showSnackBar({ msg: 'System Definition updated successfully.' })
    }

    if (response) {
      await store.fetchSystemDefinitions()
      store.closeSystemDefDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'An error occurred while saving the System Definition.', error: true })
    }
  } catch (error) {
    snackbar.showSnackBar({ msg: 'An error occurred while saving the System Definition.', error: true })
  }
}

watchEffect(() => {
  const validationErrors = validateDefinition()
  error.value = validationErrors
  isSaveDisabled.value = Object.keys(validationErrors).length > 0
})

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

