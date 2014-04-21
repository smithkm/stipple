package ca.draconic.stipple.wangtiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Preconditions;

public class RecursiveTile<T> extends Tile<T> {

    int k;
    
    RecursiveTile<T>[] subtiles;
    
    @SuppressWarnings("unchecked")
    protected RecursiveTile(TileSet<T> set, Tile<T> template, int k) {
        super(set, template);
        this.k=k;
        this.subtiles=new RecursiveTile[k*k];
    }
    
    protected RecursiveTileSet<T> getSet() {
        return (RecursiveTileSet<T>)this.set;
    }
    
    public RecursiveTile<T> getSubtile(int x, int y) {
        Preconditions.checkPositionIndex(x, k);
        Preconditions.checkPositionIndex(y, k);
        return subtiles[x+k*y];
    }
    
    protected void setSubtile(int x, int y, RecursiveTile<T> t) {
        Preconditions.checkPositionIndex(x, k);
        Preconditions.checkPositionIndex(y, k);
        Preconditions.checkArgument(t.set==this.set);
        subtiles[x+k*y]=t;
    }
    
    protected int errorsForSubtile(int x, int y) {
        return errorsForSubtile( x, y, getSubtile(x,y));
    }
    
    protected int errorsForSubtile(int x, int y, RecursiveTile<T> subtile) {
        final int subup, subdown, subleft, subright;
        Preconditions.checkPositionIndex(x, k);
        Preconditions.checkPositionIndex(y, k);
        
        if(x==0) {
            subleft = getSet().colourSequences[this.left][y];
        } else {
            subleft = getSubtile(x-1, y).right; 
        }
        if(x==k-1) {
            subright = getSet().colourSequences[this.right][y];
        } else {
            subright = getSubtile(x+1, y).left; 
        }
        if(y==0) {
            subup = getSet().colourSequences[this.up][x];
        } else {
            subup = getSubtile(x, y-1).down; 
        }
        if(y==k-1) {
            subdown = getSet().colourSequences[this.down][x];
        } else {
            subdown = getSubtile(x, y+1).up; 
        }
        
        int errors=0;
        if(subup!=subtile.up) errors++;
        if(subdown!=subtile.down) errors++;
        if(subleft!=subtile.left) errors++;
        if(subright!=subtile.right) errors++;
        
        return errors;
    }
    
    protected int hasErrors() {
        int errorsum=0;
        for(int x=0; x<k; x++) {
            for(int y=0; y<k; y++) {
//                if(errorsForSubtile(x,y)>0) return true;
                errorsum+=errorsForSubtile(x,y);
            }
        }
        return errorsum;
    }
    
    @SuppressWarnings("unchecked")
    protected RecursiveTile<T> getBetterTile(int x, int y, Random rand) {
        Preconditions.checkPositionIndex(x, k);
        Preconditions.checkPositionIndex(y, k);
        List<RecursiveTile<T>> better = new ArrayList<>();
        List<RecursiveTile<T>> equal = new ArrayList<>();
        int currentErrors = errorsForSubtile(x,y);
        RecursiveTile<T> currentTile=getSubtile(x,y);
        for(RecursiveTile<T> tile : (Set<RecursiveTile<T>>) set.getTiles()) {
            int newErrors = errorsForSubtile(x,y, tile);
            if(currentErrors>newErrors) better.add(tile);
            else if(currentErrors==newErrors && tile!=currentTile) equal.add(tile);
        }
        if(!better.isEmpty())
            return better.get(rand.nextInt(better.size()));
        else if(!equal.isEmpty())
            return equal.get(rand.nextInt(equal.size()));
        else
            return currentTile;
    }
    
    protected boolean generateSubTiles(Random rand) {
        for(int i=0; i<k*k; i++) {
            subtiles[i]=(RecursiveTile<T>) getSet().getTile(null, null, rand);
        }
        int maxTries = 1000;
        for(int tries=0; tries<maxTries; tries++) {
            int totalErrors = hasErrors();
            //if(tries%(maxTries/10)==0) System.err.printf("errors: %d", totalErrors).println();
            if(totalErrors==0) return true; // Tile is good, stop here
            
            int x=rand.nextInt(k);
            int y=rand.nextInt(k);
            
            int currentErrors = errorsForSubtile(x,y);
            assert currentErrors>=0;
            
            if(currentErrors>0) {
                RecursiveTile<T> newTile = (RecursiveTile<T>) getBetterTile(x, y, rand);
                setSubtile(x, y, newTile);
            }
        }
        
        return false; // Could not find a recursive tiling
    }
}
