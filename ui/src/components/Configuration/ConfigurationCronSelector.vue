<template>
  <div>
    <div
      class="flex"
      v-if="!props.config.advancedCrontab"
    >
      <FeatherSelect
        textProp="name"
        label="Schedule Type"
        :options="scheduleTypes"
        :error="props.errors.occurance"
        @update:modelValue="(val: unknown) => updateFormValue('occurance', val as string)"
        :modelValue="props.config.occurance"
        class="occurance"
      />
      <FeatherSelect
        v-if="props.config.occurance.name === 'Monthly'"
        textProp="name"
        label="Day of Month"
        :options="dayTypes"
        :error="props.errors.occuranceDay"
        @update:modelValue="(val: unknown) => updateFormValue('occuranceDay', val as string)"
        :modelValue="props.config.occuranceDay"
        class="occurance-day"
      />
      <FeatherSelect
        v-if="props.config.occurance.name === 'Weekly'"
        textProp="name"
        label="Day of Week"
        :options="weekTypes"
        :error="props.errors.occuranceWeek"
        @update:modelValue="(val: unknown) => updateFormValue('occuranceWeek', val as string)"
        :modelValue="props.config.occuranceWeek"
        class="occurance-week"
      />
      <FeatherInput
        type="time"
        class="time"
        label="Schedule Time"
        @update:modelValue="(val: unknown) => updateFormValue('time', val as string)"
        :modelValue="props.config.time"
      />
    </div>

    <div
      class="flex"
      v-if="props.config.advancedCrontab"
    >
      <FeatherInput
        class="advanced-entry"
        :error="props.errors.occuranceAdvanced"
        label="Advanced (Cron) Schedule"
        @update:modelValue="(val: unknown) => updateFormValue('occuranceAdvanced', val as string)"
        :modelValue="props.config.occuranceAdvanced"
      />
    </div>
    <div
      :class="`feather-input-hint-custom
      ${advancedCronTabHasErrorInHint}`"
    >
      {{ !hasCronValidationError ? scheduledTime : '' }}
    </div>
    <div class="flex">
      <div>
        <FeatherCheckbox
          :modelValue="props.config.advancedCrontab"
          @update:modelValue="(val: unknown) => updateFormValue('advancedCrontab', val as string)"
          >Advanced (Cron) Schedule</FeatherCheckbox
        >
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
import { FeatherSelect } from '@featherds/select'
import { FeatherInput } from '@featherds/input'
import { FeatherCheckbox } from '@featherds/checkbox'
import { scheduleTypes, weekTypes, dayTypes } from './copy/scheduleTypes'
import { PropType } from 'vue'
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
        ret = ErrorStrings.OccuranceDayTime
      } else if (String(e).match(/^(Error: DOW)/g)) {
        ret = ErrorStrings.OccuranceWeekTime
      }
    }
  }

  return ret
})

const errorRegex = /^Error/
const advancedCronTabHasErrorInHint = computed(() => {
  if(!props.config.advancedCrontab || !errorRegex.test(ConfigurationHelper.cronToEnglish(props.config.occuranceAdvanced))) return ''

  return 'error'
})

const hasCronValidationError = computed(() => props.errors.occuranceAdvanced || props.errors.occuranceDay || props.errors.occuranceWeek)
</script>
<style lang="scss">
.advanced-entry {
    .feather-input-sub-text {
        padding-right: 0;
        .feather-input-hint {
            text-align: right;
        }
    }
}
</style>
<style
  lang="scss"
  scoped
>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";

.feather-input-hint-custom {
    flex: 1;
    @include caption();
    color: var($secondary-text-on-surface);
    margin-top: -24px;
    display: flex;
    justify-content: flex-end;
    min-height: var($spacing-xl);
    padding: var($spacing-xxs) 0 var($spacing-xxs) var($spacing-m);
    &.error {
      color: var($error)
    }
}
div a.link {
    color: var($clickable-normal);
    display: inline-block;
    text-decoration: underline;
    &:hover {
        text-decoration: none;
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

