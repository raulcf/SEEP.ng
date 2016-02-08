package uk.ac.imperial.lsds.seep.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.api.DataReference;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;


public class Stage implements Comparable<Stage>{

	private final int stageId;
	
	private StageType type;
	/**
	 * Dependencies of this stage
	 */
	private Set<Stage> upstream;
	/**
	 * This stage is a dependency of downstream stages
	 */
	private Set<Stage> downstream;
	/**
	 * All input data references this stage must consume. These will be partitioned across available tasks
	 */
	private Map<Integer, Set<DataReference>> inputDataReferences;
	/**
	 * All data references produced by this stage
	 */
	private Map<Integer, Set<DataReference>> outputDataReferences;
	/**
	 * The logical operators that are part of this stage.
	 */
	private Deque<Integer> wrapping;
	
	/**
	 * Whether this stage will create a partitioned output or not
	 */
	private boolean hasPartitionedState = false;
	private boolean hasMultipleInput = false;
	
	/**
	 * Optional Stage priority used for Two-level Scheduling
	 */
	int priority = -1;
	

	public Stage(int stageId){
		this.stageId = stageId;
		this.upstream = new HashSet<>();
		this.downstream = new HashSet<>();
		this.inputDataReferences = new HashMap<>();
		this.outputDataReferences = new HashMap<>();
		this.wrapping = new ArrayDeque<>();
	}
	
	public Stage(int stageId, int priority){
		this.stageId = stageId;
		this.upstream = new HashSet<>();
		this.downstream = new HashSet<>();
		this.inputDataReferences = new HashMap<>();
		this.outputDataReferences = new HashMap<>();
		this.wrapping = new ArrayDeque<>();
		this.priority = priority;
	}
	
	/**
	 * Priority Stage copy Constructor
	 */
	public Stage(Stage s, int priority){
		this.stageId = s.getStageId();
		this.upstream = s.getDependencies();
		this.downstream = s.getDependants();
		this.inputDataReferences = s.getInputDataReferences();
		this.outputDataReferences = s.getOutputDataReferences();
		this.wrapping = s.getWrappedOperators();
		this.type = s.type;
		this.hasPartitionedState = s.hasPartitionedState;
		this.hasMultipleInput = s.hasMultipleInput;
		this.priority = priority;
	}
	
	
	public Stage() { 
		this.stageId = 0;
	}
	
	public int getStageId(){
		return stageId;
	}
	
	public void add(int opId){
		this.wrapping.push(opId);
	}
	
	public Deque<Integer> getWrappedOperators() {
		return wrapping;
	}
	
	public Map<Integer, Set<DataReference>> getInputDataReferences() {
		return inputDataReferences;
	}
	
	public void addInputDataReference(int streamId, Set<DataReference> dataReferences) {
		if(! this.inputDataReferences.containsKey(streamId)){
			this.inputDataReferences.put(streamId, new HashSet<>());
		}
		this.inputDataReferences.get(streamId).addAll(dataReferences);
	}
	
	public Map<Integer, Set<DataReference>> getOutputDataReferences() {
		return outputDataReferences;
	}
	
	public Set<EndPoint> getInvolvedNodes() {
		// FIXME: cannot depend on DR, as these can be external, i.e. no endpoint inside
		Set<EndPoint> in = new HashSet<>();
		for(Set<DataReference> drs : inputDataReferences.values()) {
			for(DataReference dr : drs) {
				in.add(dr.getEndPoint());
			}
		}
		return in;
	}
	
	public boolean responsibleFor(int opId) {
		return wrapping.contains(opId);
	}
	
	public int getIdOfOperatorBoundingStage(){
		return wrapping.peek();
	}
	
	public void setHasPartitionedState(){
		this.hasPartitionedState = true;
	}
	
	public boolean hasPartitionedStage(){
		return hasPartitionedState;
	}
	
	public void setRequiresMultipleInput(){
		this.hasMultipleInput = true;
	}
	
	public boolean hasMultipleInput(){
		return hasMultipleInput;
	}
	
	public void setStageType(StageType type){
		this.type = type;
	}
	
	public StageType getStageType(){
		return type;
	}
	
	public void dependsOn(Stage stage) {
		upstream.add(stage);
		stage.downstream.add(this);
	}
	
	public Set<Stage> getDependencies(){
		return upstream;
	}
	
	public Set<Stage> getDependants() {
		return downstream;
	}
	
	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public int hashCode(){
		return stageId;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("StageId: "+stageId);
		sb.append(Utils.NL);
		sb.append("Type: "+type.toString());
		sb.append(Utils.NL);
		sb.append("Wraps these operators: ");
		sb.append(Utils.NL);
		for(Integer opId : wrapping){
			sb.append("  op -> "+opId);
			sb.append(Utils.NL);
		}
		sb.append(Utils.NL);
		sb.append("DependsOn: ");
		sb.append(Utils.NL);
		for(Stage s : upstream){
			sb.append("  st -> "+s.stageId);
			sb.append(Utils.NL);
		}
		sb.append("Serves: ");
		sb.append(Utils.NL);
		for(Stage s : downstream){
			sb.append("  st -> "+s.stageId);
			sb.append(Utils.NL);
		}
		if(this.priority != -1){
			sb.append("Priotity: ");
			sb.append(Utils.NL);
			sb.append(this.getPriority());
			sb.append(Utils.NL);
		}
		return sb.toString();
	}

	/** 
	 * Compare stages by their priorities for priority scheduling
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Stage o) {
		return o.getPriority()- this.getPriority();
	}
	
}
