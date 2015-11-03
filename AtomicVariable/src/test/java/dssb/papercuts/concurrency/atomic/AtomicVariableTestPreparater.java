package dssb.papercuts.concurrency.atomic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import nawaman.papercuts.concurrency.atomicvariable.AtomicVariable;

import org.junit.Before;

/**
 * This class help AtomicVariableTest to be cleaner by containing details not immediately related to the tests.
 * 
 * @author NawaMan
 */
public abstract class AtomicVariableTestPreparater {
    
    protected Object defValue;
    
    protected Object value;
    
    protected Object anotherValue;
    
    protected volatile AtomicVariable<Object> variable;
    
    protected volatile int countSize;
    protected volatile int threadCount;
    protected volatile Map<Integer, List<ChangeLog>> changeLogs; 
    
    private CountDownLatch[] latches;
    
    @Before
    public void init() {
        defValue = new Object();
        
        countSize   = 0;
        threadCount = 0;
    }
    
    protected AtomicVariable<Object> createVariable(
            final Object defValue) {
        return new AtomicVariable<Object>(defValue);
    }
    
    protected AtomicVariable<Object> createVariable() {
        return new AtomicVariable<Object>(defValue);
    }
    
    protected static void sleep(
            final long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isOdd(Object I) {
        return (((Integer)I).intValue() % 2) == 1;
    }
    
    public static boolean isEven(Object I) {
        return (((Integer)I).intValue() % 2) == 0;
    }
    
    protected static final class ChangeLog {
        public final int     current;
        public final int     next;
        public final boolean isChanged;
        public ChangeLog(int current, int next, boolean isChanged) {
            this.current   = current;
            this.next      = next;
            this.isChanged = isChanged;
        }
        public String toString() {
            return current + " to " + next + " = " + isChanged;
        }
    }
    
    protected void prepareChangeLogs(
            final int countSize) {
        changeLogs = Collections.synchronizedSortedMap(new TreeMap<>());
        IntStream.range(0, countSize).forEach(current -> changeLogs.put(current, new ArrayList<>()));
    }
    
    protected List<Thread> prepareDelayedThreads(
            final Consumer<Integer> threadBody) {
        latches = new CountDownLatch[countSize];
        List<Thread> threads = new ArrayList<>();
        for(int c = 0; c < countSize; c++) {
            latches[c] = new CountDownLatch(threadCount);
            for(int t = 0; t < threadCount; t++) {
                int current = c;
                
                threads.add(new Thread(()->{
                    if (current != 0) {
                        try {
                            latches[current - 1].await();
                        } catch(Exception e) {
                            
                        }
                    }
                    
                    sleep(10);
                    threadBody.accept(current);
                    latches[current].countDown();
                }));
            }
        }
        return threads;
    }
    
    protected void runAllThreadsAndWait(
            final List<Thread> threads) {
        threads.parallelStream().forEach(thread->thread.start());
        
        try {
            latches[countSize - 1].await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //sleep(((int)(countSize + 0.2))*delayInc); // 20% wait time is safety factor.
    }
    
    protected void log(
            final Integer   current,
            final ChangeLog log) {
        synchronized (changeLogs) {
            changeLogs.get(current).add(log);
        }
    }
    
}
