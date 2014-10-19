package ca.draconic.stipple.svg;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class SVGStream {
    PrintStream out;
    
    public SVGStream(PrintStream out) {
        super();
        this.out = out;
    }

    public void circle(Coordinate location, double radius, String style){
        out.printf("<circle cx='%f' cy='%f' r='%f' style='%s' />", location.x, location.y, radius, style);
    }
    
    public void segment(Coordinate start, Coordinate dest, String style){
        out.printf("<line x1='%f' y1='%f' x2='%f' y2='%f' style='%s' />", start.x, start.y, dest.x, dest.y, style);
    }
    
    public void group(Runnable body){
        out.printf("<g>");
        body.run();
        out.printf("</g>");
    }
    
    public void path(String data, String style) {
        out.printf("<path d='%s' style='%s' />", data, style);
    }
    
    protected static StringBuilder buildPath(LineString g, StringBuilder builder){
        int i=0;
        for(Coordinate p: g.getCoordinates()) {
            if(builder.length()>0) builder.append(" ");
            if(i++==0) {
                builder.append(String.format("M %f %f", p.x, p.y));
            } else {
                builder.append(String.format("L %f %f", p.x, p.y));
            }
        }
        return builder;
    }
    protected static StringBuilder buildPath(MultiLineString g, StringBuilder builder){
        for(int i=0; i<g.getNumGeometries(); i++) {
            LineString s = (LineString) g.getGeometryN(i);
            
            buildPath(s, builder);
        }
        return builder;
    }
    
    public void draw(LineString string, String style) {
        StringBuilder builder = new StringBuilder();
        buildPath(string, builder);
        path(builder.toString(), style);
    }
    public void draw(MultiLineString string, String style) {
        StringBuilder builder = new StringBuilder();
        buildPath(string, builder);
        path(builder.toString(), style);
    }
   
    public static void wrap(OutputStream out, int width, int height, Consumer<Defs> defSetter, Consumer<SVGStream> body) {
        PrintStream pout = new PrintStream(out);
        pout.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        pout.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" ");
        pout.println("  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        pout.printf("<svg width=\"%dpx\" height=\"%dpx\" version=\"1.1\"", width, height).println();
        pout.println("     xmlns=\"http://www.w3.org/2000/svg\"");
        pout.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
        
        SVGStream s = new SVGStream(pout);
        
        if(defSetter!=null) {
            pout.println("<defs>");
            defSetter.accept(s.defs);
            pout.println("</defs>");
        }
        
        body.accept(s);
        
        pout.println("</svg>");
    }
    
    Defs defs = new Defs();
    
    public class Defs {
        public String marker(String id, Consumer<SVGStream> stream) {
            out.printf("<marker id='%s'>", id).println();
            stream.accept(SVGStream.this);
            out.println("</marker>");
            return id;
        }
    }
}
