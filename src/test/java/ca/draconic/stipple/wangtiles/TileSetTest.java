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
package ca.draconic.stipple.wangtiles;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Random;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.List;

public class TileSetTest {

    static <T extends Comparable<T>> Matcher<T> halfInterval(T o1, T o2) {
        return both(greaterThanOrEqualTo(o1)).and(lessThan(o2));
    }
    
    @Test
    public void testTiling() {
        int width = 20;
        int height = 15;
        int colours = 4;
        int n = 2;
        
        @SuppressWarnings("unchecked")
        List<Tile<Object>> tiles = Arrays.asList(new Tile[width*height]);
        
        Random rand = new Random();
        
        TileSet<Object> set = TileSet.generate(colours, n, rand);
        
        // Fill a grid with tiles and ensure there are no doubles or out of bounds tiles
        
        for(PositionedTile<Object> t: set.getTiling(width, height, rand)) {
            assertThat(Integer.valueOf(t.x), halfInterval(0, width));
            assertThat(Integer.valueOf(t.y), halfInterval(0, height));
            assertThat(tiles.get(t.x+t.y*width), nullValue());
            tiles.set(t.x+t.y*width, t.tile);
        }
        
        // Check that every cell is filled and that adjacency rules are followed
        
        for(int x=0; x<width; x++) {
            for(int y=0; y<height; y++) {
                Tile<Object> t = tiles.get(x+y*width);
                assertThat(t, not(nullValue()));
                if(x>0) assertThat(t.left, equalTo(tiles.get((x-1)+y*width).right));
                if(y>0) assertThat(t.up, equalTo(tiles.get(x+(y-1)*width).down));
            }
        }
    }

}
