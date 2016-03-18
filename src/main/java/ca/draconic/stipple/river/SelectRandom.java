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
