package ca.draconic.stipple.river;

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

import java.util.Iterator;
import java.util.Stack;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.GeometryTransformer;

/**
 * Densifies a {@link Geometry} by inserting extra vertices along the line segments
 * contained in the geometry. 
 * All segments in the created densified geometry will be no longer than
 * than the given distance tolerance.
 * Densified polygonal geometries are guaranteed to be topologically correct.
 * The coordinates created during densification respect the input geometry's
 * {@link PrecisionModel}.
 * <p>
 * <b>Note:</b> At some future point this class will
 * offer a variety of densification strategies.
 * 
 * @author Martin Davis
 */
public class Densifier {
    /**
     * Densifies a geometry using a given distance tolerance,
   * and respecting the input geometry's {@link PrecisionModel}.
     * 
     * @param geom the geometry to densify
     * @param distanceTolerance the distance tolerance to densify
     * @return the densified geometry
     */
    public static Geometry densify(Geometry geom, double distanceTolerance) {
        Densifier densifier = new Densifier(geom);
        densifier.setDistanceTolerance(distanceTolerance);
        return densifier.getResultGeometry();
    }

    /**
     * Densifies a coordinate sequence.
     * 
     * @param pts
     * @param distanceTolerance
     * @return the densified coordinate sequence
     */
    private static Coordinate[] densifyPoints(Coordinate[] pts,
            double distanceTolerance, PrecisionModel precModel, SegmentInterpolator densifier) {
        LineSegment seg = new LineSegment();
        CoordinateList coordList = new CoordinateList();
        for (int i = 0; i < pts.length - 1; i++) {
            seg.p0 = pts[i];
            seg.p1 = pts[i + 1];
            coordList.add(seg.p0, false);
            Iterator<Coordinate> it = densifier.densifySegment(seg, pts, i, distanceTolerance);
            while(it.hasNext()) {
                Coordinate p = it.next();
                precModel.makePrecise(p);
                coordList.add(p, false);
            }
        }
        coordList.add(pts[pts.length - 1], false);
        return coordList.toCoordinateArray();
    }
    
