package ca.draconic.stipple.stipple;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Distance measured with toroidal topology.  The top edge connects to the bottom, and the left to
 * the right.  Otherwise everything is treated as being flat.  Points outside the envelope are
 * treated as being on other 'instances' of the tile.
 * 
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
public class ToroidalDistance implements DistanceMetric<Coordinate> {
    final Envelope env;
    
    private static double mod (final double x, final double min, final double max) {
        final double width = max-min;
        double result = (x-min) % width;
        if (result < 0)
        {
            result += width;
        }
        result += min;
        
        assert result >= min;
        assert result < max;
        
        return result;
    }
    
    /**
     * 
     * @param width
     * @param height
     */
    public ToroidalDistance(double width, double height) {
        this(new Envelope(0, width, 0, height));
    }
    /**
     * 
     */
    public ToroidalDistance(Envelope env) {
        super();
        this.env = env;
    }
    
    /**
     * Returns the canonical position of the given point.
     * @param c
     * @return
     */
    public Coordinate clamp (final Coordinate c) {
        final double x = mod(c.x, env.getMinX(), env.getMaxX());
        final double y = mod(c.y, env.getMinY(), env.getMaxY());
        final Coordinate c2 = new Coordinate (x, y);
        assert env.contains(c2);
        return c2;
    }
    
    @Override
    public double applyAsDouble(Coordinate c1, Coordinate c2) {
        final double width = env.getWidth();
        final double height = env.getHeight();
        
        c1 = clamp(c1);
        c2 = clamp(c2);
        
        double deltaX = Math.abs(c1.x-c2.x);
        double deltaY = Math.abs(c1.y-c2.y);
        
        if(deltaX*2>width) 
            deltaX=width-deltaX;
        if(deltaY*2>height) 
            deltaY=height-deltaY;
        
        return Math.hypot(deltaX,  deltaY);
    }
}
