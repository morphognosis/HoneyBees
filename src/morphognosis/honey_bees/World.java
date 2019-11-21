// For conditions of distribution and use, see copyright notice in Main.java

// World.

package morphognosis.honey_bees;

import java.security.SecureRandom;

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
            if (cells[x][y].flower != null)
            {
               if (random.nextFloat() < Parameters.FLOWER_DEATH_PROBABILITY)
               {
                  cells[x][y].flower = null;
               }
               else
               {
                  if ((cells[x][y].flower.nectar < Parameters.FLOWER_NECTAR_CAPACITY) &&
                      (random.nextFloat() < Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY))
                  {
                     cells[x][y].flower.nectar++;
                  }
               }
            }
            else
            {
               if (!cells[x][y].hive && (cells[x][y].bee == null))
               {
                  if (random.nextFloat() < Parameters.FLOWER_SPROUT_PROBABILITY)
                  {
                     cells[x][y].flower = new Flower();
                  }
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

      // Run bees in random starting order.
      int n = random.nextInt(Parameters.NUM_BEES);

      for (int i = 0; i < Parameters.NUM_BEES; i++, n = (n + 1) % Parameters.NUM_BEES)
      {
         // Update landmarks.
         HoneyBee bee = bees[n];
         bee.landmarkMap[bee.x][bee.y] = true;

         // Cycle bee.
         int response = bee.cycle(getSensors(bee));

         // Process response.
         if (response < Compass.NUM_POINTS)
         {
            bee.orientation = response;
         }
         else
         {
            switch (response)
            {
            case HoneyBee.FORWARD:
               if ((bee.toX >= 0) && (bee.toX < width) && (bee.toY >= 0) && (bee.toY < height) &&
                   (cells[bee.toX][bee.toY].bee == null))
               {
                  cells[bee.x][bee.y].bee = null;
                  bee.x = bee.toX;
                  bee.y = bee.toY;
                  cells[bee.toX][bee.toY].bee = bee;
               }
               break;

            case HoneyBee.EXTRACT_NECTAR:
               if ((bee.toX >= 0) && (bee.toX < width) && (bee.toY >= 0) && (bee.toY < height) &&
                   (cells[bee.toX][bee.toY].flower.nectar > 0) && !bee.nectarCarry)
               {
                  cells[bee.toX][bee.toY].flower.nectar--;
               }
               break;

            case HoneyBee.DEPOSIT_NECTAR:
               if (cells[bee.x][bee.y].hive && bee.nectarCarry)
               {
                  collectedNectar++;
               }
               break;
            }
         }
      }
   }


   // Get bee sensors.
   public float[] getSensors(HoneyBee bee)
   {
      float[] sensors = new float[HoneyBee.NUM_SENSORS];
      int width  = Parameters.WORLD_WIDTH;
      int height = Parameters.WORLD_HEIGHT;

      int toX = bee.x;
      int toY = bee.y;
      switch (bee.orientation)
      {
      case Compass.NORTH:
         toY++;
         break;

      case Compass.NORTHEAST:
         toX++;
         toY++;
         break;

      case Compass.EAST:
         toX++;
         break;

      case Compass.SOUTHEAST:
         toX++;
         toY--;
         break;

      case Compass.SOUTH:
         toY--;
         break;

      case Compass.SOUTHWEST:
         toX--;
         toY--;
         break;

      case Compass.WEST:
         toX--;
         break;

      case Compass.NORTHWEST:
         toX--;
         toY++;
         break;
      }
      bee.toX = toX;
      bee.toY = toY;
      if ((toX >= 0) && (toX < width) && (toY >= 0) && (toY < height))
      {
         if (cells[toX][toY].hive)
         {
            sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 1.0f;
         }
         else
         {
            sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 0.0f;
         }
         if (cells[toX][toY].flower != null)
         {
            sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX] = (float)cells[toX][toY].flower.nectar;
         }
         else
         {
            sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX] = 0.0f;
         }
         if (cells[toX][toY].bee != null)
         {
            sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX]     = (float)cells[toX][toY].bee.orientation;
            sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX] = (float)cells[toX][toY].bee.nectarDistanceDisplay;
         }
         else
         {
            sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX]     = -1.0f;
            sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX] = -1.0f;
         }
      }
      else
      {
         sensors[HoneyBee.HIVE_PRESENCE_INDEX] = 0.0f;
         sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX] = 0.0f;
         sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX]        = -1.0f;
         sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX]    = -1.0f;
      }
      return(sensors);
   }


   // Set bee drivers.
   public void setDriver(int driver)
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


   // Write metamporph dataset.
   public void writeMetamorphDataset() throws Exception
   {
      if (bees != null)
      {
         for (int i = 0; i < bees.length; i++)
         {
            if (bees[i] != null)
            {
               bees[i].writeMetamorphDataset(true);
            }
         }
      }
   }
}
