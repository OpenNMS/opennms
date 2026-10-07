<template>
  <a
    v-if="uei"
    :href="notificationsHref"
    data-test="threshold-uei-link"
    :title="`Notifications for ${uei}`"
  >
    {{ uei }}
  </a>
  <span v-else data-test="threshold-uei-empty">--</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useMenuStore } from '@/stores/menuStore'

const props = defineProps<{
  uei?: string
}>()

const menuStore = useMenuStore()

/**
 * Links to the notification wizard's "notifications for this UEI" page.
 *
 * The JSP editor POSTed a hidden form so the wizard would return to the group afterwards. That relied on an
 * unvalidated `returnPage` redirect parameter, so this uses the page's plain GET entry point instead; the
 * wizard then ends on the notifications list rather than bouncing back here.
 */
const notificationsHref = computed(() => {
  const baseHref = menuStore.mainMenu?.baseHref ?? '/opennms/'
  return `${baseHref}admin/notification/noticeWizard/notifsForUEI.jsp?uei=${encodeURIComponent(props.uei ?? '')}`
})
</script>
