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
