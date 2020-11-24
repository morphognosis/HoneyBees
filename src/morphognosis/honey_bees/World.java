// For conditions of distribution and use, see copyright notice in Main.java

// World.

package morphognosis.honey_bees;

import java.util.ArrayList;
import java.util.Random;

import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Orientation;
import morphognosis.Utility;
import morphognosis.Morphognostic.Neighborhood;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class World
{
   // Cells.
   public Cell[][]        cells;

   // Honey bees.
   public HoneyBee[] bees;

   // Collected nectar.
   public int collectedNectar;

   // Random numbers.
   public Random random;
   public int    randomSeed;

   // Driver.
   int driver;

   // Metamorphs.
   public int currentMetamorphIdx;
   public ArrayList<Metamorph> metamorphs;

   // Metamorph neural network.
   public MetamorphNN metamorphNN;

   // Metamorph dataset file name.
   public static String METAMORPH_DATASET_FILE_BASENAME = "metamorphs";
   public String        metamorphDatasetFilename;

   // Maximum distance between equivalent morphognostics.
   public static float EQUIVALENT_MORPHOGNOSTIC_DISTANCE = 0.0f;

   // Constructor.
   public World(int randomSeed)
   {
      // Random numbers.
      random          = new Random();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);

      // Create cells.
      int cx = Parameters.WORLD_WIDTH / 2;
      int cy = Parameters.WORLD_HEIGHT / 2;
      cells = new Cell[Parameters.WORLD_WIDTH][Parameters.WORLD_HEIGHT];
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y] = new Cell(random);
            if ((Math.abs(x - cx) + Math.abs(y - cy)) <= Parameters.HIVE_RADIUS)
            {
               cells[x][y].hive = true;
            }
         }
      }

      // Create flowers.
      for (int i = 0; i < Parameters.NUM_FLOWERS; i++)
      {
         for (int j = 0; j < 100; j++)
         {
            int x = random.nextInt(Parameters.WORLD_WIDTH);
            int y = random.nextInt(Parameters.WORLD_HEIGHT);
            if (Math.sqrt(((double)y - cy) * ((double)y - cy) + ((double)x - cx) * (
                             (double)x - cx)) <= (double)Parameters.FLOWER_RANGE)
            {
               Cell cell = cells[x][y];
               if (!cell.hive && (cell.bee == null))
               {
                  Flower flower = new Flower(true, random);
                  cell.flower = flower;
                  break;
               }
            }
            if (j == 99)
            {
               System.err.println("Cannot place flower in world");
               System.exit(1);
            }
         }
      }

      // Create bees.
      bees = new HoneyBee[Parameters.NUM_BEES];
      for (int i = 0; i < Parameters.NUM_BEES; i++)
      {
         bees[i] = new HoneyBee(i + 1, this, random);
      }
      for (int i = 0; i < Parameters.NUM_BEES; i++)
      {
         HoneyBee bee = bees[i];
         float[] sensors = getSensors(bee);
         for (int j = 0; j < HoneyBee.NUM_SENSORS; j++)
         {
            bee.sensors[j] = sensors[j];
         }
      }

      // Nectar collector.
      collectedNectar = 0;

      // Create metamorphs.
      currentMetamorphIdx = -1;
      metamorphs          = new ArrayList<Metamorph>();

      // Initialize driver.
      driver = Driver.AUTOPILOT;
   }


   // Reset.
   public void reset()
   {
      random = new Random();
      random.setSeed(randomSeed);
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y].clear();
         }
      }
      double cx = Parameters.WORLD_WIDTH / 2.0;
      double cy = Parameters.WORLD_HEIGHT / 2.0;
      for (int i = 0; i < Parameters.NUM_FLOWERS; i++)
      {
         for (int j = 0; j < 100; j++)
         {
            int x = random.nextInt(Parameters.WORLD_WIDTH);
            int y = random.nextInt(Parameters.WORLD_HEIGHT);
            if (Math.sqrt(((double)y - cy) * ((double)y - cy) + ((double)x - cx) * (
                             (double)x - cx)) <= (double)Parameters.FLOWER_RANGE)
            {
               Cell cell = cells[x][y];
               if (!cell.hive && (cell.bee == null))
               {
                  Flower flower = new Flower(true, random);
                  cell.flower = flower;
                  break;
               }
            }
         }
      }
      if (bees != null)
      {
         for (int i = 0; i < Parameters.NUM_BEES; i++)
         {
            bees[i].reset();
         }
      }
      collectedNectar     = 0;
      currentMetamorphIdx = -1;
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
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y].save(writer);
         }
      }
      for (int i = 0; i < Parameters.NUM_BEES; i++)
      {
         bees[i].save(writer);
      }
      Utility.saveInt(writer, currentMetamorphIdx);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(writer);
      }
      Utility.saveFloat(writer, EQUIVALENT_MORPHOGNOSTIC_DISTANCE);
      Utility.saveInt(writer, collectedNectar);
      writer.flush();
   }


   // Load from file..
   public void load(String filename) throws IOException
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
   public void load(DataInputStream reader) throws IOException
   {
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y].load(reader);
         }
      }
      for (int i = 0; i < Parameters.NUM_BEES; i++)
      {
         bees[i].load(reader);
      }
      currentMetamorphIdx = Utility.loadInt(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Utility.loadFloat(reader);
      collectedNectar = Utility.loadInt(reader);
   }


   // Step world.
   public void step()
   {
      stepFlowers();
      stepBees();
   }


   // Step flowers.
   public void stepFlowers()
   {
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            if (cells[x][y].flower != null)
            {
               cells[x][y].flower.regenerateNectar();
            }
         }
      }
   }


   // Step bees.
   public void stepBees()
   {
      int width  = Parameters.WORLD_WIDTH;
      int height = Parameters.WORLD_HEIGHT;

      // Run sensory-response cycles.
      int responses[] = new int[Parameters.NUM_BEES];

      for (int i = 0; i < Parameters.NUM_BEES; i++)
      {
         HoneyBee bee = bees[i];
         responses[i] = bee.cycle(getSensors(bee));
      }

      // Execute responses in random order.
      int n = random.nextInt(Parameters.NUM_BEES);
      for (int i = 0; i < Parameters.NUM_BEES; i++, n = (n + 1) % Parameters.NUM_BEES)
      {
         HoneyBee bee = bees[n];
         if (responses[n] < Orientation.NUM_ORIENTATIONS)
         {
            bee.orientation = responses[n];
         }
         else
         {
            switch (responses[n])
            {
            case HoneyBee.FORWARD:
               if ((bee.toX >= 0) && (bee.toX < width) &&
                   (bee.toY >= 0) && (bee.toY < height))
               {
                  if (cells[bee.toX][bee.toY].bee == null)
                  {
                     cells[bee.x][bee.y].bee = null;
                     bee.x = bee.toX;
                     bee.y = bee.toY;
                     cells[bee.toX][bee.toY].bee = bee;
                  }
               }
               else
               {
                  // Override physically impossible forward movement with turn.
                  bee.response = bee.orientation = random.nextInt(Orientation.NUM_ORIENTATIONS);
               }
               break;

            case HoneyBee.EXTRACT_NECTAR:
               if ((cells[bee.x][bee.y].flower != null) && (cells[bee.x][bee.y].flower.nectar))
               {
                  bee.nectarCarry = true;
                  cells[bee.x][bee.y].flower.extractNectar();
               }
               break;

            case HoneyBee.DEPOSIT_NECTAR:
               if (bee.nectarCarry && cells[bee.x][bee.y].hive)
               {
                  collectedNectar++;
               }
               bee.nectarCarry = false;
               break;
            }
         }
         if (responses[n] == HoneyBee.DISPLAY_NECTAR_LONG_DISTANCE)
         {
            bee.nectarDistanceDisplay = 0;
         }
         else if (responses[n] == HoneyBee.DISPLAY_NECTAR_SHORT_DISTANCE)
         {
            bee.nectarDistanceDisplay = 1;
         }
         else
         {
            bee.nectarDistanceDisplay = -1;
         }
      }
   }


   // Get bee sensors.
   public float[] getSensors(HoneyBee bee)
   {
      float[] sensors = new float[HoneyBee.NUM_SENSORS];

      // Get hive and nectar sensors.
      if (cells[bee.x][bee.y].hive)
      {
         sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 1.0f;
      }
      else
      {
         sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 0.0f;
      }
      if ((cells[bee.x][bee.y].flower != null) && (cells[bee.x][bee.y].flower.nectar))
      {
         sensors[HoneyBee.NECTAR_PRESENCE_INDEX] = 1.0f;
      }
      else
      {
         sensors[HoneyBee.NECTAR_PRESENCE_INDEX] = 0.0f;
      }

      // If in hive, check for dancing bee nectar direction and distance display.
      sensors[HoneyBee.NECTAR_DANCE_DIRECTION_INDEX] = -1.0f;
      sensors[HoneyBee.NECTAR_DANCE_DISTANCE_INDEX]  = -1.0f;
      if (cells[bee.x][bee.y].hive && !bee.nectarCarry)
      {
         int i = 0;
         for ( ; i < Parameters.NUM_BEES; i++)
         {
            if (bees[i] == bee) { break; }
         }
         for (int k = 0, j = i; k < Parameters.NUM_BEES; k++, j = (j + 1) % Parameters.NUM_BEES)
         {
            HoneyBee dancingBee = bees[j];
            if (cells[dancingBee.x][dancingBee.y].hive && (dancingBee.nectarDistanceDisplay != -1))
            {
               sensors[HoneyBee.NECTAR_DANCE_DIRECTION_INDEX] = (float)dancingBee.orientation;
               sensors[HoneyBee.NECTAR_DANCE_DISTANCE_INDEX]  = (float)dancingBee.nectarDistanceDisplay;
               break;
            }
         }
      }

      // Determine forward cell.
      int toX = bee.x;
      int toY = bee.y;
      switch (bee.orientation)
      {
      case Orientation.NORTH:
         toY++;
         break;

      case Orientation.NORTHEAST:
         toX++;
         toY++;
         break;

      case Orientation.EAST:
         toX++;
         break;

      case Orientation.SOUTHEAST:
         toX++;
         toY--;
         break;

      case Orientation.SOUTH:
         toY--;
         break;

      case Orientation.SOUTHWEST:
         toX--;
         toY--;
         break;

      case Orientation.WEST:
         toX--;
         break;

      case Orientation.NORTHWEST:
         toX--;
         toY++;
         break;
      }
      bee.toX = toX;
      bee.toY = toY;

      return(sensors);
   }


   // Set bee drivers.
   public void setDriver(int driver)
   {
      this.driver = driver;
      if (driver != Driver.LOCAL_OVERRIDE)
      {
         if (bees != null)
         {
            for (int i = 0; i < bees.length; i++)
            {
               if (bees[i] != null)
               {
                  bees[i].driver = driver;
               }
            }
         }
      }
   }


   // Update metamorphs.
   public void updateMetamorphs(Morphognostic morphognostic, int response, float goalValue)
   {
      Metamorph metamorph = new Metamorph(morphognostic.clone(), response,
                                          goalValue, HoneyBee.getResponseName(response));

      metamorph.morphognostic.orientation = Orientation.NORTH;
      int foundIdx = -1;
      for (int i = 0, j = metamorphs.size(); i < j; i++)
      {
         Metamorph m = metamorphs.get(i);
         if (m.morphognostic.compare(metamorph.morphognostic) <= EQUIVALENT_MORPHOGNOSTIC_DISTANCE)
         {
            foundIdx = i;
            break;
         }
      }
      if (foundIdx == -1)
      {
         metamorphs.add(metamorph);
         foundIdx = metamorphs.size() - 1;
      }
      if (currentMetamorphIdx != -1)
      {
         Metamorph currentMetamorph = metamorphs.get(currentMetamorphIdx);
         for (int i = 0, j = currentMetamorph.effectIndexes.size(); i < j; i++)
         {
            if (currentMetamorph.effectIndexes.get(i) == foundIdx)
            {
               foundIdx = -1;
               break;
            }
         }
         if (foundIdx != -1)
         {
            currentMetamorph.effectIndexes.add(foundIdx);
            metamorphs.get(foundIdx).causeIndexes.add(currentMetamorphIdx);

            // Propagate goal value.
            propagateGoalValue(currentMetamorph, metamorphs.get(foundIdx).goalValue);
         }
      }
      currentMetamorphIdx = foundIdx;
   }


   // Propagate goal value.
   public void propagateGoalValue(Metamorph metamorph, float effectGoalValue)
   {
      float v = effectGoalValue * HoneyBee.GOAL_VALUE_DISCOUNT_FACTOR;

      if (v > metamorph.goalValue)
      {
         metamorph.goalValue = v;
         for (int i = 0, j = metamorph.causeIndexes.size(); i < j; i++)
         {
            propagateGoalValue(metamorphs.get(metamorph.causeIndexes.get(i)), v);
         }
      }
   }


   // Train metamorph neural network.
   public void trainMetamorphNN()
   {
      metamorphNN = new MetamorphNN(random);
      metamorphNN.train(metamorphs);
   }


   // Save metamorph neural network.
   public void saveMetamorphNN(String filename)
   {
      if (metamorphNN != null)
      {
         metamorphNN.saveModel(filename);
      }
      else
      {
         System.err.println("Cannot save null metamorph neural network to file " + filename);
      }
   }


   // Load metamorph neural network.
   public void loadMetamorphNN(String filename)
   {
      if (metamorphNN == null)
      {
         metamorphNN = new MetamorphNN(random);
      }
      metamorphNN.loadModel(filename);
   }


   // Clear metamorphs.
   public void clearMetamorphs()
   {
      metamorphs.clear();
      currentMetamorphIdx = -1;
   }


   // Write metamporph dataset.
   public void writeMetamorphDataset(String filename) throws Exception
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      if (metamorphs.size() > 0)
      {
         Morphognostic morphognostic = metamorphs.get(0).morphognostic;
         String        oldlinesep    = System.getProperty("line.separator");
         System.setProperty("line.separator", "\n");
         PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
         for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
         {
            int n = morphognostic.neighborhoods.get(i).sectors.length;
            for (int x = 0; x < n; x++)
            {
               for (int y = 0; y < n; y++)
               {
                  for (int d = 0; d < morphognostic.eventDimensions; d++)
                  {
                     for (int j = 0; j < morphognostic.eventValueDimensions[d]; j++)
                     {
                        writer.print(i + "-" + x + "-" + y + "-" + d + "-" + j + ",");
                     }
                  }
               }
            }
         }
         writer.println("response");
         for (Metamorph m : metamorphs)
         {
            writer.println(morphognostic2csv(m.morphognostic) + "," + m.response);
         }
         writer.flush();
         writer.close();
         System.setProperty("line.separator", oldlinesep);
      }
      output.close();
   }


   // Flatten morphognostic to csv string.
   public String morphognostic2csv(Morphognostic morphognostic)
   {
      String  output    = "";
      boolean skipComma = true;
      int     dx        = 0;

      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood neighborhood = morphognostic.neighborhoods.get(i);
         float[][][] densities = neighborhood.rectifySectorValueDensities();
         int n = neighborhood.sectors.length;
         for (int j = 0, j2 = n * n; j < j2; j++)
         {
            for (int d = dx, d2 = morphognostic.eventDimensions; d < d2; d++)
            {
               for (int k = 0, k2 = morphognostic.eventValueDimensions[d]; k < k2; k++)
               {
                  if (skipComma)
                  {
                     skipComma = false;
                  }
                  else
                  {
                     output += ",";
                  }
                  output += (densities[j][d][k] + "");
               }
            }
         }
      }
      return(output);
   }
}
