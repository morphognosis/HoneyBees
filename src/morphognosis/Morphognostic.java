// For conditions of distribution and use, see copyright notice in Morphognosis.java

package morphognosis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

/*
 * Morphognostic space-time neighborhoods:
 * Neighborhoods are nested by increasing spatial
 * size and receding temporal distance from the present.
 * A neighborhood is a tiled configuration of sectors.
 * A sector is a cube of 2D space-time which contains a
 * vector of event type densities contained within it.
 */
public class Morphognostic
{
   // Parameters.
   public static int DEFAULT_NUM_NEIGHBORHOODS = 2;
   public static int DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION    = 3;
   public static int DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE     = 0;
   public static int DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER = 3;
   public static int DEFAULT_EPOCH_INTERVAL_STRIDE             = 1;
   public static int DEFAULT_EPOCH_INTERVAL_MULTIPLIER         = 3;
   public int        NUM_NEIGHBORHOODS = DEFAULT_NUM_NEIGHBORHOODS;
   public int        NEIGHBORHOOD_INITIAL_DIMENSION    = DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
   public int        NEIGHBORHOOD_DIMENSION_STRIDE     = DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
   public int        NEIGHBORHOOD_DIMENSION_MULTIPLIER = DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
   public int        EPOCH_INTERVAL_STRIDE             = DEFAULT_EPOCH_INTERVAL_STRIDE;
   public int        EPOCH_INTERVAL_MULTIPLIER         = DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

   // Neighborhood.
   public class Neighborhood
   {
      public int dx, dy, dimension;
      public int epoch, duration;

      // Sector.
      public class Sector
      {
         public int       dx, dy, dimension;
         public float[][] typeDensities;
         public int[][][] events;

         public Sector(int dx, int dy, int dimension)
         {
            this.dx        = dx;
            this.dy        = dy;
            this.dimension = dimension;
            typeDensities  = new float[eventDimensions][];
            for (int d = 0; d < eventDimensions; d++)
            {
               typeDensities[d] = new float[numEventTypes[d]];
               for (int i = 0; i < numEventTypes[d]; i++)
               {
                  typeDensities[d][i] = 0.0f;
               }
            }
            events = new int[dimension][dimension][eventDimensions];
            for (int x = 0; x < dimension; x++)
            {
               for (int y = 0; y < dimension; y++)
               {
                  for (int d = 0; d < eventDimensions; d++)
                  {
                     events[x][y][d] = -1;
                  }
               }
            }
         }


         public void setTypeDensity(int dimension, int index, float density)
         {
            typeDensities[dimension][index] = density;
         }


         public float getTypeDensity(int dimension, int index)
         {
            return(typeDensities[dimension][index]);
         }
      }

      // Sectors.
      public Sector[][] sectors;

      // Constructor.
      public Neighborhood(int dx, int dy, int dimension,
                          int epoch, int duration, int sectorDimension)
      {
         this.dx        = dx;
         this.dy        = dy;
         this.dimension = dimension;
         this.epoch     = epoch;
         this.duration  = duration;
         int d = dimension / sectorDimension;
         if ((d * sectorDimension) < dimension) { d++; }
         sectors = new Sector[d][d];
         float f = 0.0f;
         if (d > 1)
         {
            f = (float)((d * sectorDimension) - dimension) / (float)(d - 1);
         }
         for (int x = 0; x < d; x++)
         {
            for (int y = 0; y < d; y++)
            {
               int sdx = (int)((float)(x * sectorDimension) - ((float)x * f));
               int sdy = (int)((float)(y * sectorDimension) - ((float)y * f));
               sectors[x][y] = new Sector(sdx, sdy, sectorDimension);
            }
         }
      }


      // Update.
      public void update(int[][][][] events, int cx, int cy)
      {
         // Clear type densities.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int i = 0; i < eventDimensions; i++)
               {
                  for (int j = 0; j < numEventTypes[i]; j++)
                  {
                     s.typeDensities[i][j] = 0.0f;
                  }
               }
               for (int x = 0; x < s.dimension; x++)
               {
                  for (int y = 0; y < s.dimension; y++)
                  {
                     for (int d = 0; d < eventDimensions; d++)
                     {
                        s.events[x][y][d] = -1;
                     }
                  }
               }
            }
         }

