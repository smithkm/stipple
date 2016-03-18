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
package ca.draconic.stipple.svg;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;

public class TestSVG {
    
    public static void svgOut(String fileName, int width, int height, Consumer<PrintStream> defs, Consumer<PrintStream> body) {
        
        if (fileName!=null) {
            
            File f = new File(fileName);
            
            try(PrintStream out = new PrintStream(f)) {
                
                svgOut(out, width, height, defs, body);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            }
        } else {
            svgOut(System.out, width, height, defs, body);
        }
    }

    public static void svgOut(PrintStream out, int width, int height, Consumer<PrintStream> defs, Consumer<PrintStream> body) {
        out.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" ");
        out.println("  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        out.printf("<svg width=\"%dpx\" height=\"%dpx\" version=\"1.1\"", width, height).println();
        out.println("     xmlns=\"http://www.w3.org/2000/svg\"");
        out.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
        
        if(defs!=null) {
            out.println("  <defs>");
            defs.accept(out);
            out.println("  </defs>");
        }
        
        body.accept(out);
        
        out.println("</svg>");
    }
    
}
