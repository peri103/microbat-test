package microbat.debugpilot.pathfinding;

import java.util.List;

import microbat.debugpilot.settings.PathFinderSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class CorrectnessGreedyPathFinder extends GreedyPathFinder {

  public CorrectnessGreedyPathFinder(final PathFinderSettings settings) {
    this(settings.getTrace(), settings.getSlicedTrace());
  }

  public CorrectnessGreedyPathFinder(Trace trace, List<TraceNode> slicedTrace) {
    super(trace, slicedTrace);
  }

  public CorrectnessGreedyPathFinder() {
    super();
  }

  public static DPUserFeedback giveFeedback_static(final TraceNode node) {
    GreedyPathFinder greedyPathFinder = new CorrectnessGreedyPathFinder();
    return greedyPathFinder.giveFeedback(node);
  }

  @Override
  protected double getCost(VarValue varValue) {
    return varValue.getCorrectness();
  }
}
