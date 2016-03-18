/*
 * (c) 2014 - 2016 Kevin Smith
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Kevin Smith
 */
package ca.draconic.stipple.river;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;

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
