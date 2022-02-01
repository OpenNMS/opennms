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
        <FeatherSelect
            class="side-input"
            textProp="name"
            hint="Hint Text"
            label="Type"
            :options="requisitionTypes"
            :error="props.item.errors.type"
            v-model="props.item.config.type"
            @update:modelValue="newRequisition"
        />
        <div v-if="['DNS','VMWare','HTTP','HTTPS'].includes(stateIn?.type?.name)">
            <FeatherInput
                label="Host"
                class="side-input"
                :error="props.item.errors.host"
                v-model="props.item.config.host"
            @update:modelValue="updateValidation"
                hint="Hint Text"
            />
        </div>
        <div v-if="['Requisition'].includes(stateIn?.type?.name)">
            <FeatherSelect
                class="side-input"
                textProp="name"
                hint="Hint Text"
                label="Requisition Type"
                :options="requisitionSubTypes"
            @update:modelValue="updateValidation"
                v-model="stateIn.subType"
            />
        </div>
        <div v-if="['DNS'].includes(stateIn?.type?.name)">
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
        <div v-if="['VMWare'].includes(stateIn?.type?.name)">
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
                label="Password"
                class="side-input full-width"
                :error="props.item.errors.password"
                v-model="props.item.config.password"
            @update:modelValue="updateValidation"
                hint="Hint Text"
            />
            </div>
        </div>
        <div v-if="['File'].includes(stateIn?.type?.name)">
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
import { requisitionTypes, requisitionSubTypes } from './copy/requisitionTypes'
import { scheduleTypes } from './copy/scheduleTypes'
import { rescanItems } from './copy/rescanItems'
import { FeatherInput } from '@featherds/input'
import { FeatherRadioGroup, FeatherRadio } from '@featherds/radio'
import { PropType } from 'vue'
import { ConfigurationService } from './ConfigurationService'

const props = defineProps({
    item: { type: Object as PropType<LocalConfigurationWrapper>, required: true },
    stateIn: { type: Object, required: true },
    clearAdvancedOptions: { type: Function, required: true}
})

const newRequisition = (tyu:{name:string}) => {
 //   props.clearAdvancedOptions();
    props.item.config.host = '';
    props.item.config.advancedOptions = []
}

const updateValidation = () => {
    props.item.errors = ConfigurationService.validateLocalItem(props.item.config,true);
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
  align-items: center;
}
.full-width {
    width:100%;
}
.margin-right {
    margin-right:16px;
}
</style>