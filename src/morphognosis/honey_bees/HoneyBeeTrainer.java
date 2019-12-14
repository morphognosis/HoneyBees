// For conditions of distribution and use, see copyright notice in Main.java

// Honey bee trainer.

package morphognosis.honey_bees;

public class HoneyBeeTrainer extends World
{
   // Parameters.
   public static int NUM_TRIAL_FORAGES   = 1;
   public static int MAX_STEPS_PER_TRIAL = 100;
   public static int MIN_STEPS_TO_NECTAR = 3;
   public static int MAX_STEPS_TO_NECTAR = 10;
   public static int RANDOM_SEED         = Main.DEFAULT_RANDOM_SEED;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.HoneyBeeTrainer\n" +
      "     [-display <true | false> (default=true)]\n" +
      "     [-numTrialForages <trials> (default=" + NUM_TRIAL_FORAGES + ")]\n" +
      "     [-maxStepsPerTrial <steps> (default=" + MAX_STEPS_PER_TRIAL + ")]\n" +
      "     [-minStepsToNectar <steps> (default=" + MIN_STEPS_TO_NECTAR + ")]\n" +
      "     [-maxStepsToNectar <steps> (default=" + MAX_STEPS_TO_NECTAR + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]";

   // World display.
   public WorldDisplay worldDisplay;

   // Constructor.
   public HoneyBeeTrainer(boolean display)
   {
      super(RANDOM_SEED);
      if (display)
      {
         worldDisplay = new WorldDisplay(this);
      }
   }


   // Train.
   public void train()
   {
      int stepsToNectar = random.nextInt(MAX_STEPS_TO_NECTAR - MIN_STEPS_TO_NECTAR) + MIN_STEPS_TO_NECTAR;

      if (worldDisplay != null)
      {
         if (!worldDisplay.update(0, MAX_STEPS_PER_TRIAL))
         {
            return;
         }
      }
      for (int i = 0; i < MAX_STEPS_PER_TRIAL; i++)
      {
         step();
         if (worldDisplay != null)
         {
            if (!worldDisplay.update(i + 1, MAX_STEPS_PER_TRIAL))
            {
               return;
            }
         }
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
         if (args[i].equals("-numTrialForages"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numTrialForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_TRIAL_FORAGES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numTrialForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_TRIAL_FORAGES < 0)
            {
               System.err.println("Invalid numTrialForages option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxStepsPerTrial"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxStepsPerTrial option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_STEPS_PER_TRIAL = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxStepsPerTrial option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_STEPS_PER_TRIAL < 0)
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

      // Create trainer.
      HoneyBeeTrainer trainer = new HoneyBeeTrainer(displayWorld);

      // Train.
      trainer.train();

      System.exit(0);
   }
}
