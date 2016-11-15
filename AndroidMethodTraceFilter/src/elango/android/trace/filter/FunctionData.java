package elango.android.trace.filter;

import java.util.ArrayList;
import java.util.List;


public class FunctionData 
{
	
	int level=0;
	long traceLineNum = 0;
	int threadId = 0;
	
	String functionName = null;
	String className = null;
	String returnType = null;

	FunctionData parent = null;
	List<FunctionData> children = new ArrayList<FunctionData>();
	
	public FunctionData(String functionName, String className, String returnType, int level, long traceLineNum,int threadId)
	{
		this.functionName = functionName;
		this.className = className;
		this.returnType = returnType;
		this.level = level;
		this.traceLineNum = traceLineNum;
		this.threadId = threadId;
	}
	
	public FunctionData() {
		// TODO Auto-generated constructor stub
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public FunctionData getParent() {
		return parent;
	}

	public void setParent(FunctionData parentFunc) {
		parent = parentFunc;
	}

	public List<FunctionData> getChildren() {
		return children;
	}

	public void setChildren(List<FunctionData> children) {
		this.children = children;
	}

	public void addChild(FunctionData childFunc) {
		children.add(childFunc);
	}

	public Boolean isChildOf(String childFunctionName) {

		return true;
	}
	
	public long getTraceLineNum() {
		return traceLineNum;
	}

	public void setTraceLineNum(long traceLineNum) {
		this.traceLineNum = traceLineNum;
	}
	

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	@Override
	public boolean equals(Object v) {
		boolean retVal = false;

		if (v instanceof FunctionData) {
			FunctionData ptr = (FunctionData) v;
			retVal = (functionName == ptr.functionName)? (className == ptr.className)? true:false:false;
		}

		return retVal;
	}
	
	public boolean hasChildren()
	{
		return !children.isEmpty();
	}

	/*@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}*/
}
