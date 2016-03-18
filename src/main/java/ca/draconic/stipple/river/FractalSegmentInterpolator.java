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
