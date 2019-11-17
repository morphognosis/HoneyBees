// For conditions of distribution and use, see copyright notice in Main.java

// Flower.

package morphognosis.honey_bees;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import morphognosis.Utility;

public class Flower
{
   public int nectar;

   // Constructor.
   public Flower()
   {
      nectar = 0;
   }


   // Clear.
   public void clear()
   {
      nectar = 0;
   }


   // Save flower.
   public void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, nectar);
      writer.flush();
   }


   // Load flower.
   public void load(DataInputStream reader) throws IOException
   {
      nectar = Utility.loadInt(reader);
   }
}
