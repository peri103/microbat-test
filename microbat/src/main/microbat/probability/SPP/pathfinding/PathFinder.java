package microbat.probability.SPP.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.recommendation.UserFeedback;
import microbat.util.UniquePriorityQueue;

public class PathFinder {
	
	private final Trace trace;
	
	private final List<TraceNode> slicedTrace;;
	
	public PathFinder(Trace trace, final List<TraceNode> slicedTrace ) {
		this.trace = trace;
		this.slicedTrace = slicedTrace;
	}
	
	public ActionPath findPath_dijstra(final TraceNode startNode, final TraceNode endNode) {
		Graph<TraceNode, NodeFeedbacksPair> graph = this.constructGraph();
		DijkstraShortestPath<TraceNode, NodeFeedbacksPair> dijstraAlg = new DijkstraShortestPath<>(graph);
		List<NodeFeedbacksPair> path = dijstraAlg.getPath(startNode, endNode).getEdgeList();
		if (path == null) {
			return null;
		}
		// Add the root cause feedback to the end of the path
		UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
		NodeFeedbacksPair pair = new NodeFeedbacksPair(endNode, feedback);
		path.add(pair);
		return new ActionPath(path);
	}
	
	public ActionPath findPathway_greedy(final TraceNode startNode, final TraceNode endNode) {
		ActionPath path = new ActionPath();
		TraceNode currentNode = startNode;
		while(currentNode != null) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				return path;
			}
			if (currentNode.getOrder() <= endNode.getOrder()) {
				return null;
			}
			UserFeedback feedback = this.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = this.findNextNode(currentNode, feedback);
		}
		return null;
	}
	
	public ActionPath findPathway_greedy(final TraceNode startNode, final TraceNode endNode, final ActionPath initPath) {
		ActionPath path = new ActionPath(initPath);
		
		// There should be only one feedback so directly get(0) is fine
		UserFeedback lastFeedback = path.peek().getFeedbacks().get(0);
		TraceNode lastNode = path.peek().getNode();
		
		// Set new starting node based on the initPath
		TraceNode currentNode = this.findNextNode(lastNode, lastFeedback);
		while (currentNode != null && currentNode.getOrder() > endNode.getOrder()) {
			if (currentNode.equals(endNode)) {
				UserFeedback feedback = new UserFeedback(UserFeedback.ROOTCAUSE);
				path.addPair(currentNode, feedback);
				break;
			}
			UserFeedback feedback = this.giveFeedback(currentNode);
			path.addPair(currentNode, feedback);
			currentNode = this.findNextNode(currentNode, feedback);
		}
		return path;
	}
	
	public UserFeedback giveFeedback(final TraceNode node) {
		UserFeedback feedback = new UserFeedback();
		
		TraceNode controlDom = node.getControlDominator();
		double controlProb = 2.0;
		if (controlDom != null) {
			controlProb = controlDom.getConditionResult().getProbability();
		}
		
		double minReadProb = 2.0;
		VarValue wrongVar = null;
		for (VarValue readVar : node.getReadVariables()) {
			// If the readVar is This variable, then ignore
			if (readVar.isThisVariable()) {
				continue;
			}
			double prob = readVar.getProbability();
			if (prob < minReadProb) {
				minReadProb = prob;
				wrongVar = readVar;
			}
		}
		
		// There are no controlDom and readVar
		if (controlProb == 2.0 && minReadProb == 2.0) {
			feedback.setFeedbackType(UserFeedback.UNCLEAR);
			return feedback;
		}
		if (controlProb <= minReadProb) {
			feedback.setFeedbackType(UserFeedback.WRONG_PATH);
		} else {
			feedback.setFeedbackType(UserFeedback.WRONG_VARIABLE_VALUE);
			feedback.setOption(new ChosenVariableOption(wrongVar, null));
		}
		return feedback;
	}
	
	private Graph<TraceNode, NodeFeedbacksPair> constructGraph() {
		Graph<TraceNode, NodeFeedbacksPair> directedGraph = new DirectedWeightedMultigraph<TraceNode, NodeFeedbacksPair>(NodeFeedbacksPair.class);
		
		final TraceNode lastNode = this.slicedTrace.get(this.slicedTrace.size()-1);
		UniquePriorityQueue<TraceNode> toVisitNodes = new UniquePriorityQueue<>(new Comparator<TraceNode>() {
			@Override
			public int compare(TraceNode t1, TraceNode t2) {
				return t2.getOrder() - t1.getOrder();
			}
		});
		toVisitNodes.add(lastNode);
		
		while (!toVisitNodes.isEmpty()) {
			final TraceNode node = toVisitNodes.poll();
			directedGraph.addVertex(node);
			for (VarValue readVar : node.getReadVariables()) {
				if (readVar.isThisVariable()) {
					continue;
				}
				final TraceNode dataDom = this.trace.findDataDependency(node, readVar);
				if (dataDom != null) {
					UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_VARIABLE_VALUE);
					feedback.setOption(new ChosenVariableOption(readVar, null));
					NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
					directedGraph.addVertex(dataDom);
					directedGraph.addEdge(node, dataDom, pair);
					directedGraph.setEdgeWeight(pair, readVar.getProbability());
					toVisitNodes.add(dataDom);
				}
			}
			
			TraceNode controlDom = node.getControlDominator();
			if (controlDom != null) {
				UserFeedback feedback = new UserFeedback(UserFeedback.WRONG_PATH);
				NodeFeedbacksPair pair = new NodeFeedbacksPair(node, feedback);
				directedGraph.addVertex(controlDom);
				directedGraph.addEdge(node, controlDom, pair);
				directedGraph.setEdgeWeight(pair, controlDom.getConditionResult().getProbability());
				toVisitNodes.add(controlDom);
			}
		} 
		
		return directedGraph;
	}
	
	private TraceNode findNextNode(final TraceNode node, final UserFeedback feedback) {
		TraceNode nextNode = null;
		if (feedback.getFeedbackType() == UserFeedback.WRONG_PATH) {
			nextNode = node.getControlDominator();
		} else if (feedback.getFeedbackType() == UserFeedback.WRONG_VARIABLE_VALUE) {
			VarValue wrongVar = feedback.getOption().getReadVar();
			nextNode = this.trace.findDataDependency(node, wrongVar);
		}
		return nextNode;
	}
}