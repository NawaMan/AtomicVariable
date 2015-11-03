package nawaman.papercuts.concurrency.atomicvariable_examples.cache;

import nawaman.papercuts.concurrency.atomicvariable_examples.cache.ExpirableCache.TimeProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Client {
    
    private Logger logger;
    
    @Before
    public void init() {
        this.logger = new Logger();
    }
    
    @Test
    public void noCache() {
        System.out.println(this.getTestName() + ": ");
        
        DataProducer producer = new DataProducer(logger);
        logger.log("GET");
        producer.produce();
        logger.log("GET");
        producer.produce();
        logger.log("GET");
        assertEquals("Direct: ", "GET - PRODUCE - GET - PRODUCE - GET", logger.toString());
        System.out.println("The production happens twice!");
        System.out.println();
    }
    
    @Test
    public void withSimpleCache() {
        System.out.println(this.getTestName() + ": ");
        
        DataProducer producer = new DataProducer(logger);
        SimpleCache<Integer> cache = new SimpleCache<Integer>(()->producer.produce());
        logger.log("GET");
        cache.get();
        logger.log("GET");
        cache.get();
        logger.log("GET");
        assertEquals("Via cache: ", "GET - PRODUCE - GET - GET", logger.toString());
        System.out.println("The production happens once!");
        System.out.println();
    }
    
    @Test
    public void invalidateSimpleCache() {
        System.out.println(this.getTestName() + ": ");
        
        DataProducer producer = new DataProducer(logger);
        SimpleCache<Integer> cache = new SimpleCache<Integer>(()->producer.produce());
        logger.log("GET");
        cache.get();
        logger.log("GET");
        cache.get();
        logger.log("GET");
        cache.invalidate();
        logger.log("INVALIDATE");
        logger.log("GET");
        cache.get();
        logger.log("GET");
        assertEquals("Via cache: ", "GET - PRODUCE - GET - GET - INVALIDATE - GET - PRODUCE - GET", logger.toString());
        System.out.println("The production happens twice!");
        System.out.println("The second one happens after the invalidation.");
        System.out.println();
    }
    
    @Test
    public void expirableCache() {
        System.out.println(this.getTestName() + ": ");
        
        DataProducer producer = new DataProducer(logger);
        TimeProvider timeProvider = new TimeProvider();
        int liveTime = 5;
        ExpirableCache<Integer> cache = new ExpirableCache<>(timeProvider, liveTime, ()->producer.produce());
        logger.log("GET");
        cache.get();
        logger.log("GET");
        cache.get();
        logger.log("GET");
        timeProvider.currentTime = 3;
        logger.log("CURRENT-TIME=3");
        logger.log("GET");
        cache.get();
        logger.log("GET");
        timeProvider.currentTime = 5;
        logger.log("CURRENT-TIME=5");
        cache.get();
        logger.log("GET");
        timeProvider.currentTime = 6;
        logger.log("CURRENT-TIME=6");
        logger.log("GET");
        cache.get();
        logger.log("GET");
        assertEquals("Via cache: ", "GET - PRODUCE - GET - GET - CURRENT-TIME=3 - GET - GET - CURRENT-TIME=5 - PRODUCE - GET - CURRENT-TIME=6 - GET - GET", logger.toString());
        System.out.println("The production happens once before the time is 5!");
        System.out.println("When the time is 5 or later, there is another production.");
        System.out.println();
    }
    
    private String getTestName() {
        try {
            throw new NullPointerException();
        } catch(Exception exception) {
            return exception.getStackTrace()[1].getMethodName().toString();
        }
    }
    
    private static void assertEquals(String messagePrefix, String expected, String actual) {
        Assert.assertEquals(messagePrefix, expected, actual);
        System.out.println(messagePrefix + actual);
    }
    
}
