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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.flowpowered.noise.module.Module;
import com.flowpowered.noise.module.source.Perlin;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdge;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

import ca.draconic.jtstools.JTSTools;
import ca.draconic.stipple.river.Node;
import ca.draconic.stipple.river.RiverModel;
import ca.draconic.stipple.svg.SVGStream;

public class TestStippler {
    
    public static Collection<Point> points(MultiPoint mp) {
        return JTSTools.geometries(mp, Point.class);
    }
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        
        final Envelope env = new Envelope(0,100, 0,100);
        final Supplier<Coordinate> generator = new EuclideanUniformPointGenerator(env);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -10);
        final double initRadius = Math.hypot(env.getWidth(),env.getHeight());
        
        final int points = 100000;
        final int tries = 100;
        
        final File stippleFile = new File("/home/smithkm/stipple.dat");
        final File svgFile = new File("/home/smithkm/foo.svg");
        
        System.err.printf("decay: %.20f \t initRadius: %f \t triesPerRadius: %d", decay, initRadius, tries).println();

        Stippler<Coordinate> s;
        try {
	        try(FileInputStream fin = new FileInputStream(stippleFile);
	                ObjectInputStream oin = new ObjectInputStream(fin) ) {
	            s=(Stippler<Coordinate>) oin.readObject();
	        } catch (FileNotFoundException e) {
	        	s=new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
	        	for(int i=0; i<points; i++) {
	        		s.get();
	        	}
	        	try(FileOutputStream fout = new FileOutputStream(stippleFile);
	                    ObjectOutputStream oout = new ObjectOutputStream(fout) ) {
	        		oout.writeObject(s);
	        	}
	        }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        if(s.getAll().size()<points) {
            System.err.printf("adding %d points", points-s.getAll().size()).println();
            for(int i=0; i<points-s.getAll().size(); i++) {
                s.get();
            }
        }
        //s = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);
        GeometryFactory fact = new GeometryFactory();
        //MultiPoint sites = fact.createMultiPoint(s.getAll().toArray(new Coordinate[]{}));
        MultiPoint sites = s.getGeometry(fact::createPoint);
       
        try(FileOutputStream fout = new FileOutputStream(svgFile)){
            SVGStream.wrap(fout, 100, 100,
                defs->{
                    defs.marker("arrow", svg->{
                        svg.path("M 8.7185878,4.0337352 L -2.2072895,0.016013256 L 8.7185884,-4.0017078 C 6.9730900,-1.6296469 6.9831476,1.6157441 8.7185878,4.0337352 z ", "");
                    });
                },
                svg->{
                    /*
                    svg.group(()->{
                        points(sites).stream().forEach(p->{
                            svg.circle(p.getCoordinate(), 0.25, "fill:#000000");
                        });
                    });
                    */
                    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
                    
                    builder.setSites(sites);
                    builder.setClipEnvelope(env);
                    builder.getDiagram(fact);
                    System.err.println("Done");
                    QuadEdgeSubdivision subdiv = builder.getSubdivision();
                    
                    Collection<QuadEdge> edges = (Collection<QuadEdge>)subdiv.getVertexUniqueEdges(false);
                    
                    Set<Node> allNodes = new HashSet<>();
                    Perlin precipNoise = new Perlin();
                    precipNoise.setFrequency(0.04);
                    precipNoise.setOctaveCount(9);
                    precipNoise.setLacunarity(3.5);
                    edges.forEach(edge->{
                        Node.getNode(edge, c->{
                            int rank = (Integer) points(sites).stream().filter(p->{return p.getCoordinate().equals(c);}).findAny().get().getUserData();
                            double precipitation = precipNoise.getValue(c.x, c.y, 0)*0.75+0.1;
                            return rank<sites.getNumGeometries()*precipitation?1:0;
                            //return 1;
                        });
                    });
                    edges.forEach(edge->{
                        ((Node)edge.getData()).setAdjacency(edge);
                        allNodes.add((Node)edge.getData());
                    });
                    
                    /*
                    svg.group(()->{
                        allNodes.stream().forEach(node->{
                            node.getAdjacent().forEach(node2->{
                                svg.segment(node.getLocation(), node2.getLocation(), "stroke:rgb(0,255,0); stroke-width:0.125; marker-end:url(#arrow);");
                            });
                        });
                    });
                    */
                    Supplier<Coordinate> pointGen = new EuclideanUniformPointGenerator(env);
                    
                    MultiLineString divideBase = fact.createMultiLineString(new LineString[]{
                            fact.createLineString(new Coordinate[]{pointGen.get(), pointGen.get()}),
                            fact.createLineString(new Coordinate[]{pointGen.get(), pointGen.get()}),
                            fact.createLineString(new Coordinate[]{pointGen.get(), pointGen.get()})
                    });
                    
                    /*Densifier densifier = new Densifier(divideBase);
                    densifier.setDistanceTolerance(3);
                    densifier.setInterpolator(new FractalSegmentInterpolator(ThreadLocalRandom.current(), 0.25));
                    MultiLineString divide = (MultiLineString) densifier.getResultGeometry();
                    
                    
                    svg.draw(divide, String.format("stroke:rgb(255,0,0); stroke-width:%f;stroke-linecap:round; fill:none;", 0.25));*/
                    MultiLineString divide = divideBase;
                    
                    Polygon islandBase = fact.createPolygon(new Coordinate[]{
                            new Coordinate(env.getMinX()+env.getWidth()*0.10, env.getMinY()+env.getHeight()*0.30),
                            new Coordinate(env.getMinX()+env.getWidth()*0.30, env.getMinY()+env.getHeight()*0.10),
                            new Coordinate(env.getMinX()+env.getWidth()*(1-0.30), env.getMinY()+env.getHeight()*0.10),
                            new Coordinate(env.getMinX()+env.getWidth()*(1-0.10), env.getMinY()+env.getHeight()*0.30),
                            new Coordinate(env.getMinX()+env.getWidth()*(1-0.10), env.getMinY()+env.getHeight()*(1-0.30)),
                            new Coordinate(env.getMinX()+env.getWidth()*(1-0.30), env.getMinY()+env.getHeight()*(1-0.10)),
                            new Coordinate(env.getMinX()+env.getWidth()*(0.30), env.getMinY()+env.getHeight()*(1-0.10)),
                            new Coordinate(env.getMinX()+env.getWidth()*(0.10), env.getMinY()+env.getHeight()*(1-0.30)),
                            new Coordinate(env.getMinX()+env.getWidth()*0.10, env.getMinY()+env.getHeight()*0.30)
                    });
                    
                    /*densifier = new Densifier(islandBase);
                    densifier.setDistanceTolerance(1);
                    densifier.setInterpolator(new FractalSegmentInterpolator(ThreadLocalRandom.current(), 0.2));
                    Polygon island = (Polygon) densifier.getResultGeometry();*/
                    Polygon island = islandBase;
                    
                    svg.draw((LineString)island.getBoundary(), "stroke:rgb(0,0,255); stroke-width:0.125;stroke-linecap:round; fill: none;");

                    Set<Node> islandNodes=new HashSet<Node>(allNodes);
                    
                    islandNodes.removeIf(node->{
                        return !island.contains(fact.createPoint(node.getLocation()));
                    });
                    
                    Set<Node> oceanNodes=new HashSet<Node>(allNodes);
                    oceanNodes.removeIf(node->{
                        return islandNodes.contains(node);
                    });
                    
                    svg.group(()->{
                        oceanNodes.stream()
                        .filter(node->{
                            return node.getAdjacent().stream().anyMatch(islandNodes::contains);
                            })
                        .forEach(coastNode->{
                            coastNode.getAdjacent().stream()
                            .filter(oceanNodes::contains)
                            .filter(node->{
                                return node.getAdjacent().stream().anyMatch(islandNodes::contains);
                                })
                            .forEach(coastNode2->{
                                svg.segment(coastNode.getLocation(), coastNode2.getLocation(), "stroke:rgb(0,0,0); stroke-width:0.25;stroke-linecap:round; fill: none;");
                            });
                        });
                    });
                    
                    //allNodes.forEach(node->{
                    //    node.cutAdjacency(divide);
                    //});
                    Perlin slopeNoise = new Perlin();
                    slopeNoise.setFrequency(0.03);
                    slopeNoise.setOctaveCount(10);
                    RiverModel model = new RiverModel();
                    model.setNodePreference(node->{
                        //double d = divide.distance(fact.createPoint(node.getLocation()));
                        double d=slopeNoise.getValue(node.getLocation().x, node.getLocation().y, 0);
                        return d*0.30+0.7;
                    });
                    model.setNodes(allNodes);

                    oceanNodes.forEach(node->{
                        model.setInit(node);
                    });
                    
                    Set<Node> rivers = model.getModel();
                    
                    svg.group(()->{
                        rivers.stream().filter(node->{
                            return node.getDownStream().isPresent();
                        }).forEach(node ->{
                            Coordinate orig = node.getLocation();
                            Coordinate dest = node.getDownStream().get().getLocation();
                            svg.segment(orig, dest, String.format("stroke:rgb(0,255,0); stroke-opacity:0.5; stroke-width:%f;stroke-linecap:round;", 0.05));
                        });
                    });
                    svg.group(()->{
                        rivers.stream().filter(node->{
                            return node.getDownStream().isPresent();
                        }).forEach(node ->{
                            Coordinate orig = node.getLocation();
                            Coordinate dest = node.getDownStream().get().getLocation();
                            double width = Math.log(node.getFlow()+1)/20;
                            Densifier segmentDensifier = new Densifier(fact.createLineString(new Coordinate[]{orig, dest}));
                            segmentDensifier.setDistanceTolerance(1);
                            //segmentDensifier.setInterpolator(new FractalSegmentInterpolator(ThreadLocalRandom.current(), 0.25));
                            LineString segment = (LineString) segmentDensifier.getResultGeometry();
                            
                            svg.draw(segment, String.format("stroke:rgb(0,0,0); stroke-width:%f;stroke-linecap:round; fill: none;", width));
                        });
                    });
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        /*
        TestSVG.svgOut("/home/smithkm/foo.svg", (int)env.getMaxX(), (int)env.getMaxY(),
                null, 
                out-> {
                    out.println("<g>");
                    for(int i=0; i<points; i++) {
                        Coordinate p = s.get();
                        if(i%(points/10)==0) System.err.printf("%d",i).println();
                        out.printf("<circle cx='%f' cy='%f' r='0.25' style='fill:#000000' />", p.x, p.y).println();
                    }
                    out.println("</g>");
                    
                    GeometryFactory fact = new GeometryFactory();
                    //MultiPoint sites = fact.createMultiPoint(s.getAll().toArray(new Coordinate[]{}));
                    MultiPoint sites = s.getGeometry(fact::createPoint);
                    
                    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
                    
                    builder.setSites(sites);
                    builder.getDiagram(fact);
                    QuadEdgeSubdivision subdiv = builder.getSubdivision();
                    
                    out.println("<g>");
                    
                    Map<Vertex, Node> mapping = new HashMap<>();
                    
                    
                    Collection<QuadEdge> edges = ((Collection<QuadEdge>)(subdiv.getEdges()));
                    edges.stream().filter(edge->{
                        LineSegment seg = edge.toLineSegment();
                        return seg.getLength()<s.radius*4;
                    }).forEach(edge->{
                        Node orig = mapping.getOrDefault(edge.orig(), new Node(edge.orig().getCoordinate(), 1));
                        mapping.put(edge.orig(), orig);
                        Node dest = mapping.getOrDefault(edge.dest(), new Node(edge.dest().getCoordinate(), 1));
                        mapping.put(edge.dest(), dest);
                        orig.addAdjacent(dest);
                        dest.addAdjacent(orig);
                    });
                    Set<Node> nodes = new HashSet<Node>(mapping.values());
                    assert nodes.size()==sites.getNumGeometries();
                    
                    RiverModel model = new RiverModel();
                    model.setNodes(nodes);
                    model.setInit(nodes.iterator().next());
                    Set<Node> rivers = model.getModel();
                    rivers.stream().filter(node->{
                        return node.getDownStream().isPresent();
                    }).forEach(node ->{
                        Coordinate orig = node.getLocation();
                        Coordinate dest = node.getDownStream().get().getLocation();
                        double width = Math.log(node.getFlow()+1)/20;
                        out.printf("<line x1='%f' y1='%f' x2='%f' y2='%f' style='stroke:rgb(255,0,0);stroke-width:%f' />",
                                orig.x,
                                orig.y,
                                dest.x,
                                dest.y,
                                width
                                ).println();
                    });
                    out.println("</g>");
                });*/
        System.err.println("Done");
    }
    
    static void addAdjacent(Node node, QuadEdge edge, Map<Vertex, Node> mapping) {
        node.addAdjacent(mapping.get(edge.dest()));
        for(QuadEdge e = edge.oNext(); e!=edge; e=e.oNext()) {
            node.addAdjacent(mapping.get(e.dest()));
        }
    }
}
