// For conditions of distribution and use, see copyright notice in Main.java

// Foraging RNN.
// Test the ability of an RNN to forage when allowed to sense its relative location
// to the hive upon discovering nectar. The RNN must return to the hive and deposit nectar.
// If surplus nectar was sensed, it must also perform an appropriate dance and move toward
// the nectar.
//
// Input:
// <nectar presence><nectar carry><orientation (x8)><hive presence (25=5x5)>
// Target:
// <response>

package morphognosis.honey_bees;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.training.GradientDescent;
import morphognosis.Orientation;
import de.jannlab.misc.TimeCounter;
import de.jannlab.tools.ClassificationValidator;

public class ForagingRNN
{
   // Parameters.
   public static double  LEARNING_RATE      = 0.001;
   public static double  MOMENTUM           = 0.9;
   public static int     NUM_HIDDEN_NEURONS = 5;
   public static int     NUM_HIDDEN_LAYERS  = 1;
   public static int     EPOCHS             = 200;
   public static int     RANDOM_SEED        = 4517;
   public static boolean VERBOSE            = false;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.ForagingRNN\n" +
      "     [-numFlowers <quantity> (default=" + Parameters.NUM_FLOWERS + ")]\n" +
      "     [-learningRate <float> (default=" + LEARNING_RATE + ")]\n" +
      "     [-momentum <float> (default=" + MOMENTUM + ")]\n" +
      "     [-numHiddenNeurons <quantity> (default=" + NUM_HIDDEN_NEURONS + ")]\n" +
      "     [-numHiddenLayers <quantity> (default=" + NUM_HIDDEN_LAYERS + ")]\n" +
      "     [-epochs <quantity> (default=" + EPOCHS + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]\n" +
      "     [-exportCsvDataset <filename>]\n" +
      "     [-exportPythonDataset <filename>]\n" +
      "     [-verbose (default=" + VERBOSE + ")]\n" +
      "     [-useFlowerIds (default=false)]\n" +
      "     [-maxSequenceLength (default=-1(off))]\n" +
      "     [-fixedOrientation (default=-1(off))]\n" +
      "     [-noDanceLearn (default=false)]";

   // NN input size.
   public static int NN_INPUT_SIZE = 35;

   // Datasets.
   private static String CSVdatasetFilename    = null;
   private static String pythonDatasetFilename = null;

   private static TimeCounter TC     = new TimeCounter();
   private static Random      random = new Random();

   // Options.
   public static boolean useFlowerIds      = false;
   public static int     maxSequenceLength = -1;
   public static int     fixedOrientation  = -1;
   public static boolean noDanceLearn      = false;

   // Input is a sequence of sensor values, target is a sequence of responses.
   public static Sample generateSequence(World world, int flower,
                                         int orientation, boolean surplusNectar)
   {
      world.reset();
      HoneyBee bee = world.bees[0];

      if (VERBOSE)
      {
         System.out.println("generate sequence" + ", flower=" + flower +
                            ", orientation=" + orientation + ", surplus nectar=" + surplusNectar + ":");
      }

      // Remove other flowers.
      int fx = -1;
      int fy = -1;
      for (int x = 0, i = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            if (world.cells[x][y].flower != null)
            {
               if (i == flower)
               {
                  fx = x;
                  fy = y;
                  world.cells[fx][fy].flower.nectar = true;
               }
               else
               {
                  world.cells[x][y].flower = null;
               }
               i++;
            }
         }
      }

      // Forage to flower.
      while (world.cells[bee.x][bee.y].flower == null)
      {
         world.step();
      }
      bee.orientation = orientation;
      ArrayList<double[]> inputSeq     = new ArrayList<double[]>();
      ArrayList<double[]> targetSeq    = new ArrayList<double[]>();
      int                 extractCount = 0;
      int                 depositCount = 0;
      boolean             first        = true;
      while (first || bee.handlingNectar)
      {
         if ((maxSequenceLength != -1) && (inputSeq.size() >= maxSequenceLength)) { break; }
         double[] input = new double[NN_INPUT_SIZE];
         String nectarPresence = "false";
         if (world.cells[bee.x][bee.y].flower != null)
         {
            if (world.cells[bee.x][bee.y].flower.nectar)
            {
               input[0]       = 1.0;
               nectarPresence = "true";
            }
         }
         String nectarCarry = "false";
         if (bee.nectarCarry)
         {
            input[1]    = 1.0;
            nectarCarry = "true";
         }
         int o = bee.orientation;
         input[2 + bee.orientation] = 1.0;
         int hx = -1;
         int hy = -1;
         int bx = -1;
         int by = -1;
         if (useFlowerIds)
         {
            if (world.cells[bee.x][bee.y].hive)
            {
               input[10] = 1.0;
            }
            else
            {
               input[10] = 0.0;
            }
            if (first)
            {
               input[11 + flower] = 1.0;
            }
         }
         else
         {
            hx = -1;
            hy = -1;
            bx = bee.x;
            by = bee.y;
            if (first || world.cells[bee.x][bee.y].hive)
            {
               bx = bee.x;
               hx = bee.x / 4;
               if (hx > 4) { hx = 4; }
               hx = 4 - hx;
               by = bee.y;
               hy = bee.y / 4;
               if (hy > 4) { hy = 4; }
               hy = 4 - hy;
               input[10 + (hy * 5) + hx] = 1.0;
            }
         }
         first = false;
         world.step();
         if (bee.response == HoneyBee.EXTRACT_NECTAR) { extractCount++; }
         if (bee.response == HoneyBee.DEPOSIT_NECTAR) { depositCount++; }
         if (bee.handlingNectar && (extractCount < 2))
         {
            if (VERBOSE)
            {
               System.out.println("input: ");
               System.out.println("nectar presence [0]: " + input[0] + " (" + nectarPresence + ")");
               System.out.println("nectar carry [1]: " + input[1] + " (" + nectarCarry + ")");
               System.out.print("orientation [2-9]: ");
               for (int j = 2; j < 10; j++)
               {
                  System.out.print(input[j] + " ");
               }
               System.out.println("(" + Orientation.toName(o) + ")");
               if (useFlowerIds)
               {
                  if (input[10] == 1.0)
                  {
                     System.out.println("hive presence [10]: 1.0 (true)");
                  }
                  else
                  {
                     System.out.println("hive presence [10]: 0.0 (false)");
                  }
                  int k = 11 + Parameters.NUM_FLOWERS;
                  if (Parameters.NUM_FLOWERS == 1)
                  {
                     System.out.print("flower [11]: ");
                     for (int j = 11; j < k; j++)
                     {
                        System.out.print(input[j] + " ");
                     }
                  }
                  else
                  {
                     System.out.print("flowers [11-" + (k - 1) + "]: ");
                     for (int j = 11; j < k; j++)
                     {
                        System.out.print(input[j] + " ");
                     }
                  }
                  System.out.println();
               }
               else
               {
                  System.out.println("hive presence [10-34]:");
                  for (int i = NN_INPUT_SIZE - 5, j = NN_INPUT_SIZE, k = 0; k < 5; j -= 5, i = j - 5, k++)
                  {
                     for ( ; i < j; i++)
                     {
                        System.out.print(input[i] + " ");
                     }
                     System.out.println();
                  }
                  if (hx != -1)
                  {
                     System.out.println("(bee=[" + bx + "," + by + "]->[" + (bx / 4) + "," +
                                        (by / 4) + "], hive=[" + hx + "," + hy + "], index=" +
                                        (10 + (hy * 5) + hx + ")"));
                  }
               }
            }
            inputSeq.add(input);
            world.cells[fx][fy].flower.nectar = surplusNectar;
            double[] target      = new double[HoneyBee.NUM_RESPONSES];
            target[bee.response] = 1.0;
            if (VERBOSE)
            {
               System.out.println("target: ");
               System.out.print("response: ");
               for (int j = 0; j < HoneyBee.NUM_RESPONSES; j++)
               {
                  System.out.print(target[j] + " ");
               }
               System.out.println("(" + HoneyBee.getResponseName(bee.response) + ")");
            }
            targetSeq.add(target);
         }
      }
      if (depositCount == 0) { return(null); }
      if (VERBOSE)
      {
         System.out.println("sequence length=" + inputSeq.size());
      }
      double[] inputData = new double[inputSeq.size() * NN_INPUT_SIZE];
      int i = 0;

      for (double[] d : inputSeq)
      {
         for (int j = 0; j < NN_INPUT_SIZE; j++)
         {
            inputData[i] = d[j];
            i++;
         }
      }
      double[] targetData = new double[targetSeq.size() * HoneyBee.NUM_RESPONSES];
      i = 0;
      for (double[] d : targetSeq)
      {
         for (int j = 0; j < HoneyBee.NUM_RESPONSES; j++)
         {
            targetData[i] = d[j];
            i++;
         }
      }
      return(new Sample(inputData, targetData, NN_INPUT_SIZE, inputSeq.size(),
                        HoneyBee.NUM_RESPONSES, targetSeq.size()));
   }


   // Generate training data.
   public static SampleSet generateTrainingData(World world)
   {
      SampleSet sampleSet = new SampleSet();

      int max = -1;

      for (int i = 0; i < Parameters.NUM_FLOWERS; i++)
      {
         for (int j = 0; j < Orientation.NUM_ORIENTATIONS; j++)
         {
            int orientation = j;
            if (fixedOrientation != -1)
            {
               orientation = fixedOrientation;
            }
            boolean generated = false;
            for (int k = 0; k < 5; k++)
            {
               Sample s = generateSequence(world, i, orientation, false);
               if (s != null)
               {
                  sampleSet.add(s);
                  generated = true;
                  int n = s.getInputLength();
                  if ((max == -1) || (max < n))
                  {
                     max = n;
                  }
                  break;
               }
            }
            if (!generated)
            {
               System.err.println("Cannot generate sequence");
               System.exit(1);
            }
            if (!noDanceLearn)
            {
               generated = false;
               for (int k = 0; k < 5; k++)
               {
                  Sample s = generateSequence(world, i, orientation, true);
                  if (s != null)
                  {
                     sampleSet.add(s);
                     generated = true;
                     int n = s.getInputLength();
                     if ((max == -1) || (max < n))
                     {
                        max = n;
                     }
                     break;
                  }
               }
               if (!generated)
               {
                  System.err.println("Cannot generate sequence");
                  System.exit(1);
               }
            }
            if (fixedOrientation != -1) { break; }
         }
      }

      // Pad sequences to max size.
      if (max != -1)
      {
         SampleSet sampleSet2 = new SampleSet();
         for (Sample s : sampleSet)
         {
            int n = s.getInputLength();
            if (n < max)
            {
               double[] inputData2  = new double[max * NN_INPUT_SIZE];
               double[] targetData2 = new double[max * HoneyBee.NUM_RESPONSES];
               double[] inputData   = s.getInput();
               double[] targetData  = s.getTarget();
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  inputData2[i] = inputData[i];
               }
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  targetData2[i] = targetData[i];
               }
               int i = inputData.length;
               int t = targetData.length;
               for (int j = 0, k = max - n; j < k; j++)
               {
                  double[] input = new double[NN_INPUT_SIZE];
                  for (int p = 0; p < NN_INPUT_SIZE; p++, i++)
                  {
                     inputData2[i] = input[p];
                  }
                  double[] target       = new double[HoneyBee.NUM_RESPONSES];
                  target[HoneyBee.WAIT] = 1.0;
                  for (int p = 0; p < HoneyBee.NUM_RESPONSES; p++, t++)
                  {
                     targetData2[t] = target[p];
                  }
               }
               Sample s2 = new Sample(inputData2, targetData2, NN_INPUT_SIZE, max,
                                      HoneyBee.NUM_RESPONSES, max);
               sampleSet2.add(s2);
            }
            else
            {
               sampleSet2.add(s);
            }
         }
         sampleSet = sampleSet2;
      }
      if (VERBOSE)
      {
         System.out.println("training set size=" + sampleSet.size());
      }

      // Export CSV dataset?
      if (CSVdatasetFilename != null)
      {
         exportCsvDataset(sampleSet);
      }

      // Export Python dataset?
      if (pythonDatasetFilename != null)
      {
         exportPythonDataset(sampleSet);
      }

      return(sampleSet);
   }


   // Export CSV dataset.
   public static void exportCsvDataset(SampleSet sampleSet)
   {
      int numSequences = Parameters.NUM_FLOWERS * Orientation.NUM_ORIENTATIONS;

      if (fixedOrientation != -1)
      {
         numSequences = Parameters.NUM_FLOWERS;
      }
      if (!noDanceLearn)
      {
         numSequences *= 2;
      }

      FileOutputStream datasetOutput = null;
      PrintWriter      datasetWriter = null;
      try
      {
         datasetOutput = new FileOutputStream(new File(CSVdatasetFilename));
         datasetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(datasetOutput)));
         datasetWriter.write("training set size=" + numSequences +
                             ", input size=" + NN_INPUT_SIZE + ", target size=" + HoneyBee.NUM_RESPONSES + "\n");
         int sequence = 0;
         for (Sample s : sampleSet)
         {
            int n = s.getInputLength();
            datasetWriter.print("sequence=" + sequence++ + ", length=" + n + "\n");
            datasetWriter.print("input:\n");
            double[] inputData = s.getInput();
            for (int i = 0; i < n; i++)
            {
               for (int j = i * NN_INPUT_SIZE, k = j + NN_INPUT_SIZE; j < k; j++)
               {
                  datasetWriter.print(inputData[j] + " ");
               }
               datasetWriter.println();
            }
            datasetWriter.print("target:\n");
            double[] targetData = s.getTarget();
            for (int i = 0; i < n; i++)
            {
               for (int j = i * HoneyBee.NUM_RESPONSES, k = j + HoneyBee.NUM_RESPONSES; j < k; j++)
               {
                  datasetWriter.print(targetData[j] + " ");
               }
               datasetWriter.println();
            }
         }
      }
      catch (Exception e)
      {
         System.err.println("Cannot open dataset file " + CSVdatasetFilename + ":" + e.getMessage());
      }
      if (datasetWriter != null)
      {
         datasetWriter.flush();
         try {
            datasetOutput.close();
         }
         catch (IOException e) {}
      }
   }


   // Export Python dataset.
   public static void exportPythonDataset(SampleSet sampleSet)
   {
      if (sampleSet.size() == 0)
      {
         System.err.println("Cannot export python dataset: no data");
         return;
      }
      int numSequences = Parameters.NUM_FLOWERS * Orientation.NUM_ORIENTATIONS;
      if (fixedOrientation != -1)
      {
         numSequences = Parameters.NUM_FLOWERS;
      }
      if (!noDanceLearn)
      {
         numSequences *= 2;
      }
      FileOutputStream datasetOutput = null;
      PrintWriter      datasetWriter = null;
      try
      {
         datasetOutput = new FileOutputStream(new File(pythonDatasetFilename));
         datasetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(datasetOutput)));
         boolean first = true;
         for (Sample s : sampleSet)
         {
            double[] inputData = s.getInput();
            if (first)
            {
               first = false;
               int n = s.getInputLength();
               datasetWriter.println("X_shape = [" + numSequences + "," + n + "," + NN_INPUT_SIZE + "]");
               datasetWriter.print("X_seq = [");
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  if (i == 0)
                  {
                     datasetWriter.print(inputData[i] + "");
                  }
                  else
                  {
                     datasetWriter.print("," + inputData[i]);
                  }
               }
            }
            else
            {
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  datasetWriter.print("," + inputData[i]);
               }
            }
         }
         datasetWriter.println("]");
         first = true;
         for (Sample s : sampleSet)
         {
            double[] targetData = s.getTarget();
            if (first)
            {
               first = false;
               int n = s.getTargetLength();
               datasetWriter.println("y_shape = [" + numSequences + "," + n + "," + HoneyBee.NUM_RESPONSES + "]");
               datasetWriter.print("y_seq = [");
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  if (i == 0)
                  {
                     datasetWriter.print(targetData[i] + "");
                  }
                  else
                  {
                     datasetWriter.print("," + targetData[i]);
                  }
               }
            }
            else
            {
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  datasetWriter.print("," + targetData[i]);
               }
            }
         }
         datasetWriter.println("]");
      }
      catch (Exception e)
      {
         System.err.println("Cannot open dataset file " + pythonDatasetFilename + ":" + e.getMessage());
      }
      if (datasetWriter != null)
      {
         datasetWriter.flush();
         try {
            datasetOutput.close();
         }
         catch (IOException e) {}
      }
   }


   public static Net LSTM(final int in, final int hid, int layers, final int out)
   {
      LSTMGenerator gen = new LSTMGenerator();

      gen.inputLayer(in);
      for (int i = 0; i < layers; i++)
      {
         gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH, true);
      }
      gen.outputLayer(out, CellType.TANH);
      return(gen.generate());
   }


   public static void main(String[] args) throws IOException
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-numFlowers"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_FLOWERS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_FLOWERS < 0)
            {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-learningRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               LEARNING_RATE = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((LEARNING_RATE < 0.0) || (LEARNING_RATE > 1.0))
            {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-momentum"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MOMENTUM = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MOMENTUM < 0.0)
            {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numHiddenNeurons"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_HIDDEN_NEURONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_HIDDEN_NEURONS < 0)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numHiddenLayers"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_HIDDEN_LAYERS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_HIDDEN_LAYERS < 0)
            {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-epochs"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EPOCHS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCHS < 0)
            {
               System.err.println("Invalid epochs option");
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
               RANDOM_SEED = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-exportCsvDataset"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid exportCsvDataset option");
               System.err.println(Usage);
               System.exit(1);
            }
            CSVdatasetFilename = args[i];
            continue;
         }
         if (args[i].equals("-exportPythonDataset"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid exportPythonDataset option");
               System.err.println(Usage);
               System.exit(1);
            }
            pythonDatasetFilename = args[i];
            continue;
         }
         if (args[i].equals("-maxSequenceLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               maxSequenceLength = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-noDanceLearn"))
         {
            noDanceLearn = true;
            continue;
         }
         if (args[i].equals("-verbose"))
         {
            VERBOSE = true;
            continue;
         }
         if (args[i].equals("-useFlowerIds"))
         {
            useFlowerIds = true;
            continue;
         }
         if (args[i].equals("-fixedOrientation"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid fixedOrientation option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               fixedOrientation = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid fixedOrientation option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((fixedOrientation < -1) || (fixedOrientation >= HoneyBee.NUM_RESPONSES))
            {
               System.err.println("Invalid fixedOrientation option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-noDanceLearn"))
         {
            noDanceLearn = true;
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      //
      // generate train data.
      //
      if (useFlowerIds)
      {
         NN_INPUT_SIZE -= 24;
         NN_INPUT_SIZE += Parameters.NUM_FLOWERS;
      }
      Parameters.WORLD_WIDTH  = 21;
      Parameters.WORLD_HEIGHT = 21;
      Parameters.HIVE_RADIUS  = 3;
      Parameters.NUM_BEES     = 1;
      Parameters.FLOWER_SURPLUS_NECTAR_PROBABILITY = 0.0f;
      random.setSeed(RANDOM_SEED);
      World world = null;
      try
      {
         world = new World(RANDOM_SEED);
      }
      catch (Exception e)
      {
         System.err.println("Cannot initialize world: " + e.getMessage());
         System.exit(1);
      }
      if (VERBOSE) { System.out.println("Training data:"); }
      SampleSet trainset = generateTrainingData(world);

      //
      // build network.
      //
      Net net       = LSTM(NN_INPUT_SIZE, NUM_HIDDEN_NEURONS, NUM_HIDDEN_LAYERS, HoneyBee.NUM_RESPONSES);
      int maxlength = trainset.maxSequenceLength();
      net.rebuffer(maxlength);
      net.initializeWeights();
      //
      // setup network.
      //
      net.initializeWeights(random);
      net.rebuffer(maxlength);
      //
      // setup trainer.
      //
      GradientDescent trainer = new GradientDescent();
      trainer.setNet(net);
      trainer.setRnd(random);
      trainer.setPermute(true);
      trainer.setTrainingSet(trainset);
      trainer.setLearningRate(LEARNING_RATE);
      trainer.setMomentum(MOMENTUM);
      trainer.setEpochs(EPOCHS);
      //
      TC.reset();
      //
      // perform training.
      //
      trainer.train();
      //
      System.out.println("training time: " + TC.valueMilliDouble() + " ms.");
      //
      // evaluate learning success.
      //
      ClassificationValidator f = new ClassificationValidator(net);
      int correctResponses      = 0;
      int totalResponses        = 0;
      int correctForages        = 0;
      int totalForages          = 0;
      int setSize = trainset.size();
      System.out.println("number of forages=" + setSize);
      System.out.println("training results:");
      for (int i = 0; i < setSize; i++)
      {
         System.out.println("forage=" + i);
         boolean forageCorrect = true;
         Sample  s             = trainset.get(i);
         int     seqLength     = s.getInputLength();
         double[] inputData  = s.getInput();
         double[] targetData = s.getTarget();
         for (int j = 0; j < seqLength; j++)
         {
            System.out.println("step=" + j);
            Sample s2 = new Sample(inputData, targetData, NN_INPUT_SIZE, j + 1,
                                   HoneyBee.NUM_RESPONSES, j + 1);
            double[] prediction = f.apply(s2);
            System.out.print("prediction: ");
            int    predictionIdx = -1;
            double maxval        = -1.0;
            for (int k = 0; k < prediction.length; k++)
            {
               System.out.printf("%.2f ", prediction[k]);
               if ((predictionIdx == -1) || (maxval < prediction[k]))
               {
                  predictionIdx = k;
                  maxval        = prediction[k];
               }
            }
            System.out.println(" (" + HoneyBee.getResponseName(predictionIdx) + ")");
            System.out.print("target: ");
            int targetIdx = -1;
            for (int k = 0, q = j * HoneyBee.NUM_RESPONSES; k < HoneyBee.NUM_RESPONSES; k++)
            {
               System.out.print(targetData[q + k] + " ");
               if (targetData[q + k] == 1.0)
               {
                  targetIdx = k;
               }
            }
            System.out.println(" (" + HoneyBee.getResponseName(targetIdx) + ")");
            totalResponses++;
            if (targetIdx == predictionIdx)
            {
               correctResponses++;
               System.out.println("OK");
            }
            else
            {
               forageCorrect = false;
               System.out.println("error");
            }
         }
         if (forageCorrect)
         {
            correctForages++;
            System.out.println("forage OK");
         }
         else
         {
            System.out.println("forage error");
         }
         totalForages++;
      }
      System.out.print("correct/total responses=" + correctResponses + "/" + totalResponses);
      if (totalResponses > 0)
      {
         System.out.printf(" (%.2f", ((double)correctResponses / (double)totalResponses) * 100.0);
         System.out.println("%)");
      }
      System.out.print("correct/total forages=" + correctForages + "/" + totalForages);
      if (totalForages > 0)
      {
         System.out.printf(" (%.2f", ((double)correctForages / (double)totalForages) * 100.0);
         System.out.println("%)");
      }
      //double ratio = f.ratio();;
      //System.out.printf("classification results=%.2f", ratio * 100.0);
      //System.out.println("%");
   }
}
