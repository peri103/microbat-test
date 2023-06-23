package microbat.views.listeners;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import debuginfo.NodeFeedbacksPair;
import microbat.model.trace.TraceNode;
import microbat.views.TraceView;

public class PathViewSelectionListener implements ISelectionChangedListener {
	
	private TraceView attachedTraceView = null;
	
	public PathViewSelectionListener() {		
	}
	
	public void attachTraceView(TraceView view) {
		this.attachedTraceView = view;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (attachedTraceView == null) return;
		ISelection iSel = event.getSelection();
		if (iSel instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) iSel;
			Object obj = sel.getFirstElement();
			if (obj instanceof NodeFeedbacksPair) {
				NodeFeedbacksPair node = (NodeFeedbacksPair) obj;
				attachedTraceView.jumpToNode(node.getNode());
			}
			
		}
		
	}
	
}