package graph.exceptions;

public class GraphNodeMissingException extends GraphException {
    public GraphNodeMissingException() {
        super();
    }

    public GraphNodeMissingException(String message) {
        super(message);
    }
}