         // Accumulate types per sector.
         for (int x0 = cx + dx, x1 = x0, x2 = x0 + dimension; x1 < x2; x1++)
         {
            for (int y0 = cy + dy, y1 = y0, y2 = y0 + dimension; y1 < y2; y1++)
            {
               int x3 = x1;
               while (x3 < 0) { x3 += events.length; }
               while (x3 >= events.length) { x3 -= events.length; }
               int y3 = y1;
               while (y3 < 0) { y3 += events[0].length; }
               while (y3 >= events[0].length) { y3 -= events[0].length; }
               for (int d = 0; d < eventDimensions; d++)
               {
                  int t = -1;
                  for (int i = epoch + duration - 1; i >= epoch; i--)
                  {
                     if (events[x3][y3][d][i] != -1)
                     {
                        t = events[x3][y3][d][i];
                     }
                  }
                  if (t != -1)
                  {
                     for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
                     {
                        for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
                        {
                           int    x4 = x1 - x0;
                           int    y4 = y1 - y0;
                           Sector s  = sectors[sx1][sy1];
                           if ((x4 >= s.dx) &&
                               (x4 < (s.dx + s.dimension)) &&
                               (y4 >= s.dy) &&
                               (y4 < (s.dy + s.dimension)))
                           {
                              if (s.typeDensities[d].length == 1)
                              {
                                 s.typeDensities[d][0] += (float)t;
                              }
                              else
                              {
                                 s.typeDensities[d][t] += 1.0f;
                              }
                              s.events[x4 - s.dx][y4 - s.dy][d] = t;
                           }
                        }
                     }
                  }
               }
            }
         }

