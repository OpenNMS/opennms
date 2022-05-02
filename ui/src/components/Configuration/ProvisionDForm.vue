<template>
  <div>
    <FeatherInput
      ref="firstInput"
      class="side-input mb-16"
      label="Name"
      hint="Human-friendly name. Must be unique."
      :error="errors.name"
      :modelValue="config.name"
      @update:modelValue="(val) => updateFormValue('name', val)"
    />
    <div class="flex-center">
      <FeatherSelect
        class="side-input full-width mb-16"
        textProp="name"
        label="External Source"
        :options="requisitionTypeList"
        :error="errors.type"
        :modelValue="config.type"
        @update:modelValue="(((val: {name:string}) => {
          props.updateFormValue('type', val)
          updateHint(val.name)
        }) as any)"
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
        class="side-input host-update mb-16"
        :error="errors.host"
        :modelValue="config.host"
        @update:modelValue="(val) => updateFormValue('host', val)"
        :hint="hostHint || 'vCenter server host or IP address'"
      />
    </div>
    <div v-if="RequisitionHTTPTypes.includes(config.type.name)">
      <FeatherInput
        label="Path"
        class="side-input mb-16"
        :error="errors.urlPath"
        :modelValue="config.urlPath"
        @update:modelValue="(val) => updateFormValue('urlPath', val)"
        hint="URL path starting with a /"
      />
    </div>
    <div v-if="[RequisitionTypes.RequisitionPlugin].includes(config.type.name)">
      <FeatherSelect
        class="side-input mb-16"
        textProp="name"
        hint=""
        label="Requisition Plugin"
        :options="requisitionSubTypes"
        @update:modelValue="(val) => updateFormValue('subType', val)"
        :modelValue="config.subType"
      />
    </div>
    <div v-if="[RequisitionTypes.DNS].includes(config.type.name)">
      <FeatherInput
        label="Zone"
        class="side-input mb-16"
        :error="errors.zone"
        :modelValue="config.zone"
        @update:modelValue="(val) => updateFormValue('zone', val)"
        hint="DNS zone to use as basis for this definition"
      />
      <FeatherInput
        label="Requisition Name"
        class="side-input mb-16"
        :error="errors.foreignSource"
        :modelValue="config.foreignSource"
        @update:modelValue="(val) => updateFormValue('foreignSource', val)"
        hint="Name to use for resulting requisition"
      />
    </div>
    <div v-if="[RequisitionTypes.VMWare].includes(config.type.name)">
      <div class="flex-center side-input">
        <FeatherInput
          label="Username"
          class="side-input full-width mr-16 mb-16"
          :error="errors.username"
          :modelValue="config.username"
          @update:modelValue="(val) => updateFormValue('username', val)"
          hint="vSphere username"
        />
        <FeatherInput
          type="password"
          label="Password"
          class="side-input full-width mb-16"
          :error="errors.password"
          :modelValue="config.password"
          @update:modelValue="(val) => updateFormValue('password', val)"
          hint="vSphere password"
        />
      </div>
    </div>
    <div v-if="[RequisitionTypes.File].includes(config.type.name)">
      <FeatherInput
        label="Path"
        class="side-input mb-16"
        :error="errors.path"
        :modelValue="config.path"
        @update:modelValue="(val) => updateFormValue('path', val)"
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
        @update:modelValue="(val) => updateFormValue('rescanBehavior', val)"
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
import { PropType, computed, watch, ref } from 'vue'
import Help from '@featherds/icon/action/Help'
import { LocalConfigurationWrapper } from './configuration.types'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCronSelector from './ConfigurationCronSelector.vue'
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

/**
 * Focus the first field in the drawer when opened.
 *
 */
watch(formActive, () => {
  if (formActive.value && firstInput.value) {
    firstInput.value.focus()
  }
})

const updateCronValue = (type:string, val:string) => {
  props.updateFormValue(type, val)
}

/**
 * The following function is related to getting the Hint Text
 * to update properly in the FeatherInput component. Currently if you update the Hint Text
 * after the initial render, FeatherInput does not react to the untracked attribute.
 * We could forcibly mount + unmount the component as an alternative which would also render
 * the correct text, but I felt like these easily removable two lines of code is preferable
 * than a forced re-render.
 *
 * In the case that FeatherInput properly updates when the Hint Text is updated, just remove
 * the two proceeding lines of code (getHostHint and forceSetHint)
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
.time {
    margin-left: 16px;
    width: 100%;
}
.flex-center {
    display: flex;
}
.full-width {
    width: 100%;
}
.mr-16 {
    margin-right: 16px;
}
.mb-16 {
    margin-bottom: 16px;
}
</style>

