package ca.draconic.stipple.wangtiles;

public class PositionedTile<T extends Object> {
    public final Tile<T> tile;
    public final int x;
    public final int y;
    
    public PositionedTile(Tile<T> tile, int x, int y) {
        super();
        this.tile = tile;
        this.x = x;
        this.y = y;
    }
}
