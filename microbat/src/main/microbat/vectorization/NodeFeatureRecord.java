package microbat.vectorization;

import microbat.model.trace.TraceNode;
import microbat.vectorization.vector.Vector;

public class NodeFeatureRecord {

  //	final public NodeVector nodeVector;
  //	final public ClassificationVector classificationVector;
  public final Vector vector;
  public final TraceNode node;

  public NodeFeatureRecord(final TraceNode node, final Vector vector) {
    this.node = node;
    this.vector = vector;
  }

  @Override
  public String toString() {
    return this.vector.toString() + ":" + this.node.getCodeStatement();
  }
}
