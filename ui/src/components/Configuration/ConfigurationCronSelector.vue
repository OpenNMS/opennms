<template>
  <div>
    <div
      class="flex"
      v-if="!props.config.advancedCrontab"
    >
      <FormField label="Schedule Type" class="occurance" :error="props.errors.occurance">
        <OnmsSelect
          data-test="schedule-type-select"
          optionLabel="name"
          :options="scheduleTypes"
          :invalid="Boolean(props.errors.occurance)"
          @update:modelValue="(val: unknown) => updateFormValue('occurance', val as string)"
          :modelValue="props.config.occurance"
        />
      </FormField>
      <FormField
        v-if="props.config.occurance.name === 'Monthly'"
        label="Day of Month"
        class="occurance-day"
        :error="props.errors.occuranceDay"
      >
        <OnmsSelect
          optionLabel="name"
          :options="dayTypes"
          :invalid="Boolean(props.errors.occuranceDay)"
          @update:modelValue="(val: unknown) => updateFormValue('occuranceDay', val as string)"
          :modelValue="props.config.occuranceDay"
        />
      </FormField>
      <FormField
        v-if="props.config.occurance.name === 'Weekly'"
        label="Day of Week"
        class="occurance-week"
        :error="props.errors.occuranceWeek"
      >
        <OnmsSelect
          optionLabel="name"
          :options="weekTypes"
          :invalid="Boolean(props.errors.occuranceWeek)"
          @update:modelValue="(val: unknown) => updateFormValue('occuranceWeek', val as string)"
          :modelValue="props.config.occuranceWeek"
        />
      </FormField>
      <FormField label="Schedule Time" class="time">
        <OnmsInputText
          type="time"
          @update:modelValue="(val: unknown) => updateFormValue('time', val as string)"
          :modelValue="props.config.time"
        />
      </FormField>
    </div>

    <div
      class="flex"
      v-if="props.config.advancedCrontab"
    >
      <FormField label="Advanced (Cron) Schedule" class="advanced-entry" :error="props.errors.occuranceAdvanced">
        <OnmsInputText
          :invalid="Boolean(props.errors.occuranceAdvanced)"
          @update:modelValue="(val: unknown) => updateFormValue('occuranceAdvanced', val as string)"
          :modelValue="props.config.occuranceAdvanced"
        />
      </FormField>
    </div>
    <div
      :class="`input-hint-custom
      ${advancedCronTabHasErrorInHint}`"
    >
      {{ !hasCronValidationError ? scheduledTime : '' }}
    </div>
    <div class="flex">
      <div class="checkbox-field">
        <OnmsCheckbox
          inputId="advanced-crontab-checkbox"
          :modelValue="props.config.advancedCrontab"
          @update:modelValue="(val: unknown) => updateFormValue('advancedCrontab', val as string)"
        />
        <label for="advanced-crontab-checkbox">Advanced (Cron) Schedule</label>
      </div>
    </div>
    <div v-if="props.config.advancedCrontab">
      <a
        target="_blank"
        class="link mb-m"
        href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
        >Quartz Scheduler Documentation</a
      >
    </div>
  </div>
</template>
<script
  lang="ts"
  setup
>
import { OnmsCheckbox, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { scheduleTypes, weekTypes, dayTypes } from './copy/scheduleTypes'
import { computed, PropType } from 'vue'
import { LocalConfiguration, LocalErrors } from './configuration.types'
import { ErrorStrings } from './copy/requisitionTypes'
import { ConfigurationHelper } from './ConfigurationHelper'
import cronstrue from 'cronstrue'

const updateFormValue = (type: string, value: string) => {
  props.updateValue(type, value)
}

const props = defineProps({
  config: { type: Object as PropType<LocalConfiguration>, required: true },
  errors: { type: Object as PropType<LocalErrors>, required: true },
  updateValue: { type: Function, required: true }
})

const scheduledTime = computed(() => {
  let ret = ''

  if (props.config.advancedCrontab) {
    ret = ConfigurationHelper.cronToEnglish(props.config.occuranceAdvanced)
  } else {
    try {
      ret = cronstrue.toString(ConfigurationHelper.convertLocalToCronTab(props.config), { dayOfWeekStartIndexZero: false })
    } catch (e) {
      // custom error instead of cronstrue lib's error message
      if (String(e).match(/^(Error: DOM)/g)) {
        ret = ErrorStrings.Required('Day of the month')
      } else if (String(e).match(/^(Error: DOW)/g)) {
        ret = ErrorStrings.Required('Day of the week')
      }
    }
  }

  return ret
})

const errorRegex = /^Error/
const advancedCronTabHasErrorInHint = computed(() => {
  if (!props.config.advancedCrontab || !errorRegex.test(ConfigurationHelper.cronToEnglish(props.config.occuranceAdvanced))) {
    return ''
  }

  return 'error'
})

const hasCronValidationError = computed(() => props.errors.occuranceAdvanced || props.errors.occuranceDay || props.errors.occuranceWeek)
</script>
<style
  lang="scss"
  scoped
>
@import "@/styles/onms-tokens";
@import '@/styles/onms-typography';

// Local replacement for the removed FeatherDS global spacing utility
// (--onms-spacing-m mirrors the original FeatherDS value).
.mb-m {
    margin-bottom: var(--onms-spacing-m);
}
.input-hint-custom {
    flex: 1;
    @include onms-caption();
    color: var(--p-text-muted-color);
    margin-top: -24px;
    display: flex;
    justify-content: flex-end;
    min-height: var($spacing-xl);
    padding: var($spacing-xxs) 0 var($spacing-xxs) var($spacing-m);
    &.error {
      color: var(--p-red-500)
    }
}
div a.link {
    color: var(--p-primary-color);
    display: inline-block;
    text-decoration: underline;
    &:hover {
        text-decoration: none;
    }
}
.checkbox-field {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 1rem;
    margin-bottom: 0.5rem;

    label {
        cursor: pointer;
    }
}
.flex {
    display: flex;
    width: 100%;
    flex-wrap: wrap;
    > div {
        margin-right: 16px;
        width: calc(33.33% - 16px);
        flex-grow: 1;
        &:last-child {
            width: calc(33.33%);
            margin-right: 0;
        }
        &.advanced-entry {
            flex: 0 0 100%;
        }
    }
}
</style>
