package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BranchAndBound {
    private final PriorityBlockingQueue<SubProblem> subProblemQueue;
    private final Graph<Integer, Integer, Integer> graph;
    private final Integer candidateNode;

    public BranchAndBound(Graph<Integer, Integer, Integer> graph, Integer candidateNode) {
        this.graph = graph.clone();
        this.subProblemQueue = new PriorityBlockingQueue<>();
        this.candidateNode = candidateNode;
    }

    public TSPResult solveProblem() throws UnsolvableProblemException {
        return solveProblem(false, 1);
    }

    public TSPResult solveProblem(int threadNumber) throws UnsolvableProblemException {
        return solveProblem(false, threadNumber);
    }

    public TSPResult solveProblem(boolean ignoreOneWayNodes, int threadNumber) throws UnsolvableProblemException {
        List<Integer> oneWayNodesKeys = graph.getNodes()
                                             .stream()
                                             .filter(node -> node.getDegree() < 2)
                                             .map(Node::getKey)
                                             .collect(Collectors.toUnmodifiableList());
        if (oneWayNodesKeys.size() > 0) {
            if (ignoreOneWayNodes) {
                oneWayNodesKeys.forEach(graph::removeNode);
                System.out.printf("Removing %d nodes.\n", oneWayNodesKeys.size());
            } else {
                throw new UnsolvableProblemException(oneWayNodesKeys);
            }
        }

        TSPResult minTSPResult = new TSPResult(graph, Integer.MAX_VALUE);
        SubProblem rootProblem = new SubProblem(graph, candidateNode);
        subProblemQueue.add(rootProblem);
        minTSPResult.increaseNodeCount(1);

        ProgressBarBuilder pbb = new ProgressBarBuilder().setTaskName("Computing solution")
                                                         .setInitialMax(1)
                                                         .setUpdateIntervalMillis(50)
                                                         .setUnit(" nodes", 1)
                                                         .showSpeed()
                                                         .setSpeedUnit(ChronoUnit.SECONDS)
                                                         .setMaxRenderedLength(120);

        if (threadNumber <= 0) {
            throw new IllegalArgumentException("The thread number must be greater than 0");
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(threadNumber);
        ExecutorCompletionService<Void> termination = new ExecutorCompletionService<>(threadPool);

        try (ProgressBar bar = pbb.build()) {
            AtomicInteger currentLevel = new AtomicInteger(0);
            AtomicBoolean computationCompleted = new AtomicBoolean(false);
            CyclicBarrier threadsIdleBarrier = new CyclicBarrier(threadNumber, currentLevel::incrementAndGet);

            for (int i = 0; i < threadNumber; i++) {
                termination.submit(new NodeComputerTask(currentLevel, threadsIdleBarrier, computationCompleted,
                                                        minTSPResult, bar));
            }

            boolean anErrorOccurred = false;

            for (int tasksHandled = 0; tasksHandled < threadNumber; tasksHandled++) {
                try {
                    termination.take().isDone();
                } catch (InterruptedException e) {
                    anErrorOccurred = true;
                    e.printStackTrace();
                }
            }

            if (anErrorOccurred) {
                System.exit(2);
            }
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            e.printStackTrace();
        }

        minTSPResult.finalizeSolution();

        return minTSPResult;
    }

    private int branch(SubProblem currentProblem) {

        HashMap<Integer, Integer> parentsVector = new HashMap<>();
        dfs(currentProblem.getOneTree().getNode(candidateNode), parentsVector, currentProblem.getOneTree());
        int newNodeCount = 0;

        ArrayList<Edge<Integer, Integer>> subCycle = new ArrayList<>();

        int toNode = candidateNode;
        int fromNode = Integer.MAX_VALUE;

        while (fromNode != candidateNode) {
            fromNode = parentsVector.get(toNode);
            subCycle.add(currentProblem.getOneTree().getEdge(fromNode, toNode));
            toNode = fromNode;
        }

        ArrayList<Edge<Integer, Integer>> mandatoryEdges = currentProblem.getMandatoryEdges();
        ArrayList<Edge<Integer, Integer>> forbiddenEdges = currentProblem.getForbiddenEdges();

        for (Edge<Integer, Integer> integerIntegerEdge : subCycle) {
            if (!(currentProblem.getMandatoryEdges().contains(integerIntegerEdge) ||
                  currentProblem.getMandatoryEdges().contains(integerIntegerEdge.inverse()))
            ) {
                forbiddenEdges.add(integerIntegerEdge);
                SubProblem sp = new SubProblem(graph,
                                               new ArrayList<>(mandatoryEdges),
                                               new ArrayList<>(forbiddenEdges),
                                               candidateNode,
                                               currentProblem.getSubProblemTreeLevel() + 1);
                subProblemQueue.add(sp);
                newNodeCount++;

                forbiddenEdges.remove(integerIntegerEdge);
                mandatoryEdges.add(integerIntegerEdge);
            }
        }

        return newNodeCount;
    }

    private void dfs(@NotNull Node<Integer, Integer, Integer> nodoCorrente,
                     HashMap<Integer, Integer> vettorePadri,
                     Graph<Integer, Integer, Integer> grafo) {
        for (Edge<Integer, Integer> arcoUscente : nodoCorrente.getEdges()) {
            if (!vettorePadri.containsKey(arcoUscente.getTo())) {
                if (!vettorePadri.containsKey(nodoCorrente.getKey()) ||
                    !vettorePadri.get(nodoCorrente.getKey()).equals(arcoUscente.getTo())) {

                    vettorePadri.put(arcoUscente.getTo(), nodoCorrente.getKey());
                    dfs(grafo.getNode(arcoUscente.getTo()), vettorePadri, grafo);
                }
            }
        }
    }

    /**
     * This class contains the code that previously was run inside solveProblem().
     * Its goal is to consume SubProblems present inside the PriorityQueue, and multiple of them should be
     * instantiated in order to compute a solution faster. The amount of NodeComputers must not change since it uses
     * a CyclicBarrier (which once instantiated cannot have its party size changed).
     * <p>
     * While discussing how to improve speed, another solution deemed feasible was to have multiple Runnables that
     * compute the branches of a node, executed and managed by an ExecutorService. This solution might be more
     * scalable and might offer better improvements, if enough RAM and bandwidth are available.
     * It may prove however to be a harder solution to develop, and might bring in some cases to a bigger tree, if
     * the evaluation doesn't update the lowerBound immediately.
     */
    private class NodeComputerTask implements Callable<Void> {
        private final AtomicInteger currentLevel;
        private final CyclicBarrier threadsIdleBarrier;
        private final AtomicBoolean computationCompleted;
        private final TSPResult minTSPResult;
        private final ProgressBar bar;

        public NodeComputerTask(AtomicInteger currentLevel, CyclicBarrier threadsIdleBarrier,
                                AtomicBoolean computationCompleted, TSPResult minTSPResult, ProgressBar bar) {

            this.currentLevel = currentLevel;
            this.threadsIdleBarrier = threadsIdleBarrier;
            this.computationCompleted = computationCompleted;
            this.minTSPResult = minTSPResult;
            this.bar = bar;
        }

        @Override
        public Void call() {
            try {
                while (true) {
                    boolean shouldWait = false;
                    // Get the next SubProblem without removing it from the queue
                    SubProblem peek = subProblemQueue.peek();

                    // If there is no next SubProblem or the SP is part of the next computation level, we should
                    // wait for all the threads to complete.
                    shouldWait = peek == null || currentLevel.get() != peek.getSubProblemTreeLevel();

                    // If there is nothing to work on, just wait for more work.
                    // The Thread will be awoken again when all nodes on the current level have been expanded/closed.
                    if (shouldWait) {
                        // To recognise when the problem has been completely analyzed, we can check if we're the
                        // last thread working on it. If so, just break the barrier and release all the threads.
                        if ((threadsIdleBarrier.getNumberWaiting() == (threadsIdleBarrier.getParties() - 1)) &&
                            (peek == null)) {
                            threadsIdleBarrier.reset();
                            computationCompleted.set(true);
                            return null;
                        }

                        // There is still work to do... probably
                        // Wait for all the other threads if we haven't finished yet, but there is no more work on this
                        // search level.
                        threadsIdleBarrier.await();
                    }

                    // Had to use poll() instead of take() because there are some cases where the item in the queue
                    // that we saw was taken by another thread. This isn't the best fix to the problem, but at least
                    // it terminates now!
                    SubProblem currentProblem = subProblemQueue.poll();
                    if (currentProblem == null) {
                        // System.out.println("Sono davvero capitato qui");
                        continue;
                    }

                    if (currentProblem.isFeasible()) {
                        if (currentProblem.containsHamiltonianCycle()) {
                            if (minTSPResult.getCost() > currentProblem.getLowerBound()) {
                                // Found better solution! Closing because candidate solution.
                                minTSPResult.newSolutionFound(currentProblem.getOneTree(),
                                                              currentProblem.getLowerBound());
                                minTSPResult.increaseClosedNodesForBestCount(1);
                            }
                        } else if (currentProblem.getLowerBound() < minTSPResult.getCost()) {
                            int newNodeCount = BranchAndBound.this.branch(currentProblem);
                            minTSPResult.increaseNodeCount(newNodeCount);
                            minTSPResult.increaseIntermediateNodes(1);
                        } else {
                            minTSPResult.increaseClosedNodesForBound(1);
                        }
                    } else {
                        minTSPResult.increaseClosedNodesForUnfeasibilityCount(1);
                    }
                    // else closed because unfeasible

                    synchronized (bar) {
                        bar.maxHint(minTSPResult.getTotalNodesCount());
                        bar.stepTo(minTSPResult.getClosedNodes() + minTSPResult.getIntermediateNodesCount());
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                // Print exception only if it is REALLY unexpected.
                if (!computationCompleted.get()) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
