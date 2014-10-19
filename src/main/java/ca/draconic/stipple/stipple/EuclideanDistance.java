package ca.draconic.stipple.stipple;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Ordinary Euclidean distance for JTS Coordinate objects 
 * 
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
public class EuclideanDistance implements DistanceMetric<Coordinate>, Serializable {

    @Override
    public double applyAsDouble(Coordinate c1, Coordinate c2) {
        return c1.distance(c2);
    }
}
