// For conditions of distribution and use, see copyright notice in Main.java

// Parameters.

package morphognosis.honeybees;

public class Parameters
{
       // Dimensions.
       public static int WORLD_WIDTH = 21;
       public static int WORLD_HEIGHT = 21;
       public static int HIVE_RADIUS = 3;
       
       // Flower parameters.
       public static float FLOWER_SPROUT_PROBABILITY = .01f;
       public static float FLOWER_DEATH_PROBABILITY = .01f;
       public static int FLOWER_NECTAR_CAPACITY = 3;
       public static float FLOWER_NECTAR_PRODUCTION_PROBABILITY = .05f;
       
       // Bee parameters.
       public static int NUM_BEES = 5;
       public static float BEE_TURN_PROBABILITY = .1f;
       public static float LEAVE_HIVE_TO_FORAGE_PROBABILITY = .05f;
       public static float RETURN_TO_HIVE_PROBABILITY = .01f;
}
