package ca.draconic.stipple.river;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

public class WeightedRandomCollection<T> {
    private class Entry {
        T item;
        double lower;
        double upper;
    }
    
    ArrayList<Entry> entries;
    double cumulative=0;
    
    public WeightedRandomCollection() {
        entries = new ArrayList<Entry>();
    }
    public WeightedRandomCollection(int capacity) {
        entries = new ArrayList<Entry>(capacity);
    }
    
    public WeightedRandomCollection(Collection<? extends T> items, ToDoubleFunction<? super T> weight) {
        this(items.size());
        items.forEach(item->{
            add(item, weight.applyAsDouble(item));
        });
    }
    
    public void add(T item, double weight){
        Entry entry = new Entry();
        entry.item=item;
        entry.lower=cumulative;
        cumulative+=weight;
        entry.upper+=cumulative;
        entries.add(entry);
    }
    
    public Optional<T> get(Random rand) {
        double index = rand.nextDouble()*cumulative;
        // TODO make this more efficient, maybe use binary search.
        return entries.stream()
            .filter(entry->{return index>=entry.lower && index<entry.upper;})
            .findAny()
            .map(entry->{
                return entry.item;
            });
    }
    
    public Optional<T> get() {
        return get(ThreadLocalRandom.current());
    }
}
