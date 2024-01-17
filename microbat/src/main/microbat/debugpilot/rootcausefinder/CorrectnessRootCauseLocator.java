package microbat.debugpilot.rootcausefinder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import microbat.debugpilot.settings.RootCauseLocatorSettings;
import microbat.debugpilot.userfeedback.DPUserFeedback;
import microbat.model.trace.TraceNode;

public class CorrectnessRootCauseLocator extends AbstractRootCauseLocator {

  public CorrectnessRootCauseLocator(final RootCauseLocatorSettings settings) {
    this(settings.getSliceTrace(), settings.getFeedbacks(), settings.getOutputNode());
  }

  public CorrectnessRootCauseLocator(
      List<TraceNode> sliceTrace, Collection<DPUserFeedback> feedbacks, TraceNode outputNode) {
    super(sliceTrace, feedbacks, outputNode);
  }

  @Override
  public TraceNode locateRootCause() {
    return this.slicedTrace.stream()
        .filter(node -> !node.equals(this.outputNode))
        .filter(node -> !this.isFeedbackGivenTo(node))
        .min(Comparator.comparingDouble(TraceNode::getCorrectness))
        .orElse(null);
  }
}
