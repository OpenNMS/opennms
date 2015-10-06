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

# Calculate the quantile
Qy <- quantile(data[,columnToFilter], c(probability), na.rm = TRUE)

# Replace outliers with NA
data[columnToFilter] <- unlist(lapply(data[,columnToFilter], replace_with_na))

# Output the results
write.csv(data)
