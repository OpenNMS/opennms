<template>
  <OnmsDialog
    :visible="sessionExpiredDialogVisible"
    header="Your session has expired"
    :closable="false"
    class="session-expired-dialog"
  >
    <p>You are no longer logged in, so your last action was not saved.</p>
    <p class="steps-intro">To keep the changes on this page:</p>
    <ol>
      <li>Click <strong>Log in in a new tab</strong> and log in there.</li>
      <li>When you are logged in, come back to this tab and click <strong>Continue</strong>.</li>
      <li>Do your last action again, for example click <strong>Save</strong>.</li>
    </ol>
    <p>Or click <strong>Reload and log in</strong> to log in on this page. Changes that you did not save are lost.</p>
    <template #footer>
      <OnmsButton
        label="Log in in a new tab"
        @click="openLoginTab"
      />
      <OnmsButton
        variant="outlined"
        label="Continue"
        @click="continueAfterLogin"
      />
      <OnmsButton
        variant="text"
        label="Reload and log in"
        @click="reloadPage"
      />
    </template>
  </OnmsDialog>
</template>

<script
  setup
  lang="ts"
>
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import { rest } from '@/services/axiosInstances'
import { loginPageUrl, sessionExpiredDialogVisible } from '@/services/sessionExpiry'

// The browser shares the session cookie between tabs: after a login in the new tab, this page's
// requests work again, and the user keeps the changes on it.
const openLoginTab = () => {
  window.open(loginPageUrl(), '_blank', 'noopener')
}

// Check the login with a request: if the user is still not logged in, its 401 shows the dialog again.
const continueAfterLogin = async () => {
  sessionExpiredDialogVisible.value = false
  try {
    await rest.get('/info')
  } catch {
    // a 401 shows the dialog again (see installSessionExpiryInterceptor); other errors do not matter here
  }
}

const reloadPage = () => {
  window.location.reload()
}
</script>

<!-- Not scoped: PrimeVue teleports the dialog to <body>, out of reach of scoped styles. -->
<style lang="scss">
.session-expired-dialog {
  // wide enough for the three button labels on one line
  width: 42rem;
  max-width: calc(100vw - 2rem);

  p,
  ol {
    margin-bottom: var(--onms-spacing-m);
  }

  ol {
    padding-left: var(--onms-spacing-xl);
  }
}
</style>
