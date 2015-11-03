package nawaman.papercuts.concurrency.atomicvariable_examples.cache;

import java.util.function.Supplier;

import nawaman.papercuts.concurrency.atomicvariable.AtomicVariable;

public class ExpirableCache<V> {
    
    private final AtomicVariable<Value<V>> variable = new AtomicVariable<>(null);
    
    private final Supplier<V> supplier;
    
    private final TimeProvider timeProvider;
    
    private final long liveTime;
    
    public ExpirableCache(TimeProvider timeProvider, long liveTime, Supplier<V> supplier) {
        this.supplier = supplier;
        this.timeProvider = timeProvider;
        this.liveTime = liveTime;
    }
    
    public V get() {
        return this.variable.updateAfterCheck(current->{
            return (current == null) || current.isExpired(this.timeProvider.currentTime());
        }, current->{
            long time = this.timeProvider.currentTime(); 
            long bestBefore = time + this.liveTime;
            return new Value<V>(bestBefore, this.supplier.get());
        }).getValue();
    }
    
    public void invalidate() {
        this.variable.set(null);
    }
    
    // == AUX class ====================================================================================================
    
    static class TimeProvider {
        
        public long currentTime = 0;
        
        public long currentTime() {
            return this.currentTime;
        }
        
    }
    
    static class Value<V> {
        
        private final long bestBefore;
        
        private final V value;
        
        public Value(long bestBefore, V value) {
            this.value = value;
            this.bestBefore = bestBefore;
        }
        
        public boolean isExpired(long time) {
            return this.bestBefore <= time;
        }
        
        public V getValue() {
            return this.value;
        }
        
    }
    
}
