// For conditions of distribution and use, see copyright notice in Morphognosis.java

package morphognosis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import morphognosis.honey_bees.Parameters;

/*
 * Morphognostic neighborhoods:
 * Neighborhoods are nested by increasing spatial and temporal distance from the present.
 * A neighborhood is a tiled configuration of sectors.
 * A sector is a cube space-time which contains a vector of event value densities contained within it.
 */
public class Morphognostic
{
   // Parameters.
   public static final int     DEFAULT_NUM_NEIGHBORHOODS        = 1;
   public static final int[][] DEFAULT_NEIGHBORHOOD_DIMENSIONS  = { { 3, 1 } };
   public static final int[]   DEFAULT_NEIGHBORHOOD_DURATIONS   = { 1 };
   public static final boolean DEFAULT_BINARY_VALUE_AGGREGATION = false;
   public int NUM_NEIGHBORHOODS = DEFAULT_NUM_NEIGHBORHOODS;
   // NEIGHBORHOOD_DIMENSIONS element: { <neighborhood dimension>, <sector dimension> }
   public int[][] NEIGHBORHOOD_DIMENSIONS  = DEFAULT_NEIGHBORHOOD_DIMENSIONS;
   public int[]   NEIGHBORHOOD_DURATIONS   = DEFAULT_NEIGHBORHOOD_DURATIONS;
   public boolean BINARY_VALUE_AGGREGATION = DEFAULT_BINARY_VALUE_AGGREGATION;

   // Events.
   public class Event
   {
      public int[] values;
      public int   x;
      public int   y;
      public int   time;
      public Event(int[] values, int x, int y, int time)
      {
         int n = values.length;

         this.values = new int[n];
         for (int i = 0; i < n; i++)
         {
            this.values[i] = values[i];
         }
         this.x    = x;
         this.y    = y;
         this.time = time;
      }
   }
   public ArrayList<Event> events;
   public Event createEvent(int[] values, int x, int y, int time)
   {
      return(new Event(values, x, y, time));
   }


   public int eventsWidth, eventsHeight;

   // Event quantities.
   public int   eventDimensions;
   public int[] eventValueDimensions;
   public int   maxEventAge;
   public int   eventTime;
   public       String[] eventNames;

   // Neighborhood.
   public class Neighborhood
   {
      public int       dx, dy, dimension;
      public int       duration;
      public boolean[] eventDimensionMap;

      // Sector.
      public class Sector
      {
         public int       dx, dy, dimension;
         public float[][] valueDensities;

         public Sector(int dx, int dy, int dimension)
         {
            this.dx        = dx;
            this.dy        = dy;
            this.dimension = dimension;
            valueDensities = new float[eventDimensions][];
            for (int d = 0; d < eventDimensions; d++)
            {
               valueDensities[d] = new float[eventValueDimensions[d]];
               for (int i = 0; i < eventValueDimensions[d]; i++)
               {
                  valueDensities[d][i] = 0.0f;
               }
            }
         }


         public void setValueDensity(int dimension, int index, float density)
         {
            valueDensities[dimension][index] = density;
         }


         public float getValueDensity(int dimension, int index)
         {
            return(valueDensities[dimension][index]);
         }
      }

      // Sectors.
      public Sector[][] sectors;

      // Constructors.
      public Neighborhood(int dx, int dy, int dimension,
                          int duration, int sectorDimension)
      {
         init(dx, dy, dimension, duration, sectorDimension);
      }


      public Neighborhood(int dx, int dy, int dimension,
                          int duration, int sectorDimension,
                          boolean[] eventDimensionMap)
      {
         init(dx, dy, dimension, duration, sectorDimension);
         this.eventDimensionMap = new boolean[eventDimensionMap.length];
         for (int i = 0; i < eventDimensionMap.length; i++)
         {
            this.eventDimensionMap[i] = eventDimensionMap[i];
         }
      }


      // Initialize neighborhood.
      public void init(int dx, int dy, int dimension,
                       int duration, int sectorDimension)
      {
         this.dx        = dx;
         this.dy        = dy;
         this.dimension = dimension;
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
         eventDimensionMap = null;
      }


      // Update neighborhood.
      public void update(int cx, int cy, boolean wrapWorld)
      {
         // Clear value densities.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int i = 0; i < eventDimensions; i++)
               {
                  for (int j = 0; j < eventValueDimensions[i]; j++)
                  {
                     s.valueDensities[i][j] = 0.0f;
                  }
               }
            }
         }

