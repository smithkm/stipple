package ca.draconic.stipple.wangtiles;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class TestTiling {

    public static void main(String[] args) {
        
        if (true) {
            
            File f = new File("/home/smithkm/foo.svg");
            
            try(PrintStream out = new PrintStream(f)) {
                
                svgOut(out);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            }
        } else {
            svgOut(System.out);
        }
    }
    
    static void svgOut(PrintStream out) {
        Random rand = new Random();
        
        String[] colours = {"#FF8888", "#88FF88", "#8888FF", "#FFFF44"};
        
        TileSet<String> set = TileSet.generate(colours.length, 2, rand);
        
        out.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" ");
        out.println("  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        out.println("<svg width=\"400px\" height=\"150px\" version=\"1.1\"");
        out.println("     xmlns=\"http://www.w3.org/2000/svg\"");
        out.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
        int id=0;
        String format = "<path d='M -50 -50 L 0 0 L 50 -50 z' transform='rotate(%d)' style='fill:%s' />";
        for(Tile<String> t:set.getTiles()) {
            t.setData(String.format("tile%d", id));
            out.printf("<symbol id='%s'>", t.getData());
            out.printf(format, 0,   colours[t.up]);
            out.printf(format, 180, colours[t.down]);
            out.printf(format, 90,  colours[t.right]);
            out.printf(format, -90, colours[t.left]);
            out.print("<rect x='-50' y='-50' width='100' height='100' style='fill:none;stroke:#000000;stroke-width:1px;'/>");
            out.printf("</symbol>", t.getData());
            id++;
        }
        

        
        for(PositionedTile<String> t: set.getTiling(10, 5, rand)) {
            
            int x=t.x*100;
            int y=t.y*100;
            out.printf("<use xlink:href='#%s' x='%d' y='%d' />", t.tile.data, x, y);
            out.println();
        }
        out.println("</svg>");

    }

}
