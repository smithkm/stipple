package ca.draconic.stipple.river;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SelectRandom<T> implements Collector<T, List<T>, T> {
    int capacity;
    
    @Override
    public Set<java.util.stream.Collector.Characteristics> characteristics() {
        return Collections.emptySet();
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ()->{return new ArrayList<>(capacity);};
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return (list, e)->{list.add(e);};
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Function<List<T>, T> finisher() {
        // TODO Auto-generated method stub
        return null;
    }

}