         // Accumulate values per sector.
         for (Event event : events)
         {
            // Filter events within duration of neighborhood.
            int et = eventTime - event.time;
            if (et < duration)
            {
               // Determine closest sector in which event occurred.
               int    ex   = event.x;
               int    ey   = event.y;
               Sector s    = sectors[sectors.length / 2][sectors.length / 2];
               int    sx   = cx + dx + s.dx + (s.dimension / 2);
               int    sy   = cy + dy + s.dy + (s.dimension / 2);
               int    dist = Math.abs(sx - ex) + Math.abs(sy - ey);
               for (int x = 0, x2 = sectors.length; x < x2; x++)
               {
                  for (int y = 0, y2 = sectors.length; y < y2; y++)
                  {
                     Sector s2    = sectors[x][y];
                     int    sx2   = cx + dx + s2.dx + (s2.dimension / 2);
                     int    sy2   = cy + dy + s2.dy + (s2.dimension / 2);
                     int    dist2 = Math.abs(sx2 - ex) + Math.abs(sy2 - ey);
                     if (dist2 < dist)
                     {
                        dist = dist2;
                        s    = s2;
                        sx   = sx2;
                        sy   = sy2;
                     }
                  }
               }

               // Accumulate values.
               for (int d = 0; d < eventDimensions; d++)
               {
                  // Event dimension mapped to neighborhood?
                  if ((eventDimensionMap == null) || eventDimensionMap[d])
                  {
                     if (event.values[d] != -1)
                     {
                        int v = event.values[d];
                        if (s.valueDensities[d].length == 1)
                        {
                           s.valueDensities[d][0] += (float)v;
                        }
                        else
                        {
                           s.valueDensities[d][v] += 1.0f;
                        }
                     }
                  }
               }
            }
         }

