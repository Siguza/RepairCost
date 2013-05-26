// Bukkit Plugin "RepairCost" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.repaircost;

import java.io.*;
import java.util.*;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class Shape
{
    public static final String _filename = "shape.txt";
    private static File _file;
    private static ShapeBlock[][][] _blocks;
    private static ArrayList<int[]> _keys = new ArrayList<int[]>();
    
    public static void reload(Plugin plugin)
    {
        _file = new File(plugin.getDataFolder(), _filename);
        if(!_file.exists())
        {
            try
            {
                byte[] b = new byte[1024];
                InputStream in = plugin.getResource(_filename);
                OutputStream out = new FileOutputStream(_file);
                int bytesRead;
                while((bytesRead = in.read(b)) != -1)
                {
                    out.write(b, 0, bytesRead);
                }
            }
            catch(Throwable t)
            {
                t.printStackTrace();
            }
        }
        parse();
    }
    
    private static void parse()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_file), "UTF-8"));
            ArrayList<String> lines = new ArrayList<String>();
            String line;
            while((line = reader.readLine()) != null)
            {
                lines.add(line.replace(" ", "").replace("\t", "").toLowerCase());
            }
            reader.close();
            ArrayList<ShapeBlock[][]> list = new ArrayList<ShapeBlock[][]>();
            ArrayList<ShapeBlock[]> tmp = new ArrayList<ShapeBlock[]>();
            for(String l : lines)
            {
                if(l.equals("-"))
                {
                    list.add(tmp.toArray(new ShapeBlock[0][]));
                    tmp.clear();
                    continue;
                }
                String[] s = l.split("\\|");
                ShapeBlock[] blocks = new ShapeBlock[s.length];
                for(int i = 0; i < s.length; i++)
                {
                    if(s[i].length() > 0)
                    {
                        boolean key = false;
                        boolean remove = false;
                        while(s[i].startsWith("a") || s[i].startsWith("c"))
                        {
                            if(s[i].startsWith("a"))
                            {
                                key = true;
                            }
                            else if(s[i].startsWith("c"))
                            {
                                remove = true;
                            }
                            s[i] = s[i].substring(1);
                        }
                        if(s[i].length() == 0)
                        {
                            continue;
                        }
                        blocks[i] = new ShapeBlock(s[i], remove);
                        if(key)
                        {
                            _keys.add(new int[]{i, list.size(), tmp.size()});
                        }
                    }
                }
                tmp.add(blocks);
            }
            list.add(tmp.toArray(new ShapeBlock[0][]));
            _blocks = list.toArray(new ShapeBlock[0][][]);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    public static int apply(World world, int x0, int y0, int z0)
    {
        for(int i = 0; i < _keys.size(); i++)
        {
            int[] coords = _keys.get(i);
            if(check(world, x0 - coords[0], y0 - coords[1], z0 - coords[2]))
            {
                return i;
            }
        }
        return -1;
    }
    
    private static boolean check(World world, int x0, int y0, int z0)
    {
        for(int y = 0; y < _blocks.length; y++)
        {
            for(int z = 0; z < _blocks[y].length; z++)
            {
                for(int x = 0; x < _blocks[y][z].length; x++)
                {
                    if(_blocks[y][z][x] == null)
                    {
                        continue;
                    }
                    if(!_blocks[y][z][x].matches(world.getBlockAt(x0 + x, y0 + y, z0 + z)))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public static void remove(int key, World world, int x0, int y0, int z0)
    {
        int[] coords = _keys.get(key);
        x0 -= coords[0];
        y0 -= coords[1];
        z0 -= coords[2];
        for(int y = 0; y < _blocks.length; y++)
        {
            for(int z = 0; z < _blocks[y].length; z++)
            {
                for(int x = 0; x < _blocks[y][z].length; x++)
                {
                    if(_blocks[y][z][x] == null)
                    {
                        continue;
                    }
                    if(_blocks[y][z][x].remove())
                    {
                        world.getBlockAt(x0 + x, y0 + y, z0 + z).setTypeId(0);
                    }
                }
            }
        }
    }
}