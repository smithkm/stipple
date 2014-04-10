package ca.draconic.stipple.wangtiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TileSet implements Iterable<Tile> {
    final int colours;
    final Set<Tile> tiles;
    
    public boolean isColour(int c) {
        return c>=0 && c<colours;
    }

    protected TileSet(int colours, Set<Tile> tiles) {
        super();
        this.colours = colours;
        this.tiles = new HashSet<Tile>(tiles);
    }
    protected TileSet(int colours) {
        this(colours, Collections.emptySet());
    }
    
    /**
     * Generate a set of tiles where each pair of up and left colours appears n times.
     * @param colours
     * @param n
     * @param rand
     * @return
     */
    static TileSet generate(int colours, int n, Random rand) {
        TileSet set = new TileSet(colours);
        for(int up=0; up<colours; up++) {
            for(int left=0; left<colours; left++) {
                for(int i=0; i<n;) {
                    int right = getColour(colours, left, rand, true);
                    int down = getColour(colours, up, rand, true);

                    if(set.tiles.add(new Tile(set, up, down, left, right))) i++;
                }
            }
        }
        
        return set;
    }
    static private int getColour(int colours, int opposite, Random rand, float probMatchingOpposites) {
        return getColour(colours, opposite, rand, rand.nextDouble()>=probMatchingOpposites);
    }
    static private int getColour(int colours, int opposite, Random rand, boolean noMatchingOpposites) {
        int col;
        if(noMatchingOpposites) {
            col = rand.nextInt(colours-1);
            if (col>=opposite) col++;
        } else {
            col = rand.nextInt(colours);
        }
        return col;
    }
    
    public Tile getTile(Integer up, Integer left, Random rand) {
        List<Tile> usable = new ArrayList<>(tiles.size()/colours);
        for(Tile t: this) {
            if((up==null || t.up==up) && (left==null || t.left==left)) {
                usable.add(t);
            }
        }
        
        assert(usable.size()==tiles.size()/colours);
        
        return(usable.get(rand.nextInt(usable.size())));
    }
    
    public Iterable<PositionedTile> getTiling(final int width, final int height, final Random rand) {
        return new Iterable<PositionedTile>() {
            
            @Override
            public Iterator<PositionedTile> iterator() {
                return new Iterator<PositionedTile>() {
                    int x = 0;
                    int y = 0;
                    Integer[] previousRow = new Integer[width];
                    Integer previousColour = null;
                    
                    @Override
                    public boolean hasNext() {
                        return x<width && y<height;
                    }

                    @Override
                    public PositionedTile next() {
                        Integer previousUp=previousRow[x];
                        
                        Tile t = getTile(previousUp, previousColour, rand);
                        previousRow[x]=t.down;
                        previousColour=t.right;
                        
                        PositionedTile p = new PositionedTile(t, x, y);
                        
                        x++;
                        if(x>=width) {
                            x=0;
                            y++;
                            previousColour=null;
                        }
                        
                        return p;
                    }
                    
                };
            }
            
        };
    }
    
    @Override
    public Iterator<Tile> iterator() {
        return tiles.iterator();
    }
}
