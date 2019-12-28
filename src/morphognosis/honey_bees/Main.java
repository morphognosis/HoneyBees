/*
 * Copyright (c) 2019-2020 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Main.

package morphognosis.honey_bees;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.swing.UIManager;

import morphognosis.Morphognosis;

public class Main
{
   // Version.
   public static final String VERSION = "1.0";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java morphognosis.honey_bees.Main\n" +
      "      [-steps <steps> | -display <true | false> (default=true)]\n" +
      "      World parameters:\n" +
      "        [-worldDimensions <width> <height> (default=" + Parameters.WORLD_WIDTH + " " + Parameters.WORLD_HEIGHT + ")]\n" +
      "        [-hiveRadius <radius> (default=" + Parameters.HIVE_RADIUS + ")]\n" +
      "      Flower parameters:\n" +
      "        [-flowerSproutProbability <probability> (default=" + Parameters.FLOWER_SPROUT_PROBABILITY + ")]\n" +
      "        [-flowerNectarCapacity <quantity> (default=" + Parameters.FLOWER_NECTAR_CAPACITY + ")]\n" +
      "        [-flowerNectarProductionProbability <probability> (default=" + Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY + ")]\n" +
      "      Honey bee parameters:\n" +
      "        [-numBees <quantity> (default=" + Parameters.NUM_BEES + ")]\n" +
      "        [-beeTurnProbability <probability> (default=" + Parameters.BEE_TURN_PROBABILITY + ")]\n" +
      "        [-beeReturnToHiveProbabilityIncrement <probability> (default=" + Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT + ")]\n" +
      "        [-beeNectarDisplayeDuration <quantity> (default=" + Parameters.BEE_NECTAR_DISPLAY_DURATION + ")]\n" +
      "      Morphognosis parameters:\n" +
      "        [-numNeighborhoods <quantity> (default=" + Parameters.NUM_NEIGHBORHOODS + ")]\n" +
      "        [-neighborhoodInitialDimension <quantity> (default=" + Parameters.NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "        [-neighborhoodDimensionStride <quantity> (default=" + Parameters.NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "        [-neighborhoodDimensionMultiplier <quantity> (default=" + Parameters.NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "        [-epochIntervalStride <quantity> (default=" + Parameters.EPOCH_INTERVAL_STRIDE + ")]\n" +
      "        [-epochIntervalMultiplier <quantity> (default=" + Parameters.EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "        [-equivalentMorphognosticDistance <distance> (default=" + HoneyBee.EQUIVALENT_MORPHOGNOSTIC_DISTANCE + ")]\n" +
      "     [-driver <autopilot | metamorphs | local> (honey bees driver: default=autopilot)]\n" +
      "     [-randomSeed <random number seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-printCollectedNectar]\n" +
      "     [-noLearning (do not learn new metamorphs)]\n" +
      "     [-save <file name>]\n" +
      "     [-writeMetamorphDataset [<file name>] (write metamorph dataset file, default=" + HoneyBee.METAMORPH_DATASET_FILE_BASENAME + ".csv)]\n" +
      "  Resume run:\n" +
      "    java morphognosis.honey_bees.Main\n" +
      "      -load <file name>\n" +
      "     [-steps <steps> | -display (default)]\n" +
      "     [-driver autopilot | metamorphs | local> (default=autopilot)]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-printCollectedNectar]\n" +
      "     [-noLearning (do not learn new metamorphs)]\n" +
      "     [-save <file name>]\n" +
      "     [-writeMetamorphDataset [<file name>] (write metamorph dataset file, default=" + HoneyBee.METAMORPH_DATASET_FILE_BASENAME + ".csv)]\n" +
      "  Print parameters:\n" +
      "    java morphognosis.honey_bees.Main -printParameters\n" +
      "  Version:\n" +
      "    java morphognosis.honey_bees.Main -version\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=error";

   // World.
   public static World world;

   // Display.
   public static WorldDisplay worldDisplay;

   // Random numbers.
   public static int          randomSeed = DEFAULT_RANDOM_SEED;
   public static SecureRandom random;

   // Reset.
   public static void reset()
   {
      random.setSeed(randomSeed);
      if (world != null)
      {
         world.reset();
      }
      if (worldDisplay != null)
      {
         worldDisplay.close();
      }
   }


   // Clear.
   public static void clear()
   {
      if (worldDisplay != null)
      {
         worldDisplay.close();
         worldDisplay = null;
      }
      world = null;
   }


   // Save to file.
   public static void save(String filename) throws IOException
   {
      DataOutputStream writer;

      try
      {
         writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(writer);
      writer.close();
   }


   // Save.
   public static void save(DataOutputStream writer) throws IOException
   {
      // Save parameters.
      Parameters.save(writer);

      // Save world.
      world.save(writer);
   }


   // Load from file.
   public static void load(String filename) throws IOException
   {
      DataInputStream reader;

      try
      {
         reader = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(reader);
      reader.close();
   }


   // Load.
   public static void load(DataInputStream reader) throws IOException
   {
      // Load parameters.
      Parameters.load(reader);

      // Load world.
      world.load(reader);
   }


   // Run.
   public static void run(int steps)
   {
      random.setSeed(randomSeed);
      if (steps >= 0)
      {
         if ((worldDisplay != null) && !updateDisplay(0, steps))
         {
            return;
         }
         for (int i = 0; i < steps; i++)
         {
            world.step();
            if ((worldDisplay != null) && !updateDisplay(i + 1, steps))
            {
               return;
            }
         }
      }
      else
      {
         for (int i = 0; updateDisplay(i, steps); i++)
         {
            world.step();
         }
      }
   }


   // Create display.
   public static void createDisplay()
   {
      if (worldDisplay == null)
      {
         worldDisplay = new WorldDisplay(world);
      }
   }


   // Destroy display.
   public static void destroyDisplay()
   {
      if (worldDisplay != null)
      {
         worldDisplay.close();
         worldDisplay = null;
      }
   }


   // Update display.
   // Return false for display quit.
   public static boolean updateDisplay(int step, int steps)
   {
      if (worldDisplay != null)
      {
         if (steps != -1)
         {
            return(worldDisplay.update(step, steps));
         }
         else
         {
            return(worldDisplay.update(step));
         }
      }
      else
      {
         return(false);
      }
   }


   // Main.
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      // Get options.
      int     steps  = -1;
      int     driver = HoneyBee.DRIVER_TYPE.AUTOPILOT.getValue();
      boolean printCollectedNectar = false;
      boolean noLearning           = false;
      String  loadfile             = null;
      String  savefile             = null;
      boolean display         = true;
      boolean gotParm         = false;
      boolean printParms      = false;
      boolean gotDatasetParm  = false;
      String  datasetFilename = HoneyBee.METAMORPH_DATASET_FILE_BASENAME + ".csv";

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               steps = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (steps < 0)
            {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
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
               display = true;
            }
            else if ((args[i] != null) && args[i].equals("false"))
            {
               display = false;
            }
            else
            {
               System.err.println("Invalid display option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-worldDimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid worldDmensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.WORLD_WIDTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid world width");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.WORLD_WIDTH < 2)
            {
               System.err.println("Invalid world width");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid worldDimensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.WORLD_HEIGHT = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid world height");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.WORLD_HEIGHT < 2)
            {
               System.err.println("Invalid world height");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-hiveRadius"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid hiveRadius option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.HIVE_RADIUS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid hive radius");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.HIVE_RADIUS < 1)
            {
               System.err.println("Invalid hive radius");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-driver"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid driver option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (args[i].equals("metamorphs"))
            {
               driver = World.DRIVER_TYPE.METAMORPHS.getValue();
            }
            else if (args[i].equals("autopilot"))
            {
               driver = World.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            else if (args[i].equals("variable"))
            {
               driver = World.DRIVER_TYPE.LOCAL.getValue();
            }
            else
            {
               System.err.println("Invalid driver option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-flowerSproutProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid flowerSproutProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.FLOWER_SPROUT_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid flowerSproutProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Parameters.FLOWER_SPROUT_PROBABILITY < 0.0f) ||
                (Parameters.FLOWER_SPROUT_PROBABILITY > 1.0f))
            {
               System.err.println("Invalid flowerSproutProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-flowerNectarCapacity"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid flowerNectarCapacity option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.FLOWER_NECTAR_CAPACITY = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid flowerNectarCapacity option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.FLOWER_NECTAR_CAPACITY < 0)
            {
               System.err.println("Invalid flowerNectarCapacity option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-flowerNectarProductionProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid flowerNectarProductionProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid flowerNectarProductionProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY < 0.0f) ||
                (Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY > 1.0f))
            {
               System.err.println("Invalid flowerNectarProductionProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-numBees"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numBees option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_BEES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numBees option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_BEES < 0)
            {
               System.err.println("Invalid numBees option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-beeTurnProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid beeTurnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.BEE_TURN_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid beeTurnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Parameters.BEE_TURN_PROBABILITY < 0.0f) ||
                (Parameters.BEE_TURN_PROBABILITY > 1.0f))
            {
               System.err.println("Invalid beeTurnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-beeReturnToHiveProbabilityIncrement"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid beeReturnToHiveProbabilityIncrement option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid beeReturnToHiveProbabilityIncrement option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT < 0.0f) ||
                (Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT > 1.0f))
            {
               System.err.println("Invalid beeReturnToHiveProbabilityIncrement option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-beeNectarDisplayDuration"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid beeDanceDuration option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.BEE_NECTAR_DISPLAY_DURATION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid beeNectarDisplayDuration option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.BEE_NECTAR_DISPLAY_DURATION < 0)
            {
               System.err.println("Invalid beeNectarDisplayDuration option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-numNeighborhoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_NEIGHBORHOODS < 0)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodInitialDimension"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Parameters.NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((Parameters.NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NEIGHBORHOOD_DIMENSION_STRIDE < 0)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.EPOCH_INTERVAL_STRIDE < 0)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.EPOCH_INTERVAL_MULTIPLIER < 0)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-equivalentMorphognosticDistance"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid equivalentMorphognosticDistance option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               HoneyBee.EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid equivalentMorphognosticDistance option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (HoneyBee.EQUIVALENT_MORPHOGNOSTIC_DISTANCE < 0.0f)
            {
               System.err.println("Invalid equivalentMorphognosticDistance option");
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
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-printCollectedNectar"))
         {
            printCollectedNectar = true;
            continue;
         }
         if (args[i].equals("-noLearning"))
         {
            noLearning = true;
            continue;
         }
         if (args[i].equals("-load"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid load option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (loadfile == null)
            {
               loadfile = args[i];
            }
            else
            {
               System.err.println("Duplicate load option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-save"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid save option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (savefile == null)
            {
               savefile = args[i];
            }
            else
            {
               System.err.println("Duplicate save option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-printParameters"))
         {
            printParms = true;
            continue;
         }
         if (args[i].equals("-writeMetamorphDataset"))
         {
            gotDatasetParm = true;
            if (i < args.length - 1)
            {
               if (!args[i + 1].startsWith("-"))
               {
                  i++;
                  datasetFilename = args[i];
               }
            }
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         if (args[i].equals("-version"))
         {
            System.out.println("HoneyBees version = " + VERSION);
            System.out.println("Morphognosis version = " + Morphognosis.VERSION);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Print parameters?
      if (printParms)
      {
         System.out.println("Parameters:");
         Parameters.print();
         System.exit(0);
      }

      // Check options.
      if ((steps == -1) && !display)
      {
         System.err.println("steps and/or display option required");
         System.err.println(Usage);
         System.exit(1);
      }
      if ((loadfile != null) && gotParm)
      {
         System.err.println(Usage);
         System.exit(1);
      }

      // Set look and feel.
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         System.err.println("Warning: cannot set look and feel");
      }

      // Initialize.
      random = new SecureRandom();
      random.setSeed(randomSeed);
      try
      {
         world            = new World(randomSeed);
         world.noLearning = noLearning;
      }
      catch (Exception e)
      {
         System.err.println("Cannot initialize world: " + e.getMessage());
         System.exit(1);
      }
      if (loadfile != null)
      {
         try
         {
            load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(1);
         }
      }

      // Set driver.
      world.setDriver(driver);

      // Create display?
      if (display)
      {
         createDisplay();
      }

      // Run.
      run(steps);
      if (printCollectedNectar)
      {
         System.out.println("Collected nectar = " + world.collectedNectar);
      }

      // Save?
      if (savefile != null)
      {
         try
         {
            save(savefile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save to file " + savefile + ": " + e.getMessage());
            System.exit(1);
         }
      }

      // Write metamorph dataset?
      if (gotDatasetParm)
      {
         try
         {
            world.writeMetamorphDataset(datasetFilename);
         }
         catch (Exception e)
         {
            System.err.println("Cannot write metamorph dataset to file " + datasetFilename + ": " + e.getMessage());
            System.exit(1);
         }
      }
      System.exit(0);
   }
}