         // Type density is relative to sector dimension.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < numEventTypes[d]; i++)
                  {
                     s.typeDensities[d][i] /= (float)sectors[sx1][sy1].dimension;
                  }
               }
            }
         }
      }


      // Compare neighborhood.
      public float compare(Neighborhood n)
      {
         float c = 0.0f;

         float[][][] densities1 = rectifySectorTypeDensities();
         float[][][] densities2 = n.rectifySectorTypeDensities();
         for (int i = 0, j = sectors.length * sectors.length; i < j; i++)
         {
            for (int d = 0; d < eventDimensions; d++)
            {
               for (int k = 0; k < numEventTypes[d]; k++)
               {
                  c += Math.abs(densities1[i][d][k] - densities2[i][d][k]);
               }
            }
         }
         return(c);
      }


      // Rectify sector densities.
      public float[][][] rectifySectorTypeDensities()
      {
         float[][][] densities = new float[sectors.length * sectors.length][eventDimensions][];
         switch (orientation)
         {
         case Orientation.NORTH:
            for (int i = 0, sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
               {
                  for (int d = 0; d < eventDimensions; d++)
                  {
                     densities[i][d] = new float[numEventTypes[d]];
                     for (int j = 0; j < numEventTypes[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].typeDensities[d][j];
                     }
                  }
                  i++;
               }
            }
            break;

         case Orientation.SOUTH:
            for (int i = 0, sy1 = sectors.length - 1; sy1 >= 0; sy1--)
            {
               for (int sx1 = sectors.length - 1; sx1 >= 0; sx1--)
               {
                  for (int d = 0; d < eventDimensions; d++)
                  {
                     densities[i][d] = new float[numEventTypes[d]];
                     for (int j = 0; j < numEventTypes[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].typeDensities[d][j];
                     }
                  }
                  i++;
               }
            }
            break;

         case Orientation.EAST:
            for (int i = 0, sx1 = sectors.length - 1; sx1 >= 0; sx1--)
            {
               for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
               {
                  for (int d = 0; d < eventDimensions; d++)
                  {
                     densities[i][d] = new float[numEventTypes[d]];
                     for (int j = 0; j < numEventTypes[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].typeDensities[d][j];
                     }
                  }
                  i++;
               }
            }
            break;

         case Orientation.WEST:
            for (int i = 0, sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
            {
               for (int sy1 = sectors.length - 1; sy1 >= 0; sy1--)
               {
                  for (int d = 0; d < eventDimensions; d++)
                  {
                     densities[i][d] = new float[numEventTypes[d]];
                     for (int j = 0; j < numEventTypes[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].typeDensities[d][j];
                     }
                  }
                  i++;
               }
            }
            break;

         default:
            break;
         }
         return(densities);
      }
   }

   // Neighborhoods.
   public Vector<Neighborhood> neighborhoods;

   // Orientation.
   public int orientation;

   // Event quantities.
   public int[] numEventTypes;
   public int   eventDimensions;

   // Constructors.
   public Morphognostic(int orientation, int eventDimensions)
   {
      int[] numEventTypes = new int[eventDimensions];
      for (int i = 0; i < eventDimensions; i++)
      {
         numEventTypes[i] = 1;
      }
      init(orientation, numEventTypes);
   }


   // Construct with parameters.
   public Morphognostic(int orientation, int eventDimensions,
                        int NUM_NEIGHBORHOODS,
                        int NEIGHBORHOOD_INITIAL_DIMENSION,
                        int NEIGHBORHOOD_DIMENSION_STRIDE,
                        int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                        int EPOCH_INTERVAL_STRIDE,
                        int EPOCH_INTERVAL_MULTIPLIER
                        )
   {
      this.NUM_NEIGHBORHOODS = NUM_NEIGHBORHOODS;
      this.NEIGHBORHOOD_INITIAL_DIMENSION    = NEIGHBORHOOD_INITIAL_DIMENSION;
      this.NEIGHBORHOOD_DIMENSION_STRIDE     = NEIGHBORHOOD_DIMENSION_STRIDE;
      this.NEIGHBORHOOD_DIMENSION_MULTIPLIER = NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      this.EPOCH_INTERVAL_STRIDE             = EPOCH_INTERVAL_STRIDE;
      this.EPOCH_INTERVAL_MULTIPLIER         = EPOCH_INTERVAL_MULTIPLIER;
      int[] numEventTypes = new int[eventDimensions];
      for (int i = 0; i < eventDimensions; i++)
      {
         numEventTypes[i] = 1;
      }
      init(orientation, numEventTypes);
   }


   public Morphognostic(int orientation, int[] numEventTypes)
   {
      init(orientation, numEventTypes);
   }


   // Construct with parameters.
   public Morphognostic(int orientation, int[] numEventTypes,
                        int NUM_NEIGHBORHOODS,
                        int NEIGHBORHOOD_INITIAL_DIMENSION,
                        int NEIGHBORHOOD_DIMENSION_STRIDE,
                        int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                        int EPOCH_INTERVAL_STRIDE,
                        int EPOCH_INTERVAL_MULTIPLIER
                        )
   {
      this.NUM_NEIGHBORHOODS = NUM_NEIGHBORHOODS;
      this.NEIGHBORHOOD_INITIAL_DIMENSION    = NEIGHBORHOOD_INITIAL_DIMENSION;
      this.NEIGHBORHOOD_DIMENSION_STRIDE     = NEIGHBORHOOD_DIMENSION_STRIDE;
      this.NEIGHBORHOOD_DIMENSION_MULTIPLIER = NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      this.EPOCH_INTERVAL_STRIDE             = EPOCH_INTERVAL_STRIDE;
      this.EPOCH_INTERVAL_MULTIPLIER         = EPOCH_INTERVAL_MULTIPLIER;
      init(orientation, numEventTypes);
   }


   public void init(int orientation, int[] numEventTypes)
   {
      this.orientation   = orientation;
      this.numEventTypes = numEventTypes;
      eventDimensions    = numEventTypes.length;

      // Create neighborhoods.
      neighborhoods = new Vector<Neighborhood>();
      int d  = NEIGHBORHOOD_INITIAL_DIMENSION;
      int s  = 1;
      int t1 = 0;
      int t2 = EPOCH_INTERVAL_STRIDE;
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         neighborhoods.add(new Neighborhood(-d / 2, -d / 2, d, t1, t2 - t1, s));
         s   = d;
         d  *= NEIGHBORHOOD_DIMENSION_MULTIPLIER;
         d  += NEIGHBORHOOD_DIMENSION_STRIDE;
         t1  = t2;
         t2 *= EPOCH_INTERVAL_MULTIPLIER;
         t2 += EPOCH_INTERVAL_STRIDE;
      }
   }


   // Update.
   public void update(int[][][][] events, int cx, int cy)
   {
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         neighborhoods.get(i).update(events, cx, cy);
      }
   }


   // Compare.
   public float compare(Morphognostic m)
   {
      float d = 0.0f;

      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         d += neighborhoods.get(i).compare(m.neighborhoods.get(i));
      }
      return(d);
   }


   // Clear.
   public void clear()
   {
      for (Neighborhood n : neighborhoods)
      {
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < numEventTypes[d]; i++)
                  {
                     s.typeDensities[d][i] = 0.0f;
                  }
                  for (int x2 = 0; x2 < s.events.length; x2++)
                  {
                     for (int y2 = 0; y2 < s.events[0].length; y2++)
                     {
                        s.events[x2][y2][d] = -1;
                     }
                  }
               }
            }
         }
      }
   }


   // Save.
   public void save(DataOutputStream output) throws IOException
   {
      Utility.saveInt(output, NUM_NEIGHBORHOODS);
      Utility.saveInt(output, NEIGHBORHOOD_INITIAL_DIMENSION);
      Utility.saveInt(output, NEIGHBORHOOD_DIMENSION_STRIDE);
      Utility.saveInt(output, NEIGHBORHOOD_DIMENSION_MULTIPLIER);
      Utility.saveInt(output, EPOCH_INTERVAL_STRIDE);
      Utility.saveInt(output, EPOCH_INTERVAL_MULTIPLIER);
      Utility.saveInt(output, orientation);
      Utility.saveInt(output, eventDimensions);
      for (int d = 0; d < eventDimensions; d++)
      {
         Utility.saveInt(output, numEventTypes[d]);
      }
      for (Neighborhood n : neighborhoods)
      {
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < numEventTypes[d]; i++)
                  {
                     Utility.saveFloat(output, s.typeDensities[d][i]);
                  }
                  for (int x2 = 0; x2 < s.events.length; x2++)
                  {
                     for (int y2 = 0; y2 < s.events[0].length; y2++)
                     {
                        Utility.saveInt(output, s.events[x2][y2][d]);
                     }
                  }
               }
            }
         }
      }
      output.flush();
   }


   // Load.
   public static Morphognostic load(DataInputStream input) throws EOFException, IOException
   {
      int NUM_NEIGHBORHOODS = Utility.loadInt(input);
      int NEIGHBORHOOD_INITIAL_DIMENSION    = Utility.loadInt(input);
      int NEIGHBORHOOD_DIMENSION_STRIDE     = Utility.loadInt(input);
      int NEIGHBORHOOD_DIMENSION_MULTIPLIER = Utility.loadInt(input);
      int EPOCH_INTERVAL_STRIDE             = Utility.loadInt(input);
      int EPOCH_INTERVAL_MULTIPLIER         = Utility.loadInt(input);
      int orientation     = Utility.loadInt(input);
      int eventDimensions = Utility.loadInt(input);

      int[] numEventTypes = new int[eventDimensions];
      for (int d = 0; d < eventDimensions; d++)
      {
         numEventTypes[d] = Utility.loadInt(input);
      }
      Morphognostic m = new Morphognostic(orientation, numEventTypes,
                                          NUM_NEIGHBORHOODS,
                                          NEIGHBORHOOD_INITIAL_DIMENSION,
                                          NEIGHBORHOOD_DIMENSION_STRIDE,
                                          NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                          EPOCH_INTERVAL_STRIDE,
                                          EPOCH_INTERVAL_MULTIPLIER
                                          );
      for (Neighborhood n : m.neighborhoods)
      {
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < numEventTypes[d]; i++)
                  {
                     s.typeDensities[d][i] = Utility.loadFloat(input);
                  }
                  for (int x2 = 0; x2 < s.events.length; x2++)
                  {
                     for (int y2 = 0; y2 < s.events[0].length; y2++)
                     {
                        s.events[x2][y2][d] = Utility.loadInt(input);
                     }
                  }
               }
            }
         }
      }
      return(m);
   }


   // Clone.
   public Morphognostic clone()
   {
      Morphognostic m = new Morphognostic(orientation, numEventTypes,
                                          NUM_NEIGHBORHOODS,
                                          NEIGHBORHOOD_INITIAL_DIMENSION,
                                          NEIGHBORHOOD_DIMENSION_STRIDE,
                                          NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                          EPOCH_INTERVAL_STRIDE,
                                          EPOCH_INTERVAL_MULTIPLIER
                                          );

      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood n1 = m.neighborhoods.get(i);
         Neighborhood n2 = neighborhoods.get(i);
         for (int x = 0; x < n1.sectors.length; x++)
         {
            for (int y = 0; y < n1.sectors.length; y++)
            {
               Neighborhood.Sector s1 = n1.sectors[x][y];
               Neighborhood.Sector s2 = n2.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int j = 0; j < numEventTypes[d]; j++)
                  {
                     s1.typeDensities[d][j] = s2.typeDensities[d][j];
                  }
                  for (int x2 = 0; x2 < s1.events.length; x2++)
                  {
                     for (int y2 = 0; y2 < s1.events[0].length; y2++)
                     {
                        s1.events[x2][y2][d] = s2.events[x2][y2][d];
                     }
                  }
               }
            }
         }
      }
      return(m);
   }


   // Print.
   public void print()
   {
      printParameters();
      for (int i = 0; i < neighborhoods.size(); i++)
      {
         Neighborhood n = neighborhoods.get(i);
         System.out.println("neighborhood=" + i);
         System.out.println("\tdx/dy=" + n.dx + "/" + n.dy);
         System.out.println("\tdimension=" + n.dimension);
         System.out.println("\tepoch/duration=" + n.epoch + "/" + n.duration);
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               System.out.println("\tsector[" + x + "][" + y + "]:");
               Neighborhood.Sector s = n.sectors[x][y];
               System.out.println("\t\tdx/dy=" + s.dx + "/" + s.dy);
               for (int d = 0; d < eventDimensions; d++)
               {
                  System.out.print("\t\tdensities[" + d + "]:");
                  for (int j = 0; j < numEventTypes[d]; j++)
                  {
                     System.out.print(" " + s.typeDensities[d][j]);
                  }
                  System.out.println("");
                  System.out.println("\t\tevents[" + d + "]:");
                  for (int x2 = 0; x2 < s.events.length; x2++)
                  {
                     System.out.print("\t\t\t");
                     for (int y2 = 0; y2 < s.events[0].length; y2++)
                     {
                        if (s.events[x2][y2][d] >= 0) { System.out.print(" "); }
                        System.out.print(s.events[x2][y2][d] + " ");
                     }
                     System.out.println("");
                  }
               }
            }
         }
      }
   }


   // Print parameters.
   public void printParameters()
   {
      System.out.println("NUM_NEIGHBORHOODS=" + NUM_NEIGHBORHOODS);
      System.out.println("NEIGHBORHOOD_INITIAL_DIMENSION=" + NEIGHBORHOOD_INITIAL_DIMENSION);
      System.out.println("NEIGHBORHOOD_DIMENSION_STRIDE=" + NEIGHBORHOOD_DIMENSION_STRIDE);
      System.out.println("NEIGHBORHOOD_DIMENSION_MULTIPLIER=" + NEIGHBORHOOD_DIMENSION_MULTIPLIER);
      System.out.println("EPOCH_INTERVAL_STRIDE=" + EPOCH_INTERVAL_STRIDE);
      System.out.println("EPOCH_INTERVAL_MULTIPLIER=" + EPOCH_INTERVAL_MULTIPLIER);
      System.out.println("orientation=" + orientation);
      System.out.println("eventDimensions=" + eventDimensions);
   }
}
