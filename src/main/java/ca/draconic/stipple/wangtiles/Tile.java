package ca.draconic.stipple.wangtiles;

import com.google.common.base.Preconditions;

public class Tile<T extends Object> {

    final TileSet set;
    
    T data;
    
    final int up, down, left, right;
    
    protected Tile(TileSet set, int up, int down, int left, int right) {
        super();
        
        Preconditions.checkNotNull(set);
        Preconditions.checkArgument(set.isColour(up));
        Preconditions.checkArgument(set.isColour(down));
        Preconditions.checkArgument(set.isColour(left));
        Preconditions.checkArgument(set.isColour(right));
        
        this.set = set;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }
    
    public T getData() {
        return data;
    }

    protected void setData(T data) {
        this.data = data;
    }

}
