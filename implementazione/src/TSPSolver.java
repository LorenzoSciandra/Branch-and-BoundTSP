import graph.structures.Graph;
import org.jetbrains.annotations.NotNull;
import tsp.BranchAndBound;
import tsp.HamiltonianCycle;
import tsp.UnsolvableProblemException;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSPSolver {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Missing graph file path");
            System.exit(1);
        }

        Graph<Integer, Integer, Integer> graph = loadGraph(args[0]);

        boolean removeInvalidNodes = Boolean.parseBoolean(args[1]);

        BranchAndBound bnb = new BranchAndBound(graph, graph.getNodes().get(0).getKey());

        long time1 = System.currentTimeMillis();

        HamiltonianCycle result = null;
        try {
            result = bnb.solveProblem(removeInvalidNodes);
        } catch (UnsolvableProblemException e) {
            System.err.println("Some nodes have only one incident edge. Terminating.");
            System.err.println(e.oneWayNodesKeys.toString());
            System.exit(1);
        }

        long time = System.currentTimeMillis() - time1;

        System.out.println(result.toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi");

    }

    @NotNull
    private static Graph<Integer, Integer, Integer> loadGraph(String pathToFile) throws IOException {
        File graphFile = new File(pathToFile);

        Graph<Integer, Integer, Integer> graph = new Graph<>(false);
        Pattern edgePattern = Pattern.compile("([0-9]+)[ ,]+([0-9]+)[ ,]+([0-9.]+)");

        try (BufferedReader lineReader = new BufferedReader(new FileReader(graphFile))) {
            Iterator<String> lineIterator = lineReader.lines().iterator();

            while (lineIterator.hasNext()) {
                String line = lineIterator.next();

                // Skip lines with comments or something that doesn't look right
                char c = line.charAt(0);
                if (c != ' ' && (c < '0' || c > '9')) {
                    continue;
                }

                Matcher matcher = edgePattern.matcher(line);
                if (matcher.matches()) {
                    int from = Integer.parseInt(matcher.group(1));
                    int to = Integer.parseInt(matcher.group(2));
                    int weight;
                    try {
                        weight = Integer.parseInt(matcher.group(3));
                    } catch (NumberFormatException e) {
                        weight = (int) (Float.parseFloat(matcher.group(3)) * 100);
                    }
                    graph.addNodesEdge(from, to, weight);
                } else {
                    System.out.println(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file exists at the specified path");
            System.exit(1);
        }

        return graph;
    }
}
