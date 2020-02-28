/*
 * Optimize parameters to favor foraging cooperation.
 */

package morphognosis.honey_bees;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import morphognosis.Utility;

public class ForagingParameterOptimizer
{
   // Parameters.
   public static final int   DEFAULT_FIT_POPULATION_SIZE = 20;
   public int                FIT_POPULATION_SIZE         = DEFAULT_FIT_POPULATION_SIZE;
   public static final int   DEFAULT_NUM_MUTANTS         = 10;
   public int                NUM_MUTANTS                  = DEFAULT_NUM_MUTANTS;
   public static final int   DEFAULT_NUM_OFFSPRING        = 10;
   public int                NUM_OFFSPRING                = DEFAULT_NUM_OFFSPRING;
   public int                POPULATION_SIZE              = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   public static final float DEFAULT_MUTATION_RATE        = 0.25f;
   public float              MUTATION_RATE                = DEFAULT_MUTATION_RATE;
   public static final float DEFAULT_RANDOM_MUTATION_RATE = 0.5f;
   public float              RANDOM_MUTATION_RATE         = DEFAULT_RANDOM_MUTATION_RATE;
   public static final int   DEFAULT_RANDOM_SEED          = 4517;
   public int                RANDOM_SEED                  = DEFAULT_RANDOM_SEED;
   public static final int   SAVE_FREQUENCY               = 1;
   public static final float INVALID_FITNESS              = 1000.0f;

   public void setPopulationSize()
   {
      POPULATION_SIZE = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   }


   // Load parameters.
   public void loadParameters(DataInputStream reader) throws IOException
   {
      FIT_POPULATION_SIZE  = Utility.loadInt(reader);
      NUM_MUTANTS          = Utility.loadInt(reader);
      NUM_OFFSPRING        = Utility.loadInt(reader);
      MUTATION_RATE        = Utility.loadFloat(reader);
      RANDOM_MUTATION_RATE = Utility.loadFloat(reader);
      RANDOM_SEED          = Utility.loadInt(reader);
      setPopulationSize();
   }


