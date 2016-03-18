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
package ca.draconic.stipple.stipple;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

/**
 * Function returning the distance between two points
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
@FunctionalInterface
public interface DistanceMetric<Point> extends ToDoubleBiFunction<Point, Point> {

    /**
     * @return distance between the two points
     */
    @Override
    public double applyAsDouble(Point c1, Point c2);
    
    /**
     * Are the points within the specified distance
     * @param distance
     * @param c1
     * @param c2
     * @return
     */
    default boolean dwithin(double distance, Point c1, Point c2) {
        return applyAsDouble(c1, c2)<=distance;
    }
    
    /**
     * @param distance
     * @return Predicate checking whether two points are within the specified distance
     */
    default BiPredicate<Point, Point> dwithin(double distance) {
        return (c1,c2)-> {
            return dwithin(distance, c1, c2);
        };
    }
    
    /**
     * @param distance
     * @return Predicate checking whether a point is within the specified distance of c1
     * @param c1
     */
    default Predicate<Point> dwithin(double distance, Point c1) {
        return c2-> {
            return dwithin(distance, c1, c2);
        };
    }
}