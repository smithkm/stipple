package ca.draconic.stipple.stipple;

import java.util.function.ToDoubleBiFunction;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Metric for measuring the distance between two points
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
@FunctionalInterface
public interface DistanceMetric extends ToDoubleBiFunction<Coordinate, Coordinate> {

    default boolean dwithin(double distance, Coordinate c1, Coordinate c2) {
        return applyAsDouble(c1, c2)<=distance;
    }
}