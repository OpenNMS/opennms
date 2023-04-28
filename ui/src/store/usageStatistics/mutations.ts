import { State } from './state'
import { UsageStatisticsData, UsageStatisticsMetadata, UsageStatisticsStatus } from '@/types/usageStatistics'

const SAVE_STATISTICS = (state: State, statistics: UsageStatisticsData) => {
  state.statistics = statistics
}

const SAVE_METADATA = (state: State, metadata: UsageStatisticsMetadata) => {
  state.metadata = metadata
}

const SAVE_STATUS = (state: State, status: UsageStatisticsStatus) => {
  state.status = status
}

export default {
  SAVE_STATISTICS,
  SAVE_METADATA,
  SAVE_STATUS
}
