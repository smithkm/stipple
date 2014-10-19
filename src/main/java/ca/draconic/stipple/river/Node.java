package ca.draconic.stipple.river;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdge;

public class Node {
    Set<Node> adjacent;
    Node downstream;
    Coordinate location;
    int rainfall;
    int flow;
    
    public Node(Coordinate location, int rainfall) {
        this(new HashSet<>(), null, location, rainfall);
    }
    
    public void addAdjacent(Node a) {
        this.adjacent.add(a);
    }
    
    public Node(Set<Node> adjacent, Node downstream, Coordinate location,
            int rainfall) {
        super();
        this.adjacent = adjacent;
        this.downstream = downstream;
        this.location = location;
        this.rainfall = rainfall;
        flow = rainfall;
    }
    
    protected Optional<Double> angle(Node n){
        if(!n.getDownStream().isPresent()) return Optional.empty();
        
        Coordinate a = n.getDownStream().get().getLocation();
        Coordinate b = n.getLocation();
        Coordinate c = this.getLocation();
        Coordinate c_prime = new Coordinate(c.x+(a.x-b.x), c.y+(a.y-b.y));
        
        return Optional.of(Angle.angleBetweenOriented(b, a, c_prime));
    }
    
    public Optional<Node> flowToOneOf(final Set<Node> connected) {
        WeightedRandomCollection<Node> candidates = new WeightedRandomCollection<Node>(adjacent.size());
        ToDoubleFunction<Optional<Double>> weight = theta->{
            return Math.pow((Math.cos(theta.orElse(0.0))+1)/2, 100) - Math.pow((Math.cos(theta.orElse(0.0))+1)/2, 300)/4;
        };        
        adjacent.stream().sequential().filter(n->{
            return connected.contains(n);
        }).forEach(node ->{
            candidates.add(node, weight.applyAsDouble(this.angle(node)));
        });
        
        Optional<Node> result = candidates.get();
        
        result.ifPresent(downstreamNode->{
            downstream=downstreamNode;
            Node currentNode = this;
            Set<Node> visited = new HashSet<>();
            while(currentNode.downstream!=null) {
                if(!visited.add(currentNode)) {
                    throw new IllegalStateException("Loop in river network");
                }
                currentNode=currentNode.downstream;
                currentNode.flow+=this.flow;
            }
        });
        
        return result;
    }
    
    public Optional<Node> getDownStream() {
        return Optional.ofNullable(downstream);
    }
    
    public Coordinate getLocation() {
        return location;
    }
    
    public int getFlow() {
        return flow;
    }
    
    public static Node getNode(QuadEdge startEdge, ToIntFunction<Coordinate> rainfall) {
        if(startEdge.getData()==null) {
            
            Node n = new Node(startEdge.orig().getCoordinate(),rainfall.applyAsInt(startEdge.orig().getCoordinate()));

            QuadEdgeUtils.orbit(startEdge, QuadEdge::oNext).forEach(edge->{
                edge.setData(n);
            });
        }
        
        return (Node) startEdge.getData();
    }
    public void setAdjacency(QuadEdge startEdge) {
        if(startEdge.getData()!=this)
            throw new IllegalArgumentException();
        
        QuadEdgeUtils.orbit(startEdge.sym(), QuadEdge::dNext).forEach(edge->{
            if(edge.getData()!=null){
                assert edge.getData()!=null;
                this.addAdjacent((Node) edge.getData());
            }
        });
    }

    public Set<Node> getAdjacent() {
        return Collections.unmodifiableSet(adjacent);
    }
    
    public void cutAdjacency(Geometry g) {
        adjacent.removeIf(node ->{
            LineString segment = g.getFactory().createLineString(new Coordinate[]{getLocation(), node.getLocation()});
            return g.intersects(segment);
        });
    }
}
