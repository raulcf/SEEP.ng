package uk.ac.imperial.lsds.seepworker.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seepworker.WorkerConfig;

/***
 * Class to move Datasets between disk and memory, as requested (by the DataReferenceManager).
 * @author iv
 *
 */
public class DiskCacher {
	
	final private Logger LOG = LoggerFactory.getLogger(DiskCacher.class.getName());
	
	private Map<Integer, String> filenames;
	private static DiskCacher instance;
	
	private WorkerConfig wc;
	
	private DiskCacher(WorkerConfig wc) {
		filenames = new HashMap<Integer, String>();
		this.wc = wc;
	}

	public static DiskCacher makeDiskCacher(WorkerConfig wc) {
		if(instance == null) {
			instance = new DiskCacher(wc);
		}
		return instance;
	}
	
	public String createDatasetOnDisk(int datasetId) {
		String cacheFileName = "";
		
		//Changing to abs might lead to a conflict (HIGHLY unlikely, needs a DataSet with the opposite 
		//ID cached at exactly the same time), but files that start with - are annoying in console
		//debugging.
		cacheFileName = Math.abs(datasetId) + "_" + System.currentTimeMillis() + ".cached";
		filenames.put(datasetId, cacheFileName);
		
		return cacheFileName;
	}
	
	private String getCacheFileName(int id) {
		String cacheFileName = "";
		if(filenames == null) {
			System.out.println("filenames null");
		}
		if (filenames.containsKey(id)) {
			//Already on disk. We could claim victory and return, but this will allow us to cache any
			//items stuck in memory (see comment below).
			cacheFileName = filenames.get(id);
		} 
		else {
			//Changing to abs might lead to a conflict (HIGHLY unlikely, needs a DataSet with the opposite 
			//ID cached at exactly the same time), but files that start with - are annoying in console
			//debugging.
			cacheFileName = Math.abs(id) + "_" + System.currentTimeMillis() + ".cached";
			filenames.put(id, cacheFileName);
		}
		return cacheFileName;
	}
	
	public int cacheToDisk(Dataset data) throws FileNotFoundException, IOException {
		String cacheFileName = getCacheFileName(data.id());
		
		// Prepare channel
		WritableByteChannel writer = Channels.newChannel(new FileOutputStream(cacheFileName));
		
		// Basically get buffers from Dataset and write them in chunks, and ordered to disk
		Iterator<ByteBuffer> buffers = data.prepareForTransferToDisk();
		
		while(buffers.hasNext()) {
			ByteBuffer bb = buffers.next();
			//TODO: Is this line necessary? Or should it first be checked to see if bb was already flipped?
			bb.flip();
			ByteBuffer size = ByteBuffer.allocate(Integer.BYTES).putInt(bb.limit());
			writer.write(size);
			writer.write(bb);
		}
		
		// close
		int freedMemory = data.completeTransferToDisk();
		
		writer.close();
		
		data.setCachedLocation(cacheFileName);
		LOG.debug("Content is spilled to: {}", cacheFileName);
		
		return freedMemory;
	}
	
	public void retrieveFromDisk(Dataset data) throws FileNotFoundException {
		
		// Get cache file
		String cacheFileName = filenames.get(data.id());
		// Prepare dataset for trasnfer to memory
		ByteBuffer currentPointer = data.prepareForTransferToMemory();
		
		ReadableByteChannel reader = Channels.newChannel(new FileInputStream(cacheFileName));
		
		data.transferToMemory(reader);
		
		data.completeTransferToMemory(currentPointer);
		
		data.unsetCachedLocation();
		filenames.remove(data.id());
		if (filenames.containsKey(data.id())) {
			data.setCachedLocation(filenames.get(data.id()));
		}
	}
	
	/***
	 * Check if a Dataset is currently entirely in memory (or conversely, 
	 * (perhaps partially in the case of multithreading) on disk).
	 * @param data
	 * @return true if data is in memory.
	 */
	public boolean inMem(Dataset data) {
		return (!(filenames.containsKey(data.id())));
	}

}
