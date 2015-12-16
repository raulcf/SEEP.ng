package uk.ac.imperial.lsds.seepworker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import joptsimple.OptionParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.comm.IOComm;
import uk.ac.imperial.lsds.seep.comm.serialization.JavaSerializer;
import uk.ac.imperial.lsds.seep.config.CommandLineArgs;
import uk.ac.imperial.lsds.seep.config.ConfigKey;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.metrics.SeepMetrics;
import uk.ac.imperial.lsds.seep.util.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.util.Utils;
import uk.ac.imperial.lsds.seepworker.comm.WorkerMasterAPIImplementation;
import uk.ac.imperial.lsds.seepworker.comm.WorkerMasterCommManager;
import uk.ac.imperial.lsds.seepworker.comm.WorkerWorkerAPIImplementation;
import uk.ac.imperial.lsds.seepworker.comm.WorkerWorkerCommManager;
import uk.ac.imperial.lsds.seepworker.core.Conductor;
import uk.ac.imperial.lsds.seepworker.core.DataReferenceManager;


public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	private void executeWorker(WorkerConfig wc) {
		int masterPort = wc.getInt(WorkerConfig.MASTER_PORT);
		InetAddress masterIp = null;
		try {
			masterIp = InetAddress.getByName(wc.getString(WorkerConfig.MASTER_IP));
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Get connection to master node
		int masterId = Utils.computeIdFromIpAndPort(masterIp, masterPort);
		Connection masterConnection = new Connection(new EndPoint(masterId, masterIp, masterPort).extractMasterControlEndPoint());
		
		// Read configs with info about IP and port to bind to
		String myIpStr = wc.getString(WorkerConfig.LISTENING_IP);
		int myPort = wc.getInt(WorkerConfig.LISTENING_PORT);
		int dataPort = wc.getInt(WorkerConfig.DATA_PORT);
		int controlPort = wc.getInt(WorkerConfig.CONTROL_PORT);
		// If no IP is given, then find the local-public address
		InetAddress myIp = null;
		if(myIpStr == "")
			myIp = Utils.getPublicIp();
		else
			myIp = Utils.getIpFromStringRepresentation(myIpStr);//InetAddress.getByName(myIpStr);
			
		// Create comm object
		Comm comm = new IOComm(new JavaSerializer(), Executors.newCachedThreadPool());
		
		// Create master-worker API handler (to send commands to master)
		WorkerMasterAPIImplementation api = new WorkerMasterAPIImplementation(comm, wc);
		
		// Create DataReferenceManager
		DataReferenceManager drm = DataReferenceManager.makeDataReferenceManager(wc);
		
		// Create conductor
		Conductor c = new Conductor(myIp, api, masterConnection, wc, comm, drm);
		
		// Create and start master-worker communication manager (to receive commands from master)
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[0], this.getClass().getClassLoader());
		WorkerMasterCommManager wmcm = new WorkerMasterCommManager(myIp, myPort, wc, rcl, c);
		wmcm.start();
		
		// Start worker-worker communication manager
		WorkerWorkerAPIImplementation apiWorker = new WorkerWorkerAPIImplementation(comm, c, wc);
		WorkerWorkerCommManager wwcm = new WorkerWorkerCommManager(myIp, controlPort, apiWorker);
		wwcm.start();
		
		// Bootstrap
		myIpStr = Utils.getStringRepresentationOfIp(myIp);
		api.bootstrap(masterConnection, myIpStr, myPort, dataPort, controlPort);
		
		// Configure metrics serving
		this.configureMetricsReporting(wc);
		
		// Register JVM shutdown hook
		registerShutdownHook(Utils.computeIdFromIpAndPort(myIp, myPort), c, masterConnection, api);
	}
	
	private void configureMetricsReporting(WorkerConfig wc){
		int reportConsole = wc.getInt(WorkerConfig.REPORT_METRICS_CONSOLE_PERIOD);
		int reportJMX = wc.getInt(WorkerConfig.REPORT_METRICS_JMX);
		if(reportJMX == 1){
			SeepMetrics.startJMXReporter();
		}
		if(reportConsole > 0){
			SeepMetrics.startConsoleReporter(reportConsole);
		}
	}
	
	public static void main(String args[]){
		// Get properties from command line
		List<ConfigKey> configKeys = WorkerConfig.getAllConfigKey();
		OptionParser parser = new OptionParser();
		CommandLineArgs cla = new CommandLineArgs(args, parser, configKeys);
		Properties commandLineProperties = cla.getProperties();
		
		// Get properties from file, if any
		Properties fileProperties = Utils.readPropertiesFromFile(WorkerConfig.PROPERTIES_FILE, WorkerConfig.PROPERTIES_RESOURCE_FILE);
		
		Properties validatedProperties = Utils.overwriteSecondPropertiesWithFirst(commandLineProperties, fileProperties);
		boolean validates = validateProperties(validatedProperties);
		if(!validates){
			printHelp(parser);
			System.exit(0);
		}
		
		WorkerConfig wc = new WorkerConfig(validatedProperties);
		Main instance = new Main();
		instance.executeWorker(wc);
	}
	
	private static boolean validateProperties(Properties validatedProperties){
		if((!validatedProperties.containsKey(WorkerConfig.MASTER_IP)) ||
				validatedProperties.getProperty(WorkerConfig.MASTER_IP) == null ||
				validatedProperties.getProperty(WorkerConfig.MASTER_IP).equals("")){
			LOG.error("Missing required parameter: {}", WorkerConfig.MASTER_IP);
			return false;
		}
			
		return true;
	}
	
	private static void printHelp(OptionParser parser){
		try {
			parser.printHelpOn(System.out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void registerShutdownHook(int workerId, Conductor c, Connection masterConn, 
											WorkerMasterAPIImplementation api){
		Thread hook = new Thread(new WorkerShutdownHookWorker(workerId, c, masterConn, api));
		Runtime.getRuntime().addShutdownHook(hook);
	}
}
