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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * Generate points with a poisson disc distribution.
 * 
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 * @param <Point>
 */
public class Stippler<Point> implements Supplier<Point>, Serializable {
    final DistanceMetric<Point> metric;
    
    final List<Point> points;
    
    final Supplier<Point> generator;
    
    double radius;
    
    int triesAtCurrentRadius;
    
    final int maxTriesPerRadius;
    
    final double decay;
    
    /**
     * 
     * @param metric Distance metric for comparing points
     * @param generator A uniformly distributed random point source
     * @param radius The initial radius
     * @param maxTriesPerRadius Number of consecutive failed attempts before reducing radius
     * @param decay Size of the next radius relative to the current. Must be less than 1 and greater than 0.
     */
    public Stippler(DistanceMetric<Point> metric, Supplier<Point> generator,
            double radius, int maxTriesPerRadius, double decay) {
        this(metric, generator, decay, maxTriesPerRadius, decay, new LinkedList<>());
    }
    
    /**
     * 
     * @param metric Distance metric for comparing points
     * @param generator A uniformly distributed random point source
     * @param radius The initial radius
     * @param maxTriesPerRadius Number of consecutive failed attempts before reducing radius
     * @param decay Size of the next radius relative to the current. Must be less than 1 and greater than 0.
     * @param maxPoints The number of points that are expected.
     */
    public Stippler(DistanceMetric<Point> metric, Supplier<Point> generator,
            double radius, int maxTriesPerRadius, double decay, int maxPoints) {
        this(metric, generator, radius, maxTriesPerRadius, decay, new ArrayList<>(maxPoints));
    }
    
    private Stippler(DistanceMetric<Point> metric, Supplier<Point> generator,
            double radius, int maxTriesPerRadius, double decay, List<Point> points) {
        super();
        Preconditions.checkNotNull(metric);
        Preconditions.checkNotNull(generator);
        Preconditions.checkArgument(decay>0.0, "decay must be greater than 0.0");
        Preconditions.checkArgument(decay<1.0, "decay must be less than 1.0");
        Preconditions.checkArgument(maxTriesPerRadius>0, "maxTriesPerRadius must be greater than 0");
        assert points.isEmpty();
        
        this.metric = metric;
        this.generator = generator;
        this.radius = radius;
        this.maxTriesPerRadius = maxTriesPerRadius;
        this.decay = decay;
        this.points = points;
        this.triesAtCurrentRadius = 0;
    }

    /**
     * Get a new point
     */
    @Override
    public Point get() {
        Point p;
        
        while (true) {
            for( ;triesAtCurrentRadius<maxTriesPerRadius; triesAtCurrentRadius++){
                p = generator.get();
                if (points.parallelStream().noneMatch(metric.dwithin(radius, p))){
                    points.add(p);
                    triesAtCurrentRadius=0;
                    return p;
                }
            }
            triesAtCurrentRadius=0;
            radius*=decay;
        }
    }
    
    /**
     * @return All the previously generated point in the order they were generated
     */
    public List<Point> getAll() {
        return Collections.unmodifiableList(points);
    }
    
    /**
     * Converts the generated points into a JTS MultiPoint
     * @param convert Function converting a Stippler Point into a JTS Point 
     * @return
     */
    public MultiPoint getGeometry(BiFunction<Point, Integer, com.vividsolutions.jts.geom.Point> convert) {
        com.vividsolutions.jts.geom.Point[] converted = new com.vividsolutions.jts.geom.Point[points.size()];
        for(int i = 0; i<converted.length; i++) {
            converted[i] = convert.apply(points.get(i), i);
        }
        return converted[0].getFactory().createMultiPoint(converted);
    }
    
    /**
     * Converts the generated points into a JTS MultiPoint with the rank of each Point having its 
     * rank as its userData field.
     * @param convert Function converting a Stippler Point into a JTS Point 
     * @return
     */
    public MultiPoint getGeometry(Function<Point, com.vividsolutions.jts.geom.Point> convert) {
        return getGeometry((p,i)->{
                com.vividsolutions.jts.geom.Point c = convert.apply(p);
                c.setUserData(i);
                return c;
            });
    }
}
