package ca.draconic.stipple.wangtiles;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Random;

import org.hamcrest.Matchers;
import org.junit.Test;

public class RecursiveTileSetTest {

    @Test
    public void test() {
        Random rand = new Random();
        
        String[] colours = {"#FF8888", "#88FF88", "#8888FF"};//, "#FFFF44"};
        int subtiles = 4;
        
        RecursiveTileSet<String> set = RecursiveTileSet.generate(colours.length, 5, subtiles, rand);
        
        for(Tile<String> t: set.tiles ){
            assertThat(t, instanceOf(RecursiveTile.class));
            assertThat(((RecursiveTile<String>)t).hasErrors(), is(0));
        }
    }

}
