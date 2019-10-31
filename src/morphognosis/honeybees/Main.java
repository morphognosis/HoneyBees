/*
 * Copyright (c) 2019 Tom Portegys (portegys@gmail.com). All rights reserved.
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

package morphognosis.honeybees;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.swing.UIManager;

import morphognosis.Morphognosis;
import morphognosis.Morphognostic;
import morphognosis.Orientation;

public class Main
{
   // Version.
   public static final String VERSION = "1.0";

   // Nest image.
   public static final String NEST_IMAGE_FILE = "pufferfish_nest.png";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java morphognosis.pufferfish.Main\n" +
      "      [-steps <steps> | -display (default)]\n" +
      "      Nest properties:\n" +
      "        [-nestDimensions <width> <height> (default=" + Nest.WIDTH + " " + Nest.HEIGHT + ")]\n" +
      "        [-maxElevation <quantity> (default=" + Nest.MAX_ELEVATION + ")]\n" +
      "        [-centerRadius <quantity> (default=" + Nest.CENTER_RADIUS + ")]\n" +
      "        [-numSpokes <quantity> (default=" + Nest.NUM_SPOKES + ")]\n" +
      "        [-spokeLength <quantity> (default=" + Nest.SPOKE_LENGTH + ")]\n" +
      "        [-spokeRippleLength <quantity> (default=" + Nest.SPOKE_RIPPLE_LENGTH + ")]\n" +
      "      Morphognosis parameters:\n" +
      "        [-numNeighborhoods <quantity> (default=" + Morphognostic.DEFAULT_NUM_NEIGHBORHOODS + ")]\n" +
      "        [-neighborhoodInitialDimension <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "        [-neighborhoodDimensionStride <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "        [-neighborhoodDimensionMultiplier <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "        [-epochIntervalStride <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE + ")]\n" +
      "        [-epochIntervalMultiplier <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "        [-equivalentMorphognosticDistance <distance> (default=" + Pufferfish.EQUIVALENT_MORPHOGNOSTIC_DISTANCE + ")]\n" +
      "     [-driver <metamorphRules | autopilot> (pufferfish driver: default=autopilot)]\n" +
      "     [-randomSeed <random number seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-save <file name>]\n" +
      "     [-print (print parameters and properties)]\n" +
      "     [-writeMetamorphDataset <file name> (write metamorph dataset file, default=" + Pufferfish.DATASET_FILE_NAME + ")]\n" +
      "  Resume run:\n" +
      "    java morphognosis.pufferfish.Main\n" +
      "      -load <file name>\n" +
      "     [-steps <steps> | -display (default)]\n" +
      "     [-driver <metamorphRules | autopilot> (default=autopilot)]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "     [-print (print parameters and properties)]\n" +
      "     [-writeMetamorphDataset <file name> (write metamorph dataset file, default=" + Pufferfish.DATASET_FILE_NAME + ")]\n" +
      "  Version:\n" +
      "    java morphognosis.pufferfish.Main -version\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=error";

   // Pufferfish.
   public Pufferfish pufferfish;

   // Nest.
   public Nest nest;

   // Display.
   public NestDisplay display;

   // Random numbers.
   public int          randomSeed;
   public SecureRandom random;

   // Previous response.
   public static int previousResponse = Pufferfish.WAIT;

   // Constructor.
   public Main(int randomSeed)
   {
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
   }


   // Initialize.
   public void init(int NUM_NEIGHBORHOODS,
                    int NEIGHBORHOOD_INITIAL_DIMENSION,
                    int NEIGHBORHOOD_DIMENSION_STRIDE,
                    int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                    int EPOCH_INTERVAL_STRIDE,
                    int EPOCH_INTERVAL_MULTIPLIER)
   {
      // Create nest.
      nest = new Nest(randomSeed);

      // Create pufferfish.
      pufferfish = new Pufferfish(nest, randomSeed,
                                  NUM_NEIGHBORHOODS,
                                  NEIGHBORHOOD_INITIAL_DIMENSION,
                                  NEIGHBORHOOD_DIMENSION_STRIDE,
                                  NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                  EPOCH_INTERVAL_STRIDE,
                                  EPOCH_INTERVAL_MULTIPLIER);
   }


   // Reset.
   public void reset()
   {
      random.setSeed(randomSeed);
      if (nest != null)
      {
         nest.restore();
      }
      if (pufferfish != null)
      {
         pufferfish.reset();
      }
      if (display != null)
      {
         display.close();
      }
      previousResponse = Pufferfish.WAIT;
   }


   // Clear.
   public void clear()
   {
      if (display != null)
      {
         display.close();
         display = null;
      }
      nest       = null;
      pufferfish = null;
   }


   // Save to file.
   public void save(String filename) throws IOException
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
   public void save(DataOutputStream writer) throws IOException
   {
      // Save cells.
      nest.save(writer);

      // Save pufferfish.
      pufferfish.save(writer);
   }


   // Load from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try
      {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load.
   public void load(FileInputStream input) throws IOException
   {
      // Load cells.
      nest = new Nest();
      nest.load(input);

      // Load pufferfish.
      pufferfish = new Pufferfish(nest, randomSeed);
      pufferfish.load(input);
   }


   // Run.
   public void run(int steps)
   {
      random.setSeed(randomSeed);
      if (steps >= 0)
      {
         for ( ; steps > 0; steps--)
         {
            stepPufferfish();
         }
      }
      else
      {
         for (int i = 0; updateDisplay(i); i++)
         {
            stepPufferfish();
         }
      }
   }


   // Step pufferfish.
   public void stepPufferfish()
   {
      int x, y, toX, toY, width, height;

      float[] sensors = new float[Pufferfish.NUM_SENSORS];

      width  = nest.size.width;
      height = nest.size.height;

      // Update landmarks.
      pufferfish.landmarkMap[pufferfish.x][pufferfish.y] = true;

      // Initialize sensors.
      toX = toY = 0;
      for (int i = 0, j = Pufferfish.NUM_SENSORS - 1; i < j; i++)
      {
         x = pufferfish.x;
         y = pufferfish.y;
         switch (i)
         {
         case 0:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               x--;
               if (x < 0) { x += width; }
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               y = ((y + 1) % height);
               break;

            case Orientation.SOUTH:
               x = ((x + 1) % width);
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               y--;
               if (y < 0) { y += height; }
               break;
            }
            break;

         case 1:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               break;

            case Orientation.SOUTH:
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               break;
            }
            toX = x;
            toY = y;
            break;

         case 2:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               x = ((x + 1) % width);
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.SOUTH:
               x--;
               if (x < 0) { x += width; }
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               y = ((y + 1) % height);
               break;
            }
            break;
         }
         if (Pufferfish.IGNORE_ELEVATION_SENSOR_VALUES)
         {
            sensors[i] = 0.0f;
         }
         else
         {
            sensors[i] = (float)nest.cells[x][y][Nest.ELEVATION_CELL_INDEX];
         }
      }
      sensors[Pufferfish.PREVIOUS_RESPONSE_INDEX] = (float)previousResponse;

      // Cycle pufferfish.
      previousResponse = pufferfish.response;
      int response = pufferfish.cycle(sensors);

      // Process response.
      switch (response)
      {
      case Pufferfish.FORWARD:
         pufferfish.x = toX;
         pufferfish.y = toY;
         break;

      case Pufferfish.TURN_LEFT:
         pufferfish.orientation--;
         if (pufferfish.orientation < 0)
         {
            pufferfish.orientation += Orientation.NUM_ORIENTATIONS;
         }
         break;

      case Pufferfish.TURN_RIGHT:
         pufferfish.orientation = (pufferfish.orientation + 1) %
                                  Orientation.NUM_ORIENTATIONS;
         break;

      case Pufferfish.SMOOTH:
         nest.smooth(pufferfish.x, pufferfish.y, toX, toY);
         break;

      case Pufferfish.RAISE:
         nest.cells[pufferfish.x][pufferfish.y][Nest.ELEVATION_CELL_INDEX] = Nest.MAX_ELEVATION;
         break;

      case Pufferfish.LOWER:
         nest.cells[pufferfish.x][pufferfish.y][Nest.ELEVATION_CELL_INDEX] = 2;
         break;
      }
   }


   // Create display.
   public void createDisplay()
   {
      if (display == null)
      {
         display = new NestDisplay(nest, pufferfish, randomSeed);
      }
   }


   // Destroy display.
   public void destroyDisplay()
   {
      if (display != null)
      {
         display.close();
         display = null;
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
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      // Get options.
      int     steps             = -1;
      int     driver            = Pufferfish.DRIVER_TYPE.AUTOPILOT.getValue();
      int     randomSeed        = DEFAULT_RANDOM_SEED;
      String  loadfile          = null;
      String  savefile          = null;
      boolean display           = false;
      boolean gotParm           = false;
      boolean printParm         = false;
      boolean gotDatasetParm    = false;
      int     NUM_NEIGHBORHOODS = Morphognostic.DEFAULT_NUM_NEIGHBORHOODS;
      int     NEIGHBORHOOD_INITIAL_DIMENSION    = Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
      int     NEIGHBORHOOD_DIMENSION_STRIDE     = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
      int     NEIGHBORHOOD_DIMENSION_MULTIPLIER = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      int     EPOCH_INTERVAL_STRIDE             = Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE;
      int     EPOCH_INTERVAL_MULTIPLIER         = Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

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
            display = true;
            continue;
         }
         if (args[i].equals("-nestDimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid nestDmensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.WIDTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid nest width");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.WIDTH < 2)
            {
               System.err.println("Invalid nest width");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid nestDimensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.HEIGHT = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid nest height");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.HEIGHT < 2)
            {
               System.err.println("Invalid nest height");
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
            if (args[i].equals("metamorphRules"))
            {
               driver = Pufferfish.DRIVER_TYPE.METAMORPH_RULES.getValue();
            }
            else if (args[i].equals("autopilot"))
            {
               driver = Pufferfish.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            else
            {
               System.err.println("Invalid driver option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxElevation"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.MAX_ELEVATION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.MAX_ELEVATION < 0)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-centerRadius"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid centerRadius option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.CENTER_RADIUS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid centerRadius option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.CENTER_RADIUS <= 0)
            {
               System.err.println("Invalid centerRadius option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-numSpokes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numSpokes option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.NUM_SPOKES = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numSpokes option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.NUM_SPOKES < 0)
            {
               System.err.println("Invalid numSpokes option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-spokeLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid spokeLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.SPOKE_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid spokeLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.SPOKE_LENGTH < 0)
            {
               System.err.println("Invalid spokeLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-spokeRippleLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid spokeRippleLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.SPOKE_RIPPLE_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid spokeRippleLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.SPOKE_RIPPLE_LENGTH < 0)
            {
               System.err.println("Invalid spokeRippleLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((Nest.SPOKE_RIPPLE_LENGTH % 2) == 1)
            {
               System.err.println("Invalid spokeRippleLength option: must be even");
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
               NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_NEIGHBORHOODS < 0)
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
               NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
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
               NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NEIGHBORHOOD_DIMENSION_STRIDE < 0)
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
               NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
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
               EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCH_INTERVAL_STRIDE < 0)
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
               EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCH_INTERVAL_MULTIPLIER < 0)
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
               Pufferfish.EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid equivalentMorphognosticDistance option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Pufferfish.EQUIVALENT_MORPHOGNOSTIC_DISTANCE < 0.0f)
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
         if (args[i].equals("-print"))
         {
            printParm = true;
            continue;
         }
         if (args[i].equals("-writeMetamorphDataset"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid writeMetamorphDataset option");
               System.err.println(Usage);
               System.exit(1);
            }
            Pufferfish.DATASET_FILE_NAME = args[i];
            gotDatasetParm = true;
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         if (args[i].equals("-version"))
         {
            System.out.println("Pufferfish version = " + VERSION);
            System.out.println("Morphognosis version = " + Morphognosis.VERSION);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Check options.
      if ((steps != -1) && display)
      {
         System.err.println(Usage);
         System.exit(1);
      }
      else if ((steps == -1) && !display)
      {
         display = true;
      }
      if (!display)
      {
         if (driver == Pufferfish.DRIVER_TYPE.MANUAL.getValue())
         {
            System.err.println("Cannot run manually without display");
            System.err.println(Usage);
            System.exit(1);
         }
      }
      if ((loadfile != null) && gotParm)
      {
         System.err.println(Usage);
         System.exit(1);
      }

      // Set look and feel.
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         System.err.println("Warning: cannot set look and feel");
      }

      // Create world.
      Main main = new Main(randomSeed);
      if (loadfile != null)
      {
         try
         {
            main.load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(1);
         }
      }
      else
      {
         try
         {
            main.init(NUM_NEIGHBORHOODS,
                      NEIGHBORHOOD_INITIAL_DIMENSION,
                      NEIGHBORHOOD_DIMENSION_STRIDE,
                      NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                      EPOCH_INTERVAL_STRIDE,
                      EPOCH_INTERVAL_MULTIPLIER);
         }
         catch (Exception e)
         {
            System.err.println("Cannot initialize: " + e.getMessage());
            System.exit(1);
         }
      }

      // Print parameters and properties?
      if (printParm)
      {
         System.out.println("Morphognosis parameters:");
         main.pufferfish.morphognostic.printParameters();
         main.nest.printProperties();
      }

      // Create display?
      if (display)
      {
         main.createDisplay();
      }
      else
      {
         main.reset();
      }

      // Set pufferfish driver.
      main.pufferfish.driver = driver;

      // Run.
      main.run(steps);

      // Save?
      if (savefile != null)
      {
         try
         {
            main.save(savefile);
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
            main.pufferfish.writeMetamorphDataset();
         }
         catch (Exception e)
         {
            System.err.println("Cannot write metamorph dataset to file " + Pufferfish.DATASET_FILE_NAME + ": " + e.getMessage());
            System.exit(1);
         }
      }
      System.exit(0);
   }
}