         // Scale value densities by duration.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < eventValueDimensions[d]; i++)
                  {
                     if (BINARY_VALUE_AGGREGATION)
                     {
                        if (s.valueDensities[d][i] > 1.0f)
                        {
                           s.valueDensities[d][i] = 1.0f;
                        }
                     }
                     else
                     {
                        s.valueDensities[d][i] /= (float)duration;
                     }
                  }
               }
            }
         }
      }


      // Compare neighborhood.
      public float compare(Neighborhood n)
      {
         float c = 0.0f;

         float[][][] densities1 = rectifySectorValueDensities();
         float[][][] densities2 = n.rectifySectorValueDensities();
         for (int i = 0, j = sectors.length * sectors.length; i < j; i++)
         {
            for (int d = 0; d < eventDimensions; d++)
            {
               for (int k = 0; k < eventValueDimensions[d]; k++)
               {
                  c += Math.abs(densities1[i][d][k] - densities2[i][d][k]);
               }
            }
         }
         return(c);
      }


      // Rectify sector densities.
      public float[][][] rectifySectorValueDensities()
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
                     densities[i][d] = new float[eventValueDimensions[d]];
                     for (int j = 0; j < eventValueDimensions[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].valueDensities[d][j];
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
                     densities[i][d] = new float[eventValueDimensions[d]];
                     for (int j = 0; j < eventValueDimensions[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].valueDensities[d][j];
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
                     densities[i][d] = new float[eventValueDimensions[d]];
                     for (int j = 0; j < eventValueDimensions[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].valueDensities[d][j];
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
                     densities[i][d] = new float[eventValueDimensions[d]];
                     for (int j = 0; j < eventValueDimensions[d]; j++)
                     {
                        densities[i][d][j] = sectors[sx1][sy1].valueDensities[d][j];
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

   // Constructors.
   public Morphognostic(int orientation,
                        int[] eventValueDimensions,
                        int eventsWidth, int eventsHeight,
                        int NUM_NEIGHBORHOODS,
                        int[][]          NEIGHBORHOOD_DIMENSIONS,
                        int[]          NEIGHBORHOOD_DURATIONS,
                        boolean BINARY_VALUE_AGGREGATION)
   {
      this.orientation             = orientation;
      this.eventValueDimensions    = eventValueDimensions;
      this.eventsWidth             = eventsWidth;
      this.eventsHeight            = eventsHeight;
      eventDimensions              = eventValueDimensions.length;
      this.NUM_NEIGHBORHOODS       = NUM_NEIGHBORHOODS;
      this.NEIGHBORHOOD_DIMENSIONS = new int[NUM_NEIGHBORHOODS][2];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         this.NEIGHBORHOOD_DIMENSIONS[i] = NEIGHBORHOOD_DIMENSIONS[i];
      }
      this.NEIGHBORHOOD_DURATIONS = new int[NUM_NEIGHBORHOODS];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         this.NEIGHBORHOOD_DURATIONS[i] = NEIGHBORHOOD_DURATIONS[i];
      }
      this.BINARY_VALUE_AGGREGATION = BINARY_VALUE_AGGREGATION;
      init();
   }


   public Morphognostic(int orientation,
                        int[] eventValueDimensions,
                        int eventsWidth, int eventsHeight)
   {
      this.orientation          = orientation;
      this.eventValueDimensions = eventValueDimensions;
      this.eventsWidth          = eventsWidth;
      this.eventsHeight         = eventsHeight;
      eventDimensions           = eventValueDimensions.length;
      init();
   }


   // Construct with mapped neighborhood events.
   public Morphognostic(int orientation,
                        int[] eventValueDimensions,
                        boolean[][] neighborhoodEventMap,
                        int eventsWidth, int eventsHeight,
                        int NUM_NEIGHBORHOODS,
                        int[][]          NEIGHBORHOOD_DIMENSIONS,
                        int[]          NEIGHBORHOOD_DURATIONS,
                        boolean BINARY_VALUE_AGGREGATION)
   {
      this.orientation             = orientation;
      this.eventValueDimensions    = eventValueDimensions;
      this.eventsWidth             = eventsWidth;
      this.eventsHeight            = eventsHeight;
      eventDimensions              = eventValueDimensions.length;
      this.NUM_NEIGHBORHOODS       = NUM_NEIGHBORHOODS;
      this.NEIGHBORHOOD_DIMENSIONS = new int[NUM_NEIGHBORHOODS][2];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         this.NEIGHBORHOOD_DIMENSIONS[i][0] = NEIGHBORHOOD_DIMENSIONS[i][0];
         this.NEIGHBORHOOD_DIMENSIONS[i][1] = NEIGHBORHOOD_DIMENSIONS[i][1];
      }
      this.NEIGHBORHOOD_DURATIONS = new int[NUM_NEIGHBORHOODS];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         this.NEIGHBORHOOD_DURATIONS[i] = NEIGHBORHOOD_DURATIONS[i];
      }
      this.BINARY_VALUE_AGGREGATION = BINARY_VALUE_AGGREGATION;
      init(neighborhoodEventMap);
   }


   public Morphognostic(int orientation,
                        int[] eventValueDimensions,
                        boolean[][] neighborhoodEventMap,
                        int eventsWidth, int eventsHeight)
   {
      this.orientation          = orientation;
      this.eventValueDimensions = eventValueDimensions;
      this.eventsWidth          = eventsWidth;
      this.eventsHeight         = eventsHeight;
      eventDimensions           = eventValueDimensions.length;
      init(neighborhoodEventMap);
   }


   public void init()
   {
      init(null);
   }


   public void init(boolean[][] neighborhoodEventMap)
   {
      neighborhoods = new Vector<Neighborhood>();
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         int d = NEIGHBORHOOD_DIMENSIONS[i][0];
         int s = NEIGHBORHOOD_DIMENSIONS[i][1];
         int t = NEIGHBORHOOD_DURATIONS[i];
         if (neighborhoodEventMap == null)
         {
            neighborhoods.add(new Neighborhood(-d / 2, -d / 2, d, t, s));
         }
         else
         {
            // Create neighborhood with mapped events.
            neighborhoods.add(new Neighborhood(-d / 2, -d / 2, d, t, s, neighborhoodEventMap[i]));
         }
      }
      if (NUM_NEIGHBORHOODS > 0)
      {
         maxEventAge = neighborhoods.get(NUM_NEIGHBORHOODS - 1).duration - 1;
      }
      else
      {
         maxEventAge = 0;
      }
      events     = new ArrayList<Event>();
      eventTime  = 0;
      eventNames = null;
   }


   // Name events.
   public void nameEvents(String[] eventNames)
   {
      this.eventNames = eventNames;
   }


   // Update morphognostic.
   public void update(int[] eventValues, int cx, int cy)
   {
      update(eventValues, cx, cy, false);
   }


   public void update(int[] eventValues, int cx, int cy, boolean wrapWorld)
   {
      // Update events.
      events.add(new Event(eventValues, cx, cy, eventTime));
      if ((eventTime - events.get(0).time) > maxEventAge)
      {
         events.remove(0);
      }

      // Update neighborhoods.
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         neighborhoods.get(i).update(cx, cy, wrapWorld);
      }
      eventTime++;
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
                  for (int i = 0; i < eventValueDimensions[d]; i++)
                  {
                     s.valueDensities[d][i] = 0.0f;
                  }
               }
            }
         }
      }
      events.clear();
   }


   // Save.
   public void save(DataOutputStream output) throws IOException
   {
      Utility.saveInt(output, NUM_NEIGHBORHOODS);
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(output, NEIGHBORHOOD_DIMENSIONS[i][0]);
         Utility.saveInt(output, NEIGHBORHOOD_DIMENSIONS[i][1]);
      }
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(output, NEIGHBORHOOD_DURATIONS[i]);
      }
      int v = 0;
      if (BINARY_VALUE_AGGREGATION) { v = 1; }
      Utility.saveInt(output, v);
      Utility.saveInt(output, orientation);
      Utility.saveInt(output, eventsWidth);
      Utility.saveInt(output, eventsHeight);
      Utility.saveInt(output, eventDimensions);
      for (int d = 0; d < eventDimensions; d++)
      {
         Utility.saveInt(output, eventValueDimensions[d]);
      }
      for (Neighborhood n : neighborhoods)
      {
         if (n.eventDimensionMap == null)
         {
            Utility.saveInt(output, 0);
         }
         else
         {
            Utility.saveInt(output, 1);
            for (int i = 0; i < eventDimensions; i++)
            {
               if (n.eventDimensionMap[i])
               {
                  Utility.saveInt(output, 1);
               }
               else
               {
                  Utility.saveInt(output, 0);
               }
            }
         }
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < eventValueDimensions[d]; i++)
                  {
                     Utility.saveFloat(output, s.valueDensities[d][i]);
                  }
               }
            }
         }
      }
      Utility.saveInt(output, events.size());
      for (Event event : events)
      {
         for (int value : event.values)
         {
            Utility.saveInt(output, value);
         }
         Utility.saveInt(output, event.x);
         Utility.saveInt(output, event.y);
         Utility.saveInt(output, event.time);
      }
      Utility.saveInt(output, eventTime);
      output.flush();
   }


   // Load.
   public static Morphognostic load(DataInputStream input) throws EOFException, IOException
   {
      int NUM_NEIGHBORHOODS = Utility.loadInt(input);

      int[][] NEIGHBORHOOD_DIMENSIONS = new int[NUM_NEIGHBORHOODS][2];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DIMENSIONS[i][0] = Utility.loadInt(input);
         NEIGHBORHOOD_DIMENSIONS[i][1] = Utility.loadInt(input);
      }
      int[] NEIGHBORHOOD_DURATIONS = new int[NUM_NEIGHBORHOODS];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DURATIONS[i] = Utility.loadInt(input);
      }
      boolean BINARY_VALUE_AGGREGATION = false;
      int     v = Utility.loadInt(input);
      if (v == 1)
      {
         BINARY_VALUE_AGGREGATION = true;
      }
      int orientation     = Utility.loadInt(input);
      int eventsWidth     = Utility.loadInt(input);
      int eventsHeight    = Utility.loadInt(input);
      int eventDimensions = Utility.loadInt(input);

      int[] eventValueDimensions = new int[eventDimensions];
      for (int d = 0; d < eventDimensions; d++)
      {
         eventValueDimensions[d] = Utility.loadInt(input);
      }
      Morphognostic m = new Morphognostic(orientation,
                                          eventValueDimensions,
                                          eventsWidth, eventsHeight,
                                          NUM_NEIGHBORHOODS,
                                          NEIGHBORHOOD_DIMENSIONS,
                                          NEIGHBORHOOD_DURATIONS,
                                          BINARY_VALUE_AGGREGATION);
      for (Neighborhood n : m.neighborhoods)
      {
         if (Utility.loadInt(input) == 1)
         {
            n.eventDimensionMap = new boolean[eventDimensions];
            for (int i = 0; i < eventDimensions; i++)
            {
               if (Utility.loadInt(input) == 1)
               {
                  n.eventDimensionMap[i] = true;
               }
               else
               {
                  n.eventDimensionMap[i] = false;
               }
            }
         }
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int i = 0; i < eventValueDimensions[d]; i++)
                  {
                     s.valueDensities[d][i] = Utility.loadFloat(input);
                  }
               }
            }
         }
      }
      m.events.clear();
      int n = Utility.loadInt(input);
      for (int i = 0; i < n; i++)
      {
         int[] values = new int[eventDimensions];
         for (int j = 0; j < eventDimensions; j++)
         {
            values[j] = Utility.loadInt(input);
         }
         int   x     = Utility.loadInt(input);
         int   y     = Utility.loadInt(input);
         int   t     = Utility.loadInt(input);
         Event event = m.createEvent(values, x, y, t);
         m.events.add(m.events.size(), event);
      }
      m.eventTime = Utility.loadInt(input);

      return(m);
   }


   // Clone.
   public Morphognostic clone()
   {
      Morphognostic m = new Morphognostic(orientation,
                                          eventValueDimensions,
                                          eventsWidth, eventsHeight,
                                          NUM_NEIGHBORHOODS,
                                          NEIGHBORHOOD_DIMENSIONS,
                                          NEIGHBORHOOD_DURATIONS,
                                          BINARY_VALUE_AGGREGATION);

      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood n1 = m.neighborhoods.get(i);
         Neighborhood n2 = neighborhoods.get(i);
         if (n2.eventDimensionMap != null)
         {
            n1.eventDimensionMap = new boolean[eventDimensions];
            for (int j = 0; j < n2.eventDimensionMap.length; j++)
            {
               n1.eventDimensionMap[j] = n2.eventDimensionMap[j];
            }
         }
         for (int x = 0; x < n1.sectors.length; x++)
         {
            for (int y = 0; y < n1.sectors.length; y++)
            {
               Neighborhood.Sector s1 = n1.sectors[x][y];
               Neighborhood.Sector s2 = n2.sectors[x][y];
               for (int d = 0; d < eventDimensions; d++)
               {
                  for (int j = 0; j < eventValueDimensions[d]; j++)
                  {
                     s1.valueDensities[d][j] = s2.valueDensities[d][j];
                  }
               }
            }
         }
      }
      m.events.clear();
      for (int i = 0, j = events.size(); i < j; i++)
      {
         Event event = events.get(i);
         int[] values = new int[eventDimensions];
         for (int k = 0; k < eventDimensions; k++)
         {
            values[k] = event.values[k];
         }
         Event event2 = m.createEvent(values, event.x, event.y, event.time);
         m.events.add(m.events.size(), event2);
      }
      m.eventTime  = eventTime;
      m.eventNames = eventNames;
      return(m);
   }


   // Locate event.
   // Return direction to event, NUM_ORIENTATIONS for center event, or -1 for not found.
   public int locateEvent(int valueIndex)
   {
      return(locateEvent(valueIndex, false));
   }


   public int locateEvent(int valueIndex, boolean verbose)
   {
      for (int n = 0; n < NUM_NEIGHBORHOODS; n++)
      {
         int location = locateEvent(n, valueIndex, verbose);
         if (location != -1)
         {
            return(location);
         }
      }
      return(-1);
   }


   public int locateEvent(int neighborhood, int valueIndex, boolean verbose)
   {
      int          centerXY = Parameters.NEIGHBORHOOD_DIMENSIONS[neighborhood][0] / 2;
      Neighborhood n        = neighborhoods.get(neighborhood);

      for (int x = 0; x < n.sectors.length; x++)
      {
         for (int y = 0; y < n.sectors.length; y++)
         {
            Neighborhood.Sector s = n.sectors[x][y];
            if ((x != centerXY) || (y != centerXY))
            {
               if (s.valueDensities[valueIndex][0] > 0.0f)
               {
                  if (verbose)
                  {
                     System.out.println("locateEvent, valueIndex=" + valueIndex +
                                        ",neighborhood=" + neighborhood + ",duration=" + n.duration + ",sector=" + x + "/" + y +
                                        ",density=" + s.valueDensities[valueIndex][0]);
                  }
                  if (x == 0)
                  {
                     if (y == 0)
                     {
                        return(Orientation.SOUTHWEST);
                     }
                     else if (y == n.sectors.length - 1)
                     {
                        return(Orientation.NORTHWEST);
                     }
                     else
                     {
                        return(Orientation.WEST);
                     }
                  }
                  else if (x == n.sectors.length - 1)
                  {
                     if (y == 0)
                     {
                        return(Orientation.SOUTHEAST);
                     }
                     else if (y == n.sectors.length - 1)
                     {
                        return(Orientation.NORTHEAST);
                     }
                     else
                     {
                        return(Orientation.EAST);
                     }
                  }
                  else
                  {
                     if (y == 0)
                     {
                        return(Orientation.SOUTH);
                     }
                     else
                     {
                        return(Orientation.NORTH);
                     }
                  }
               }
            }
         }
      }
      Neighborhood.Sector s = n.sectors[centerXY][centerXY];
      if (s.valueDensities[valueIndex][0] > 0.0f)
      {
         if (verbose)
         {
            System.out.println("locateEvent, valueIndex=" + valueIndex +
                               ",neighborhood=" + neighborhood + ",duration=" + n.duration + ",sector=" + centerXY + "/" + centerXY +
                               ",density=" + s.valueDensities[valueIndex][0]);
         }
         return(Orientation.NUM_ORIENTATIONS);
      }
      return(-1);
   }


   // Clear event.
   public void clearEvent(int valueIndex)
   {
      for (int n = 0; n < NUM_NEIGHBORHOODS; n++)
      {
         clearEvent(n, valueIndex);
      }
   }


   public void clearEvent(int neighborhood, int valueIndex)
   {
      Neighborhood n = neighborhoods.get(neighborhood);

      for (int x = 0; x < n.sectors.length; x++)
      {
         for (int y = 0; y < n.sectors.length; y++)
         {
            Neighborhood.Sector s = n.sectors[x][y];
            s.valueDensities[valueIndex][0] = 0.0f;
         }
      }
      for (Event event : events)
      {
         event.values[valueIndex] = 0;
      }
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
         System.out.println("\tduration=" + n.duration);
         if (n.eventDimensionMap == null)
         {
            System.out.println("\teventDimensionMap=null");
         }
         else
         {
            System.out.print("\teventDimensionMap={");
            for (int j = 0; j < eventDimensions; j++)
            {
               System.out.print(n.eventDimensionMap[j]);
               if (j < eventDimensions - 1)
               {
                  System.out.print(",");
               }
            }
            System.out.println("}");
         }
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               System.out.println("\tsector[" + x + "][" + y + "]:");
               Neighborhood.Sector s = n.sectors[x][y];
               System.out.println("\t\tdx/dy=" + s.dx + "/" + s.dy);
               for (int d = 0; d < eventDimensions; d++)
               {
                  if (n.eventDimensionMap[d])
                  {
                     if ((eventNames == null) || (eventNames[d] == null))
                     {
                        System.out.print("\t\tdensities[" + d + "] = ");
                     }
                     else
                     {
                        System.out.print("\t\tdensities[" + d + "] (" + eventNames[d] + ") =");
                     }
                     for (int j = 0; j < eventValueDimensions[d]; j++)
                     {
                        System.out.print(" " + s.valueDensities[d][j]);
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
      System.out.print("NEIGHBORHOOD_DIMENSIONS (element: { <neighborhood dimension>, <sector dimension> })={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print("{" + NEIGHBORHOOD_DIMENSIONS[i][0] + "," +
                          NEIGHBORHOOD_DIMENSIONS[i][1] + "}");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.print("NEIGHBORHOOD_DURATIONS={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print(NEIGHBORHOOD_DURATIONS[i] + "");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.println("BINARY_VALUE_AGGREGATION=" + BINARY_VALUE_AGGREGATION);
      System.out.println("orientation=" + orientation);
      System.out.println("eventDimensions=" + eventDimensions);
      System.out.print("eventValueDimensions={");
      for (int i = 0; i < eventDimensions; i++)
      {
         System.out.print(eventValueDimensions[i] + "");
         if (i < eventDimensions - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.println("eventsWidth=" + eventsWidth);
      System.out.println("eventsHeight=" + eventsHeight);
   }
}
