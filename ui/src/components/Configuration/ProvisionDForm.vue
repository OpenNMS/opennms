<template>
  <div>
    <FormField label="Name" class="side-input mb-m" hint="Human-friendly name. Must be unique." :error="errors.name">
      <PInputText
        ref="firstInput"
        :invalid="Boolean(errors.name)"
        :modelValue="config.name"
        @update:modelValue="(val: any) => updateFormValue('name', val)"
      />
    </FormField>
    <div class="flex-center">
      <FormField label="External Source" class="side-input full-width mb-m" :error="errors.type">
        <PSelect
          data-test="external-source-select"
          optionLabel="name"
          :options="requisitionTypeList"
          :invalid="Boolean(errors.type)"
          :modelValue="config.type"
          @update:modelValue="updateExternalSource"
        />
      </FormField>
      <div class="icon">
        <OnmsIconButton
          text
          aria-label="Help"
          v-tooltip="'Help'"
          :icon="Help"
          @click="() => props.toggleHelp()"
        />
      </div>
    </div>
    <div v-if="RequsitionTypesUsingHost.includes(config.type.name)">
      <FormField label="Host" class="side-input host-update mb-m" :error="errors.host" :hint="hostHint || 'vCenter server host or IP address'">
        <PInputText
          :invalid="Boolean(errors.host)"
          :modelValue="config.host"
          @update:modelValue="(val: any) => updateFormValue('host', val)"
        />
      </FormField>
    </div>
    <div v-if="RequisitionHTTPTypes.includes(config.type.name)">
      <FormField label="Path" class="side-input mb-m" :error="errors.urlPath" hint="URL path starting with a /">
        <PInputText
          :invalid="Boolean(errors.urlPath)"
          :modelValue="config.urlPath"
          @update:modelValue="(val: any) => updateFormValue('urlPath', val)"
        />
      </FormField>
    </div>
    <div v-if="[RequisitionTypes.RequisitionPlugin].includes(config.type.name)">
      <FormField label="Requisition Plugin" class="side-input mb-m">
        <PSelect
          optionLabel="name"
          :options="requisitionSubTypes"
          :modelValue="config.subType"
          @update:modelValue="(val: any) => updateFormValue('subType', val)"
        />
      </FormField>
    </div>
    <div v-if="[RequisitionTypes.DNS].includes(config.type.name)">
      <FormField label="Zone" class="side-input mb-m" :error="errors.zone" hint="DNS zone to use as basis for this definition">
        <PInputText
          :invalid="Boolean(errors.zone)"
          :modelValue="config.zone"
          @update:modelValue="(val: any) => updateFormValue('zone', val)"
        />
      </FormField>
    </div>
    <div v-if="[RequisitionTypes.DNS].includes(config.type.name) || [RequisitionTypes.VMWare].includes(config.type.name)">
      <FormField label="Requisition Name" class="side-input mb-m" :error="errors.foreignSource" hint="Name to use for resulting requisition">
        <PInputText
          :invalid="Boolean(errors.foreignSource)"
          :modelValue="config.foreignSource"
          @update:modelValue="(val: any) => updateFormValue('foreignSource', val)"
        />
      </FormField>
    </div>
    <div v-if="[RequisitionTypes.VMWare].includes(config.type.name)">
      <div class="flex-center side-input">
        <FormField label="Username" class="side-input full-width mr-m mb-m" :error="errors.username" hint="vSphere username (optional)">
          <PInputText
            :invalid="Boolean(errors.username)"
            :modelValue="config.username"
            @update:modelValue="(val: any) => updateFormValue('username', val)"
          />
        </FormField>
        <FormField label="Password" class="side-input full-width mb-m" :error="errors.password" hint="vSphere password (optional)">
          <PInputText
            type="password"
            :invalid="Boolean(errors.password)"
            :modelValue="config.password"
            @update:modelValue="(val: any) => updateFormValue('password', val)"
          />
        </FormField>
      </div>
    </div>
    <div v-if="[RequisitionTypes.File].includes(config.type.name)">
      <FormField label="Path" class="side-input mb-m" :error="errors.path" hint="File path starting with a /">
        <PInputText
          :invalid="Boolean(errors.path)"
          :modelValue="config.path"
          @update:modelValue="(val: any) => updateFormValue('path', val)"
        />
      </FormField>
    </div>
    <ConfigurationCronSelector
      :config="config"
      :errors="errors"
      :updateValue="updateCronValue"
    />
    <div>
      <div class="side-label">
        <span class="group-label">Rescan Behavior</span>
        <div
          class="radio-option"
          v-for="({ value, name }) in rescanItems"
          :key="name"
        >
          <PRadioButton
            :inputId="`rescan-${value}`"
            :value="value"
            :modelValue="config.rescanBehavior"
            @update:modelValue="(val: any) => updateFormValue('rescanBehavior', val)"
          />
          <label :for="`rescan-${value}`">{{ name }}</label>
        </div>
      </div>
    </div>
  </div>
</template>
<script
  lang="ts"
  setup
>
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import RadioButton from 'primevue/radiobutton'
import FormField from '@/components/Common/FormField.vue'
import { requisitionSubTypes, RequsitionTypesUsingHost, RequisitionTypes, requisitionTypeList, RequisitionHTTPTypes } from './copy/requisitionTypes'
import { rescanItems } from './copy/rescanItems'
import OnmsIconButton from '@/components/Common/OnmsIconButton.vue'
import { PropType, computed, ref, watch } from 'vue'
import Help from '@/components/icons/action/Help.vue'
import { LocalConfigurationWrapper } from './configuration.types'
import { ConfigurationHelper } from './ConfigurationHelper'
import ConfigurationCronSelector from './ConfigurationCronSelector.vue'
import { UpdateModelFunction } from '@/types'

const PSelect = Select
const PInputText = InputText
const PRadioButton = RadioButton

const firstInput = ref()

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
    firstInput.value.$el?.focus()
  }
})

const updateExternalSource: UpdateModelFunction = (val: { name: string }) => {
  props.updateFormValue('type', val)
}

const updateCronValue = (type: string, val: string) => {
  props.updateFormValue(type, val)
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
    align-items: flex-start;
}
.full-width {
    width: 100%;
}
.icon {
    display: flex;
    align-items: center;
    // Offset past the FormField label (~1.6875rem) and center within the input's
    // height so the icon lines up with the input row, not the label or any
    // hint/error rendered below it.
    height: 3rem;
    margin-top: 1.6875rem;
}
.side-label {
    margin-top: 1rem;
    .group-label {
        display: block;
        font-weight: 700;
        margin-bottom: 0.5rem;
        color: var(--p-primary-color);
    }
    .radio-option {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 0.5rem;

        label {
            cursor: pointer;
        }
    }
}
</style>
