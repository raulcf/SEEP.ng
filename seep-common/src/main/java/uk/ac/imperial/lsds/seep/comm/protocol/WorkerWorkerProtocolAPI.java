package uk.ac.imperial.lsds.seep.comm.protocol;

public enum WorkerWorkerProtocolAPI {
	
	ACK((short)0, new AckCommand()),
	CRASH((short)1, new CrashCommand()),
	REQUEST_DATAREF((short)2, new RequestDataReferenceCommand());
	
	private short type;
	private short familyType;
	private CommandType ct;
	
	WorkerWorkerProtocolAPI(short type, CommandType ct){
		this.type = type;
		this.familyType = CommandFamilyType.WORKERCOMMAND.ofType();
		this.ct = ct;
	}
	
	public short type(){
		return type;
	}
	
	public short familyType() {
		return familyType;
	}
	
	public CommandType clazz(){
		return ct;
	}
}
