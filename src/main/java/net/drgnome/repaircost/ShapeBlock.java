// Bukkit Plugin "RepairCost" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.repaircost;

import org.bukkit.block.*;

public class ShapeBlock
{
    private final boolean _remove;
    private final int _id;
    private final int _meta;
    
    public ShapeBlock(String data, boolean remove)
    {
        _remove = remove;
        if(data.contains(":"))
        {
            String[] split = data.split(":");
            data = split[0];
            _meta = Util.tryParse(split[1], 0);
        }
        else
        {
            _meta = -1;
        }
        _id = Util.tryParse(data, 0);
    }
    
    public boolean matches(Block block)
    {
        return (block.getTypeId() == _id) && ((_meta == -1) || ((byte)block.getData() == _meta));
    }
    
    public boolean remove()
    {
        return _remove;
    }
}