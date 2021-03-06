// For conditions of distribution and use, see copyright notice in Main.java

// Cell.

package morphognosis.honey_bees;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import morphognosis.Utility;

public class Cell
{
   // Properties.
   public boolean  hive;
   public Flower   flower;
   public HoneyBee bee;
   public Random   random;

   // Constructor.
   public Cell(Random random)
   {
      this.random = random;
      hive        = false;
      flower      = null;
      bee         = null;
   }


   // Clear.
   public void clear()
   {
      flower = null;
      bee    = null;
   }


   // Save cell.
   public void save(DataOutputStream writer) throws IOException
   {
      int v = 0;

      if (hive)
      {
         v = 1;
      }
      Utility.saveInt(writer, v);
      v = 0;
      if (flower != null)
      {
         v = 1;
         Utility.saveInt(writer, v);
         flower.save(writer);
      }
      else
      {
         Utility.saveInt(writer, v);
      }
      writer.flush();
   }


   // Load cell.
   public void load(DataInputStream reader) throws IOException
   {
      clear();
      if (Utility.loadInt(reader) == 1)
      {
         hive = true;
      }
      if (Utility.loadInt(reader) == 1)
      {
         flower = new Flower(false, random);
         flower.load(reader);
      }
   }
}
