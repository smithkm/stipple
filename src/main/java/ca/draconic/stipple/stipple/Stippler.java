package ca.draconic.stipple.stipple;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;

public class Stippler<Point> implements Supplier<Point>, Serializable {
    final DistanceMetric<Point> metric;
    
    final List<Point> points;
    
    final Supplier<Point> generator;
    
    double radius;
    
    int triesAtCurrentRadius;
    
    final int maxTriesPerRadius;
    
    final double decay;
    
    public Stippler(DistanceMetric<Point> metric, Supplier<Point> generator,
            double radius, int maxTriesPerRadius, double decay) {
        this(metric, generator, decay, maxTriesPerRadius, decay, new LinkedList<>());
    }
    public Stippler(DistanceMetric<Point> metric, Supplier<Point> generator,
            double radius, int maxTriesPerRadius, double decay, int maxPoints) {
        this(metric, generator, decay, maxTriesPerRadius, decay, new ArrayList<>(maxPoints));
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
    
    public List<Point> getAll() {
        return Collections.unmodifiableList(points);
    }
    
    public MultiPoint getGeometry(BiFunction<Point, Integer, com.vividsolutions.jts.geom.Point> convert) {
        com.vividsolutions.jts.geom.Point[] converted = new com.vividsolutions.jts.geom.Point[points.size()];
        for(int i = 0; i<converted.length; i++) {
            converted[i] = convert.apply(points.get(i), i);
        }
        return converted[0].getFactory().createMultiPoint(converted);
    }
    
    public MultiPoint getGeometry(Function<Point, com.vividsolutions.jts.geom.Point> convert) {
        return getGeometry((p,i)->{
                com.vividsolutions.jts.geom.Point c = convert.apply(p);
                c.setUserData(i);
                return c;
            });
    }
}
