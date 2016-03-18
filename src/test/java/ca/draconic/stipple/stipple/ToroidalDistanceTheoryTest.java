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
/**
 * 
 */
package ca.draconic.stipple.stipple;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import ca.draconic.stipple.stipple.ToroidalDistance;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Kevin Smith <smithkm@draconic.ca>
 *
 */
@RunWith(Theories.class)
public class ToroidalDistanceTheoryTest {

    DistanceMetric<Coordinate> distance;
    double width;
    double height;
    double diagonal;
    
    @Before
    public void setUp() {
        width=3;
        height=4;
        diagonal = Math.hypot(width, height);
        distance = new ToroidalDistance(width, height);
    }
    
    @DataPoints public static Coordinate[] points() {
        return new Coordinate[] {
            new Coordinate(0.0,0.0),
            new Coordinate(3.0,4.0),
            new Coordinate(3.0,0.0),
            new Coordinate(0.0,4.0),
            new Coordinate(1.0,1.0),
            new Coordinate(-1.5,2.0),
            new Coordinate(1.5,2.0),
            new Coordinate(4.5,2.0),
            new Coordinate(-1.5,-2.0),
            new Coordinate(1.5,-2.0),
            new Coordinate(4.5,-2.0),
            new Coordinate(-1.5,6.0),
            new Coordinate(1.5,6.0),
            new Coordinate(4.5,6.0),
            new Coordinate(2.9,3.9),
            new Coordinate(0.1,0.1),
            new Coordinate(1.5,2.0) //duplicate
        };
    }
    
    @Theory
    public void theorySamePointZero(Coordinate c1, Coordinate c2) {
        assumeThat(c1, equalTo(c2));
        
        assertThat(distance.applyAsDouble(c1, c2), equalTo(0.0d));
    }
    
    @Theory
    public void theoryDistanceLessThanHalfDiagonal(Coordinate c1, Coordinate c2) {
        assertThat(distance.applyAsDouble(c1, c2), lessThanOrEqualTo(diagonal/2));
    }
    
    @Theory
    public void theoryEuclideanClosePointsSameAsEuclidean(Coordinate c1, Coordinate c2) {
        assumeThat(c1.distance(c2), lessThan(diagonal/4));
        assertThat(distance.applyAsDouble(c1, c2), closeTo(c1.distance(c2), 0.0000001));
    }
    
    @Theory
    public void theoryEuclideanDistantPointsHaveShorterTheEuclideanDistance(Coordinate c1, Coordinate c2) {
        assumeThat(c1.distance(c2), greaterThan(diagonal*3/4));
        assertThat(distance.applyAsDouble(c1, c2), lessThanOrEqualTo(diagonal/2));
    }

}
