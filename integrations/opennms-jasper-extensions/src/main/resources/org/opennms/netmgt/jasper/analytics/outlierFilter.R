library(zoo)

# Options
inputFile <- 'stdin'

columnToFilter <- '${columnToFilter}'
probability <- ${probability?c}

# Read the data from stdin
data <- read.csv(file=inputFile)

# Replace any values less than the quantile with NA
replace_with_na <- function(x) {
  if(!is.na(x) && x > Qy[1]) {
    NA
  } else {
    x
  }
}

numNonNaValues <- sum(!is.na(data[,columnToFilter]))

# Only perform the replacement/interpolation if there are some at lease 2 non-NA values
if (numNonNaValues >= 2) {
  # Calculate the quantile
  Qy <- quantile(data[,columnToFilter], c(probability), na.rm = TRUE)

  data[columnToFilter] <- unlist(lapply(data[,columnToFilter], replace_with_na))

  # Replace the NAs with interpolated values
  Zxy <- zoo(data)
  Zxy <- na.approx(Zxy)

  # Output the results
  write.csv(Zxy)
} else {
  write.csv(data)
}

