/** */
package microbat.sql;

import java.util.List;

import java.util.HashMap;

import microbat.instrumentation.instr.instruction.info.SerializableLineInfo;
import microbat.model.trace.Trace;

/**
 * @author knightsong
 */
public interface TraceRecorder {
  void store(List<Trace> trace);

  void serialize(HashMap<Integer, SerializableLineInfo> instructionTable);
}
