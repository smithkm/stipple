package ca.draconic.stipple.river;

import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import ca.draconic.stipple.river.Densifier.SubdividingSegmentInterpolator;

public class FractalSegmentInterpolator extends SubdividingSegmentInterpolator {
    Random rand;
    
    double scale;
    
    public FractalSegmentInterpolator(Random rand, double scale) {
        super();
        this.rand = rand;
        this.scale = scale;
    }
    
    @Override
    public Coordinate midpoint(LineSegment seg) {
        double length = seg.getLength();
        double offset = (rand.nextDouble()*2-1)*scale*length;
        Coordinate c = seg.pointAlongOffset(0.5, offset);
        return c;
    }

}
