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
        <div class="switch-row">
          <SwitchRender
            :checked="status"
            @click="onChangeStatus"
            data-test="system-def-status-input"
          />
          <label class="switch-label">{{ status ? 'Enabled' : 'Disabled' }}</label>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="label">General Details</div>
        <FeatherInput
          label="Name"
          v-model.trim="name"
          data-test="system-def-name-input"
          :error="errors.name"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherRadioGroup
          :label="'OID Type'"
          v-model.trim="oidType"
          data-test="system-def-oid-type-input"
          :error="errors.oidType"
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
          :error="errors.oidValue"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="label">MIB Groups</div>
        <FeatherAutocomplete
          class="my-autocomplete"
          label="MIB Groups"
          type="multi"
          v-model="mibGroupNames"
          :loading="loading"
          :results="results"
          @search="search"
          data-test="system-def-mib-groups-input"
          :error="errors.mibGroupNames"
        ></FeatherAutocomplete>
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
import { computed, ref, watch, watchEffect } from 'vue'

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
import { SwitchRender } from '@featherds/switch'
import { DEFAULT_OID_TYPE, DEFAULT_STATUS, OID_PATTERN, OID_MASK_PATTERN, OID_TYPE_OPTIONS } from '@/lib/constants'

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
const errors = ref<SystemDefErrors>({})
const isSaveDisabled = ref<boolean>(true)
const drawerTitle = computed(() =>
  store.systemDefDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create System Definition'
    : 'Edit System Definition'
)

const loadInitialData = () => {
  mibGroupNames.value = store.mibGroupNames.map(name => ({ _text: name, _value: name }))
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Create) {
    name.value = ''
    oidType.value = DEFAULT_OID_TYPE
    oidValue.value = ''
    status.value = DEFAULT_STATUS
    mibGroupNames.value = []
  }
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
    const def = store.selectedSystemDef
    if (def) {
      name.value = def.name
      oidType.value = def.sysoidMask ? 'mask' : def.sysoid ? 'single' : ''
      oidValue.value = def.sysoid || def.sysoidMask || ''
      status.value = def.enabled
      mibGroupNames.value = def.mibGroupNames.map((x: string) => ({ _text: x, _value: x }))
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
  if (oidType.value === 'single' && oidValue.value && !OID_PATTERN.test(oidValue.value)) {
    validationErrors['oidValue'] = 'OID Value format is invalid.'
  }
  if (oidType.value === 'mask' && oidValue.value && !OID_MASK_PATTERN.test(oidValue.value)) {
    validationErrors['oidValue'] = 'OID Mask format is invalid.'
  }
  if (mibGroupNames.value.length === 0) {
    validationErrors['mibGroupNames'] = 'At least one MIB Group must be selected.'
  }
  return validationErrors
}

const onChangeStatus = () => {
  status.value = !status.value
}

const search = (q: string) => {
  loading.value = true
  if (timeout.value) {
    clearTimeout(timeout.value)
  }
  timeout.value = setTimeout(() => {
    results.value = store.mibGroupNames
      .filter(x => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map(x => ({
        _text: x,
        _value: x
      }))
    loading.value = false
  }, 500)
}

const saveSystemDef = async () => {
  errors.value = validateDefinition()
  if (Object.keys(errors.value).length > 0) {
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
      [],
      [],
      mibGroupNames.value.map(x => x._value as string),
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
  } catch (_error) {
    snackbar.showSnackBar({ msg: 'An error occurred while saving the System Definition.', error: true })
  }
}

watchEffect(() => {
  errors.value = validateDefinition()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => store.systemDefDrawerState.visible,
  (visible) => {
    if (visible) {
      loadInitialData()
    } else {
      name.value = ''
      oidType.value = DEFAULT_OID_TYPE
      oidValue.value = ''
      status.value = DEFAULT_STATUS
      mibGroupNames.value = []
      errors.value = {}
      isSaveDisabled.value = true
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

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

      .label {
        @include headline4;
        margin-bottom: 0.5em;
      }

      .switch-row {
        display: flex;
        align-items: center;
        gap: 0.75rem;

        .switch-label {
          font-size: 12px;
          font-weight: 600;
        }
      }
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
