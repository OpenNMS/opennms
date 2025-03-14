<template>
  <div class="card">
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
              <span class="title">Zenith Connect Success</span>
            </div>
            <div>
              <div>Connection response:</div>
              <div class="spacer-medium"></div>
              <table>
                <tbody>
                  <tr>
                    <td>NMS Username</td>
                    <td>{{ nmsUsername }}</td>
                  </tr>
                  <tr>
                    <td>NMS System ID</td>
                    <td>{{ nmsSystemId }}</td>
                  </tr>
                  <tr>
                    <td>NMS Display Name</td>
                    <td>{{ nmsDisplayName }}</td>
                  </tr>
                  <tr>
                    <td>Access Token</td>
                    <td>{{ accessToken }}</td>
                  </tr>
                  <tr>
                    <td>Refresh Token</td>
                    <td>{{ refreshToken }}</td>
                  </tr>
                  <tr>
                    <td>Saved?</td>
                    <td>{{ savedDisplay }}</td>
                  </tr>
                </tbody>
              </table>
              <div class="spacer-medium"></div>
              <div>
                <div>
                  Click to save values. You will then be redirected back to /zenith-connect to view your existing connections.
                </div>
                <FeatherButton
                  primary
                  :disabled="savedSuccess"
                  @click="onSaveValues"
                >
                    Save Values
                </FeatherButton>
              </div>
              <div v-if="savedSuccess">
                <div>
                  Click to return to view connections. Eventually this will be automatic.
                </div>
                <FeatherButton
                  primary 
                  :disabled="!savedSuccess"
                  @click="onViewConnections"
                >
                    View Connections
                </FeatherButton>
              </div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { v2 } from '@/services/axiosInstances'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'
import { useRoute } from 'vue-router'

const menuStore = useMenuStore()
const route = useRoute()
const router = useRouter()

const accessToken = ref('')
const refreshToken = ref('')
const nmsUsername = ref('')
const nmsSystemId = ref('')
const nmsDisplayName = ref('')
const savedDisplay = ref('--')
const savedSuccess = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect Success', to: '#', position: 'last' }
  ]
})

const onSaveValues = async () => {
  const requestData = {
    accessToken: accessToken.value,
    refreshToken: refreshToken.value,
    nmsUsername: nmsUsername.value,
    nmsSystemId: nmsSystemId.value,
    nmsDisplayName: nmsDisplayName.value,
    clientId: 'zenith'
  }

  savedDisplay.value = 'Connecting...'

  try {
    const resp = await v2.post('/zenith-connect/registration-success', requestData)

    if (resp.status === 200) {
      savedDisplay.value = 'Saved!'
    }

    savedDisplay.value = 'Did not save'
  } catch (err) {
    console.error('Error!: ', err)
    savedDisplay.value = `Error!: ${err}`
  }
}

const onViewConnections = () => {
  router.push('zenith-connect')
}

onMounted(() => {
  // TODO: actual implementation, don't allow duplicate registrations
  accessToken.value = route.query.accessToken as string ?? ''
  refreshToken.value = route.query.refreshToken as string ?? ''
  nmsUsername.value = route.query.nmsUsername as string ?? ''
  nmsSystemId.value = route.query.nmsSystemId as string ?? ''
  nmsDisplayName.value = route.query.nmsDisplayName as string ?? ''
})
</script>

<style scoped lang="scss">
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/typography";

table {
  margin-top: 0px !important;
  font-size: 12px !important;
  @include table;
  @include table-condensed;
  @include row-striped;
}

.card {
  background: var($surface);
  padding: 0px 20px 20px 20px;

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
          margin: 24px 0px 24px 19px;
          display: block;
        }
      }

      .input {
        width: 50%;
      }

      .spacer-medium {
        margin-bottom: 0.25rem;
      }
    }
  }
}
</style>
