// For conditions of distribution and use, see copyright notice in Main.java

// Flower.

package morphognosis.honey_bees;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import morphognosis.Utility;

public class Flower
{
   public boolean      nectar;
   public int          nectarRegenerationTimer;
   public SecureRandom random;

   // Constructor.
   public Flower(boolean nectar, SecureRandom random)
   {
      this.nectar             = nectar;
      this.random             = random;
      nectarRegenerationTimer = -1;
   }


   // Clear.
   public void clear()
   {
      nectar = false;
      nectarRegenerationTimer = -1;
   }


   // Save flower.
   public void save(DataOutputStream writer) throws IOException
   {
      if (nectar)
      {
         Utility.saveInt(writer, 1);
      }
      else
      {
         Utility.saveInt(writer, 0);
      }
      Utility.saveInt(writer, nectarRegenerationTimer);
      writer.flush();
   }


   // Load flower.
   public void load(DataInputStream reader) throws IOException
   {
      if (Utility.loadInt(reader) == 1)
      {
         nectar = true;
      }
      else
      {
         nectar = false;
      }
      nectarRegenerationTimer = Utility.loadInt(reader);
   }


   // Extract nectar.
   public void extractNectar()
   {
      if (random.nextFloat() < Parameters.FLOWER_SURPLUS_NECTAR_PROBABILITY)
      {
         nectar = true;
      }
      else
      {
         nectar = false;
         nectarRegenerationTimer = Parameters.FLOWER_NECTAR_REGENERATION_TIME;
      }
   }


   // Regenerate nectar.
   public void regenerateNectar()
   {
      if (nectarRegenerationTimer != -1)
      {
         nectarRegenerationTimer--;
         if (nectarRegenerationTimer == -1)
         {
            nectar = true;
         }
      }
   }
}
