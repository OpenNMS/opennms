# Options
inputFile <- 'stdin'
columnToForecast <- '${columnToForecast?js_string}'
numSamplesPerSeason <- ${numSamplesPerSeason?c}
numForecasts <- ${numForecasts?c}
confidenceLevel <- ${confidenceLevel?c}
firstIndex <- ${firstIndex?c}
lastIndex <- ${lastIndex?c}

# Read the data from stdin
data <- read.csv(file=inputFile)

# Convert the input to a time series
series <- ts(data[,columnToForecast][firstIndex:lastIndex], frequency = numSamplesPerSeason)

# Make seasonal forecasts if there is more than one sample per season
doSeasonalForecasts <- FALSE
if (numSamplesPerSeason > 1 ) {
  doSeasonalForecasts <- TRUE
}

# Make the predictions
m <- HoltWinters(series, gamma = doSeasonalForecasts)
m.fit <- fitted(m)
p <- predict(m, numForecasts, prediction.interval = TRUE, level = confidenceLevel)

# Create the output matrix
num.rows <- nrow(m.fit) + nrow(p)
results <- matrix(, nrow = num.rows, ncol = 0)

# Build the columns of the matrix
fit <- c(m.fit[, 'xhat'], p[,'fit'])
upr <- c(rep(NA, num.rows - nrow(p)), p[,'upr'])
lwr <- c(rep(NA, num.rows - nrow(p)), p[,'lwr'])

# Combine the columns
results <- cbind(results, fit)
results <- cbind(results, upr)
results <- cbind(results, lwr)

# Send the results to stdout
write.csv(results)