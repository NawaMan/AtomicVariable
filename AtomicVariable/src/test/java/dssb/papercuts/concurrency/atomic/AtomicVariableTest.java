package dssb.papercuts.concurrency.atomic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Test for Variable.
 * 
 * @author NawaMan
 */
public class AtomicVariableTest extends AtomicVariableTestPreparater {
    
    /** Variable value can be given when it is created. */
    @Test
    public void createRefernceWithValueAndVerifyTheValue() {
        defValue = new Object();
        
        variable = createVariable(defValue);
        
        assertEquals(defValue, variable.get());
    }
    
    /** Variable value can be set at later time. */
    @Test
    public void variableAllowsValueToBeSet() {
        value = new Object();
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        variable.set(value);
        assertEquals(value, variable.get());
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableCompareBeforeSet() {
        defValue = new Object();
        value = new Object();
        anotherValue = new Object();
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertTrue(variable.compareBeforeSet(defValue, value));
        assertEquals(value, variable.get());
        
        // Unexpected current value
        assertFalse(variable.compareBeforeSet(defValue, anotherValue));
        assertEquals(value, variable.get());
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableCompareBeforeUpdate() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        
        variable = createVariable(defValue);
        assertEquals(startValue, variable.get());
        
        // Expected current value
        assertTrue(variable.compareBeforeUpdate(defValue, current->((Integer)current)+1));
        assertEquals(startValue + 1, variable.get());
        
        // Unexpected current value
        assertFalse(variable.compareBeforeUpdate(defValue, current->((Integer)current)+1));
        assertEquals(startValue + 1, variable.get());
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableCheckBeforeSet() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        value = Integer.valueOf(-1);
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertTrue(variable.checkBeforeSet(current-> isOdd(current), value));
        assertEquals(value, variable.get());
        
        // Unexpected current value
        assertFalse(variable.checkBeforeSet(current-> isEven(current), value));
        assertEquals(value, variable.get());
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableCheckBeforeUpdate() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertTrue(variable.checkBeforeUpdate(current-> isOdd(current), current->((Integer)current)+1));
        assertEquals(124, variable.get());
        
        // Unexpected current value
        assertTrue(variable.checkBeforeUpdate(current-> isEven(current), current->((Integer)current)+1));
        assertEquals(125, variable.get());
        
        // Unexpected current value
        assertFalse(variable.checkBeforeUpdate(current-> isEven(current), current->((Integer)current)+1));
        assertEquals(125, variable.get());
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableSetAfterCompare() {
        defValue = new Object();
        value = new Object();
        anotherValue = new Object();
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertEquals(value, variable.setAfterCompare(defValue, value));
        
        // Unexpected current value
        assertEquals(value, variable.setAfterCompare(defValue, anotherValue));
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableUpdateAfterCompare() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        
        variable = createVariable(defValue);
        assertEquals(startValue, variable.get());
        
        // Expected current value
        assertEquals(startValue + 1, variable.updateAfterCompare(defValue, current->((Integer)current)+1));
        
        // Unexpected current value
        assertEquals(startValue + 1, variable.updateAfterCompare(defValue, current->((Integer)current)+1));
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableSetAfterCheck() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        value = Integer.valueOf(-1);
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertEquals(value, variable.setAfterCheck(current-> isOdd(current), value));
        
        // Unexpected current value
        assertEquals(value, variable.setAfterCheck(current-> isEven(current), value));
    }
    
    /** Variable value setting can be done only when the current value is as expected. */
    @Test
    public void variableUpdateAfterCheck() {
        int startValue = 123;
        defValue = Integer.valueOf(startValue);
        
        variable = createVariable();
        assertEquals(defValue, variable.get());
        
        // Expected current value
        assertEquals(startValue + 1, variable.updateAfterCheck(current-> isOdd(current), current->((Integer)current)+1));
        
        // Unexpected current value
        assertEquals(startValue + 2, variable.updateAfterCheck(current-> isEven(current), current->((Integer)current)+1));
        
        // Unexpected current value
        assertEquals(startValue + 2, variable.updateAfterCheck(current-> isEven(current), current->((Integer)current)+1));
    }
    
    /** Variable value can only be changed to another value only when the current value is expected. */
    @Test
    public void variableCompareBeforeSetIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    int next = current + 1;
                    boolean isChanged = variable.compareBeforeSet(current, next);
                    log(current, new ChangeLog(current, next, isChanged));
                });
        runAllThreadsAndWait(threads);
        
