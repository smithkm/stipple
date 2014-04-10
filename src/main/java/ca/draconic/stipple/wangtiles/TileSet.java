package ca.draconic.stipple.wangtiles;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class TileSet<T extends Object> {
    final int colours;
    final Set<Tile<T>> tiles;
    
    public boolean isColour(int c) {
        return c>=0 && c<colours;
    }

    protected TileSet(int colours, Set<Tile<T>> tiles) {
        super();
        this.colours = colours;
        this.tiles = new HashSet<>(tiles);
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
    static<T extends Object> TileSet<T> generate(int colours, int n, Random rand) {
        TileSet<T> set = new TileSet<T>(colours);
        for(int up=0; up<colours; up++) {
            for(int left=0; left<colours; left++) {
                for(int i=0; i<n;) {
                    int right = getColour(colours, left, rand, true);
                    int down = getColour(colours, up, rand, true);

                    if(set.tiles.add(new Tile<T>(set, up, down, left, right))) i++;
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
    
    public Tile<T> getTile(Integer up, Integer left, Random rand) {
        List<Tile<T>> usable = new ArrayList<>(tiles.size()/colours);
        for(Tile<T> t: this.tiles) {
            if((up==null || t.up==up) && (left==null || t.left==left)) {
                usable.add(t);
            }
        }
        
        return(usable.get(rand.nextInt(usable.size())));
    }
    
    public Set<Tile<T>> getTiles() {
        return Collections.unmodifiableSet(tiles);
    }
    
    public Collection<PositionedTile<T>> getTiling(final int width, final int height, final Random rand) {
        return new AbstractCollection<PositionedTile<T>>() {
            
            @Override
            public Iterator<PositionedTile<T>> iterator() {
                return new Iterator<PositionedTile<T>>() {
                    int x = 0;
                    int y = 0;
                    Integer[] previousRow = new Integer[width];
                    Integer previousColour = null;
                    
                    @Override
                    public boolean hasNext() {
                        return x<width && y<height;
                    }

                    @Override
                    public PositionedTile<T> next() {
                        Integer previousUp=previousRow[x];
                        
                        Tile<T> t = getTile(previousUp, previousColour, rand);
                        previousRow[x]=t.down;
                        previousColour=t.right;
                        
                        PositionedTile<T> p = new PositionedTile<>(t, x, y);
                        
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

            @Override
            public int size() {
                return width*height;
            }
            
        };
    }
}
