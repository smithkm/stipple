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