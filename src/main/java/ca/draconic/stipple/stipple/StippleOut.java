package ca.draconic.stipple.stipple;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.Supplier;

import ca.draconic.stipple.svg.SVGStream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class StippleOut {
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
        final Envelope env = new Envelope(0,100, 0,100);
        final Supplier<Coordinate> generator = new EuclideanUniformPointGenerator(env);
        final DistanceMetric<Coordinate> metric = new EuclideanDistance();
        
        final double decay = 1.0-Math.pow(2.0, -10);
        final double initRadius = Math.hypot(env.getWidth(),env.getHeight());
        
        final int points = 100;
        final int tries = 300;
        
        Stippler<Coordinate> gen;
        
        try( FileInputStream fileIn = new FileInputStream("/home/smithkm/stipple.dat");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                ){
            gen = (Stippler<Coordinate>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            System.err.printf("decay: %.20f \t initRadius: %f \t triesPerRadius: %d", decay, initRadius, tries).println();
            
            gen = new Stippler<Coordinate>(metric, generator, initRadius, tries, decay, points);

        }
        
        for(int i = 0; i<points; i++) {
            Coordinate c = gen.get();
            System.err.println(i);
        }
        
        List<Coordinate> pointList = gen.getAll();
        
        try( FileOutputStream fileOut = new FileOutputStream("/home/smithkm/stipple.dat");
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
            ) {
            out.writeObject(gen);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        try( FileOutputStream fileOut = new FileOutputStream("/home/smithkm/foo.svg")) {
            Coordinate[] dots = load(points);
            SVGStream.wrap(fileOut, (int)env.getMaxX(), (int)env.getMaxY(), null, out->{
                for (int i=0; i<dots.length; i++) {
                    out.circle(dots[i], 0.125, String.format("fill: rgb(%d, %d, %d);", 256-256*i/dots.length, 0, 256*i/dots.length));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static Coordinate[] load(int count) throws IOException {
        try( FileInputStream fileIn = new FileInputStream("/home/smithkm/stipple.dat");
                ObjectInputStream in = new ObjectInputStream(fileIn);
               ) {
            @SuppressWarnings("unchecked")
			Stippler<Coordinate> gen = (Stippler<Coordinate>) in.readObject();
            return gen.getAll().toArray(new Coordinate[]{});
       } catch (ClassNotFoundException ex) {
           throw new IllegalStateException(ex);
       }
    }
}
