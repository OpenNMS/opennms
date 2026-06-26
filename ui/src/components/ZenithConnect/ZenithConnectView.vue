<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="zc-container">
        <div class="content-container">
          <div class="title-search">
            <span class="title">Zenith Connect</span>
          </div>
          <div>
            <h3>Current Registrations</h3>
          </div>
          <div class="registrations-container">
            <PDataTable :value="registrationRows">
              <PColumn header="Registration Status">
                <template #body="{ data }">
                  <div
                    v-if="data.registered"
                    class="register-success"
                  >Registered</div>
                  <div
                    v-else
                    class="register-failed"
                  >Unregistered</div>
                </template>
              </PColumn>
              <PColumn header="Registered On">
                <template #body="{ data }">{{ formatRegistrationDate(data) }}</template>
              </PColumn>
              <PColumn header="Active">
                <template #body="{ data }">
                  <div
                    v-if="data.active"
                    class="register-success"
                  >Active</div>
                  <div
                    v-else
                    class="register-failed"
                  >Inactive</div>
                </template>
              </PColumn>
              <PColumn
                field="systemId"
                header="System ID"
              />
              <PColumn
                field="displayName"
                header="Display Name"
              />
              <PColumn header="Access Token">
                <template #body="{ data }">
                  {{ ellipsify(data.accessToken ?? '', 30) }}
                  <PButton
                    aria-label="Copy Access Token"
                    @click.prevent="() => onCopyToken(data.accessToken ?? '')"
                  >
                    <FeatherIcon :icon="icons.ContentCopy" />
                  </PButton>
                </template>
              </PColumn>
              <PColumn header="Refresh Token">
                <template #body="{ data }">
                  {{ ellipsify(data.refreshToken ?? '', 30) }}
                  <PButton
                    aria-label="Copy Refresh Token"
                    @click.prevent="() => onCopyToken(data.refreshToken ?? '')"
                  >
                    <FeatherIcon :icon="icons.ContentCopy" />
                  </PButton>
                </template>
              </PColumn>
              <PColumn header="Actions">
                <template #body="{ data }">
                  <PButton
                    label="Send Data"
                    :disabled="!data.registered || !data.systemId"
                    @click.prevent="() => onSendData(data)"
                  />
                </template>
              </PColumn>
            </PDataTable>
          </div>
          <div class="spacer"></div>
          <h3>Register</h3>
          <div>
            Register your Meridian instance with Zenith in order to send data:
            <div class="spacer"></div>
            <PButton
              label="Register with Zenith"
              @click="gotoRegister"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import { format as fnsFormat } from 'date-fns'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { FeatherIcon } from '@featherds/icon'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { useMenuStore } from '@/stores/menuStore'
import { useZenithConnectStore } from '@/stores/zenithConnectStore'
import { BreadCrumb } from '@/types'
import { ZenithConnectRegistration } from '@/types/zenithConnect'
import ContentCopy from '@featherds/icon/action/ContentCopy'

const PButton = Button
const PDataTable = DataTable
const PColumn = Column

const menuStore = useMenuStore()
const zenithConnectStore = useZenithConnectStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)
const currentRegistration = computed<ZenithConnectRegistration | undefined>(() => zenithConnectStore.currentRegistration)
const registrationRows = computed(() => [currentRegistration.value ?? {} as ZenithConnectRegistration])

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect', to: '#', position: 'last' }
  ]
})

const icons = markRaw({
  ContentCopy
})

const formatRegistrationDate = (reg?: ZenithConnectRegistration) => {
  if (reg?.createTimeMs) {
    const date = new Date(reg.createTimeMs)

    return fnsFormat(date, 'yyyy-MM-dd HH:mm:ss')
  }

  return '--'
}

const onCopyToken = async (token: string) => {
  try {
    await navigator.clipboard.writeText(token)

    showSnackBar({
      msg: 'Token copied'
    })
  } catch {
    showSnackBar({
      msg: 'Failed to copy token.'
    })
  }
}

const onSendData = (reg?: ZenithConnectRegistration) => {
  if (reg && reg.registered && reg.systemId) {
    // TODO: fake for now, should set 'active' in DB and possibly notify exporter process
    reg.active = true

    showSnackBar({
      msg: `Sending data for ${reg.displayName} (${reg.systemId})`
    })
  }
}

const gotoRegister = () => {
  router.push('/zenith-connect/register')
}

onMounted(async () => {
  await zenithConnectStore.fetchRegistrations()
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.zc-container {
  display: flex;

  .content-container {
    width: 35rem;
    flex: auto;

    .title-search {
      display: flex;
      justify-content: space-between;

      .title {
        @include headline1;
        margin: 16px 0px 16px 19px;
        display: block;
      }
    }

    .spacer {
      margin-bottom: 1rem;
    }
  }

  .register-success {
    background-color: var(--p-success-color);
    color: white;
    border-radius: 5px;
    text-align: center;
    font-weight: bold;
  }

  .register-failed {
    background-color: var(--p-error-color);
    color: white;
    border-radius: 5px;
    text-align: center;
    font-weight: bold;
  }
}
</style>
