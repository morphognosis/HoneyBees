// For conditions of distribution and use, see copyright notice in Main.java

// World.

package morphognosis.honeybees;

import java.security.SecureRandom;
import javax.imageio.ImageIO;
import morphognosis.SectorDisplay;
import morphognosis.Utility;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class World
{
   // Properties.
   public static int WIDTH               = 21;
   public static int HEIGHT              = 21;
   public static int MAX_ELEVATION       = 7;
   public static int CENTER_RADIUS       = 2;
   public static int NUM_SPOKES          = 28;
   public static int SPOKE_LENGTH        = 4;
   public static int SPOKE_RIPPLE_LENGTH = 2;

   // Cells.
   // See SectorDisplay.EMPTY_CELL_VALUE.
   public static final int CELL_DIMENSIONS      = 1;
   public static final int ELEVATION_CELL_INDEX = 0;
   public Dimension        size;
   public int[][][]        cells;
   public int[][][]        restoreCells;

   // Random numbers.
   public SecureRandom random;
   public int          randomSeed;

   // Lock.
   public Object lock;

   // Nest image file.
   public String nestImageFile;

   // Constructors.
   public World(int randomSeed)
   {
      int x, y;

      // Random numbers.
      random          = new SecureRandom();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);

      // Create cells.
      size         = new Dimension(WIDTH, HEIGHT);
      cells        = new int[WIDTH][HEIGHT][CELL_DIMENSIONS];
      restoreCells = new int[WIDTH][HEIGHT][CELL_DIMENSIONS];
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = restoreCells[x][y][d] = random.nextInt(3) + 4;
            }
         }
      }
      lock = new Object();
   }


   public World(Dimension size, int randomSeed, String nestImageFile)
   {
      int x, y, width, height;

      this.nestImageFile = nestImageFile;

      // Random numbers.
      random          = new SecureRandom();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);

      // Create cells.
      this.size    = size;
      width        = size.width;
      height       = size.height;
      cells        = new int[width][height][CELL_DIMENSIONS];
      restoreCells = new int[width][height][CELL_DIMENSIONS];
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d]        = SectorDisplay.EMPTY_CELL_VALUE;
               restoreCells[x][y][d] = SectorDisplay.EMPTY_CELL_VALUE;
            }
         }
      }

      // Load elevations from nest image.
      loadNestImageElevations();
      checkpoint();
      lock = new Object();
   }


   public World()
   {
      size = new Dimension();
      lock = new Object();
   }


   // Get grid width.
   public int getWidth()
   {
      return(size.width);
   }


   // Get grid height.
   public int getHeight()
   {
      return(size.height);
   }


   // Cell distance.
   public int cellDist(int fromX, int fromY, int toX, int toY)
   {
      int w  = size.width;
      int w2 = w / 2;
      int h  = size.height;
      int h2 = h / 2;
      int dx = Math.abs(toX - fromX);

      if (dx > w2) { dx = w - dx; }
      int dy = Math.abs(toY - fromY);
      if (dy > h2) { dy = h - dy; }
      return(dx + dy);
   }


   // Load nest image elevations.
   private void loadNestImageElevations()
   {
      // Load image as resource.
      Image image = null;

      try
      {
         image = (Image)ImageIO.read(getClass().getResource(nestImageFile));
      }
      catch (Exception e)
      {
      }

      // Load external image file.
      if (image == null)
      {
         try
         {
            image = (Image)ImageIO.read(new File(nestImageFile));
         }
         catch (Exception e)
         {
         }
      }

      if (image == null)
      {
         System.err.println("Cannot load nest image file " + nestImageFile);
         return;
      }

      // Create cells image.
      int           w            = size.width;
      int           h            = size.height;
      int           numCellTypes = MAX_ELEVATION + 1;
      float         q            = 256.0f / (float)numCellTypes;
      Image         s            = image.getScaledInstance(w, h, Image.SCALE_DEFAULT);
      BufferedImage b            = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      Graphics      g            = b.createGraphics();
      g.drawImage(s, 0, 0, null);
      g.dispose();
      for (int x = 0; x < w; x++)
      {
         for (int y = 0; y < h; y++)
         {
            int cy = (h - 1) - y;
            int t  = (int)((float)(b.getRGB(x, y) & 0xFF) / q);
            if (t >= numCellTypes)
            {
               t = numCellTypes - 1;
            }
            cells[x][cy][ELEVATION_CELL_INDEX] = t;
         }
      }
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
      int x, y;

      Utility.saveInt(writer, WIDTH);
      Utility.saveInt(writer, HEIGHT);
      Utility.saveInt(writer, MAX_ELEVATION);
      Utility.saveInt(writer, CENTER_RADIUS);
      Utility.saveInt(writer, NUM_SPOKES);
      Utility.saveInt(writer, SPOKE_LENGTH);
      Utility.saveInt(writer, SPOKE_RIPPLE_LENGTH);
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, cells[x][y][d]);
            }
         }
      }
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, restoreCells[x][y][d]);
            }
         }
      }
      writer.flush();
   }


   // Load cells from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load cells.
   public void load(FileInputStream input) throws IOException
   {
      int x, y;

      DataInputStream reader = new DataInputStream(input);

      WIDTH               = Utility.loadInt(reader);
      HEIGHT              = Utility.loadInt(reader);
      MAX_ELEVATION       = Utility.loadInt(reader);
      CENTER_RADIUS       = Utility.loadInt(reader);
      NUM_SPOKES          = Utility.loadInt(reader);
      SPOKE_LENGTH        = Utility.loadInt(reader);
      SPOKE_RIPPLE_LENGTH = Utility.loadInt(reader);
      size.width          = WIDTH;
      size.height         = HEIGHT;
      cells               = new int[size.width][size.height][2];
      restoreCells        = new int[size.width][size.height][2];
      clear();

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }
   }


   // Clear cells.
   public void clear()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = SectorDisplay.EMPTY_CELL_VALUE;
            }
         }
      }
   }


   // Checkpoint cells.
   public void checkpoint()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = cells[x][y][d];
            }
         }
      }
   }


   // Restore cells.
   public void restore()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = restoreCells[x][y][d];
            }
         }
      }
   }


   // Smooth the left, center, and right cell elevations.
   public void smooth(int fromX, int fromY, int centerX, int centerY)
   {
      synchronized (lock)
      {
         if ((fromX != centerX) || (fromY != centerY))
         {
            Point[] forwardCoords = getForwardCoords(fromX, fromY, centerX, centerY);
            int smoothElevation = Nest.MAX_ELEVATION / 2;
            cells[fromX][fromY][ELEVATION_CELL_INDEX]     = smoothElevation;
            cells[centerX][centerY][ELEVATION_CELL_INDEX] = smoothElevation;
            cells[forwardCoords[0].x][forwardCoords[0].y][ELEVATION_CELL_INDEX] = smoothElevation;
            cells[forwardCoords[2].x][forwardCoords[2].y][ELEVATION_CELL_INDEX] = smoothElevation;
         }
      }
   }


   // Plow the surface elevations.
   public void plow(int fromX, int fromY, int toX, int toY)
   {
      synchronized (lock)
      {
         int fishElevation = cells[fromX][fromY][ELEVATION_CELL_INDEX];
         if (fishElevation < cells[toX][toY][ELEVATION_CELL_INDEX])
         {
            if ((fromX != toX) || (fromY != toY))
            {
               Point[] plow = getForwardCoords(fromX, fromY, toX, toY);
               int n = cells[toX][toY][ELEVATION_CELL_INDEX] - fishElevation;
               cells[toX][toY][ELEVATION_CELL_INDEX] = fishElevation;
               int j = random.nextInt(3);
               for (int i = 0; i < n; i++)
               {
                  cells[plow[j].x][plow[j].y][ELEVATION_CELL_INDEX]++;
                  if (cells[plow[j].x][plow[j].y][ELEVATION_CELL_INDEX] > Nest.MAX_ELEVATION)
                  {
                     cells[plow[j].x][plow[j].y][ELEVATION_CELL_INDEX] = Nest.MAX_ELEVATION;
                  }
                  j = (j + 1) % 3;
               }
            }
         }
      }
   }


   // Get forward cell coordinates.
   public Point[] getForwardCoords(int fromX, int fromY, int toX, int toY)
   {
      int[] coordX = new int[3];
      int[] coordY = new int[3];
      if ((toX < fromX) || ((toX == (size.width - 1)) && (fromX == 0)))
      {
         coordX[0] = toX;
         coordX[1] = toX - 1;
         if (coordX[1] < 0)
         {
            coordX[1] += size.width;
         }
         coordX[2] = toX;
         coordY[0] = (toY + 1) % size.height;
         coordY[1] = toY;
         coordY[2] = toY - 1;
         if (coordY[2] < 0)
         {
            coordY[2] += size.height;
         }
      }
      else if ((toX > fromX) || ((toX == 0) && (fromX == (size.width - 1))))
      {
         coordX[0] = toX;
         coordX[1] = (toX + 1) % size.width;
         coordX[2] = toX;
         coordY[0] = (toY + 1) % size.height;
         coordY[1] = toY;
         coordY[2] = toY - 1;
         if (coordY[2] < 0)
         {
            coordY[2] += size.height;
         }
      }
      else if ((toY < fromY) || ((toY == (size.height - 1)) && (fromY == 0)))
      {
         coordY[0] = toY;
         coordY[1] = toY - 1;
         if (coordY[1] < 0)
         {
            coordY[1] += size.height;
         }
         coordY[2] = toY;
         coordX[0] = (toX + 1) % size.width;
         coordX[1] = toX;
         coordX[2] = toX - 1;
         if (coordX[2] < 0)
         {
            coordX[2] += size.width;
         }
      }
      else if ((toY > fromY) || ((toY == 0) && (fromY == (size.height - 1))))
      {
         coordY[0] = toY;
         coordY[1] = (toY + 1) % size.height;
         coordY[2] = toY;
         coordX[0] = (toX + 1) % size.width;
         coordX[1] = toX;
         coordX[2] = toX - 1;
         if (coordX[2] < 0)
         {
            coordX[2] += size.width;
         }
      }
      Point[] result = new Point[3];
      for (int i = 0; i < 3; i++)
      {
         result[i] = new Point(coordX[i], coordY[i]);
      }
      return(result);
   }


   // Print properties.
   public void printProperties()
   {
      System.out.println("Nest properties:");
      System.out.println("WIDTH = " + WIDTH);
      System.out.println("HEIGHT = " + HEIGHT);
      System.out.println("MAX_ELEVATION = " + MAX_ELEVATION);
      System.out.println("CENTER_RADIUS = " + CENTER_RADIUS);
      System.out.println("NUM_SPOKES = " + NUM_SPOKES);
      System.out.println("SPOKE_LENGTH = " + SPOKE_LENGTH);
      System.out.println("SPOKE_RIPPLE_LENGTH = " + SPOKE_RIPPLE_LENGTH);
   }
}
