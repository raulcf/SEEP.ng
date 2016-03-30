package uk.ac.imperial.lsds.java2sdg.bricks.sdg;

import java.util.Map;

public class SDGNode {
	
	/* Each SDGNode must have a unique ID*/
	private static int sdgCount= 0;
	private int id = 0;
	private String name;
	private Map<Integer, TaskElementRepr> taskElements;
	private StateElementRepr stateElement;
	
	public SDGNode(String name, Map<Integer, TaskElementRepr> taskElements, StateElementRepr stateElement) {
		this.id = SDGNode.sdgCount++;
		this.name = name;
		this.taskElements = taskElements;
		this.stateElement = stateElement;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SDG Node: "+ id+ "\n");
		sb.append("Name: "+ name+"\n");
		for(Integer task : taskElements.keySet())
			sb.append("\t TaskElem: "+ task + " Repr: "+taskElements.get(task) +"\n" );
		sb.append("StateElement: "+ stateElement +"\n");
		return sb.toString();
	}
	
}
