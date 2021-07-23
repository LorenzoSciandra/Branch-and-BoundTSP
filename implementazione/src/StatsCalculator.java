import graph.exceptions.GraphNodeMissingException;
import graph.structures.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsp.BranchAndBound;
import tsp.TSPResult;

import java.util.*;
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
            random = new Random(seed);
        } else {
            seed = Integer.parseInt(args[6]);
            logger.info("Using seed {} .", seed);
            random = new Random(seed);
        }

        ExecutorService service = Executors.newSingleThreadExecutor();

        ArrayList<BnBStats[]> stats = new ArrayList<>();

        for (int nodeCount = fromNodeCount; nodeCount <= toNodeCount; nodeCount++) {
            logger.info("Testing with {} nodes", nodeCount);

            ArrayList<BranchAndBound> graphs = new ArrayList<>();

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

                for (BranchAndBound branchAndBound : graphs) {


                    long time1 = System.currentTimeMillis();
                    Future<TSPResult> future = service.submit(() -> branchAndBound.solveProblem(threadCount));

                    try {
                        TSPResult result = future.get(maxExecutionTime, TimeUnit.SECONDS);
                        long time = System.currentTimeMillis() - time1;

                        logger.info("Completed computation after {} milliseconds", time);
/*                    logger.info(result);
                    logger.info("Created: {}; Interm: {}; Viable: {}; Bound: {}; Unfeas: {}",
                                result.getTotalNodesCount(),
                                result.getIntermediateNodesCount(),
                                result.getClosedNodesForBestCount(),
                                result.getClosedNodesForBound(),
                                result.getClosedNodesForUnfeasibilityCount());*/

                        threadResults[j].addValue(time);
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e);
                    } catch (TimeoutException e) {
                        logger.warn("Timeout reached");
                        future.cancel(true);
                    }
                }
            }

            stats.add(threadResults);
        }

        for (BnBStats[] statBlock : stats) {
            logger.info("=================================================================");
            logger.info("Results for {} Nodes ({} Edges)", statBlock[0].nodeCount, statBlock[0].edgeCount);
            for (BnBStats threadStats : statBlock) {
                logger.info("{} Threads - Average: {}ms ({} / {}); Best: {}ms; Worst: {}ms.",
                            threadStats.getThreads(),
                            threadStats.getAverageTime().getAverage(),
                            threadStats.getAverageTime().getCount(),
                            repeatsForNodeCount,
                            threadStats.getBestTime(),
                            threadStats.getWorstTime());
            }
        }

        service.shutdownNow();
    }

    public static class BnBStats {
        private final long nodeCount, edgeCount, threads;
        private final AverageTime averageTime = new AverageTime();
        private final ConditionalTime bestTime = new ConditionalTime((nv, ov) -> nv < ov);
        private final ConditionalTime worstTime = new ConditionalTime((nv, ov) -> nv > ov);

        public BnBStats(long nodeCount, long edgeCount, long threads) {
            this.nodeCount = nodeCount;
            this.edgeCount = edgeCount;
            this.threads = threads;
        }

        public void addValue(long value) {
            averageTime.addValue(value);
            bestTime.tryUpdateValue(value);
            worstTime.tryUpdateValue(value);
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

        public AverageTime getAverageTime() {
            return averageTime;
        }

        public long getBestTime() {
            return bestTime.getValue();
        }

        public long getWorstTime() {
            return worstTime.getValue();
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

    public static class AverageTime {
        private long time, count;

        public AverageTime() {
            this.time = 0;
            this.count = 0;
        }

        public void addValue(long val) {
            time += val;
            count++;
        }

        public long getAverage() {
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
