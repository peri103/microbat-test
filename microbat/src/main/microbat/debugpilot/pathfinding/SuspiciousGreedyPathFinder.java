package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class SuspiciousGreedyPathFinder extends GreedyPathFinder {

  protected static final double eps = 1e-7;

  public SuspiciousGreedyPathFinder(final PathFinderSettings settings) {
    this(settings.getTrace(), settings.getSlicedTrace());
  }

  public SuspiciousGreedyPathFinder(Trace trace, List<TraceNode> slicedTrace) {
    super(trace, slicedTrace);
  }

  public SuspiciousGreedyPathFinder() {
    super();
  }

  public static DPUserFeedback giveFeedback_static(final TraceNode node) {
    GreedyPathFinder greedyPathFinder = new SuspiciousGreedyPathFinder();
    return greedyPathFinder.giveFeedback(node);
  }

  @Override
  protected double getCost(VarValue varValue) {
    return 1.0d / (varValue.getSuspiciousness() + SuspiciousGreedyPathFinder.eps);
  }
}
