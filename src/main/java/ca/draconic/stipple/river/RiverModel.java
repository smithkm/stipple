package ca.draconic.stipple.river;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.MultiLineString;

public class RiverModel {
    Set<Node> connected;
    Set<Node> unconnected;
    Set<Node> adjacent;
    
    public void setNodes(Collection<? extends Node> nodes) {
        connected = new HashSet<>();
        unconnected = new HashSet<>(nodes);
        adjacent = new HashSet<>();
        assert adjacent.stream().parallel().allMatch(unconnected::contains);
        assert unconnected.stream().parallel().noneMatch(connected::contains);
    }
    
    public void setInit(Node n) {
        connected.add(n);
        unconnected.remove(n);
        adjacent.remove(n);
        n.adjacent.stream().sequential().filter(unconnected::contains).forEach(adjacent::add);
        assert adjacent.stream().parallel().allMatch(unconnected::contains);
        assert unconnected.stream().parallel().noneMatch(connected::contains);
    }
    
    static <T> Optional<T> randomFromSet(Set<T> set, Random rand) {
        if(set.isEmpty()) return Optional.empty();
        Iterator<T> it = set.iterator();
        int i = rand.nextInt(set.size());
        for(int j = 0; true; j++) {
            if(i==j) {
                return Optional.of(it.next());
            } else {
                it.next();
            }
        }
    }
    
    ToDoubleFunction<Node> nodePreference = node->{return 1.0;};
    
    
    public void setNodePreference(ToDoubleFunction<Node> nodePreference) {
        this.nodePreference = nodePreference;
    }

    private void connectRandomNode() {
        
        WeightedRandomCollection<Node> randomizer = new WeightedRandomCollection<Node>(adjacent, nodePreference);
        Optional<Node> upstream = randomizer.get();
        if(!upstream.isPresent()) throw new IllegalStateException();
        
        Optional<Node> downstream = upstream.get().flowToOneOf(connected);
        if(!downstream.isPresent()) return;
        
        connected.add(upstream.get());
        unconnected.remove(upstream.get());
        adjacent.remove(upstream.get());
        upstream.get().adjacent.stream().filter(unconnected::contains).forEach(adjacent::add);
        
        assert adjacent.stream().allMatch(unconnected::contains);
        assert unconnected.stream().noneMatch(connected::contains);
    }
    
    private void connectAll() {
        int i = 0;
        while(!adjacent.isEmpty()) {
            connectRandomNode();
            
            if(i%10==0) {
                System.err.printf("Connected: %d  Adjacent: %d  Unconnected: %d", connected.size(), adjacent.size(), unconnected.size()).println();
            }
            i++;
        }
        assert adjacent.stream().parallel().allMatch(unconnected::contains);
        assert unconnected.stream().parallel().noneMatch(connected::contains);
    }
    
    public Set<Node> getModel(){
        connectAll();
        return Collections.unmodifiableSet(connected);
    }
}
