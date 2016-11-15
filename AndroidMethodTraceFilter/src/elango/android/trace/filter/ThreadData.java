package elango.android.trace.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ThreadData {
	
	Vector<FunctionData> nodesInThread = new Vector<FunctionData>();
	List<FunctionData> rootNodes = new ArrayList<FunctionData>();
	
	public Vector<FunctionData> getNodesInThread() {
		return nodesInThread;
	}
	public void setNodesInThread(Vector<FunctionData> nodesInThread) {
		this.nodesInThread = nodesInThread;
	}
	public List<FunctionData> getRootNodes() {
		return rootNodes;
	}
	public void setRootNodes(List<FunctionData> rootNodes) {
		this.rootNodes = rootNodes;
	}
	
	

}
