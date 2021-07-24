import graph.exceptions.GraphNodeMissingException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import tsp.BranchAndBound;
import tsp.TSPResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class StatsCalculator {
    final static Logger logger = LogManager.getLogger(StatsCalculator.class);

    public static void main(String[] args) throws GraphNodeMissingException {
        // todo improve parameter insertion
        // todo accept as a parameter the number of threads (as a list optionally: eg. 1,2,4,8)
        // todo accept a range or a list of numbers for the graph node count
        // Unfortunately there are a lot of values that have to be known in order to test...
        // Number of threads to run each test with
        List<Integer> threadCountList = List.of(1, 2, 4, 8);
        // Range for node # for tests. Each test is run with all the thread counts.
        int fromNodeCount = Integer.parseInt(args[0]);
        int toNodeCount = Integer.parseInt(args[1]);
        // Range (inclusive) of values that can be generated for the edges of the Graphs
        int edgeMinValue = Integer.parseInt(args[2]);
        int edgeMaxValue = Integer.parseInt(args[3]);
        // Maximum execution time for a single test, after which it is terminated.
        // Statistics aren't updated with a failed test.
        int maxExecutionTime = Integer.parseInt(args[4]);
        // How many times a test (nodeNumber, threadCount) should be ran (with a different graph).
        // An average result is calculated.
        int repeatsForNodeCount = Integer.parseInt(args[5]);

        Random random;
        long seed;
        if (args.length < 7) {
            seed = System.currentTimeMillis();
            logger.info("Using seed {} as it hasn't been specified.", seed);
        } else {
            seed = Long.parseLong(args[6]);
            logger.info("Using seed {}", seed);
        }
        random = new Random(seed);

        int skipFirstNGraphs = 0;
        if (args.length >= 8) {
            skipFirstNGraphs = Integer.parseInt(args[7]);
        }

        // We need an executor service to run a single test. Only one test at a time is run, in order to dedicate all
        // resources to it (also given how much memory a single TSP computation takes, it is for the best)
        ExecutorService service = Executors.newSingleThreadExecutor();
        // A list of the stats generated during testing.
        ArrayList<BnBStats[]> stats = new ArrayList<>();

        // Iterate over each node count
        for (int nodeCount = fromNodeCount; nodeCount <= toNodeCount; nodeCount++) {
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
            BnBStats[] threadResults = new BnBStats[threadCountList.size()];


            for (int j = 0; j < threadCountList.size(); j++) {
                Integer threadCount = threadCountList.get(j);

                threadResults[j] = new BnBStats(nodeCount,
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

                        threadResults[j].addTime(time.orElseThrow());
                        threadResults[j].addGeneratedNodes(result.getTotalNodesCount());

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

        for (BnBStats[] statBlock : stats) {
            logger.info("=================================================================");
            logger.info("Results for {} Nodes ({} Edges)", statBlock[0].nodeCount, statBlock[0].edgeCount);
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
