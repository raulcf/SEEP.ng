package uk.ac.imperial.lsds.java2sdg.analysis;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.janino.JavaSourceClassLoader;

import uk.ac.imperial.lsds.java2sdg.api.SeepProgram;
import uk.ac.imperial.lsds.java2sdg.api.SeepProgramConfiguration;
import uk.ac.imperial.lsds.java2sdg.bricks.CodeRepr;
import uk.ac.imperial.lsds.java2sdg.bricks.WorkflowRepr;
import uk.ac.imperial.lsds.java2sdg.utils.ConductorUtils;

public class WorkflowConfigurationAnalysis {
	
	private static ConductorUtils cu = new ConductorUtils();
	
	public static Map<String, WorkflowRepr> getWorkflows(String inputFilePath, Map<String, CodeRepr> workflowBodies){
		File inputFile = cu.getFile(inputFilePath);
		String className = cu.getClassName(inputFile);
		
		WorkflowConfigurationAnalysis wa = new WorkflowConfigurationAnalysis();
		SeepProgramConfiguration spc = compileAndExecuteMain(wa, inputFile, className);
		// Get workflows from configuration code
		Map<String, WorkflowRepr> workflows = spc.getWorkflows();
		// Fill WorkflowRepr with workflow body
		for(Entry<String, WorkflowRepr> entry : workflows.entrySet()){
			WorkflowRepr wr = entry.getValue();
			// Possible Name incompatibility => User must use the exact method signature in SeepProgramConfiguration!
			CodeRepr code = workflowBodies.get(entry.getKey());
			wr.setCode(code);
		}
		
		return workflows;
	}
	
	private static SeepProgramConfiguration compileAndExecuteMain(WorkflowConfigurationAnalysis wa, File inputFile, String className){
		SeepProgramConfiguration spc = null;
		ClassLoader cl = new JavaSourceClassLoader(wa.getClass().getClassLoader());
		((JavaSourceClassLoader)cl).setSourcePath(new File[] {inputFile});
		try {
			Object o = cl.loadClass(className).newInstance();
			spc = ((SeepProgram) o).configure();
		} 
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return spc;
	}

}
