import graph.exceptions.GraphNodeMissingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        List<Integer> threadCountList = List.of(1, 2, 4, 8);
        int fromNodeCount = Integer.parseInt(args[0]);
        int toNodeCount = Integer.parseInt(args[1]);
        int edgeMinValue = Integer.parseInt(args[2]);
        int edgeMaxValue = Integer.parseInt(args[3]);
        int maxExecutionTime = Integer.parseInt(args[4]);
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

        ExecutorService service = Executors.newSingleThreadExecutor();

        ArrayList<BnBStats[]> stats = new ArrayList<>();

        for (int nodeCount = fromNodeCount; nodeCount <= toNodeCount; nodeCount++) {
            logger.info("Testing with {} nodes", nodeCount);

            ArrayList<BranchAndBound> graphs = new ArrayList<>();

            for (int i = 0; i < skipFirstNGraphs; i++) {
                BasicCompleteGraphGenerator.generateCompleteGraph(nodeCount,
                                                                  edgeMinValue,
                                                                  edgeMaxValue,
                                                                  random);
            }

            for (int i = 0; i < repeatsForNodeCount; i++) {
                BranchAndBound bnb = new BranchAndBound(BasicCompleteGraphGenerator.generateCompleteGraph(nodeCount,
                                                                                                          edgeMinValue,
                                                                                                          edgeMaxValue,
                                                                                                          random));
                bnb.shouldTerminateIfError = false;
                graphs.add(bnb);
            }

            BnBStats[] threadResults = new BnBStats[threadCountList.size()];


            for (int j = 0; j < threadCountList.size(); j++) {
                Integer threadCount = threadCountList.get(j);

                threadResults[j] = new BnBStats(nodeCount,
                                                (nodeCount - 1L) * (nodeCount) / 2L,
                                                threadCount);

                for (int i = 0; i < graphs.size(); i++) {
                    BranchAndBound branchAndBound = graphs.get(i);

                    Future<TSPResult> future = service.submit(() -> branchAndBound.solveProblem(threadCount));

                    try {
                        TSPResult result = future.get(maxExecutionTime, TimeUnit.SECONDS);
                        Optional<Long> time = result.getComputationTime();

                        logger.info("{} Nodes, {} Threads (Run {}/{}) completed after {} milliseconds",
                                    nodeCount,
                                    threadCount,
                                    i + 1, repeatsForNodeCount,
                                    time.orElseThrow());

                        threadResults[j].addTime(time.orElseThrow());
                        threadResults[j].addGeneratedNodes(result.getTotalNodesCount());

                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e);
                    } catch (TimeoutException e) {
                        logger.warn("Timeout reached. There might be an exception right above or below ↕");
                        future.cancel(true);
                    } finally {
                        System.gc();
                    }
                }
            }

            stats.add(threadResults);
        }

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

        Toolkit.getDefaultToolkit().beep();
    }

    public static class BnBStats {
        private final long nodeCount, edgeCount, threads;
        private final AverageAccumulator averageTime = new AverageAccumulator();
        private final AverageAccumulator averageNodes = new AverageAccumulator();
        private final ConditionalTime bestTime = new ConditionalTime((nv, ov) -> nv < ov);
        private final ConditionalTime worstTime = new ConditionalTime((nv, ov) -> nv > ov);

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

    public static class ConditionalTime {
        private final BiFunction<Long, Long, Boolean> better;
        private Long value = null;

        public ConditionalTime(BiFunction<Long, Long, Boolean> better) {
            this.better = better;
        }

        public Long getValue() {
            return value;
        }

        public boolean tryUpdateValue(long newValue) {
            if (value == null || better.apply(newValue, this.value)) {
                this.value = newValue;
                return true;
            }

            return false;
        }
    }

    public static class AverageAccumulator {
        private long time, count;

        public AverageAccumulator() {
            this.time = 0;
            this.count = 0;
        }

        public void addValue(long val) {
            time += val;
            count++;
        }

        public long getAverage() {
            if (count == 0) {
                return 0;
            }
            return time / count;
        }

        public long getTime() {
            return time;
        }

        public long getCount() {
            return count;
        }
    }
}
