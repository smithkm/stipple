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

import com.google.common.base.Preconditions;

public class Tile<T extends Object> {

    final TileSet<T> set;
    
    T data;
    
    final int up, down, left, right;
    
    protected Tile(TileSet<T> set, int up, int down, int left, int right) {
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
    
    protected Tile(TileSet<T> set, Tile<T> template) {
        this(set, template.up, template.down, template.left, template.right);
        this.data = template.data;
    }
    
    public T getData() {
        return data;
    }

    protected void setData(T data) {
        this.data = data;
    }

}
