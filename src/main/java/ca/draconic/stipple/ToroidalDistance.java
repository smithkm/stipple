package ca.draconic.stipple;

import com.vividsolutions.jts.geom.Coordinate;

public class ToroidalDistance {
    final double width;
    final double height;
    
    private static double mod (double x, double y) {
        double result = x % y;
        if (result < 0)
        {
            result += y;
        }
        return result;
    }
    
    public ToroidalDistance(double width, double height) {
        super();
        this.width = width;
        this.height = height;
    }

    public Coordinate clamp (Coordinate c) {
        return new Coordinate (mod(c.x, width), mod(c.y, height));
    }
    
    public double distance(Coordinate c1, Coordinate c2) {
        c1 = clamp(c1);
        c2 = clamp(c2);
        
        double minDist = Double.POSITIVE_INFINITY;
        for(int x_offset=0; x_offset<2; x_offset++) {
            for(int y_offset=0; y_offset<2; y_offset++) {
                Coordinate c_offset = new Coordinate (c1.x+width*x_offset, c1.y+width*y_offset);
                double dist = c2.distance(c_offset);
                if(dist<minDist) minDist=dist;
            }
        }
        return minDist;
    }
}
