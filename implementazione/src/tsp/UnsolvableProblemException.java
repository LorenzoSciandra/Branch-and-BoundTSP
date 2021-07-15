package tsp;

import java.util.Collections;
import java.util.List;

public class UnsolvableProblemException extends Throwable {
    public final List<Integer> oneWayNodesKeys;

    public UnsolvableProblemException(List<Integer> oneWayNodesKeys) {
        this.oneWayNodesKeys = Collections.unmodifiableList(oneWayNodesKeys);
    }
}
