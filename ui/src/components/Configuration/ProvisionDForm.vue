<template>
  <div>
    <FeatherInput
      ref="firstInput"
      class="side-input mb-m"
      label="Name"
      hint="Human-friendly name. Must be unique."
      :error="errors.name"
      :modelValue="config.name"
      @update:modelValue="(val: any) => updateFormValue('name', val)"
    />
    <div class="flex-center">
      <FeatherSelect
        data-test="external-source-select"
        class="side-input full-width mb-m"
        textProp="name"
        label="External Source"
        :options="requisitionTypeList"
        :error="errors.type"
        :modelValue="config.type"
        @update:modelValue="updateExternalSource"
      />
      <div class="icon">
        <FeatherButton
          icon="Help"
          @click="() => props.toggleHelp()"
        >
          <FeatherIcon
            class="help-icon"
            :icon="Help"
          ></FeatherIcon>
        </FeatherButton>
      </div>
    </div>
    <div v-if="RequsitionTypesUsingHost.includes(config.type.name)">
      <FeatherInput
        label="Host"
        class="side-input host-update mb-m"
        :error="errors.host"
        :modelValue="config.host"
        @update:modelValue="(val: any) => updateFormValue('host', val)"
        :hint="hostHint || 'vCenter server host or IP address'"
      />
    </div>
    <div v-if="RequisitionHTTPTypes.includes(config.type.name)">
      <FeatherInput
        label="Path"
        class="side-input mb-m"
        :error="errors.urlPath"
        :modelValue="config.urlPath"
        @update:modelValue="(val: any) => updateFormValue('urlPath', val)"
        hint="URL path starting with a /"
      />
    </div>
    <div v-if="[RequisitionTypes.RequisitionPlugin].includes(config.type.name)">
      <FeatherSelect
        class="side-input mb-m"
        textProp="name"
        hint=""
        label="Requisition Plugin"
        :options="requisitionSubTypes"
        @update:modelValue="(val: any) => updateFormValue('subType', val)"
        :modelValue="config.subType"
      />
    </div>
    <div v-if="[RequisitionTypes.DNS].includes(config.type.name)">
      <FeatherInput
        label="Zone"
        class="side-input mb-m"
        :error="errors.zone"
        :modelValue="config.zone"
        @update:modelValue="(val: any) => updateFormValue('zone', val)"
        hint="DNS zone to use as basis for this definition"
      />
    </div>
    <div v-if="[RequisitionTypes.DNS].includes(config.type.name) || [RequisitionTypes.VMWare].includes(config.type.name)">
      <FeatherInput
        label="Requisition Name"
        class="side-input mb-m"
        :error="errors.foreignSource"
        :modelValue="config.foreignSource"
        @update:modelValue="(val: any) => updateFormValue('foreignSource', val)"
        hint="Name to use for resulting requisition"
      />
    </div>
    <div v-if="[RequisitionTypes.VMWare].includes(config.type.name)">
      <div class="flex-center side-input">
        <FeatherInput
          label="Username"
          class="side-input full-width mr-m mb-m"
          :error="errors.username"
          :modelValue="config.username"
          @update:modelValue="(val: any) => updateFormValue('username', val)"
          hint="vSphere username (optional)"
        />
        <FeatherInput
          type="password"
          label="Password"
          class="side-input full-width mb-m"
          :error="errors.password"
          :modelValue="config.password"
          @update:modelValue="(val: any) => updateFormValue('password', val)"
          hint="vSphere password (optional)"
        />
      </div>
    </div>
    <div v-if="[RequisitionTypes.File].includes(config.type.name)">
      <FeatherInput
        label="Path"
        class="side-input mb-m"
        :error="errors.path"
        :modelValue="config.path"
        @update:modelValue="(val: any) => updateFormValue('path', val)"
        hint="File path starting with a /"
      />
    </div>
    <ConfigurationCronSelector
      :config="config"
      :errors="errors"
      :updateValue="updateCronValue"
    />
    <div>
      <FeatherRadioGroup
        class="side-label"
        label="Rescan Behavior"
        :modelValue="config.rescanBehavior"
        @update:modelValue="(val: any) => updateFormValue('rescanBehavior', val)"
      >
        <FeatherRadio
          v-for="({value, name}) in rescanItems"
          :value="value"
          :key="name"
          >{{ name }}</FeatherRadio
        >
      </FeatherRadioGroup>
    </div>
  </div>
</template>
<script
  lang="ts"
  setup
>
import { FeatherSelect } from '@featherds/select'
import { requisitionSubTypes, RequsitionTypesUsingHost, RequisitionTypes, requisitionTypeList, RequisitionHTTPTypes } from './copy/requisitionTypes'
import { rescanItems } from './copy/rescanItems'
import { FeatherInput } from '@featherds/input'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import { FeatherRadioGroup, FeatherRadio } from '@featherds/radio'
import { PropType } from 'vue'
import Help from '@featherds/icon/action/Help'
import { LocalConfigurationWrapper } from './configuration.types'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCronSelector from './ConfigurationCronSelector.vue'
import { UpdateModelFunction } from '@/types'
const firstInput = ref<HTMLInputElement | null>(null)

const props = defineProps({
  item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
  helpState: { type: Boolean, required: true },
  toggleHelp: { type: Function, required: true },
  updateFormValue: { type: Function, required: true },
  formActive: { type: Boolean, required: true }
})

const config = computed(() => props.item.config)
const errors = computed(() => props.item.errors)
const formActive = computed(() => props.formActive)
const hostHint = computed(() => {
  return ConfigurationHelper.getHostHint(props.item.config.type.name)
})

// Focus the first field in the drawer when opened.
watch(formActive, () => {
  if (formActive.value && firstInput.value) {
    firstInput.value.focus()
  }
})

const updateExternalSource: UpdateModelFunction = (val: {name:string}) => {
  props.updateFormValue('type', val)
  updateHint(val.name)
}

const updateCronValue = (type:string, val:string) => {
  props.updateFormValue(type, val)
}

/**
 * The following function is related to getting the Hint Text to update properly in the FeatherInput component. Currently if you update the Hint Text after the initial render, FeatherInput does not react to the untracked attribute.
 * We could forcibly mount + unmount the component as an alternative which would also render the correct text, but I felt like these easily removable two lines of code is preferable than a forced re-render.
 * In the case that FeatherInput properly updates when the Hint Text is updated, just remove the two proceeding lines of code (getHostHint and forceSetHint)
 **/
const updateHint = (val:string) => {
  const hint = ConfigurationHelper.getHostHint(val)
  ConfigurationHelper.forceSetHint({hint}, 0,'.host-update')
}
</script>
<style
  lang="scss"
  scoped
>
.side-input {
    padding-bottom: 0;
}
.occurance {
    width: 100%;
}
.flex-center {
    display: flex;
}
.full-width {
    width: 100%;
}
</style>

