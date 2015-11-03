package nawaman.papercuts.concurrency.atomicvariable_examples.cache;

import java.util.function.Supplier;

import nawaman.papercuts.concurrency.atomicvariable.AtomicVariable;

public class SimpleCache<V> {
    
    private final AtomicVariable<V> variable = new AtomicVariable<>(null);
    
    private final Supplier<V> supplier;
    
    public SimpleCache(Supplier<V> supplier) {
        this.supplier = supplier;
    }
    
    public V get() {
        return this.variable.updateAfterCompare(null, current->{
            return this.supplier.get();
        });
    }
    
    public void invalidate() {
        this.variable.set(null);
    }
    
}