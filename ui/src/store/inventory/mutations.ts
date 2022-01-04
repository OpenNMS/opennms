import { IPRange, IPRangeResponse, ProvisionRequest, SNMPDetectRequest, SNMPDetectResponse } from '@/types'
import { State } from './state'

const SET_SHOW_ADD_STEP_NEXT_BUTTON = (state: State, bool: boolean) => {
  state.showAddStepNextButton = bool
}

const SET_SHOW_CONFIGURE_SERVICE_STEP_NEXT_BUTTON = (state: State, bool: boolean) => {
  state.showConfigureServiceStepNextButton = bool
}

const ADD_COMPLETED_SERVICE = (state: State, service: string) => {
  state.completedServices = [...state.completedServices, service]
}

const SET_SHOW_COMPLETE_BUTTON = (state: State, val: boolean | string) => {
  state.showCompleteButton = val
}

const SAVE_IP_RANGE_SCAN_RESPONSE = (state: State, ipRangeResponses: IPRangeResponse[]) => {
  state.ipRangeResponses = ipRangeResponses
}

const SAVE_IP_RANGE_SCAN_REQ = (state: State, ipRanges: IPRange[]) => {
  state.ipRanges = ipRanges
}

const SAVE_SNMP_DETECT_RESPONSE = (state: State, snmpDetectResponses: SNMPDetectResponse[]) => {
  state.snmpDetectResponses = snmpDetectResponses
}

const SAVE_SNMP_DETECT_REQ = (state: State, snmpDetectReq: SNMPDetectRequest[]) => {
  state.snmpDetectRequest = snmpDetectReq
}

const SAVE_PROVISION_REQUEST = (state: State, provisionRequest: ProvisionRequest) => {
  state.provisionRequest = provisionRequest
}

export default {
  ADD_COMPLETED_SERVICE,
  SET_SHOW_COMPLETE_BUTTON,
  SET_SHOW_ADD_STEP_NEXT_BUTTON,
  SET_SHOW_CONFIGURE_SERVICE_STEP_NEXT_BUTTON,
  SAVE_IP_RANGE_SCAN_RESPONSE,
  SAVE_IP_RANGE_SCAN_REQ,
  SAVE_SNMP_DETECT_RESPONSE,
  SAVE_SNMP_DETECT_REQ,
  SAVE_PROVISION_REQUEST
}
