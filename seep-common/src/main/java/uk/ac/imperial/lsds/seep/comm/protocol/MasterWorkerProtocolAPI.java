package uk.ac.imperial.lsds.seep.comm.protocol;


public enum MasterWorkerProtocolAPI {
	
	BOOTSTRAP((short)0, new BootstrapCommand()), 
	CRASH((short)1, new CrashCommand()), 
	CODE((short)2, new CodeCommand()), 
	LOCAL_ELECT((short)3, new LocalSchedulerElectCommand()),
//	QUERYDEPLOY((short)3, new QueryDeployCommand()),
	STARTQUERY((short)5, new StartQueryCommand()),
	STOPQUERY((short)6, new StopQueryCommand()),
	DEADWORKER((short)7, new DeadWorkerCommand()),
	SCHEDULE_TASKS((short)8, new ScheduleDeployCommand()),
	SCHEDULE_STAGE((short)9, new ScheduleStageCommand()),
	STAGE_STATUS((short)10, new StageStatusCommand()),
	MATERIALIZE_TASK((short)11, new MaterializeTaskCommand()),
	LOCAL_SCHEDULE((short)12, new LocalSchedulerStagesCommand());
	
	private short type;
	private CommandType c;
	
	MasterWorkerProtocolAPI(short type, CommandType c){
		this.type = type;
		this.c = c;
	}
	
	public short type(){
		return type;
	}
	
	public CommandType clazz(){
		return c;
	}

}
