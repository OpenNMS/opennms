<template>
  <div class="schedules-tab">
    <p
      v-if="settingsMissing"
      class="empty"
      role="alert"
      data-test="settings-missing"
    >Schedule settings could not be loaded; reload the page.</p>
    <p
      v-else-if="!cards.length"
      class="empty"
      data-test="no-schedules"
    >No schedules: the supervisor covers every time.</p>
    <OnCallScheduleCard
      v-for="(card, index) in cards"
      :key="card.key"
      :ref="el => setRef(card.key, el)"
      :card="card"
      :members="members"
      :highlighted="highlighted === card.key"
      @update:card="c => replaceCard(index, c)"
      @remove="removeCard(index)"
    />
    <div>
      <OnmsButton
        variant="outlined"
        size="small"
        icon="pi pi-plus"
        label="Add Schedule"
        data-test="add-schedule"
        @click="addCard"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'

import { OnmsButton } from '@opennms/onms-ui'

import OnCallScheduleCard from '@/components/OnCallRoles/OnCallScheduleCard.vue'
import { newCard, ScheduleCard } from '@/lib/onCallSchedule'

const props = defineProps<{
  cards: ScheduleCard[]
  members: string[]
  // the schedule settings failed to load, so the cards are not the role's schedules
  settingsMissing?: boolean
  highlightKey: string | null
}>()

const emit = defineEmits<{
  'update:cards': [cards: ScheduleCard[]]
  // the highlight has started: the parent clears highlightKey so the same card can be highlighted again
  highlighted: []
}>()

const HIGHLIGHT_MS = 2000

const highlighted = ref<string | null>(null)
const refs = new Map<string, unknown>()
let timer: ReturnType<typeof setTimeout> | undefined
let unmounted = false

const setRef = (key: string, el: unknown) => {
  if (el) {
    refs.set(key, el)
  } else {
    refs.delete(key)
  }
}

const replaceCard = (index: number, card: ScheduleCard) => {
  emit('update:cards', props.cards.map((c, i) => i === index ? card : c))
}

const removeCard = (index: number) => emit('update:cards', props.cards.filter((_, i) => i !== index))

const addCard = () => {
  emit('update:cards', [...props.cards, newCard('', 'weekly')])
}

// immediate, so a key already set when the tab mounts (switching tabs and highlighting in one tick) works
watch(() => props.highlightKey, async (key) => {
  if (!key) {
    return
  }
  // let the card render first
  await nextTick()
  // the tab may have been closed while waiting
  if (unmounted) {
    return
  }
  const el = (refs.get(key) as { $el?: HTMLElement } | undefined)?.$el
  // scrollIntoView is missing in jsdom/happy-dom
  if (el && typeof el.scrollIntoView === 'function') {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
  highlighted.value = key
  emit('highlighted')
  clearTimeout(timer)
  timer = setTimeout(() => {
    highlighted.value = null
  }, HIGHLIGHT_MS)
}, { immediate: true })

onBeforeUnmount(() => {
  unmounted = true
  clearTimeout(timer)
})
</script>

<style lang="scss" scoped>
.schedules-tab {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 0.5rem;
}

.empty {
  margin: 0;
  color: var(--p-text-muted-color);
}
</style>
