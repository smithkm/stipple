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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

public enum QuadEdgeUtils {
    ;
    /**
     * Returns the orbit of start under step.  If the orbit is open this will be infinite.
     * @param start
     * @param step
     * @return
     */
    public static <T> Iterable<T> orbit(T start, UnaryOperator<T> step){
        return new Iterable<T>(){

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>(){
                    boolean first=true;
                    boolean done =false;
                    T current = start;
                    T peek = null;
                    
                    @Override
                    public boolean hasNext() {
                        if(peek==null) {
                            peek=step.apply(current);
                        }
                        return !peek.equals(start);
                    }

                    @Override
                    public T next() {
                        if(first) {
                            first=false;
                            return current;
                        } else if(peek==null){
                            current=step.apply(current);
                        } else {
                            current=peek;
                            peek=null;
                        }
                        if(done || start.equals(current)) {
                            done=true;
                            throw new NoSuchElementException();
                        }
                        return current;
                    }};
                
            }
            
        };
    }
}
