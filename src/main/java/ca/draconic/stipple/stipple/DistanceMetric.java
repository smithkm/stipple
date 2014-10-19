package ca.draconic.stipple.stipple;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;

/**
 * Metric for measuring the distance between two points
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
@FunctionalInterface
public interface DistanceMetric<Point> extends ToDoubleBiFunction<Point, Point> {

    default boolean dwithin(double distance, Point c1, Point c2) {
        return applyAsDouble(c1, c2)<=distance;
    }
    
    default BiPredicate<Point, Point> dwithin(double distance) {
        return (c1,c2)-> {
            return dwithin(distance, c1, c2);
        };
    }
    default Predicate<Point> dwithin(double distance, Point c1) {
        return c2-> {
            return dwithin(distance, c1, c2);
        };
    }
}