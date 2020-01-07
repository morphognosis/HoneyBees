// For conditions of distribution and use, see copyright notice in Main.java

// Honey bee trainer.

package morphognosis.honey_bees;

import java.security.SecureRandom;

import morphognosis.Orientation;

public class HoneyBeeTrainer extends World
{
   // Parameters.
   public static int NUM_FORAGES          = 2;
   public static int MAX_STEPS_PER_FORAGE = 100;
   public static int MIN_STEPS_TO_NECTAR  = 3;
   public static int MAX_STEPS_TO_NECTAR  = 10;
   public static int RANDOM_SEED          = Main.DEFAULT_RANDOM_SEED;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.HoneyBeeTrainer\n" +
      "     [-display <true | false> (default=true)]\n" +
      "     [-numForages <quantity> (default=" + NUM_FORAGES + ")]\n" +
      "     [-maxStepsPerForage <steps> (default=" + MAX_STEPS_PER_FORAGE + ")]\n" +
      "     [-minStepsToNectar <steps> (default=" + MIN_STEPS_TO_NECTAR + ")]\n" +
      "     [-maxStepsToNectar <steps> (default=" + MAX_STEPS_TO_NECTAR + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]";

   // World display.
   public WorldDisplay worldDisplay;

   // Flower location.
   int flowerX;
   int flowerY;

   // Random numbers.
   SecureRandom trainRandom;

   // Constructor.
   public HoneyBeeTrainer(boolean display)
   {
      super(RANDOM_SEED);
      trainRandom = new SecureRandom();
      trainRandom.setSeed(RANDOM_SEED);
      if (display)
      {
         worldDisplay = new WorldDisplay(this);
      }
   }


   // Train.
   public void train()
   {
      HoneyBee bee = bees[0];

      for (int i = 0; i < NUM_FORAGES; i++)
      {
         randomSeed = trainRandom.nextInt();
         reset();
         placeBee(bee);
         flowerX = flowerY = -1;
         setDriver(Driver.AUTOPILOT);
         forage(bee, "Training forage " + i);
         try
         {
            bee.writeMetamorphDataset(bee.metamorphDatasetFilename, true);
         }
         catch (Exception e)
         {
            System.err.println("Cannot write metamorph dataset");
         }
         reset();
         if (flowerX != -1)
         {
            Flower flower = new Flower();
            flower.nectar = 1;
            cells[flowerX][flowerY].flower = flower;
         }
         setDriver(Driver.METAMORPH_DB);
         forage(bee, "Testing forage " + i);
         System.out.println("Collected nectar = " + collectedNectar);
      }
   }


   // Place bee in world.
   public void placeBee(HoneyBee bee)
   {
      cells[bee.x][bee.y].bee = null;
      for (int i = 0; i < 20; i++)
      {
         int dx = random.nextInt(Parameters.HIVE_RADIUS);
         if (random.nextBoolean()) { dx = -dx; }
         int dy = random.nextInt(Parameters.HIVE_RADIUS);
         if (random.nextBoolean()) { dy = -dy; }
         bee.x = bee.x2 = (Parameters.WORLD_WIDTH / 2) + dx;
         bee.y = bee.y2 = (Parameters.WORLD_HEIGHT / 2) + dy;
         if (cells[bee.x][bee.y].hive && (cells[bee.x][bee.y].bee == null))
         {
            cells[bee.x][bee.y].bee = bee;
            break;
         }
         if (i == 19)
         {
            System.err.println("Cannot place bee in world");
            System.exit(1);
         }
      }
      bee.orientation = bee.orientation2 = random.nextInt(Orientation.NUM_ORIENTATIONS);
   }


   // Forage.
   public void forage(HoneyBee bee, String status)
   {
      System.out.println(status);
      int stepsToNectar = random.nextInt(MAX_STEPS_TO_NECTAR - MIN_STEPS_TO_NECTAR) + MIN_STEPS_TO_NECTAR;
      if (worldDisplay != null)
      {
         worldDisplay.controls.messageText.setText(status);
         if (!worldDisplay.update(0, MAX_STEPS_PER_FORAGE))
         {
            return;
         }
      }
      for (int i = 0; i < MAX_STEPS_PER_FORAGE; i++)
      {
         if ((i >= stepsToNectar) && (flowerX == -1) && !cells[bee.x][bee.y].hive)
         {
            flowerX = bee.x;
            flowerY = bee.y;
            Flower flower = new Flower();
            flower.nectar = 1;
            cells[bee.x][bee.y].flower = flower;
         }
         step();
         if (worldDisplay != null)
         {
            worldDisplay.controls.messageText.setText(status);
            if (!worldDisplay.update(i + 1, MAX_STEPS_PER_FORAGE))
            {
               return;
            }
         }
         if (collectedNectar > 0) { return; }
      }
   }


   // Main.
   public static void main(String[] args)
   {
      boolean displayWorld = true;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-display"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid display option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((args[i] != null) && args[i].equals("true"))
            {
               displayWorld = true;
            }
            else if ((args[i] != null) && args[i].equals("false"))
            {
               displayWorld = false;
            }
            else
            {
               System.err.println("Invalid display option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numForages"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_FORAGES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_FORAGES < 0)
            {
               System.err.println("Invalid numForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxStepsPerForage"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxStepsPerForage option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_STEPS_PER_FORAGE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxStepsPerForage option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_STEPS_PER_FORAGE < 0)
            {
               System.err.println("Invalid maxStepsPerTrial option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-minStepsToNectar"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid minStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MIN_STEPS_TO_NECTAR = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid minStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MIN_STEPS_TO_NECTAR < 0)
            {
               System.err.println("Invalid minStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxStepsToNectar"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_STEPS_TO_NECTAR = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_STEPS_TO_NECTAR < 0)
            {
               System.err.println("Invalid maxStepsToNectar option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               RANDOM_SEED = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }
      if (MIN_STEPS_TO_NECTAR > MAX_STEPS_TO_NECTAR)
      {
         System.err.println("minStepsToNectar cannot be greater than maxStepsToNectar");
         System.exit(1);
      }

      // Set parameters.
      Parameters.FLOWER_SPROUT_PROBABILITY            = 0.0f;
      Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY = 0.0f;
      Parameters.NUM_BEES = 1;

      // Train.
      HoneyBeeTrainer trainer = new HoneyBeeTrainer(displayWorld);
      trainer.train();

      System.exit(0);
   }
}
