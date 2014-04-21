package ca.draconic.stipple.stipple;

import com.vividsolutions.jts.geom.Coordinate;

public class EuclideanDistance implements DistanceMetric {

    @Override
    public double applyAsDouble(Coordinate c1, Coordinate c2) {
        return c1.distance(c2);
    }
}
