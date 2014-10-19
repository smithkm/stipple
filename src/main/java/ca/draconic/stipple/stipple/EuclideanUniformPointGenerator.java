package ca.draconic.stipple.stipple;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Generates random coordinates with a uniform distribution over a finite envelope.
 * 
 * @author Kevin Smith, <smithkm@draconic.ca>
 *
 */
public class EuclideanUniformPointGenerator implements Supplier<Coordinate>, Serializable {
    Envelope envelope;
    
    protected Random getRandom() {
        return ThreadLocalRandom.current();
    }
    
    @Override
    public Coordinate get() {
        Random rand = getRandom();
        double x = rand.nextDouble()*envelope.getWidth()+envelope.getMinX();
        double y = rand.nextDouble()*envelope.getHeight()+envelope.getMinY();
        
        Coordinate c = new Coordinate(x,y);
        assert envelope.contains(c);
        return c;
    }
    
    public EuclideanUniformPointGenerator(Envelope envelope) {
        Preconditions.checkNotNull(envelope);
        this.envelope = envelope;
    }

}
