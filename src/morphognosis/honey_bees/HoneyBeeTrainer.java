// For conditions of distribution and use, see copyright notice in Main.java

// Honey bee trainer.

package morphognosis.honey_bees;

import java.security.SecureRandom;

public class HoneyBeeTrainer
{
   // Parameters.
   public static int NUM_TRIAL_FORAGES   = 1;
   public static int MIN_STEPS_TO_NECTAR = 3;
   public static int MAX_STEPS_TO_NECTAR = 10;
   public static int RANDOM_SEED         = Main.DEFAULT_RANDOM_SEED;

   // World.
   public World world;

   // World display.
   public WorldDisplay display;

   // Random numbers.
   public SecureRandom random;

   // Constructor.
   public HoneyBeeTrainer()
   {
      random = new SecureRandom();
      random.setSeed(RANDOM_SEED);
      world   = new World(random.nextInt());
      display = new WorldDisplay(world, random.nextInt());
   }


   // Train.
   public void train()
   {
      //int steps = random.nextInt(MAX_STEPS_TO_NECTAR - MIN_STEPS_TO_NECTAR) + MIN_STEPS_TO_NECTAR;
      int steps = -1;

      if (steps >= 0)
      {
         for ( ; steps > 0; steps--)
         {
            world.step();
         }
      }
      else
      {
         for (int i = 0; updateDisplay(i); i++)
         {
            world.step();
         }
      }
   }


   // Update display.
   // Return false for display quit.
   public boolean updateDisplay(int steps)
   {
      if (display != null)
      {
         display.update(steps);
         if (display.quit)
         {
            display = null;
            return(false);
         }
         else
         {
            return(true);
         }
      }
      else
      {
         return(false);
      }
   }


   // Main.
   public static void main(String[] argv)
   {
      HoneyBeeTrainer trainer = new HoneyBeeTrainer();

      trainer.train();
   }
}
