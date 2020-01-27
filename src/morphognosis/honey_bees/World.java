// For conditions of distribution and use, see copyright notice in Main.java

// World.

package morphognosis.honey_bees;

import java.security.SecureRandom;
import java.util.ArrayList;

import morphognosis.Metamorph;
import morphognosis.Orientation;
import morphognosis.Utility;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class World
{
   // Cells.
   public Cell[][]        cells;

   // Honey bees.
   public HoneyBee[] bees;

   // Collected nectar.
   public int collectedNectar;

   // Random numbers.
   public SecureRandom random;
   public int          randomSeed;

   // Driver.
   int driver;

   // No metamorph learning?
   public boolean noLearning;

   // Metamorph neural network.
   public MetamorphNN metamorphNN;

   // Constructor.
   public World(int randomSeed)
   {
      // Random numbers.
      random          = new SecureRandom();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);

      // Create cells.
      double cx = Parameters.WORLD_WIDTH / 2.0;
      double cy = Parameters.WORLD_HEIGHT / 2.0;
      cells = new Cell[Parameters.WORLD_WIDTH][Parameters.WORLD_HEIGHT];
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y] = new Cell();
            if (Math.sqrt(((double)y - cy) * ((double)y - cy) + ((double)x - cx) * (
                             (double)x - cx)) <= (double)Parameters.HIVE_RADIUS)
            {
               cells[x][y].hive = true;
            }
         }
      }

      // Create flowers.
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            Cell cell = cells[x][y];
            if (!cell.hive && (cell.bee == null))
            {
               if (random.nextFloat() < Parameters.FLOWER_SPROUT_PROBABILITY)
               {
                  Flower flower = new Flower();
                  cell.flower = flower;
                  if ((Parameters.FLOWER_NECTAR_CAPACITY > 0) &&
                      (random.nextFloat() < Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY))
                  {
                     flower.nectar = 1;
                  }
               }
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

      // Initialize driver.
      driver = Driver.AUTOPILOT;
   }


   // Reset.
   public void reset()
   {
      random = new SecureRandom();
      random.setSeed(randomSeed);
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            cells[x][y].clear();
         }
      }
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            Cell cell = cells[x][y];
            if (!cell.hive && (cell.bee == null))
            {
               if (random.nextFloat() < Parameters.FLOWER_SPROUT_PROBABILITY)
               {
                  Flower flower = new Flower();
                  cell.flower = flower;
                  if ((Parameters.FLOWER_NECTAR_CAPACITY > 0) &&
                      (random.nextFloat() < Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY))
                  {
                     flower.nectar = 1;
                  }
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
      collectedNectar = 0;
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
            if ((cells[x][y].flower != null) && (cells[x][y].bee == null))
            {
               if ((cells[x][y].flower.nectar < Parameters.FLOWER_NECTAR_CAPACITY) &&
                   (random.nextFloat() < Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY))
               {
                  cells[x][y].flower.nectar++;
               }
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
         // Update landmarks.
         HoneyBee bee = bees[i];
         bee.landmarkMap[bee.x][bee.y] = true;

         // Cycle bee.
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
               if ((cells[bee.x][bee.y].flower != null) &&
                   (cells[bee.x][bee.y].flower.nectar > 0))
               {
                  bee.nectarCarry = true;
                  cells[bee.x][bee.y].flower.nectar--;
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
         if ((responses[n] >= HoneyBee.DISPLAY_NECTAR_DISTANCE) && (responses[n] < HoneyBee.WAIT))
         {
            bee.nectarDistanceDisplay = responses[n] - HoneyBee.DISPLAY_NECTAR_DISTANCE;
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

      // Get hive and nectar sensors.
      if (cells[bee.x][bee.y].hive)
      {
         sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 1.0f;
      }
      else
      {
         sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 0.0f;
      }
      if ((cells[bee.x][bee.y].flower != null) && (cells[bee.x][bee.y].flower.nectar > 0))
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


   // Train metamorph neural network.
   public void trainMetamorphNN()
   {
      // Create cumulative list of metamorphs.
      ArrayList<Metamorph> metamorphs = new ArrayList<Metamorph>();
      for (HoneyBee bee : bees)
      {
         for (Metamorph metamorph : bee.metamorphs)
         {
            metamorphs.add(metamorph);
         }
      }

      // Create and train neural network.
      metamorphNN = new MetamorphNN(random);
      metamorphNN.train(metamorphs);

      // Distribute model to bees.
      for (HoneyBee bee : bees)
      {
         bee.metamorphNN = metamorphNN;
      }
   }


   // Save metamorph neural network.
   public void saveMetamorphNN(String filename)
   {
      if (metamorphNN != null)
      {
         metamorphNN.saveModel(filename);
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
      if (bees != null)
      {
         for (int i = 0; i < bees.length; i++)
         {
            if (bees[i] != null)
            {
               bees[i].metamorphs.clear();
            }
         }
      }
   }


   // Write metamorph dataset.
   public void writeMetamorphDataset(String filename) throws Exception
   {
      if (bees != null)
      {
         boolean append = false;
         for (int i = 0; i < bees.length; i++)
         {
            if (bees[i] != null)
            {
               bees[i].writeMetamorphDataset(filename, append);
               append = true;
            }
         }
      }
   }
}
