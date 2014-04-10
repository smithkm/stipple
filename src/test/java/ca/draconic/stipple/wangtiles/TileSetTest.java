package ca.draconic.stipple.wangtiles;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Random;

import org.hamcrest.Matcher;
import org.junit.Test;

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
        Tile[] tiles = new Tile[width*height];
        
        Random rand = new Random();
        
        TileSet set = TileSet.generate(colours, n, rand);
        
        // Fill a grid with tiles and ensure there are no doubles or out of bounds tiles
        
        for(PositionedTile t: set.getTiling(width, height, rand)) {
            assertThat(Integer.valueOf(t.x), halfInterval(0, width));
            assertThat(Integer.valueOf(t.y), halfInterval(0, height));
            assertThat(tiles[t.x+t.y*width], nullValue());
            tiles[t.x+t.y*width]=t.tile;
        }
        
        // Check that every cell is filled and that adjacency rules are followed
        
        for(int x=0; x<width; x++) {
            for(int y=0; y<height; y++) {
                Tile t = tiles[x+y*width];
                assertThat(t, not(nullValue()));
                if(x>0) assertThat(t.left, equalTo(tiles[(x-1)+y*width].right));
                if(y>0) assertThat(t.up, equalTo(tiles[x+(y-1)*width].down));
            }
        }
    }

}
