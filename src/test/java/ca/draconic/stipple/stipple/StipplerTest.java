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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

public class StipplerTest {

    @Test
    public void testOnePoint() throws Exception {
        @SuppressWarnings("unchecked")
        final Supplier<Coordinate> generator = createMock(Supplier.class);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -1);
        final double initRadius = 100;
        
        final int points = 10;
        final int tries = 5;

        final Coordinate expectedPoint1 = new Coordinate(10,10);
        
        EasyMock.expect(generator.get()).andReturn(expectedPoint1).once();
        
        replay(generator);
        
        Stippler<Coordinate> stippler = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
        
        Coordinate resultPoint1 = stippler.get();
        assertThat(resultPoint1, is(expectedPoint1));
        
        List<Coordinate> pointList = stippler.getAll();
        
        assertThat(pointList, contains(expectedPoint1));
        
        verify(generator);
    }
    
    @Test
    public void testTwoPoints() throws Exception {
        @SuppressWarnings("unchecked")
        final Supplier<Coordinate> generator = createMock(Supplier.class);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -1);
        final double initRadius = 100;
        
        final int points = 10;
        final int tries = 5;

        final Coordinate expectedPoint1 = new Coordinate(10,10);
        final Coordinate expectedPoint2 = new Coordinate(111,10);
        
        EasyMock.expect(generator.get()).andReturn(expectedPoint1).once();
        EasyMock.expect(generator.get()).andReturn(expectedPoint2).once();
        
        replay(generator);
        
        Stippler<Coordinate> stippler = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
        
        Coordinate resultPoint1 = stippler.get();
        assertThat(resultPoint1, is(expectedPoint1));
        
        Coordinate resultPoint2 = stippler.get();
        assertThat(resultPoint2, is(expectedPoint2));
        
        List<Coordinate> pointList = stippler.getAll();
        
        assertThat(pointList, contains(expectedPoint1, expectedPoint2));
        
        verify(generator);
    }
    
    @Test
    public void testTwoPointsRejectOne() throws Exception {
        @SuppressWarnings("unchecked")
        final Supplier<Coordinate> generator = createMock(Supplier.class);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -1);
        final double initRadius = 100;
        
        final int points = 10;
        final int tries = 5;

        final Coordinate expectedPoint1 = new Coordinate(10,10);
        final Coordinate rejectedPoint1 = new Coordinate(109,10);
        final Coordinate expectedPoint2 = new Coordinate(111,10);
        
        EasyMock.expect(generator.get()).andReturn(expectedPoint1).once();
        EasyMock.expect(generator.get()).andReturn(rejectedPoint1).once();
        EasyMock.expect(generator.get()).andReturn(expectedPoint2).once();
        
        replay(generator);
        
        Stippler<Coordinate> stippler = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
        
        Coordinate resultPoint1 = stippler.get();
        assertThat(resultPoint1, is(expectedPoint1));
        
        Coordinate resultPoint2 = stippler.get();
        assertThat(resultPoint2, is(expectedPoint2));
        
        List<Coordinate> pointList = stippler.getAll();
        
        assertThat(pointList, contains(expectedPoint1, expectedPoint2));
        
        verify(generator);
    }
    
    @Test
    public void testTwoPointsRejectUntilDecay() throws Exception {
        @SuppressWarnings("unchecked")
        final Supplier<Coordinate> generator = createMock(Supplier.class);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -1);
        final double initRadius = 100;
        
        final int points = 10;
        final int tries = 5;

        final Coordinate expectedPoint1 = new Coordinate(10,10);
        final Coordinate expectedPoint2 = new Coordinate(109,10);
        final Coordinate rejectedPoint1 = new Coordinate(10,59);
        final Coordinate expectedPoint3 = new Coordinate(10,61);
        
        expect(generator.get()).andReturn(expectedPoint1).once();
        expect(generator.get()).andReturn(expectedPoint2).times(tries+1); // Just inside the old radius
        expect(generator.get()).andReturn(rejectedPoint1).once(); // Just inside the new radius
        expect(generator.get()).andReturn(expectedPoint3).once(); // Just outside the new radius
        
        replay(generator);
        
        Stippler<Coordinate> stippler = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
        
        Coordinate resultPoint1 = stippler.get();
        assertThat(resultPoint1, is(expectedPoint1));
        
        Coordinate resultPoint2 = stippler.get();
        assertThat(resultPoint2, is(expectedPoint2));
        
        Coordinate resultPoint3 = stippler.get();
        assertThat(resultPoint3, is(expectedPoint3));
        
        List<Coordinate> pointList = stippler.getAll();
        
        assertThat(pointList, contains(expectedPoint1, expectedPoint2, expectedPoint3));
        
        verify(generator);
    }

}