   // Save parameters.
   public void saveParameters(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, FIT_POPULATION_SIZE);
      Utility.saveInt(writer, NUM_MUTANTS);
      Utility.saveInt(writer, NUM_OFFSPRING);
      Utility.saveFloat(writer, MUTATION_RATE);
      Utility.saveFloat(writer, RANDOM_MUTATION_RATE);
      Utility.saveInt(writer, RANDOM_SEED);
   }


   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java morphognosis.honey_bees.ForagingParameterOptimizer\n" +
      "      -generations <generations>\n" +
      "      -steps <world steps>\n" +
      "      -output <output file name>\n" +
      "     [-fitPopulationSize <fit population size> (default=" + DEFAULT_FIT_POPULATION_SIZE + ")]\n" +
      "     [-numMutants <number of mutants> (default=" + DEFAULT_NUM_MUTANTS + ")]\n" +
      "     [-numOffspring <number of offspring> (default=" + DEFAULT_NUM_OFFSPRING + ")]\n" +
      "     [-mutationRate <mutation rate> (default=" + DEFAULT_MUTATION_RATE + ")]\n" +
      "     [-randomMutationRate <random mutation rate> (default=" + DEFAULT_RANDOM_MUTATION_RATE + ")]\n" +
      "     [-randomSeed <random seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-logfile <log file name>]\n" +
      "  Resume run:\n" +
      "    java morphognosis.honey_bees.ForagingParameterOptimizer\n" +
      "      -generations <generations>\n" +
      "      -steps <world steps>\n" +
      "      -input <input file name>\n" +
      "      -output <output file name>\n" +
      "     [-logfile <log file name>]\n" +
      "  Print population properties:\n" +
      "    java morphognosis.honey_bees.ForagingParameterOptimizer\n" +
      "      -properties\n" +
      "      -input <input file name>\n" +
      "  Print optimization statistics:\n" +
      "    java morphognosis.honey_bees.ForagingParameterOptimizer\n" +
      "      -statistics\n" +
      "      -input <input file name>";

   /*
    * Genome.
    */
   public class Genome
   {
      /*
       * Gene.
       */
      public class Gene
      {
         // Mutation rate.
         float mutationRate;

         // Probability of random mutation.
         float randomMutationRate;

         // Random numbers.
         int    randomSeed;
         Random randomizer;

         // Value types.
         static final int INTEGER_VALUE = 0;
         static final int FLOAT_VALUE   = 1;
         static final int DOUBLE_VALUE  = 2;

         // Mutable value.
         int    type;
         String name;
         int    ivalue, imin, imax, idelta;
         float  fvalue, fmin, fmax, fdelta;
         double dvalue, dmin, dmax, ddelta;

         // Constructors.
         Gene(float mutationRate, float randomMutationRate, int randomSeed)
         {
            type                    = DOUBLE_VALUE;
            name                    = null;
            ivalue                  = imin = imax = idelta = 0;
            fvalue                  = fmin = fmax = fdelta = 0.0f;
            dvalue                  = dmin = dmax = ddelta = 0.0;
            this.mutationRate       = mutationRate;
            this.randomMutationRate = randomMutationRate;
            this.randomSeed         = randomSeed;
            randomizer              = new Random(randomSeed);
         }


         Gene(String name, int value, int min, int max, int delta,
              float mutationRate, float randomMutationRate, int randomSeed)
         {
            type                    = INTEGER_VALUE;
            this.name               = new String(name);
            ivalue                  = imin = imax = idelta = 0;
            fvalue                  = fmin = fmax = fdelta = 0.0f;
            dvalue                  = dmin = dmax = ddelta = 0.0;
            ivalue                  = value;
            imin                    = min;
            imax                    = max;
            idelta                  = delta;
            this.mutationRate       = mutationRate;
            this.randomMutationRate = randomMutationRate;
            this.randomSeed         = randomSeed;
            randomizer              = new Random(randomSeed);
         }


         Gene(String name, float value, float min, float max, float delta,
              float mutationRate, float randomMutationRate, int randomSeed)
         {
            type                    = FLOAT_VALUE;
            this.name               = new String(name);
            ivalue                  = imin = imax = idelta = 0;
            fvalue                  = fmin = fmax = fdelta = 0.0f;
            dvalue                  = dmin = dmax = ddelta = 0.0;
            fvalue                  = value;
            fmin                    = min;
            fmax                    = max;
            fdelta                  = delta;
            this.mutationRate       = mutationRate;
            this.randomMutationRate = randomMutationRate;
            this.randomSeed         = randomSeed;
            randomizer              = new Random(randomSeed);
         }


         Gene(String name, double value, double min, double max, double delta,
              float mutationRate, float randomMutationRate, int randomSeed)
         {
            type                    = DOUBLE_VALUE;
            this.name               = new String(name);
            ivalue                  = imin = imax = idelta = 0;
            fvalue                  = fmin = fmax = fdelta = 0.0f;
            dvalue                  = dmin = dmax = ddelta = 0.0;
            dvalue                  = value;
            dmin                    = min;
            dmax                    = max;
            ddelta                  = delta;
            this.mutationRate       = mutationRate;
            this.randomMutationRate = randomMutationRate;
            this.randomSeed         = randomSeed;
            randomizer              = new Random(randomSeed);
         }


         // Mutate gene.
         void mutate()
         {
            int    i;
            float  f;
            double d;

            if (randomizer.nextFloat() > mutationRate)
            {
               return;
            }

            switch (type)
            {
            case INTEGER_VALUE:
               if (randomizer.nextFloat() <= randomMutationRate)
               {
                  i = imax - imin;
                  if (i > 0)
                  {
                     ivalue = randomizer.nextInt(imax - imin) + imin;
                  }
                  else
                  {
                     ivalue = imin;
                  }
               }
               else
               {
                  i = ivalue;
                  if (randomizer.nextBoolean())
                  {
                     i += idelta;
                     if (i > imax) { i = imax; }
                  }
                  else
                  {
                     i -= idelta;
                     if (i < imin) { i = imin; }
                  }
                  ivalue = i;
               }
               break;

            case FLOAT_VALUE:
               if (randomizer.nextFloat() <= randomMutationRate)
               {
                  fvalue = (randomizer.nextFloat() * (fmax - fmin)) + fmin;
               }
               else
               {
                  f = fvalue;
                  if (randomizer.nextBoolean())
                  {
                     f += fdelta;
                     if (f > fmax) { f = fmax; }
                  }
                  else
                  {
                     f -= fdelta;
                     if (f < fmin) { f = fmin; }
                  }
                  fvalue = f;
               }
               break;

            case DOUBLE_VALUE:
               if (randomizer.nextFloat() <= randomMutationRate)
               {
                  dvalue = (randomizer.nextDouble() * (dmax - dmin)) + dmin;
               }
               else
               {
                  d = dvalue;
                  if (randomizer.nextBoolean())
                  {
                     d += ddelta;
                     if (d > dmax) { d = dmax; }
                  }
                  else
                  {
                     d -= ddelta;
                     if (d < dmin) { d = dmin; }
                  }
                  dvalue = d;
               }
               break;
            }
         }


         // Copy gene value.
         void copyValue(Gene from)
         {
            switch (type)
            {
            case INTEGER_VALUE:
               ivalue = from.ivalue;
               break;

            case FLOAT_VALUE:
               fvalue = from.fvalue;
               break;

            case DOUBLE_VALUE:
               dvalue = from.dvalue;
               break;
            }
         }


         // Load value.
         void loadValue(DataInputStream reader) throws IOException
         {
            int itype = Utility.loadInt(reader);

            switch (itype)
            {
            case 0:
               ivalue = Utility.loadInt(reader);
               break;

            case 1:
               fvalue = Utility.loadFloat(reader);
               break;

            case 2:
               dvalue = Utility.loadDouble(reader);
               break;
            }
         }


         // Save value.
         void saveValue(DataOutputStream writer) throws IOException
         {
            switch (type)
            {
            case INTEGER_VALUE:
               Utility.saveInt(writer, 0);
               Utility.saveInt(writer, ivalue);
               break;

            case FLOAT_VALUE:
               Utility.saveInt(writer, 1);
               Utility.saveFloat(writer, fvalue);
               break;

            case DOUBLE_VALUE:
               Utility.saveInt(writer, 2);
               Utility.saveDouble(writer, dvalue);
               break;
            }
            writer.flush();
         }


         // Print gene.
         void print()
         {
            switch (type)
            {
            case INTEGER_VALUE:
               System.out.println(name + "=" + ivalue);
               break;

            case FLOAT_VALUE:
               System.out.println(name + "=" + fvalue);
               break;

            case DOUBLE_VALUE:
               System.out.println(name + "=" + dvalue);
               break;
            }
         }
      }

      // Genes.
      Vector<Gene> genes;

      // Mutation rate.
      float mutationRate;

      // Probability of random mutation.
      float randomMutationRate;

      // Random numbers.
      int    randomSeed;
      Random randomizer;

      // Constructor.
      Genome(float mutationRate, float randomMutationRate, int randomSeed)
      {
         this.mutationRate       = mutationRate;
         this.randomMutationRate = randomMutationRate;
         this.randomSeed         = randomSeed;
         randomizer = new Random(randomSeed);
         genes      = new Vector<Gene>();
      }


      // Mutate.
      void mutate()
      {
         for (int i = 0; i < genes.size(); i++)
         {
            genes.get(i).mutate();
         }
      }


      // Randomly merge genome values from given genome.
      void meldValues(Genome from1, Genome from2)
      {
         Gene gene;

         for (int i = 0; i < genes.size(); i++)
         {
            gene = genes.get(i);
            if (randomizer.nextBoolean())
            {
               gene.copyValue(from1.genes.get(i));
            }
            else
            {
               gene.copyValue(from2.genes.get(i));
            }
         }
      }


      // Copy genome values from given genome.
      void copyValues(Genome from)
      {
         Gene gene;

         for (int i = 0; i < genes.size(); i++)
         {
            gene = genes.get(i);
            gene.copyValue(from.genes.get(i));
         }
      }


      // Get genome as key-value pairs.
      HashMap<String, Object> getKeyValues()
      {
         Gene gene;

         HashMap<String, Object> map = new HashMap<String, Object>();

         for (int i = 0; i < genes.size(); i++)
         {
            gene = genes.get(i);
            switch (gene.type)
            {
            case Gene.INTEGER_VALUE:
               map.put(new String(gene.name), new Integer(gene.ivalue));
               break;

            case Gene.FLOAT_VALUE:
               map.put(new String(gene.name), new Float(gene.fvalue));
               break;

            case Gene.DOUBLE_VALUE:
               map.put(new String(gene.name), new Double(gene.dvalue));
               break;
            }
         }
         return(map);
      }


      // Load values.
      void loadValues(DataInputStream reader) throws IOException
      {
         for (int i = 0; i < genes.size(); i++)
         {
            genes.get(i).loadValue(reader);
         }
      }


      // Save values.
      void saveValues(DataOutputStream writer) throws IOException
      {
         for (int i = 0; i < genes.size(); i++)
         {
            genes.get(i).saveValue(writer);
         }
         writer.flush();
      }


      // Print genome.
      void print()
      {
         for (int i = 0; i < genes.size(); i++)
         {
            genes.get(i).print();
         }
      }
   }

   // Generations.
   int Generation;
   int Generations;

   // Steps.
   int Steps;

   // File names.
   String      InputFileName;
   String      OutputFileName;
   String      LogFileName;
   PrintWriter LogWriter;

   // Random numbers.
   Random Randomizer;

   // Print population properties.
   boolean PrintProperties;

   // Print optimization statistics.
   boolean PrintStatistics;

   // Optimization statistics.
   float[] Fittest;
   float[] Average;

   // Population.
   Member[] Population;

   // Constructor.
   public ForagingParameterOptimizer(String[] args)
   {
      int i;

      // Get options.
      Generation    = 0;
      Generations   = -1;
      Steps         = -1;
      InputFileName = OutputFileName = LogFileName = null;
      LogWriter     = null;
      boolean gotFitPopulationSize  = false;
      boolean gotNumMutants         = false;
      boolean gotNumOffspring       = false;
      boolean gotMutationRate       = false;
      boolean gotRandomMutationRate = false;
      boolean gotRandomSeed         = false;
      PrintProperties = false;
      PrintStatistics = false;

      for (i = 0; i < args.length; i++)
      {
         if (args[i].equals("-generations"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            Generations = Integer.parseInt(args[i]);
            if (Generations < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }

         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            Steps = Integer.parseInt(args[i]);
            if (Steps < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }

         if (args[i].equals("-input"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            InputFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-output"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            OutputFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-fitPopulationSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            FIT_POPULATION_SIZE = Integer.parseInt(args[i]);
            if (FIT_POPULATION_SIZE < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotFitPopulationSize = true;
            continue;
         }

         if (args[i].equals("-numMutants"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_MUTANTS = Integer.parseInt(args[i]);
            if (NUM_MUTANTS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotNumMutants = true;
            continue;
         }

         if (args[i].equals("-numOffspring"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_OFFSPRING = Integer.parseInt(args[i]);
            if (NUM_OFFSPRING < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotNumOffspring = true;
            continue;
         }

         if (args[i].equals("-mutationRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            MUTATION_RATE = Float.parseFloat(args[i]);
            if ((MUTATION_RATE < 0.0f) || (MUTATION_RATE > 1.0f))
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotMutationRate = true;
            continue;
         }

         if (args[i].equals("-randomMutationRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            RANDOM_MUTATION_RATE = Float.parseFloat(args[i]);
            if ((RANDOM_MUTATION_RATE < 0.0f) || (RANDOM_MUTATION_RATE > 1.0f))
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotRandomMutationRate = true;
            continue;
         }

         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            RANDOM_SEED   = Integer.parseInt(args[i]);
            gotRandomSeed = true;
            continue;
         }

         if (args[i].equals("-logfile"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            LogFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-properties"))
         {
            PrintProperties = true;
            continue;
         }

         if (args[i].equals("-statistics"))
         {
            PrintStatistics = true;
            continue;
         }

         System.err.println(Usage);
         System.exit(1);
      }

      // Print properties?
      if (PrintProperties || PrintStatistics)
      {
         if ((Generations != -1) || (Steps != -1) ||
             (InputFileName == null) ||
             (OutputFileName != null) ||
             (LogFileName != null) ||
             gotFitPopulationSize || gotNumMutants || gotNumOffspring ||
             gotMutationRate || gotRandomMutationRate || gotRandomSeed)
         {
            System.err.println(Usage);
            System.exit(1);
         }
      }
      else
      {
         if (Generations == -1)
         {
            System.err.println("Generations option required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (Steps == -1)
         {
            System.err.println("Steps option required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (OutputFileName == null)
         {
            System.err.println("Output file required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (InputFileName != null)
         {
            if (gotFitPopulationSize || gotNumMutants || gotNumOffspring ||
                gotMutationRate || gotRandomMutationRate || gotRandomSeed)
            {
               System.err.println(Usage);
               System.exit(1);
            }
         }
      }

      // Seed random numbers.
      Randomizer = new Random(RANDOM_SEED);

      // Open log file?
      if (LogFileName != null)
      {
         try
         {
            LogWriter = new PrintWriter(new FileOutputStream(new File(LogFileName)));
         }
         catch (Exception e) {
            System.err.println("Cannot open log file " + LogFileName +
                               ":" + e.getMessage());
            System.exit(1);
         }
      }
   }


   // Run optimization.
   public void run()
   {
      // Initialize populations?
      if (InputFileName == null)
      {
         init();
      }
      else
      {
         // Load populations.
         load();
      }

      // Log run.
      log("Initializing optimization:");
      log("  Options:");
      log("    generations=" + Generations);
      log("    steps=" + Steps);
      if (InputFileName != null)
      {
         log("    input=" + InputFileName);
      }
      log("    output=" + OutputFileName);
      log("    FIT_POPULATION_SIZE=" + FIT_POPULATION_SIZE);
      log("    NUM_MUTANTS=" + NUM_MUTANTS);
      log("    NUM_OFFSPRING=" + NUM_OFFSPRING);
      log("    MUTATION_RATE=" + MUTATION_RATE);
      log("    RANDOM_MUTATION_RATE=" + RANDOM_MUTATION_RATE);
      log("    RANDOM_SEED=" + RANDOM_SEED);

      // Print population properties?
      if (PrintProperties)
      {
         printProperties();
         return;
      }

      // Print optimization statistics?
      if (PrintStatistics)
      {
         printStatistics();
         return;
      }

      // Optimization loop.
      log("Begin optimization:");
      for (Generations += Generation; Generation < Generations; Generation++)
      {
         log("Generation=" + Generation);

         optimize(Generation);

         // Save populations?
         if ((Generation % SAVE_FREQUENCY) == 0)
         {
            save(Generation);
         }
      }

      // Save populations.
      save(Generation - 1);

      log("End optimization");
   }


   // Initialize optimization.
   void init()
   {
      int i;

      Population = new Member[POPULATION_SIZE];
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         if (i == 0)
         {
            Population[i] = new Member(0, Randomizer);
            Population[i].evaluate(Steps, this);
         }
         else
         {
            // Mutate parameters.
            Population[i] = new Member(Population[0], 0, Randomizer);
            Population[i].evaluate(Steps, this);
         }
      }
      Fittest = new float[Generations + 1];
      Average = new float[Generations + 1];
   }


   // Load optimization.
   void load()
   {
      int             i;
      FileInputStream input  = null;
      DataInputStream reader = null;

      // Open the file.
      try
      {
         input  = new FileInputStream(new File(InputFileName));
         reader = new DataInputStream(input);
      }
      catch (Exception e) {
         System.err.println("Cannot open input file " + InputFileName +
                            ":" + e.getMessage());
      }

      try
      {
         Generation = Utility.loadInt(reader);
         Generation++;
      }
      catch (Exception e) {
         System.err.println("Cannot load from file " + InputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         // Load parameters.
         loadParameters(reader);

         // Load population.
         Population = new Member[POPULATION_SIZE];
         for (i = 0; i < POPULATION_SIZE; i++)
         {
            Population[i] = new Member(0, Randomizer);
            Population[i].load(input);
         }
         Fittest = new float[Generation + Generations + 1];
         Average = new float[Generation + Generations + 1];
         for (i = 0; i < Generation; i++)
         {
            Fittest[i] = Utility.loadFloat(reader);
            Average[i] = Utility.loadFloat(reader);
         }
         input.close();
      }
      catch (Exception e) {
         System.err.println("Cannot load populations from file " + InputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }
   }


   // Save optimization.
   void save(int generation)
   {
      int              i, n;
      FileOutputStream output = null;
      DataOutputStream writer = null;

      try
      {
         output = new FileOutputStream(new File(OutputFileName));
         writer = new DataOutputStream(new BufferedOutputStream(output));
      }
      catch (Exception e) {
         System.err.println("Cannot open output file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         Utility.saveInt(writer, generation);
         writer.flush();
      }
      catch (Exception e) {
         System.err.println("Cannot save to file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         // Save parameters.
         saveParameters(writer);

         // Save population.
         for (i = 0; i < POPULATION_SIZE; i++)
         {
            Population[i].save(output);
         }
         for (i = 0, n = generation + 1; i < n; i++)
         {
            Utility.saveFloat(writer, Fittest[i]);
            Utility.saveFloat(writer, Average[i]);
         }
         writer.flush();
         output.close();
      }
      catch (Exception e) {
         System.err.println("Cannot save populations to file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }
   }


   // Optimization generation.
   void optimize(int generation)
   {
      String logEntry;

      log("Population:");
      for (int i = 0; i < POPULATION_SIZE; i++)
      {
         logEntry = "    member=" + i + ", " + Population[i].getInfo();
         log(logEntry);
      }

      // Prune unfit members.
      prune();

      // Create new members by mutation.
      mutate();

      // Create new members by mating.
      mate();
   }


   // Prune unfit members.
   void prune()
   {
      float min, f;
      int   i, j, m;

      Member member;

      log("Select:");
      Member[] fitPopulation = new Member[FIT_POPULATION_SIZE];
      min = INVALID_FITNESS;
      for (i = 0; i < FIT_POPULATION_SIZE; i++)
      {
         m = -1;
         for (j = 0; j < POPULATION_SIZE; j++)
         {
            member = Population[j];
            if (member == null)
            {
               continue;
            }
            if ((m == -1) || (member.fitness < min))
            {
               m   = j;
               min = member.fitness;
            }
         }
         member           = Population[m];
         Population[m]    = null;
         fitPopulation[i] = member;
         log("    " + member.getInfo());
      }
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         if (Population[i] != null)
         {
            Population[i] = null;
         }
      }
      f = 0.0f;
      for (i = 0; i < FIT_POPULATION_SIZE; i++)
      {
         Population[i]    = fitPopulation[i];
         fitPopulation[i] = null;
         f += Population[i].fitness;
      }
      Fittest[Generation] = Population[0].fitness;
      Average[Generation] = f / (float)FIT_POPULATION_SIZE;
   }


   // Mutate members.
   void mutate()
   {
      int i, j;

      Member member, mutant;

      log("Mutate:");
      for (i = 0; i < NUM_MUTANTS; i++)
      {
         // Select a fit member to mutate.
         j      = Randomizer.nextInt(FIT_POPULATION_SIZE);
         member = Population[j];

         // Create mutant member.
         mutant = new Member(member, member.generation + 1, Randomizer);
         mutant.evaluate(Steps, this);
         Population[FIT_POPULATION_SIZE + i] = mutant;
         log("    member=" + j + ", " + member.getInfo() +
             " -> member=" + (FIT_POPULATION_SIZE + i) +
             ", " + mutant.getInfo());
      }
   }


   // Produce offspring by melding parent parameters.
   void mate()
   {
      int i, j, k;

      Member member1, member2, offspring;

      log("Mate:");
      if (FIT_POPULATION_SIZE > 1)
      {
         for (i = 0; i < NUM_OFFSPRING; i++)
         {
            // Select a pair of fit members to mate.
            j       = Randomizer.nextInt(FIT_POPULATION_SIZE);
            member1 = Population[j];
            while ((k = Randomizer.nextInt(FIT_POPULATION_SIZE)) == j) {}
            member2 = Population[k];

            // Create offspring.
            offspring = new Member(member1, member2,
                                   (member1.generation > member2.generation ?
                                    member1.generation : member2.generation) + 1, Randomizer);
            offspring.evaluate(Steps, this);
            Population[FIT_POPULATION_SIZE + NUM_MUTANTS + i] = offspring;
            log("    member=" + j + ", " + member1.getInfo() + " + member=" +
                k + ", " + member2.getInfo() +
                " -> member=" + (FIT_POPULATION_SIZE +
                                 NUM_MUTANTS + i) +
                ", " + offspring.getInfo());
         }
      }
   }


   // Print population properties.
   void printProperties()
   {
      int i;

      System.out.println("Population properties:");

      System.out.println("=============================");
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         System.out.println("-----------------------------");
         Population[i].printProperties();
      }
   }


   // Print optimization statistics.
   void printStatistics()
   {
      int i;

      System.out.println("Optimization statistics:");

      System.out.println("Generation\tFittest");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Fittest[i]);
      }
      System.out.println("Generation\tAverage");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Average[i]);
      }
   }


   // Logging.
   void log(String message)
   {
      if (LogWriter != null)
      {
         LogWriter.println(message);
         LogWriter.flush();
      }
   }


   // Parameter genome.
   public class ParmGenome extends Genome
   {
      // Constructor.
      public ParmGenome(Random randomizer)
      {
         super(MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt());

         genes.add(
            new Gene("HIVE_RADIUS", 3, 2, 3, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         genes.add(
            new Gene("NUM_FLOWERS", 3, 1, 6, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         genes.add(
            new Gene("NUM_BEES", 5, 1, 10, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         genes.add(
            new Gene("BEE_TURN_PROBABILITY", .1f, .01f, .5f, .01f,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         genes.add(
            new Gene("BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT", .01f, 0.0f, .05f, .01f,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));
      }


      // Mutate.
      public void mutate()
      {
         super.mutate();
      }
   }

   // ID dispenser.
   public int IDdispenser = 0;

   // Population member.
   public class Member
   {
      public int    id;
      public int    generation;
      public float  fitness;
      public Random randomizer;

      // Parameters.
      public ParmGenome parmGenome;

      // Constructors.
      public Member(int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create parameter genome.
         parmGenome = new ParmGenome(randomizer);
      }


      // Construct mutation of given member.
      public Member(Member member, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create and mutate parameter genome.
         parmGenome = new ParmGenome(randomizer);
         parmGenome.copyValues(member.parmGenome);
         parmGenome.mutate();
      }


      // Construct by mating given members.
      public Member(Member member1, Member member2, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create and meld parameter genome.
         parmGenome = new ParmGenome(randomizer);
         parmGenome.meldValues(member1.parmGenome, member2.parmGenome);
      }


      // Evaluate fitness.
      public void evaluate(int steps, ForagingParameterOptimizer optimizer)
      {
         fitness = INVALID_FITNESS;

         // Set parameters.
         Map<String, Object> parameters = parmGenome.getKeyValues();
         Parameters.HIVE_RADIUS          = (Integer)parameters.get("HIVE_RADIUS");
         Parameters.NUM_FLOWERS          = (Integer)parameters.get("NUM_FLOWERS");
         Parameters.NUM_BEES             = (Integer)parameters.get("NUM_BEES");
         Parameters.BEE_TURN_PROBABILITY = (Float)parameters.get("BEE_TURN_PROBABILITY");
         Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = (Float)parameters.get("BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT");

         // Run with cooperative foraging.
         int   randomSeed = optimizer.Randomizer.nextInt();
         World world      = new World(randomSeed);
         for (int i = 0; i < steps; i++)
         {
            world.step();
         }
         int collectedNectar = world.collectedNectar;

         // Run without foraging cooperation.
         world = new World(randomSeed);
         for (int i = 0; i < steps; i++)
         {
            world.step();
         }

         // Determine fitness.
         fitness = (float)(collectedNectar - world.collectedNectar);
         if (fitness < 0.0f) { fitness = 0.0f; }
      }


      // Load member.
      void load(FileInputStream input) throws IOException
      {
         // DataInputStream is for unbuffered input.
         DataInputStream reader = new DataInputStream(input);

         id = Utility.loadInt(reader);
         if (id >= IDdispenser)
         {
            IDdispenser = id + 1;
         }
         generation = Utility.loadInt(reader);
         fitness    = Utility.loadFloat(reader);

         // Load parameter genome.
         parmGenome.loadValues(reader);
      }


      // Save member.
      void save(FileOutputStream output) throws IOException
      {
         DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(output));

         Utility.saveInt(writer, id);
         Utility.saveInt(writer, generation);
         Utility.saveFloat(writer, fitness);
         writer.flush();

         // Save parameter genome.
         parmGenome.saveValues(writer);
         writer.flush();
      }


      // Print properties.
      void printProperties()
      {
         System.out.println(getInfo());
         System.out.println("parameters:");
         parmGenome.print();
      }


      // Get information.
      String getInfo()
      {
         return("id=" + id +
                ", fitness=" + fitness +
                ", generation=" + generation);
      }
   }

   // Main.
   public static void main(String[] args)
   {
      ForagingParameterOptimizer optimizer = new ForagingParameterOptimizer(args);

      optimizer.run();
      System.exit(0);
   }
}
