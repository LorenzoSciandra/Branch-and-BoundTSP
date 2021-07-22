import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Function;

public class BasicCompleteGraphGenerator {
    public static void main(String[] args) throws GraphNodeMissingException, IOException {
        Integer nodeCount = null;
        Integer minCost = null;
        Integer maxCost = null;

        File dir = new File(args[0]);
        if (!dir.isDirectory()) {
            System.err.println("Specified path is not a valid directory");
            System.exit(1);
        }

        Scanner kb = new Scanner(System.in);

        nodeCount = getFromArgsOrScanner(args, 1,
                                         kb, (val) -> val >= 2,
                                         "Quanti nodi devono essere generati (>= 2)? ",
                                         "Inserisci un intero >= 2: ");

        minCost = getFromArgsOrScanner(args, 2,
                                       kb, (val) -> val >= 1,
                                       "Costo minimo per gli archi? (>= 1)",
                                       "Inserisci un intero >= 1");

        maxCost = getFromArgsOrScanner(args, 3,
                                       kb, (val) -> val >= 1,
                                       "Costo minimo per gli archi? (>= 1)",
                                       "Inserisci un intero >= 1");

        if (minCost > maxCost) {
            Integer t = minCost;
            minCost = maxCost;
            maxCost = t;
        }

        File graphFile = new File(String.format("%s%c%sN_%sMi_%sMa.mtx",
                                                dir.getPath(),
                                                File.separatorChar,
                                                nodeCount, minCost, maxCost));

        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        for (int i = 1; i <= nodeCount; i++) {
            graph.addNode(i);
        }

        for (int i = 1; i <= nodeCount; i++) {
            for (int j = i + 1; j <= nodeCount; j++) {
                graph.addEdge(i, j, (int) (Math.random() * (maxCost - minCost) * minCost));
            }
        }

        if (!graphFile.createNewFile()) {
            System.err.printf("Cannot create file with path %s.\n", graphFile);
            System.exit(2);
        }


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(graphFile, false))) {
            Iterator<Edge<Integer, Integer>> iterator = graph.getEdges().iterator();
            while (iterator.hasNext()) {
                Edge<Integer, Integer> edge = iterator.next();
                String format = String.format("%d %d %d", edge.getFrom(), edge.getTo(), edge.getLabel());
                if (iterator.hasNext()) {
                    format += '\n';
                }
                writer.write(format);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Graph exported to %s\n", graphFile);
    }

    private static Integer getFromArgsOrScanner(String[] args,
                                                int index,
                                                Scanner scanner,
                                                Function<Integer, Boolean> validator,
                                                String prompt,
                                                String errorMessage) {
        if (args.length < index + 1) {
            System.out.print(prompt);
            return readInteger(scanner, validator, errorMessage);
        } else {
            return Integer.parseInt(args[index]);
        }
    }

    private static Integer readInteger(Scanner scanner,
                                       Function<Integer, Boolean> validator,
                                       String errorMessage) {
        Integer nodeCount = null;
        do {
            try {
                nodeCount = Integer.parseInt(scanner.next());
                if (!validator.apply(nodeCount)) {
                    nodeCount = null;
                    System.out.print(errorMessage);
                }
            } catch (NumberFormatException ignored) {
                System.out.print(errorMessage);
            }
        } while (nodeCount == null);

        return nodeCount;
    }
}
