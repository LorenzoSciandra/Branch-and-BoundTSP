import graph.exceptions.GraphNodeMissingException;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import tsp.BranchAndBound;
import tsp.TSPResult;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatsCalculator {
    final static Logger logger = LogManager.getLogger(StatsCalculator.class);

    public static void main(String[] args) throws GraphNodeMissingException {
        // Unfortunately there are a lot of values that have to be known in order to test...
        // All the parameters that are mandatory don't have a default value set here. They're all nulls just to make
        // the compiler happy.
        // Number of threads to run each test with
        List<Integer> threadCounts = null;
        // Range for node # for tests. Each test is run with all the thread counts.
        IntStream nodesCount = null;
        // Range (inclusive) of values that can be generated for the edges of the Graphs
        Integer edgeMinValue = null;
        Integer edgeMaxValue = null;
        // Maximum execution time for a single test, after which it is terminated.
        // Statistics aren't updated with a failed test.
        Integer maxExecutionTime = null;
        // How many times a test (nodeNumber, threadCount) should be ran (with a different graph).
        // An average result is calculated.
        Integer repeatsForNodeCount = null;
        long seed = System.currentTimeMillis();
        int skipFirstNGraphs = 0;

        try {
            Options options = generateCLIOptions();
            CommandLineParser parser = new DefaultParser();

            // Optional parameters: s S h.

            CommandLine cl = parser.parse(generateHelpOption(), args, true);

            // https://stackoverflow.com/questions/36720946/apache-cli-required-options-contradicts-with-help-option/36722157
            if (cl.hasOption('h')) {
                new HelpFormatter().printHelp("tester", options, true);
                System.exit(0);
            }

            cl = parser.parse(options, args);

            if (cl.hasOption('s')) {
                seed = (long) cl.getOptionObject('s');
                logger.info("Using seed {}", seed);
            } else {
                logger.info("Generated seed {} as it hasn't been specified.", seed);
            }

            if (cl.hasOption('S')) {
                skipFirstNGraphs = (int) cl.getOptionObject('S');
            }

            // nr/nl er t T r
            if (cl.hasOption("nr")) {
                String[] range = cl.getOptionValues("nr");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);

                if (min > max) {
                    int t = min;
                    min = max;
                    max = t;
                }

                nodesCount = IntStream.rangeClosed(min, max);
            } else {
                // cl.hasOption("nl"); => true
                String[] list = cl.getOptionValues("nl");
                nodesCount = Arrays.stream(list).mapToInt(Integer::parseInt);
            }

            threadCounts = Arrays.stream(cl.getOptionValues('T'))
                                 .map(Integer::parseInt)
                                 .collect(Collectors.toList());

            edgeMinValue = Integer.parseInt(cl.getOptionValues("er")[0]);
            edgeMaxValue = Integer.parseInt(cl.getOptionValues("er")[1]);

            if (edgeMinValue > edgeMaxValue) {
                int t = edgeMinValue;
                edgeMinValue = edgeMaxValue;
                edgeMaxValue = t;
            }

            maxExecutionTime = Integer.parseInt(cl.getOptionValue("t"));

            repeatsForNodeCount = Integer.parseInt(cl.getOptionValue("r"));

        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Random random = new Random(seed);

        // We need an executor service to run a single test. Only one test at a time is run, in order to dedicate all
        // resources to it (also given how much memory a single TSP computation takes, it is for the best)
        ExecutorService service = Executors.newSingleThreadExecutor();
        // A list of the stats generated during testing.
        ArrayList<ArrayList<BnBStats>> stats = new ArrayList<>();

        // Iterate over each node count
        Iterator<Integer> nodesIterator = nodesCount.boxed().iterator();
        while (nodesIterator.hasNext()) {
            int nodeCount = nodesIterator.next();
            logger.info("Testing with {} nodes", nodeCount);

            ArrayList<BranchAndBound> graphs = new ArrayList<>();

            // First for that node count, skip the first N graphs. May be removed in the future.
            for (int i = 0; i < skipFirstNGraphs; i++) {
                BasicCompleteGraphGenerator.generateCompleteGraph(nodeCount,
                                                                  edgeMinValue,
                                                                  edgeMaxValue,
                                                                  random);
            }

            // Since we may test on multiple thread counts, store the BnB instances in order to re-use them. This
            // brought me to the discovery of one memory leak with the priority queue.
            for (int i = 0; i < repeatsForNodeCount; i++) {
                BranchAndBound bnb = new BranchAndBound(BasicCompleteGraphGenerator.generateCompleteGraph(nodeCount,
                                                                                                          edgeMinValue,
                                                                                                          edgeMaxValue,
                                                                                                          random));
                // If this value is set, the BnB doesn't terminate the problem from the children thread
                bnb.shouldTerminateIfError = false;
                graphs.add(bnb);
            }

            // Prepare the array of stats. An element for each thread count.
            ArrayList<BnBStats> threadResults = new ArrayList<>(threadCounts.size());

            for (Integer threadCount : threadCounts) {
                BnBStats currentTestResults = new BnBStats(nodeCount,
                                                           (nodeCount - 1L) * (nodeCount) / 2L,
                                                           threadCount);

                for (int i = 0; i < graphs.size(); i++) {
                    BranchAndBound branchAndBound = graphs.get(i);

                    // Submit the problem as a callable.
                    Future<TSPResult> future = service.submit(() -> branchAndBound.solveProblem(threadCount));

                    // This section is surrounded in a try/catch because we run the computation only for a specific
                    // amount of time, after which future#get throws a TimeoutException which we must handle.
                    try {
                        TSPResult result = future.get(maxExecutionTime, TimeUnit.SECONDS);
                        Optional<Long> time = result.getComputationTime();

                        // Just some logging and timekeeping.
                        logger.info("{} Nodes, {} Threads (Run {}/{}) completed after {} milliseconds",
                                    nodeCount,
                                    threadCount,
                                    i + 1, repeatsForNodeCount,
                                    time.orElseThrow());

                        currentTestResults.addTime(time.orElseThrow());
                        currentTestResults.addGeneratedNodes(result.getTotalNodesCount());

                    } catch (InterruptedException | ExecutionException e) {
                        // Unusual error!
                        logger.error(e);
                    } catch (TimeoutException e) {
                        logger.warn("Timeout reached. There might be an exception right above or below ↕");
                        // Cancel the computation. This sends an interrupt to the thread. The thread has to catch it
                        // and stop.
                        future.cancel(true);
                    } finally {
                        System.gc();
                    }
                }

                threadResults.add(currentTestResults);
            }

            stats.add(threadResults);
        }

        // Finished testing. Woohoo!
        // Now close everything. If we don't call this, the program won't terminate.
        service.shutdown();

        try {
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            e.printStackTrace();
        }

        for (ArrayList<BnBStats> statBlock : stats) {
            logger.info("=================================================================");
            logger.info("Results for {} Nodes ({} Edges)", statBlock.get(0).nodeCount, statBlock.get(0).edgeCount);
            for (BnBStats threadStats : statBlock) {
                logger.info("{} Threads - Average: {}ms ({} / {}); Best: {}ms; Worst: {}ms; Nodes: {}",
                            threadStats.getThreads(),
                            threadStats.getAverageTime().getAverage(),
                            threadStats.getAverageTime().getCount(),
                            repeatsForNodeCount,
                            threadStats.getBestTimeString(),
                            threadStats.getWorstTimeString(),
                            threadStats.getAverageNodes().getAverage());
            }
        }

        // 🔊
        Toolkit.getDefaultToolkit().beep();
    }

    private static @NotNull Options generateCLIOptions() {

        Options options = new Options();

        OptionGroup nodeCountGroup = new OptionGroup();
        nodeCountGroup.addOption(Option.builder("nr")
                                       .longOpt("node-range")
                                       .desc("Range of nodes count to test. Inclusive. Separate the min value from " +
                                             "the max with a '-' (eg 5-10).")
                                       .argName("MIN-MAX")
                                       .numberOfArgs(2).valueSeparator('-').type(Integer.class).build())
                      .addOption(Option.builder("nl")
                                       .longOpt("node-list")
                                       .desc("List of nodes count to test. Separate the values with a comma ','.")
                                       .argName("V1,V2,V3,...")
                                       .hasArgs().valueSeparator(',').type(Integer.class).build());
        nodeCountGroup.setRequired(true);

        options.addOptionGroup(nodeCountGroup)
               .addOption(Option.builder("er")
                                .longOpt("edge-cost-range")
                                .desc("Range of edges cost to generate. Inclusive. Separate the min value from the " +
                                      "max with a '-' (eg 5-10).")
                                .argName("MIN-MAX")
                                .required().numberOfArgs(2).valueSeparator('-').type(Number.class).build())
               .addOption(Option.builder("t")
                                .longOpt("timeout")
                                .desc("Time in seconds after which a single test is terminated.")
                                .argName("SECONDS")
                                .required().hasArg().type(Number.class).build())
               .addOption(Option.builder("T")
                                .longOpt("threads")
                                .desc("A list of the thread count to be used for the tests. The same graph is tested " +
                                      "REPEATS * |THREADS| times, for each node count.")
                                .argName("THREADS")
                                .required().hasArgs().valueSeparator(',').type(Number.class).build())
               .addOption(Option.builder("r")
                                .longOpt("repeats")
                                .desc("How many times has a test to be repeated.")
                                .argName("REPEATS")
                                .required().hasArg().type(Number.class).build())
               .addOption(Option.builder("s")
                                .longOpt("seed")
                                .desc("A seed to be used for Graph generation. If not specified, the chosen value " +
                                      "will be system time in milliseconds.")
                                .argName("SEED")
                                .hasArg().type(Number.class).build())
               .addOption(Option.builder("S")
                                .longOpt("skip")
                                .desc("How many graphs should be skipped before starting the tests for a particular " +
                                      "node count.")
                                .argName("GRAPHS #")
                                .hasArg().type(Number.class).build())
               .addOption("h", "help", false, "Print this message.");

        return options;
    }

    private static @NotNull Options generateHelpOption() {
        return new Options().addOption("h", "help", false, "Print this message.");
    }

    public static class BnBStats {
        private final long nodeCount, edgeCount, threads;
        private final AverageAccumulator averageTime = new AverageAccumulator();
        private final AverageAccumulator averageNodes = new AverageAccumulator();
        private final ConditionalStorage bestTime = new ConditionalStorage((nv, ov) -> nv < ov);
        private final ConditionalStorage worstTime = new ConditionalStorage((nv, ov) -> nv > ov);

        public BnBStats(long nodeCount, long edgeCount, long threads) {
            this.nodeCount = nodeCount;
            this.edgeCount = edgeCount;
            this.threads = threads;
        }

        public void addTime(long value) {
            averageTime.addValue(value);
            bestTime.tryUpdateValue(value);
            worstTime.tryUpdateValue(value);
        }

        public void addGeneratedNodes(int nodes) {
            averageNodes.addValue(nodes);
        }

        public long getNodeCount() {
            return nodeCount;
        }

        public long getEdgeCount() {
            return edgeCount;
        }

        public long getThreads() {
            return threads;
        }

        public AverageAccumulator getAverageTime() {
            return averageTime;
        }

        public AverageAccumulator getAverageNodes() {
            return averageNodes;
        }

        public Long getBestTime() {
            return bestTime.getValue();
        }

        public String getBestTimeString() {
            return bestTime.getValue() == null ? "ND" : bestTime.getValue().toString();
        }

        public long getWorstTime() {
            return worstTime.getValue();
        }

        public String getWorstTimeString() {
            return worstTime.getValue() == null ? "ND" : worstTime.getValue().toString();
        }
    }

    /**
     * A simple class that holds a single value which can be updated if the new value satisfies a specified condition.
     */
    public static class ConditionalStorage {
        private final BiFunction<Long, Long, Boolean> condition;
        private Long value = null;

        /**
         * @param condition the condition to be used to check if a newly submitted value should replace the old one.
         */
        public ConditionalStorage(BiFunction<Long, Long, Boolean> condition) {
            this.condition = condition;
        }

        public Long getValue() {
            return value;
        }

        /**
         * Submit a new value. Replaces the old value if it satisfies the ConditionalStorage condition passed when
         * constructed.
         *
         * @param newValue The new value to be submitted
         * @return true if the new value has substituted the old one; false otherwise.
         */
        public boolean tryUpdateValue(long newValue) {
            if (value == null || condition.apply(newValue, this.value)) {
                this.value = newValue;
                return true;
            }

            return false;
        }
    }

    /**
     * A simple class that acts as an accumulator which can also return the average of the submitted values.
     */
    public static class AverageAccumulator {
        private long time, count;

        public AverageAccumulator() {
            this.time = 0;
            this.count = 0;
        }

        /**
         * Adds a new value to the accumulator.
         *
         * @param val the value to be added
         */
        public void addValue(long val) {
            time += val;
            count++;
        }

        /**
         * @return the average of all the values submitted to the accumulator.
         */
        public long getAverage() {
            if (count == 0) {
                return 0;
            }
            return time / count;
        }

        public long getAccumulatedValue() {
            return time;
        }

        public long getCount() {
            return count;
        }
    }
}
