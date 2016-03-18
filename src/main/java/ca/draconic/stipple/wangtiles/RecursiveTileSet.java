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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RecursiveTileSet<T> extends TileSet<T> {

    int k;
    
    int colourSequences[][];
    
    protected RecursiveTileSet(int colours, int k) {
        super(colours);
        this.k=k;
        this.colourSequences = new int[colours][k];
    }
    
    protected static <T> RecursiveTileSet<T> fromTemplate(TileSet<T> template, int k) {
        RecursiveTileSet<T> set = new RecursiveTileSet<>(template.colours, k);
        
        for(Tile<T> t: template.getTiles()) {
            set.tiles.add(new RecursiveTile<T>(set, t, k));
        }
        
        return set;
        
    }
    
    protected static void regenerateSequences(int[][] colourSequences, int colours, Random rand) {
        for(int i=0; i< colours; i++) {
            int[] sequence = colourSequences[i];
            generateSequence: while (true){
                for(int j=0; j<sequence.length; j++) {
                    sequence[j]=rand.nextInt(colours);
                }
                for(int j=0; j<i; j++) {
                    if (Arrays.equals(sequence, colourSequences[j])) 
                        continue generateSequence; // Not unique, try again
                }
                break; // The new sequence is unique
            }
        }
        assert true;
    }
    
    public static <T> RecursiveTileSet<T> generate(int colours, int n, int k, Random rand) {
        RecursiveTileSet<T> set = fromTemplate(TileSet.generate(colours, n, rand), k);
        newSequences: while(true) {
            System.err.println();
            System.err.println("Generating colour sequences");
            
            regenerateSequences(set.colourSequences, colours, rand);
            
            System.err.println("Generating subtiles");
            if (set.getTiles().stream().parallel().allMatch(t -> {
               System.err.print(".");
               return t.generateSubTiles(rand);
            })) break;
            /*
            for(RecursiveTile<T> t: set.getTiles()) {
                System.err.println("Generating subtiles");
                if(!t.generateSubTiles(rand)) continue newSequences; // Start over with new mappings
            }
            break; // We got recursive tilings for all of the tiles*/
        }
        assert set.tiles.size()==colours*colours*n;
        return set;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<? extends RecursiveTile<T>> getTiles() {
        // TODO Auto-generated method stub
        return (Set<RecursiveTile<T>>) super.getTiles();
    }
    
    
}
