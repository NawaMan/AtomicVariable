package nawaman.papercuts.concurrency.atomicvariable_examples.cache;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    
    private final List<String> msgs = new ArrayList<>();
    
    public void log(String msg) {
        this.msgs.add(msg);
    }
    
    public String toString() {
        return msgs.stream().collect(joining(" - "));
    }
    
}
