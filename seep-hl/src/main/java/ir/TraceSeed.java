package ir;

import java.util.ArrayList;
import java.util.List;

import api.objects.Locatable;
import api.topology.GridPosition;

public class TraceSeed implements Locatable {

	// Attributes for Traceable
	private int id;
	private List<Traceable> inputs;
	private List<Traceable> outputs;
	private String name;
	private IdGen idGen;
	
	// Attributes for Locatable
	private GridPosition gridPosition;
	
	public TraceSeed(int id, int i, int j) {
		this.id = id;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.gridPosition = new GridPosition(i, j);
	}
	
	@Override
	public void composeIdGenerator(IdGen idGen) {
		this.idGen = idGen;
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addInput(Traceable t) {
		inputs.add(t);
	}

	@Override
	public void addOutput(Traceable t) {
		outputs.add(t);
	}

	@Override
	public void isInputOf(Traceable t) {
		t.addInput(this);
	}

	@Override
	public void isOutputOf(Traceable t) {
		t.addOutput(this);
	}
	
	@Override
	public TraceableType getTraceableType() {
		return TraceableType.TASK;
	}
	
	@Override
	public List<Traceable> getOutput() {
		return outputs;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID: " + id);
		sb.append(System.lineSeparator());
		sb.append("Name: " + name);
		sb.append(System.lineSeparator());
		sb.append("Type: " + this.getTraceableType());
		sb.append(System.lineSeparator());
		sb.append("Position: " + this.getPositionInTopology());
		sb.append(System.lineSeparator());
		
		sb.append("Inputs: " + inputs.size());
		sb.append(System.lineSeparator());
		for(int i = 0; i < inputs.size(); i++) {
			sb.append("  Input ID: " + inputs.get(i).getId());
			sb.append(System.lineSeparator());
			sb.append("  Input name: " + inputs.get(i).getName());
			sb.append(System.lineSeparator());
		}
		
		sb.append("Outputs: " + outputs.size());
		sb.append(System.lineSeparator());
		for(int i = 0; i < outputs.size(); i++) {
			sb.append(outputs.get(i));
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}
	
	/**
	 * Implement Locatable interface
	 */

	@Override
	public GridPosition getPositionInTopology() {
		return gridPosition;
	}

	@Override
	public int rowIndex() {
		return gridPosition.getRowIdx();
	}

	@Override
	public int colIndex() {
		return gridPosition.getColIdx();
	}

	@Override
	public void moveTo(int i, int j) {
		// TODO Auto-generated method stub
		
	}

}