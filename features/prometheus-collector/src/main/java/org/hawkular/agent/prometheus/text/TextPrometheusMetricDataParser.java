/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hawkular.agent.prometheus.PrometheusMetricDataParser;
import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a method that can scrape Permetheus text metric data from input streams.
 */
public class TextPrometheusMetricDataParser extends PrometheusMetricDataParser<MetricFamily> {
    private static final Logger log = LoggerFactory.getLogger(TextPrometheusMetricDataParser.class);

    private String lastLineReadFromStream; // this is only set when we break from the while loop in parse()
    
    private final SampleLineParser sampleLineParser = new SampleLineParser();
    private final TextFormatStrategy strategy;

    /**
     * Provides the input stream where the parser will look for metric data.
     * NOTE: this object will not own this stream - it should never attempt to close it.
     *
     * @param inputStream the stream where the metric data can be found
     */
    public TextPrometheusMetricDataParser(InputStream inputStream, TextFormatStrategy strategy) {
        super(inputStream);
        this.strategy = strategy;
    }

    private class ParserContext {
        // this is the metric family that has been fully built
        public MetricFamily finishedMetricFamily;

        // these are used when building a metric family
        public String name = "";
        public String help = "";
        public MetricType type = null;
        public List<TextSample> textSamples = new ArrayList<>();
        public SampleNameValidator sampleNameValidator = SampleNameValidator.rejectAll();

        // starts a fresh metric family
        public void clear() {
            name = "";
            help = "";
            type = null;
            sampleNameValidator = SampleNameValidator.rejectAll();
            textSamples.clear();
        }

        // complete the construction of the metric family
        public void finishMetricFamily() {
            if (finishedMetricFamily != null) {
                return;
            }

            MetricFamily.Builder metricFamilyBuilder = new MetricFamily.Builder();
            metricFamilyBuilder.setName(name);
            metricFamilyBuilder.setHelp(help);
            metricFamilyBuilder.setType(type);

            // need to convert the samples to metrics
            // We know if the family is a counter or a gauge, all samples are full metrics
            // so we can convert them easily one-for-one.
            // For summary metrics, we need to combine all quantile samples, sum, and count.
            // For histogram metrics, we need to combine all bucket samples, sum, and count.

            Map<Map<String, String>, Metric.Builder<?>> builders = new LinkedHashMap<>();
            
            TextSampleProcessor textSampleProcessor = strategy.getSampleProcessor(type);

            for (TextSample textSample : textSamples) {
                try {
                    textSampleProcessor.process(builders, name, textSample);
                } catch (Exception e) {
                    log.debug("Error processing sample. This metric sample will be ignored: {}",
                            textSample.getLine(), e);
                }
            }

            // now that we've combined everything into individual metric builders, we can build all our metrics
            for (Metric.Builder<?> builder : builders.values()) {
                try {
                    metricFamilyBuilder.addMetric(builder.build());
                } catch (Exception e) {
                    log.debug("Error building metric for metric family [%s] - it will be ignored", name, e);
                }
            }

            finishedMetricFamily = metricFamilyBuilder.build();
        }
    }

    @Override
    public MetricFamily parse() throws IOException {

        // determine the first line we should process. If we were previously called, we already
        // read a line - start from that last line read. Otherwise, prime the pump and read
        // the first line from the stream.
        String line;
        if (lastLineReadFromStream != null) {
            line = lastLineReadFromStream;
            lastLineReadFromStream = null;
        } else {
            line = readLine(getInputStream());
        }

        if (line == null) {
            return null;
        }

        // do a quick check to see if we are getting passed in binary format rather than text
        if (!line.isEmpty() && !new String(new char[] { line.charAt(0) }).matches("\\p{ASCII}*")) {
            throw new IOException("Doesn't look like the metric data is in text format");
        }

        ParserContext context = new ParserContext();

        while (line != null) {
            line = line.trim();

            try {
                if (line.isEmpty()) {
                    // ignore blank lines
                } else if (line.charAt(0) == '#') {
                    String[] parts = line.split("[ \t]+", 4); // 0 is #, 1 is HELP or TYPE, 2 is metric name, 3 is doc
                    if (parts.length < 2) {
                        // ignore line - probably a comment
                    } else if (parts[1].equals("HELP")) {
                        if (!parts[2].equals(context.name)) {
                            // we are hitting a new metric family
                            if (!context.name.isEmpty()) {
                                // break and we'll finish the metric family we previously were building up
                                this.lastLineReadFromStream = line;
                                break;
                            }
                            // start anew
                            context.clear();
                            context.name = parts[2];
                            context.type = MetricType.UNKNOWN; // default in case we don't get a TYPE
                            context.sampleNameValidator = strategy.getNameValidator(context.type, context.name);
                        }

                        if (parts.length == 4) {
                            context.help = unescapeHelp(parts[3]);
                        } else {
                            context.help = "";
                        }
                    } else if (parts[1].equals("TYPE")) {
                        if (!parts[2].equals(context.name)) {
                            if (!context.name.isEmpty()) {
                                // break and we'll finish the metric family we previously were building up
                                this.lastLineReadFromStream = line;
                                break;
                            }
                            // start anew
                            context.clear();
                            context.name = parts[2];
                        }
                        String type = parts.length < 4 ? "" : parts[3];
                        context.type = StringUtils.isEmpty(type) ? MetricType.UNKNOWN : MetricType.tryParse(type);
                        if(context.type != null) {
                            context.sampleNameValidator = strategy.getNameValidator(context.type, context.name);
                        }
                        else {
                            // we don't recognize this type, so we will ignore all samples for this family
                            context.clear();
                        }                        
                    } else {
                        // ignore other tokens - probably a comment
                    }
                } else {
                    // parse the sample line that contains a single metric (or part of a metric as in summary/histo)
                    TextSample sample = sampleLineParser.parse(line);
                    if (!context.sampleNameValidator.isValid(sample.getName())) {
                        if (!context.name.isEmpty()) {
                            // break and we'll finish the metric family we previously were building up
                            this.lastLineReadFromStream = line;
                            break;
                        }
                        context.clear();
                        log.debug("Ignoring an unexpected metric: {}", line);
                    } else {
                        // add the sample to the family we are building up
                        context.textSamples.add(sample);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to process line - it will be ignored: {}", line);
                log.debug("Exception: {}", e);
            }

            // go to the next line
            line = readLine(getInputStream());
        }

        if (!context.name.isEmpty() && context.type != null) {
            // finish the metric family we previously were building up
            context.finishMetricFamily();
        }

        return context.finishedMetricFamily;
    }

    private String unescapeHelp(String text) {
        // algorithm from parser.py
        if (text == null || !text.contains("\\")) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        boolean slash = false;
        for (int c = 0; c < text.length(); c++) {
            char charAt = text.charAt(c);
            if (slash) {
                if (charAt == '\\') {
                    result.append('\\');
                } else if (charAt == 'n') {
                    result.append('\n');
                } else {
                    result.append('\\').append(charAt);
                }
                slash = false;
            } else {
                if (charAt == '\\') {
                    slash = true;
                } else {
                    result.append(charAt);
                }
            }
        }
        if (slash) {
            result.append("\\");
        }
        return result.toString();
    }

    private String readLine(InputStream inputStream) throws IOException {
        int lineChar;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Prometheus end of line character is a newline
        for (lineChar = inputStream.read(); (lineChar != '\n' && lineChar != -1); lineChar = inputStream.read()) {
            baos.write(lineChar);
        }

        if (lineChar == -1 && baos.size() == 0) {
            // EOF
            return null;
        }

        return baos.toString("UTF-8");
    }

}
