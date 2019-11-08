// For conditions of distribution and use, see copyright notice in Main.java

// World.

package morphognosis.honey_bees;

import java.security.SecureRandom;
import java.awt.Point;
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
      cells        = new Cell[Parameters.WORLD_WIDTH][Parameters.WORLD_HEIGHT];
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
               cells[x][y] = new Cell();
         }
      }
   }

   // Cell distance.
   public int cellDist(int fromX, int fromY, int toX, int toY)
   {
      int w  = Parameters.WORLD_WIDTH;
      int w2 = w / 2;
      int h  = Parameters.WORLD_HEIGHT;
      int h2 = h / 2;
      int dx = Math.abs(toX - fromX);

      if (dx > w2) { dx = w - dx; }
      int dy = Math.abs(toY - fromY);
      if (dy > h2) { dy = h - dy; }
      return(dx + dy);
   }

   // Save cells.
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


   // Save cells.
   public void save(DataOutputStream writer) throws IOException
   {     
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
               cells[x][y].save(writer);
         }
      }
      writer.flush();
   }


   // Load cells from file.
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


   // Load cells.
   public void load(DataInputStream reader) throws IOException
   {
      clear();
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
               cells[x][y].load(reader);
         }
      }
   }


   // Clear cells.
   public void clear()
   {
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
               cells[x][y].clear();
         }
      }
   }

   // Get forward cell coordinates.
   public Point[] getForwardCoords(int fromX, int fromY, int toX, int toY)
   {
      int[] coordX = new int[3];
      int[] coordY = new int[3];
      if ((toX < fromX) || ((toX == (Parameters.WORLD_WIDTH - 1)) && (fromX == 0)))
      {
         coordX[0] = toX;
         coordX[1] = toX - 1;
         if (coordX[1] < 0)
         {
            coordX[1] += Parameters.WORLD_WIDTH;
         }
         coordX[2] = toX;
         coordY[0] = (toY + 1) % Parameters.WORLD_HEIGHT;
         coordY[1] = toY;
         coordY[2] = toY - 1;
         if (coordY[2] < 0)
         {
            coordY[2] += Parameters.WORLD_HEIGHT;
         }
      }
      else if ((toX > fromX) || ((toX == 0) && (fromX == (Parameters.WORLD_WIDTH - 1))))
      {
         coordX[0] = toX;
         coordX[1] = (toX + 1) % Parameters.WORLD_WIDTH;
         coordX[2] = toX;
         coordY[0] = (toY + 1) % Parameters.WORLD_HEIGHT;
         coordY[1] = toY;
         coordY[2] = toY - 1;
         if (coordY[2] < 0)
         {
            coordY[2] += Parameters.WORLD_HEIGHT;
         }
      }
      else if ((toY < fromY) || ((toY == (Parameters.WORLD_HEIGHT - 1)) && (fromY == 0)))
      {
         coordY[0] = toY;
         coordY[1] = toY - 1;
         if (coordY[1] < 0)
         {
            coordY[1] += Parameters.WORLD_HEIGHT;
         }
         coordY[2] = toY;
         coordX[0] = (toX + 1) % Parameters.WORLD_WIDTH;
         coordX[1] = toX;
         coordX[2] = toX - 1;
         if (coordX[2] < 0)
         {
            coordX[2] += Parameters.WORLD_WIDTH;
         }
      }
      else if ((toY > fromY) || ((toY == 0) && (fromY == (Parameters.WORLD_HEIGHT - 1))))
      {
         coordY[0] = toY;
         coordY[1] = (toY + 1) % Parameters.WORLD_HEIGHT;
         coordY[2] = toY;
         coordX[0] = (toX + 1) % Parameters.WORLD_WIDTH;
         coordX[1] = toX;
         coordX[2] = toX - 1;
         if (coordX[2] < 0)
         {
            coordX[2] += Parameters.WORLD_WIDTH;
         }
      }
      Point[] result = new Point[3];
      for (int i = 0; i < 3; i++)
      {
         result[i] = new Point(coordX[i], coordY[i]);
      }
      return(result);
   }
}
