package microbat.debugpilot.propagation;

import microbat.debugpilot.propagation.BP.ProbInfer;
import microbat.debugpilot.propagation.spp.SPP_C;
import microbat.debugpilot.propagation.spp.SPP_CS;
import microbat.debugpilot.settings.PropagatorSettings;
import microbat.log.Log;

public class PropagatorFactory {

  private PropagatorFactory() {}

  public static ProbabilityPropagator getPropagator(final PropagatorSettings propagatorSettings) {
    switch (propagatorSettings.getPropagatorType()) {
      case None:
        return new EmptyPropagator();
      case ProfInfer:
        return new ProbInfer(propagatorSettings);
      case SPPS_C:
        return new SPP_C(propagatorSettings);
      case SPPS_CS:
        return new SPP_CS(propagatorSettings);
      default:
        throw new RuntimeException(
            Log.genMsg(
                PropagatorFactory.class,
                "Undefined propagator type: " + propagatorSettings.getPropagatorType()));
    }
  }
}
