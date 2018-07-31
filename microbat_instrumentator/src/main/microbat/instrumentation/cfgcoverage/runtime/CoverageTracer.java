package microbat.instrumentation.cfgcoverage.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.AgentLogger;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFNode;
import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.runtime.value.ValueExtractor;
import microbat.instrumentation.runtime.ITracer;
import microbat.instrumentation.runtime.TracingState;
import microbat.model.BreakPointValue;
import microbat.model.ClassLocation;

public class CoverageTracer implements ICoverageTracer, ITracer {
	private static CoverageTracerStore rtStore = new CoverageTracerStore();
	public static volatile CoverageSFlowGraph coverageFlowGraph;
	public static volatile Map<Integer, List<Integer>> testcaseGraphExecPaths = new HashMap<>();
	public static volatile Map<Integer, BreakPointValue> testInputData = new HashMap<>(); 
	private static int currentTestCaseIdx;
	
	private long threadId;
	private int testIdx;
	private TracingState state = TracingState.INIT;
	private NoProbeTracer noProbeTracer = new NoProbeTracer(this);
	private int methodInvokeLevel = 0;
	private CoverageSFNode currentNode;
	private List<Integer> execPath;
	MethodCallStack methodCallStack = new MethodCallStack();
	private ValueExtractor valueExtractor = new ValueExtractor();
	
	public CoverageTracer(long threadId, int testIdx) {
		this.threadId = threadId;
		this.testIdx = testIdx;
	}
	
	@Override
	public void _reachNode(String methodId, int nodeIdx) {
		if (currentNode == null) {
			currentNode = coverageFlowGraph.getStartNode();
			execPath = new ArrayList<>();
			testcaseGraphExecPaths.put(testIdx, execPath);
		} else {
			CoverageSFNode branch = currentNode.getCorrespondingBranch(methodId, nodeIdx);
			if (branch != null) {
				currentNode.markCoveredBranch(branch, testIdx);
				currentNode = branch;
			} else {
				if (!currentNode.isAliasNode()) {
					AgentLogger.debug(String.format("cannot find branch %s:%d of node %d [testidx=%d]", methodId, nodeIdx,
							currentNode.getId(), testIdx));
				}
				return;
			}
		}
		execPath.add(currentNode.getId());
		currentNode.addCoveredTestcase(testIdx);
	}
	
	@Override
	public void enterMethod(String methodId, String paramTypeSignsCode, String paramNamesCode, Object[] params,
			boolean isEntryPoint) {
		if (isEntryPoint) {
			// keep the last one
			currentNode = null;
			ClassLocation loc = InstrumentationUtils.getClassLocation(methodId);
			BreakPointValue inputData = valueExtractor.extractInputValue(String.valueOf(testIdx), 
					loc.getClassCanonicalName(), loc.getMethodSign(), paramTypeSignsCode, paramNamesCode, params);
			testInputData.put(testIdx, inputData);
			methodInvokeLevel = 0;
			methodCallStack.clear();
		}
		methodInvokeLevel++;
		methodCallStack.push(methodId);
	}
	
	@Override
	public void _exitMethod(String methodId) {
		methodInvokeLevel--;
		methodCallStack.safePop();
	}
	
	private boolean doesNotNeedToRecord(String methodId) {
		try {
			if (methodInvokeLevel >= coverageFlowGraph.getExtensionLayer() 
					|| methodCallStack.size() > methodInvokeLevel) {
				return true;
			}
			CoverageSFNode correspondingNode = currentNode.getCorrespondingBranch(methodId);
			if (correspondingNode == null) {
				return true;
			}
			return false;
		} catch(Throwable t) {
			AgentLogger.error(t);
			return false;
		}
	}
	
	public synchronized static ICoverageTracer _getTracer(String methodId, boolean isEntryPoint, String paramNamesCode,
			String paramTypeSignsCode, Object[] params) {
		try {
			long threadId = Thread.currentThread().getId();
			CoverageTracer coverageTracer = rtStore.get(threadId, currentTestCaseIdx);
			if (coverageTracer == null && isEntryPoint) {
				coverageTracer = rtStore.create(threadId, currentTestCaseIdx);
				coverageTracer.state = TracingState.RECORDING;
			}
			if (coverageTracer == null || coverageTracer.state != TracingState.RECORDING) {
				return EmptyCoverageTracer.getInstance();
			}
			ICoverageTracer tracer = coverageTracer;
			if (!isEntryPoint && coverageTracer.doesNotNeedToRecord(methodId)) {
				tracer = coverageTracer.noProbeTracer;
			}
			tracer.enterMethod(methodId, paramTypeSignsCode, paramNamesCode, params, isEntryPoint);
			return tracer;
		} catch(Throwable t) {
			AgentLogger.error(t);
			return EmptyCoverageTracer.getInstance();
		}
	}

	@Override
	public long getThreadId() {
		return threadId;
	}
	
	public static void startTestcase(String testcase, int testcaseIdx) {
		AgentLogger.debug(String.format("Start testcase %s, testIdx=%s", testcase, testcaseIdx));
		coverageFlowGraph.addCoveredTestcase(testcase, testcaseIdx);
		CoverageTracer.currentTestCaseIdx = testcaseIdx;
	}
	
	public int getTestIdx() {
		return testIdx;
	}
	
	public static void endTestcase(String testcase, long threadId) {
		AgentLogger.debug(String.format("End testcase %s, testIdx=%s, thread=%s", testcase, currentTestCaseIdx, threadId));
	}
	
}
