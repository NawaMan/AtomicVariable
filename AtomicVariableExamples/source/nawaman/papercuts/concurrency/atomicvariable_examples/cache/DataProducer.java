package nawaman.papercuts.concurrency.atomicvariable_examples.cache;

public class DataProducer {
    
    private final Logger logger;
    
    public DataProducer(Logger logger) {
        this.logger = logger;
    }
    
    public int produce() {
        this.logger.log("PRODUCE");
        return 42;
    }
    
}
