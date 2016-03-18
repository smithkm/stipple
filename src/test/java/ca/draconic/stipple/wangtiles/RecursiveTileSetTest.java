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
