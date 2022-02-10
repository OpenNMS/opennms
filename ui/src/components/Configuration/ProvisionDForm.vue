<template>
    <div>
        <FeatherInput
            ref="firstInput"
            class="side-input"
            label="Name"
            hint="Hint Text"
            :error="errors.name"
            :modelValue="config.name"
            @update:modelValue="(val: string) => updateFormValue('name', val)"
        />
        <div class="flex-center">
            <FeatherSelect
                class="side-input full-width"
                textProp="name"
                hint="Hint Text"
                label="Type"
                :options="requisitionTypeList"
                :error="errors.type"
                :modelValue="config.type"
                @update:modelValue="(val: string) => updateFormValue('type', val)"
            />
            <div class="icon">
                <FeatherButton icon="Help" @click="() => props.toggleHelp()">
                    <FeatherIcon class="help-icon" :icon="Help"></FeatherIcon>
                </FeatherButton>
            </div>
        </div>
        <div v-if="RequsitionTypesUsingHost.includes(config.type.name)">
            <FeatherInput
                label="Host"
                class="side-input"
                :error="errors.host"
                :modelValue="config.host"
                @update:modelValue="(val: string) => updateFormValue('host', val)"
                hint="Hint Text"
            />
        </div>
        <div v-if="[RequisitionTypes.RequisitionPlugin].includes(config.type.name)">
            <FeatherSelect
                class="side-input"
                textProp="name"
                hint="Hint Text"
                label="Requisition Plugin"
                :options="requisitionSubTypes"
                @update:modelValue="(val: string) => updateFormValue('subType', val)"
                :modelValue="config.subType"
            />
        </div>
        <div v-if="[RequisitionTypes.DNS].includes(config.type.name)">
            <FeatherInput
                label="Zone"
                class="side-input"
                :error="errors.zone"
                :modelValue="config.zone"
                @update:modelValue="(val: string) => updateFormValue('zone', val)"
                hint="Hint Text"
            />
            <FeatherInput
                label="Foreign Source"
                class="side-input"
                :error="errors.foreignSource"
                :modelValue="config.foreignSource"
                @update:modelValue="(val: string) => updateFormValue('foreignSource', val)"
                hint="Hint Text"
            />
        </div>
        <div v-if="[RequisitionTypes.VMWare].includes(config.type.name)">
            <div class="flex-center side-input">
                <FeatherInput
                    label="Username"
                    class="side-input full-width margin-right"
                    :error="errors.username"
                    :modelValue="config.username"
                    @update:modelValue="(val: string) => updateFormValue('username', val)"
                    hint="Hint Text"
                />
                <FeatherInput
                    type="password"
                    label="Password"
                    class="side-input full-width"
                    :error="errors.password"
                    :modelValue="config.password"
                    @update:modelValue="(val: string) => updateFormValue('password', val)"
                    hint="Hint Text"
                />
            </div>
        </div>
        <div v-if="[RequisitionTypes.File].includes(config.type.name)">
            <FeatherInput
                label="Path"
                class="side-input"
                :error="errors.path"
                :modelValue="config.path"
                @update:modelValue="(val: string) => updateFormValue('path', val)"
                hint="Hint Text"
            />
        </div>
        <div class="flex-center side-input">
            <FeatherSelect
                textProp="name"
                label="Monthly"
                :options="scheduleTypes"
                :error="errors.occurance"
                @update:modelValue="(val: string) => updateFormValue('occurance', val)"
                :modelValue="config.occurance"
                class="occurance"
            />
            <FeatherInput type="time" class="time" label="Schedule Time" :modelValue="config.time" />
        </div>
        <div>
            <FeatherRadioGroup
                class="side-label"
                label="Rescan Behavior"
                :modelValue="config.rescanBehavior"
                @update:modelValue="(val: string) => updateFormValue('rescanBehavior', val)"
            >
                <FeatherRadio
                    v-for="item in rescanItems"
                    :value="item.value"
                    :key="item.name"
                >{{ item.name }}</FeatherRadio>
            </FeatherRadioGroup>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { FeatherSelect } from '@featherds/select'
import { requisitionSubTypes, RequsitionTypesUsingHost, RequisitionTypes, requisitionTypeList } from './copy/requisitionTypes'
import { scheduleTypes } from './copy/scheduleTypes'
import { rescanItems } from './copy/rescanItems'
import { FeatherInput } from '@featherds/input'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import { FeatherRadioGroup, FeatherRadio } from '@featherds/radio'
import { PropType, computed,watch, ref } from 'vue'
import Help from '@featherds/icon/action/Help'
import { LocalConfigurationWrapper } from './configuration.types'

const firstInput = ref<HTMLInputElement | null>(null)

const props = defineProps({
  item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
  helpState: { type: Boolean, required: true },
  toggleHelp: { type: Function, required: true },
  updateFormValue: { type: Function, required: true },
  formActive: {type: Boolean, required:true}
})

const config = computed(() => props.item.config)
const errors = computed(() => props.item.errors)
const formActive = computed(() => props.formActive)
/**
 * Focus the first field in the drawer when opened.
 */
watch(formActive, () => {
  if (formActive.value) {
    if (firstInput.value) {
      firstInput.value.focus()
    }
  }
})
</script>
<style lang="scss" scoped>
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
.margin-right {
    margin-right: 16px;
}
</style>