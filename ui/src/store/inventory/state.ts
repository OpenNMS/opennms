import { IPRange, IPRangeResponse, ProvisionRequest, SNMPDetectRequest, SNMPDetectResponse } from '@/types'

export interface State {
  completedServices: string[]
  showCompleteButton: boolean | string
  showAddStepNextButton: boolean
  showConfigureServiceStepNextButton: boolean
  ipRanges: IPRange[]
  ipRangeResponses: IPRangeResponse[]
  snmpDetectRequest: SNMPDetectRequest[]
  snmpDetectResponses: SNMPDetectResponse[]
  provisionRequest: ProvisionRequest
}

const state: State = {
  completedServices: [],
  showCompleteButton: false,
  showAddStepNextButton: false,
  showConfigureServiceStepNextButton: false,
  ipRanges: [],
  ipRangeResponses: [],
  snmpDetectRequest: [],
  snmpDetectResponses: [],
  provisionRequest: {} as ProvisionRequest
}

export default state
