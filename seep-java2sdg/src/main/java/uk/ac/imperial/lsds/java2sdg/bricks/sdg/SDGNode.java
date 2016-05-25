package uk.ac.imperial.lsds.java2sdg.bricks.sdg;

import java.util.Map;

public class SDGNode {

	/* Each SDGNode must have a unique ID - keep the total Nodes count here*/
	private static int sdgCount = 0;
	private int id = 0;
	private String name;
	private Map<Integer, TaskElement> taskElements;
	private String builtCode;
	private StateElement stateElement;
	
	public SDGNode(String name, Map<Integer, TaskElement> taskElements, StateElement stateElement) {
		this.id = SDGNode.sdgCount++;
		this.name = name;
		this.taskElements = taskElements;
		this.stateElement = stateElement;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the taskElements
	 */
	public Map<Integer, TaskElement> getTaskElements() {
		return taskElements;
	}

	/**
	 * @return the stateElement
	 */
	public StateElement getStateElement() {
		return stateElement;
	}
	
	/**
	 * @return the buildCode
	 */
	public String getBuiltCode() {
		return builtCode;
	}

	/**
	 * @param buildCode the buildCode to set
	 */
	public void setBuiltCode(String buildCode) {
		this.builtCode = buildCode;
	}
	
	/**
	 * Need to know the type of the Node
	 */
	public boolean isSource(){
		return (this.taskElements.size()==1) && this.taskElements.values().iterator().next().isSouce();
	}
	
	public boolean isSink(){
		return (this.taskElements.size()==1) && this.taskElements.values().iterator().next().isSink();
	}
	
	public boolean isStateful(){
		return (this.getStateElement() != null);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SDG Node: "+ id+ "\n");
		sb.append("Name: "+ name+"\n");
		for(Integer task : taskElements.keySet())
			sb.append("\t TaskElem: "+ task + " Repr: "+taskElements.get(task) +"\n" );
		sb.append("\t StateElement: "+ ( stateElement!= null ? stateElement.getStateName() +" Class: "+ stateElement.getStateRepr().getStateType() + " Annotation: "+ stateElement.getStateRepr().getStateAnnotation(): null));
		return sb.toString();
	}
	
}
