import graph.structures.Graph;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import tsp.BranchAndBound;
import tsp.TSPResult;
import tsp.UnsolvableProblemException;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TSPSolver {

    public static void main(String[] args) throws IOException {
        // Commandline options reading \\
        Options options = generateCLIOptions();
        CommandLineParser parser = new DefaultParser();

        String graphPath = null;
        boolean removeInvalidNodes = false;
        int threadCount = 1;

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                new HelpFormatter().printHelp("solver", options);
                System.exit(0);
            }

            if (!line.hasOption("g")) {
                System.err.println("""
                                      Mandatory option -g hasn't been specified.
                                      Use the command option -h for more informations.
                                      """);
                System.exit(0);
            }

            graphPath = line.getOptionValue("g");

            removeInvalidNodes = line.hasOption("i");

            if (line.hasOption("t")) {
                try {
                    threadCount = Integer.parseInt(line.getOptionValue("t"));
                    if (threadCount <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Parameter -t requires a positive integer value.");
                    System.exit(1);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Problem posing \\
        Graph<Integer, Integer, Integer> graph = loadGraph(graphPath);

        BranchAndBound bnb = new BranchAndBound(graph);

        long time1 = System.currentTimeMillis();
        // Problem Solving \\
        TSPResult result = null;
        try {
            result = bnb.solveProblem(removeInvalidNodes, threadCount);
        } catch (UnsolvableProblemException e) {
            System.err.println("Some nodes have only one incident edge. Terminating. " +
                               "Follows a list of the nodes with issues:");
            System.err.println(e.oneWayNodesKeys.toString());
            System.exit(1);
        }

        long time = System.currentTimeMillis() - time1;

        System.out.println(result.toString());

        System.out.println(result.getStats());

        System.out.println("Execution time: " + time + " milliseconds");

    }

    /**
     * A simple file reader that builds an Integer graph.
     * It expects to find multiple rows. Every row must contain information about exactly ONE edge, formatted like
     * this regular expression: <br>
     * ([0-9]+)[ ,]+([0-9]+)[ ,]+([0-9.]+)<br>
     * [NodeFrom] [NodeTo] [EdgeCost]
     * The cost can be a floating point number.<br><br>
     * If the first character of a line is not a space or a number, it will be skipped.
     * <p>
     * If no file exists at the specified path, the method terminates the program.
     *
     * @param pathToFile The path to the file to be loaded
     * @return A graph based on integer values.
     * @throws IOException Currently I'm not sure in which case this exception is thrown.
     * @implNote If the edge cost is a floating point number, it will be multiplied by 100, before being truncated to
     * an integer.
     */
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
                    System.out.printf("[WARN]: During loading a line was malformed: \"%s\"", line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file exists at the specified path");
            System.exit(1);
        }

        return graph;
    }

    private static @NotNull Options generateCLIOptions() {
        Options options = new Options();

        options.addOption("g", "graph-path", true, "path to the file containing the graph")
               .addOption("i", "ignore-low-degree-nodes", false, "ignore all nodes of degree < 2")
               .addOption("t", "threads", true, "set how many threads are available for computations")
               .addOption("h", "help", false, "print this message");

        return options;
    }
}