    /**
     * The scheme used to generate intermediate points
     */
    public interface SegmentInterpolator {
        /**
         * 
         * @param seg The current segment being interpolated
         * @param pts The full sequence of original points
         * @param i The index of the starting element of the current segment in the full sequence
         * @param distanceTolerance The maximum length allowable between consecutive result points
         * @return iterator over the intermediate points to divide the given segment
         */
        Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i, double distanceTolerance);
    }
    
    /**
     * Interpolator which generates points using a function of segFract, where segFract is evenly
     * stepped along the length of the segment.
     * 
     * @author Kevin Smith
     *
     */
    static abstract public class SteppingSegmentInterpolator implements SegmentInterpolator {
        @Override
        public Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i, double distanceTolerance) {
            return new Iterator<Coordinate>() {
                int j=1;
                double len = seg.getLength();
                int densifiedSegCount=(int) (len / distanceTolerance) + 1;
                double densifiedSegLen = len / densifiedSegCount;
                
                @Override
                public boolean hasNext() {
                    return j<densifiedSegCount;
                }

                @Override
                public Coordinate next() {
                    double segFract = (j * densifiedSegLen) / len;
                    j++;
                    return pointAlong(seg, segFract);
                }
                
            };
            
        }
        public abstract Coordinate pointAlong(LineSegment seg, double segFract);
    }
    
    /**
     * Interpolator which generates points by dividing the segment in two and then repeating 
     * recursively until all the segments are under the threshold.
     * 
     * @author Kevin Smith
     *
     */
    static abstract public class SubdividingSegmentInterpolator implements SegmentInterpolator {
        @Override
        public Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i, double distanceTolerance) {
            final Stack<LineSegment> stack = new Stack<LineSegment>();
            stack.push(seg);
            
            return new Iterator<Coordinate>() {
                
                @Override
                public boolean hasNext() {
                    return stack.size()>1 || stack.peek().getLength()>distanceTolerance;
                }

                @Override
                public Coordinate next() {
                    
                    LineSegment currentSeg = stack.pop();
                    
                    while(currentSeg.getLength()>distanceTolerance){
                        Coordinate midpoint = midpoint(currentSeg);
                        stack.push(new LineSegment(midpoint, currentSeg.p1));
                        currentSeg.p1=midpoint;
                    }
                    
                    return currentSeg.p1;
                }
                
            };
        }
        public abstract Coordinate midpoint(LineSegment seg);
    }
    
    /**
     * Produces a minimal set of intermediate points evenly spaced along the segment.
     */
    public static class DefaultSegmentInterpolator extends SteppingSegmentInterpolator {

        @Override
        public Coordinate pointAlong(LineSegment seg, double segFract) {
            return seg.pointAlong(segFract);
        }

        
    }

    private Geometry inputGeom;

    private double distanceTolerance;
    
    private SegmentInterpolator interpolator = new DefaultSegmentInterpolator();

    /**
     * Creates a new densifier instance.
     * 
     * @param inputGeom
     */
    public Densifier(Geometry inputGeom) {
        this.inputGeom = inputGeom;
    }

    /**
     * Sets the distance tolerance for the densification. All line segments
     * in the densified geometry will be no longer than the distance tolereance.
     * simplified geometry will be within this distance of the original geometry.
     * The distance tolerance must be positive.
     * 
     * @param distanceTolerance
     *          the densification tolerance to use
     */
    public void setDistanceTolerance(double distanceTolerance) {
        if (distanceTolerance <= 0.0)
            throw new IllegalArgumentException("Tolerance must be positive");
        this.distanceTolerance = distanceTolerance;
    }
    
    /**
     * Sets the interpolation scheme to use to generate new points.
     * 
     * @param distanceTolerance
     *          the interpolation scheme to use
     */
    public void setInterpolator(SegmentInterpolator interpolator) {
        if (interpolator==null)
            throw new NullPointerException();
        this.interpolator = interpolator;
    }

    /**
     * Gets the densified geometry.
     * 
     * @return the densified geometry
     */
    public Geometry getResultGeometry() {
        return (new DensifyTransformer()).transform(inputGeom);
    }

    class DensifyTransformer extends GeometryTransformer {
        protected CoordinateSequence transformCoordinates(
                CoordinateSequence coords, Geometry parent) {
            Coordinate[] inputPts = coords.toCoordinateArray();
            Coordinate[] newPts = Densifier
                    .densifyPoints(inputPts, distanceTolerance, parent.getPrecisionModel(), interpolator);
            // prevent creation of invalid linestrings
            if (parent instanceof LineString && newPts.length == 1) {
                newPts = new Coordinate[0];
            }
            return factory.getCoordinateSequenceFactory().create(newPts);
        }

        protected Geometry transformPolygon(Polygon geom, Geometry parent) {
            Geometry roughGeom = super.transformPolygon(geom, parent);
            // don't try and correct if the parent is going to do this
            if (parent instanceof MultiPolygon) {
                return roughGeom;
            }
            return createValidArea(roughGeom);
        }

        protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
            Geometry roughGeom = super.transformMultiPolygon(geom, parent);
            return createValidArea(roughGeom);
        }

        /**
         * Creates a valid area geometry from one that possibly has bad topology
         * (i.e. self-intersections). Since buffer can handle invalid topology, but
         * always returns valid geometry, constructing a 0-width buffer "corrects"
         * the topology. Note this only works for area geometries, since buffer
         * always returns areas. This also may return empty geometries, if the input
         * has no actual area.
         * 
         * @param roughAreaGeom
         *          an area geometry possibly containing self-intersections
         * @return a valid area geometry
         */
        private Geometry createValidArea(Geometry roughAreaGeom) {
            return roughAreaGeom.buffer(0.0);
        }
    }

}