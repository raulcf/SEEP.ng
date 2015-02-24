import java.util.Properties;

import uk.ac.imperial.lsds.seep.api.DataOrigin;
import uk.ac.imperial.lsds.seep.api.DataOriginType;
import uk.ac.imperial.lsds.seep.api.LogicalOperator;
import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.data.Schema;
import uk.ac.imperial.lsds.seep.api.data.Schema.SchemaBuilder;
import uk.ac.imperial.lsds.seep.api.data.Type;
import uk.ac.imperial.lsds.seepcontrib.kafka.config.KafkaConfig;


public class Base implements QueryComposer {

	private final KafkaConfig kafkaConfig;
	
	public Base() {
		Properties p = new Properties();
		p.setProperty(KafkaConfig.KAFKA_SERVER, "localhost:9092");
		p.setProperty(KafkaConfig.ZOOKEEPER_CONNECT, "localhost:2181");
		p.setProperty(KafkaConfig.PRODUCER_CLIENT_ID, "seep");
		p.setProperty(KafkaConfig.CONSUMER_GROUP_ID, "seep");
		p.setProperty(KafkaConfig.BASE_TOPIC, "seep");
		
		kafkaConfig = new KafkaConfig(p);
	}
	
	@Override
	public LogicalSeepQuery compose() {
		System.out.println("[Base] Start to build query");
		
		Schema schema = SchemaBuilder.getInstance().newField(Type.INT, "userId").newField(Type.LONG, "ts")
												   .newField(Type.STRING, "text").build();
		
		LogicalOperator src = queryAPI.newStatelessSource(new Source(), 0);
		LogicalOperator processor = queryAPI.newStatelessOperator(new Processor(), 1);
		LogicalOperator snk = queryAPI.newStatelessSink(new Sink(), 2);
		
		src.connectTo(processor, 1, schema, new DataOrigin(DataOriginType.KAFKA, null, null, kafkaConfig));
		processor.connectTo(snk, 2, schema, new DataOrigin(DataOriginType.KAFKA, null, null, kafkaConfig));
		
		System.out.println("###### Build query finished");
		return queryAPI.build();
	}

}
