package nawaman.papercuts.concurrency.atomicvariable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is another implementation of AtomicReference with more option of lazy evaluation and compare options.
 * 
 * @author NawaMan
 */
public class AtomicVariable<V> implements Supplier<V> {
    
    private volatile V value;
    
    /**
     * Construct a variable with a default value.
     * 
     * @param defaultValue
     *          the default value.
     */
    public AtomicVariable(
            final V defaultValue) {
        this.value = defaultValue;
    }
    
    /**
     * Obtains the variable value.
     * 
     * @return the variable value.
     **/
    @Override
    public final V get() {
        return this.value;
    }
    
    /**
     * Change the variable value.
     * 
     * NOTE: If other thread override the value just after the change occur but before this method returns,
     *         this method will returns the overridden value.
     * 
     * @param value
     *          the new value.
     * @return the actual new value.
     */
    public final V set(
            final V value) {
        synchronized (this) {
            this.value = value;
        }
        return value;
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value is as expected.
     * 
     * NOTE: The {@code Objects#equals(current,expected)} is consulted to see if the current value is as expected.
     * 
     * @param expected
     *          the expected current value.
     * @param newValue
     *          the new value.
     * @return {@code true} if the change is successful.
     */
    public final boolean compareBeforeSet(
            final V expected,
            final V newValue) {
        return this.checkBeforeUpdate(
                current -> Objects.equals(current, expected),
                current->newValue);
    }
    
    /**
     * Atomically change the variable value to the new value determined from the current value ONLY when the current
     *   value is as expected.
     * 
     * NOTE: The {@code Objects#equals(current,expected)} is consulted to see if the current value is as expected.
     * 
     * @param expected
     *          the expected current value.
     * @param newValueFunction
     *          the function to calculate the new value from the current one.
     * @return {@code true} if the change is successful.
     */
    public final boolean compareBeforeUpdate(
            final V              expected,
            final Function<V, V> newValueFunction) {
        return this.checkBeforeUpdate(
                current -> Objects.equals(current, expected),
                newValueFunction);
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value pass the check.
     * 
     * NOTE: The expectedChecker might to be called more than once for execution.
     * 
     * @param expectedChecker
     *          the predicate to determine if the current value still as expected.
     * @param newValue
     *          the new value.
     * @return {@code true} if the change is successful.
     */
    public final boolean checkBeforeSet(
            final Predicate<V> expectedChecker,
            final V            newValue) {
        return this.checkBeforeUpdate(
                expectedChecker,
                current->newValue);
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value pass the check.
     * 
     * NOTE: The expectedChecker might to be called more than once for execution.
     * 
     * @param expectedChecker
     *          the predicate to determine if the current value still as expected. 
     * @param newValueFunction
     *          the function to calculate the new value from the current one.
     * @return {@code true} if the change is successful.
     */
    public final boolean checkBeforeUpdate(
            final Predicate<V>   expectedChecker,
            final Function<V, V> newValueFunction) {
        if (expectedChecker.test(this.value)) {
            synchronized (this) {
                if (expectedChecker.test(this.value)) {
                    this.value = newValueFunction.apply(this.value);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value is as expected.
     * 
     * NOTE: The {@code Objects#equals(current,expected)} is consulted to see if the current value is as expected.
     * 
     * @param expected
     *          the expected current value.
     * @param newValue
     *          the new value.
     * @return the value that is in the variable at the end of the invocation.
     */
    public final V setAfterCompare(
            final V expected,
            final V newValue) {
        return updateAfterCheck(
                current -> Objects.equals(current, expected),
                current->newValue);
    }
    
    /**
     * Atomically change the variable value to the new value determined from the current value ONLY when the current
     *   value is as expected.
     * 
     * NOTE: The {@code Objects#equals(current,expected)} is consulted to see if the current value is as expected.
     * 
     * @param expected
     *          the expected current value.
     * @param newValueFunction
     *          the function to calculate the new value from the current one.
     * @return the value that is in the variable at the end of the invocation.
     */
    public final V updateAfterCompare(
            final V              expected,
            final Function<V, V> newValueFunction) {
        return updateAfterCheck(
                current -> Objects.equals(current, expected),
                newValueFunction);
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value pass the check.
     * 
     * NOTE: The expectedChecker might to be called more than once for execution.
     * 
     * @param expectedChecker
     *          the predicate to determine if the current value still as expected. 
     * @param newValue
     *          the new value.
     * @return the value that is in the variable at the end of the invocation.
     */
    public final V setAfterCheck(
            final Predicate<V> expectedChecker,
            final V            newValue) {
        return updateAfterCheck(
                expectedChecker,
                current->newValue);
    }
    
    /**
     * Atomically change the variable value to the new value ONLY when the current value pass the check.
     * 
     * NOTE: The expectedChecker might to be called more than once for execution.
     * 
     * @param expectedChecker
     *          the predicate to determine if the current value still as expected. 
     * @param newValueFunction
     *          the function to calculate the new value from the current one.
     * @return the value that is in the variable at the end of the invocation.
     */
    public final V updateAfterCheck(
            final Predicate<V>   expectedChecker,
            final Function<V, V> newValueFunction) {
        if (expectedChecker.test(this.value)) {
            synchronized (this) {
                if (expectedChecker.test(this.value)) {
                    this.value = newValueFunction.apply(this.value);
                }
            }
        }
        return this.value;
    }
    
}