        changeLogs.values().stream().forEach(list -> {
            // Assert that all threads are run for each current value.
            assertEquals(threadCount, list.size());
            // Assert that only one of them is successful in changing the value.
            ChangeLog changeLog = list.get(0);
            String msg = changeLog.current + " -> " + changeLog.next;
            assertEquals(msg, 1, list.stream().filter(log->log.isChanged).count());
        });
    }
    
    @Test
    public void variableCompareBeforeUpdateIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    AtomicInteger next = new AtomicInteger();
                    boolean isChanged = variable.compareBeforeUpdate(
                            current,
                            curValue->{
                                int nextValue = (int)curValue + 1;
                                next.set(nextValue);
                                return nextValue;
                            });
                    log(current, new ChangeLog(current, next.get(), isChanged));
                });
        runAllThreadsAndWait(threads);
        
        changeLogs.values().stream().forEach(list -> {
            // Assert that all threads are run for each current value.
            assertEquals(threadCount, list.size());
            // Assert that only one of them is successful in changing the value.
            ChangeLog changeLog = list.get(0);
            String msg = changeLog.current + " -> " + changeLog.next;
            assertEquals(msg, 1, list.stream().filter(log->log.isChanged).count());
        });
    }
    
    /** Variable value can only be changed to another value only when the current value is expected. */
    @Test
    public void variableCheckBeforeSetIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    int next = current + 1;
                    boolean isChanged = variable.checkBeforeSet(
                            curValue->curValue.equals(current),
                            next);
                    log(current, new ChangeLog(current, next, isChanged));
                });
        runAllThreadsAndWait(threads);
        
        changeLogs.values().stream().forEach(list -> {
            // Assert that all threads are run for each current value.
            assertEquals(threadCount, list.size());
            // Assert that only one of them is successful in changing the value.
            ChangeLog changeLog = list.get(0);
            String msg = changeLog.current + " -> " + changeLog.next;
            assertEquals(msg, 1, list.stream().filter(log->log.isChanged).count());
        });
    }
    
    @Test
    public void variableCheckBeforeUpdateIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    AtomicInteger next = new AtomicInteger();
                    boolean isChanged = variable.checkBeforeUpdate(
                            curValue->curValue.equals(current),
                            curValue->{
                                int nextValue = (int)curValue + 1;
                                next.set(nextValue);
                                return nextValue;
                            });
                    log(current, new ChangeLog(current, next.get(), isChanged));
                });
        runAllThreadsAndWait(threads);
        
        changeLogs.values().stream().forEach(list -> {
            // Assert that all threads are run for each current value.
            assertEquals(threadCount, list.size());
            // Assert that only one of them is successful in changing the value.
            ChangeLog changeLog = list.get(0);
            String msg = changeLog.current + " -> " + changeLog.next;
            assertEquals(msg, 1, list.stream().filter(log->log.isChanged).count());
        });
    }
    
    /** Variable value can only be changed to another value only when the current value is expected. */
    @Test
    public void variableSetAfterCompareIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    int next = current + 1;
                    variable.setAfterCompare(current, next);
                });
        runAllThreadsAndWait(threads);
        
        assertEquals(countSize, variable.get());
    }
    
    @Test
    public void variableUpdateAfterCompareIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    AtomicInteger next = new AtomicInteger();
                    variable.updateAfterCompare(
                            current,
                            curValue->{
                                int nextValue = (int)curValue + 1;
                                next.set(nextValue);
                                return nextValue;
                            });
                });
        runAllThreadsAndWait(threads);
        
        assertEquals(countSize, variable.get());
    }
    
    /** Variable value can only be changed to another value only when the current value is expected. */
    @Test
    public void variableSetAfterCheckIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    int next = current + 1;
                    variable.setAfterCheck(
                            curValue->curValue.equals(current),
                            next);
                });
        runAllThreadsAndWait(threads);
        
        assertEquals(countSize, variable.get());
    }
    
    @Test
    public void variableUpdateAfterCheckIsAtomic() {
        defValue    =     0;    // The initial value.
        countSize   =    10;    // The starting value to change from.
        threadCount =  1000;    // Number of threads to attempt to change the same value.
        prepareChangeLogs(countSize);
        
        variable = createVariable((Integer)defValue);
        assertEquals(defValue, variable.get());
        
        // Creates threads to count from current value to next (by 1).
        // Creates #threadCount# threads for each count value.
        List<Thread> threads = prepareDelayedThreads(
                current->{
                    AtomicInteger next = new AtomicInteger();
                    variable.updateAfterCheck(
                            curValue->curValue.equals(current),
                            curValue->{
                                int nextValue = (int)curValue + 1;
                                next.set(nextValue);
                                return nextValue;
                            });
                });
        runAllThreadsAndWait(threads);
        
        assertEquals(countSize, variable.get());
    }
    
}
