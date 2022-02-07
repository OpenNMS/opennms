<template>
    <div>
        <FeatherInput
            ref="firstInput"
            class="side-input"
            label="Name"
            hint="Hint Text"
            :error="props.item.errors.name"
            v-model="props.item.config.name"
            @update:modelValue="updateValidation"
        />
        <div class="flex-center">
            <FeatherSelect
                class="side-input full-width"
                textProp="name"
                hint="Hint Text"
                label="Type"
                :options="requisitionTypeList"
                :error="props.item.errors.type"
                v-model="props.item.config.type"
                @update:modelValue="newRequisition"
            />
            <div class="icon">
                <FeatherButton icon="Help" @click="() => props.toggleHelp()">
                    <FeatherIcon class="help-icon" :icon="Help"></FeatherIcon>
                </FeatherButton>
            </div>
        </div>
        <div v-if="RequsitionTypesUsingHost.includes(stateIn?.type?.name)">
            <FeatherInput
                label="Host"
                class="side-input"
                :error="props.item.errors.host"
                v-model="props.item.config.host"
                @update:modelValue="updateValidation"
                hint="Hint Text"
            />
        </div>
        <div v-if="[RequisitionTypes.RequisitionPlugin].includes(stateIn?.type?.name)">
            <FeatherSelect
                class="side-input"
                textProp="name"
                hint="Hint Text"
                label="Requisition Plugin"
                :options="requisitionSubTypes"
                @update:modelValue="updateValidation"
                v-model="stateIn.subType"
            />
        </div>
        <div v-if="[RequisitionTypes.DNS].includes(stateIn?.type?.name)">
            <FeatherInput
                label="Zone"
                class="side-input"
                :error="props.item.errors.zone"
                v-model="props.item.config.zone"
                @update:modelValue="updateValidation"
                hint="Hint Text"
            />
            <FeatherInput
                label="Foreign Source"
                class="side-input"
                :error="props.item.errors.foreignSource"
                v-model="props.item.config.foreignSource"
                @update:modelValue="updateValidation"
                hint="Hint Text"
            />
        </div>
        <div v-if="[RequisitionTypes.VMWare].includes(stateIn?.type?.name)">
            <div class="flex-center side-input">
                <FeatherInput
                    label="Username"
                    class="side-input full-width margin-right"
                    :error="props.item.errors.username"
                    v-model="props.item.config.username"
                    @update:modelValue="updateValidation"
                    hint="Hint Text"
                />
                <FeatherInput
                    type="password"
                    label="Password"
                    class="side-input full-width"
                    :error="props.item.errors.password"
                    v-model="props.item.config.password"
                    @update:modelValue="updateValidation"
                    hint="Hint Text"
                />
            </div>
        </div>
        <div v-if="[RequisitionTypes.File].includes(stateIn?.type?.name)">
            <FeatherInput
                label="Path"
                class="side-input"
                :error="props.item.errors.path"
                v-model="props.item.config.path"
                @update:modelValue="updateValidation"
                hint="Hint Text"
            />
        </div>
        <div class="flex-center side-input">
            <FeatherSelect
                textProp="name"
                label="Monthly"
                :options="scheduleTypes"
                :error="props.item.errors.occurance"
                @update:modelValue="updateValidation"
                v-model="stateIn.occurance"
                class="occurance"
            />
            <FeatherInput type="time" class="time" label="Schedule Time" v-model="stateIn.time" />
        </div>
        <div>
            <FeatherRadioGroup
                class="side-label"
                label="Rescan Behavior"
                v-model="stateIn.rescanBehavior"
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
import { PropType } from 'vue'
import { ConfigurationService } from './ConfigurationService'
import Help from '@featherds/icon/action/Help'

const props = defineProps({
    item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
    stateIn: { type: Object, required: true },
    clearAdvancedOptions: { type: Function, required: true },
    helpState: { type: Boolean, required: true },
    toggleHelp: { type: Function, required: true },
})

const newRequisition = () => {
    props.item.config.host = '';
    props.item.config.advancedOptions = []
}

const updateValidation = () => {
    props.item.errors = ConfigurationService.validateLocalItem(props.item.config, true);
}

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