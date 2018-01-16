package microbat.codeanalysis.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequestManager;

import microbat.evaluation.junit.TestCaseAnalyzer;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;

@SuppressWarnings("restriction")
public class ExecutionStatementCollector extends Executor {

	private boolean isOverLong = false;
	private boolean isMultiThread = false;
	private List<BreakPoint> executionOrderList = new ArrayList<>();
	private List<BreakPoint> executionStatements = new ArrayList<>();

	public List<BreakPoint> collectBreakPoints(AppJavaClassPath appClassPath, boolean isTestcaseEvaluation) {
		long t1 = System.currentTimeMillis();
		List<String> excludes = MicroBatUtil.extractExcludeFiles("", appClassPath.getExternalLibPaths());
		
//		List<String> abstractPrefixes = abstractPrefixes(excludes);
		
		this.addLibExcludeList(excludes);

		steps = 0;
		List<BreakPoint> pointList = new ArrayList<>();

		VirtualMachine vm = new VMStarter(appClassPath, isTestcaseEvaluation).start();

		EventRequestManager erm = vm.eventRequestManager();
		addClassWatch(erm);

		EventQueue queue = vm.eventQueue();

		boolean connected = true;
		String previousEvent = null;
		
		while (connected) {
			try {
				EventSet eventSet = queue.remove(TIME_OUT);
				if (eventSet != null) {
					for (Event event : eventSet) {
						previousEvent = createEventLog(event);
						if (event instanceof VMStartEvent) {
							System.out.println("start collecting execution...");

							ThreadReference thread = ((VMStartEvent) event).thread();
							addStepWatch(erm, thread);
							addExceptionWatch(erm);
							addMethodWatch(erm);
							addThreadStartWatch(erm);

							if (isTestcaseEvaluation) {
								disableAllStepRequests();
							} else {
								excludeJUnitLibs();
								this.methodEntryRequest.disable();
								this.methodExitRequest.disable();
							}

						} 
						else if (event instanceof ThreadStartEvent) {
							ThreadReference threadReference = ((ThreadStartEvent) event).thread();
							if(hasValidThreadName(threadReference)) {
//								addStepWatch(erm, threadReference);
//								excludeJUnitLibs();	
								setMultiThread(true);
								System.currentTimeMillis();
							}
						}
						else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
							connected = false;
						} else if (event instanceof ClassPrepareEvent) {
						} else if (event instanceof StepEvent) {
							StepEvent sEvent = (StepEvent) event;
							Location location = sEvent.location();
							
							if(isInIncludedLibrary(location)){
								continue;
							}

							int lineNumber = location.lineNumber();

							String path = null;
							try {
								path = location.sourcePath();
							} catch (AbsentInformationException e) {
								e.printStackTrace();
								continue;
							}
							
							if(lineNumber==-1){
								continue;
							}
							
							String declaringCompilationUnit = path.replace(".java", "");
							declaringCompilationUnit = declaringCompilationUnit.replace(File.separatorChar, '.');

							BreakPoint breakPoint = new BreakPoint(location.declaringType().name(),
									declaringCompilationUnit, lineNumber);
//							System.out.println(breakPoint);

							if (!isInTestRunner(breakPoint)) {
								if (!pointList.contains(breakPoint)) {
									pointList.add(breakPoint);
								}

								this.executionOrderList.add(breakPoint);
								steps++;
							}

							if (steps >= Settings.stepLimit) {
								this.setOverLong(true);
								connected = false;
							}
						} else if (event instanceof MethodEntryEvent) {
							Method method = ((MethodEntryEvent) event).method();
							if(isInIncludedLibrary(method.location())){
								continue;
							}

//							System.out.println(method.declaringType().name() + "." + method.name());

							if (isTestcaseEvaluation) {
								String declaringTypeName = method.declaringType().name();
								// if(appClassPath.getOptionalTestClass().equals(declaringTypeName)){
								if (isTagJUnitCall(declaringTypeName, method.name())) {
									enableAllStepRequests();
									this.methodEntryRequest.disable();
									this.methodExitRequest.disable();
									excludeJUnitLibs();
								}
							}
						} 
						else if (event instanceof ExceptionEvent) {
							System.currentTimeMillis();
						} 
					}

					eventSet.resume();
				} else {
					System.out.println("JVM time out when collecting statement");
					System.out.println("Last event: " + previousEvent);
					vm.exit(0);
					vm.dispose();
					connected = false;
				}

			} catch (InterruptedException e) {
				connected = false;
				e.printStackTrace();
			} /*catch (AbsentInformationException e) {
				e.printStackTrace();
			}*/

		}

		if (vm != null) {
			vm.exit(0);
			vm.dispose();
		}

		// System.out.println("There are totally " + steps + " steps in this
		// execution.");

		this.setExecutionStatements(pointList);
		long t2 = System.currentTimeMillis();
		this.setRunningTime(t2-t1);
		return pointList;
	}
	
	private long runningTime;

	protected boolean isInTestRunner(BreakPoint breakPoint) {
		String className = breakPoint.getDeclaringCompilationUnitName();
		if (className.equals(TestCaseAnalyzer.TEST_RUNNER)) {
			return true;
		}
		return false;
	}

	protected void addBreakPointWatch(EventRequestManager erm, ReferenceType refType, List<ClassLocation> anchorPoint) {
		for (Iterator<ClassLocation> iterator = anchorPoint.iterator(); iterator.hasNext();) {
			ClassLocation classLocation = (ClassLocation) iterator.next();
			System.out.println(refType.name());
			if (classLocation.getClassCanonicalName().equals(refType.name())) {
				List<Location> listOfLocations;
				try {
					listOfLocations = refType.locationsOfLine(classLocation.getLineNumber());
					if (listOfLocations.size() == 0) {
						System.out.println("No element in the list of locations ");
						return;
					}
					Location loc = (Location)listOfLocations.get(0);
					breakpointRequest = erm.createBreakpointRequest(loc);
					breakpointRequest.setEnabled(true);
				} catch (AbsentInformationException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public int getStepNum() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public boolean isOverLong() {
		return isOverLong;
	}

	public void setOverLong(boolean isOverLong) {
		this.isOverLong = isOverLong;
	}

	public List<BreakPoint> getExecutionOrderList() {
		return executionOrderList;
	}

	public void setExecutionOrderList(List<BreakPoint> executionOrderList) {
		this.executionOrderList = executionOrderList;
	}


	public boolean isMultiThread() {
		return isMultiThread;
	}


	public void setMultiThread(boolean isMultiThread) {
		this.isMultiThread = isMultiThread;
	}

	public List<BreakPoint> getExecutionStatements() {
		return executionStatements;
	}

	public void setExecutionStatements(List<BreakPoint> executionStatements) {
		this.executionStatements = executionStatements;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

}
