// For conditions of distribution and use, see copyright notice in Main.java

// Foraging RNN.
// Test the ability of an RNN to track relative location, a skill necessary to return to hive.
// There are 9 possible responses: 8 orientations plus a forward movement.
// Classification: the orientation to the initial location.

package morphognosis.honey_bees;

import java.io.IOException;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.training.GradientDescent;
import de.jannlab.misc.TimeCounter;
import de.jannlab.tools.ClassificationValidator;
import de.jannlab.tools.RegressionValidator;

public class ForagingRNN
{
   // Parameters.
   public static double LEARNING_RATE       = 0.001;
   public static double MOMENTUM            = 0.9;
   public static int    NUM_HIDDEN_NEURONS  = 5;
   public static int    NUM_HIDDEN_LAYERS   = 1;
   public static float  TURN_PROBABILITY    = 0.5f;
   public static int    NUM_TRAIN           = 5;
   public static int    NUM_TEST            = 1;
   public static int    MIN_SEQUENCE_LENGTH = 5;
   public static int    MAX_SEQUENCE_LENGTH = 15;
   public static int    EPOCHS      = 200;
   public static int    RANDOM_SEED = 4517;

   private static TimeCounter TC  = new TimeCounter();
   private static Random      rnd = new Random(RANDOM_SEED);

   // Sample is a sequence of responses, where each response is an orientation (x8) or a forward movement.
   public static Sample generateSample(final int length, boolean verbose)
   {
      double[] data = new double[length * 9];
      //
      int x           = 0;
      int y           = 0;
      int orientation = 0;
      if (verbose) { System.out.println("data:"); }
      for (int i = 0; i < length; i++)
      {
         int response = 8;
         if (rnd.nextFloat() < TURN_PROBABILITY)
         {
            response = rnd.nextInt(8);
         }
         data[(i * 9) + response] = 1.0;
         if (verbose)
         {
            for (int j = 0; j < 9; j++)
            {
               System.out.print(data[(i * 9) + j] + " ");
            }
            System.out.println();
         }
         if (response < 8)
         {
            orientation = response;
         }
         else
         {
            switch (orientation)
            {
            case 0:
               y++;
               break;

            case 1:
               x++;
               y++;
               break;

            case 2:
               x++;
               break;

            case 3:
               x++;
               y--;
               break;

            case 4:
               y--;
               break;

            case 5:
               x--;
               y--;
               break;

            case 6:
               x--;
               break;

            case 7:
               x--;
               y++;
               break;
            }
         }
      }
      //
      double[] target = new double[9];
      if (x > 0)
      {
         if (y > 0)
         {
            target[1] = 1.0;
         }
         else if (y < 0)
         {
            target[3] = 1.0;
         }
         else
         {
            target[2] = 1.0;
         }
      }
      else if (x < 0)
      {
         if (y > 0)
         {
            target[7] = 1.0;
         }
         else if (y < 0)
         {
            target[5] = 1.0;
         }
         else
         {
            target[6] = 1.0;
         }
      }
      else
      {
         if (y > 0)
         {
            target[0] = 1.0;
         }
         else if (y < 0)
         {
            target[4] = 1.0;
         }
         else
         {
            target[8] = 1.0;
         }
      }
      if (verbose)
      {
         System.out.println("target:");
         for (int j = 0; j < 9; j++)
         {
            System.out.print(target[j] + " ");
         }
         System.out.println();
      }
      return(new Sample(data, target, 9, length, 9, 1));
   }


   public static SampleSet generate(int n)
   {
      SampleSet set = new SampleSet();

      //
      for (int i = 0; i < n; i++)
      {
         int length = rnd.nextInt((MAX_SEQUENCE_LENGTH - MIN_SEQUENCE_LENGTH + 1)) + MIN_SEQUENCE_LENGTH;
         set.add(generateSample(length, true));
      }
      //
      return(set);
   }


   public static Net LSTM(final int in, final int hid, int layers, final int out)
   {
      //
      LSTMGenerator gen = new LSTMGenerator();

      gen.inputLayer(in);
      for (int i = 0; i < layers; i++)
      {
         gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH, true);
      }
      gen.outputLayer(out, CellType.TANH);
      //
      return(gen.generate());
   }


   public static void main(String[] args) throws IOException
   {
      //
      // generate train and test data.
      //
      SampleSet trainset = generate(NUM_TRAIN);
      SampleSet testset  = generate(NUM_TEST);
      //
      // build network.
      //
      Net net    = LSTM(9, NUM_HIDDEN_NEURONS, NUM_HIDDEN_LAYERS, 8);
      int length = Math.max(trainset.maxSequenceLength(), testset.maxSequenceLength());

      net.rebuffer(length);
      net.initializeWeights();
      //
      // setup network.
      //
      final int maxlength = Math.max(trainset.maxSequenceLength(), testset.maxSequenceLength());
      net.initializeWeights(rnd);
      net.rebuffer(maxlength);
      //
      // setup trainer.
      //
      GradientDescent trainer = new GradientDescent();
      trainer.setNet(net);
      trainer.setRnd(rnd);
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
      System.out.println(
         "training time: " +
         TC.valueMilliDouble() +
         " ms."
         );
      //
      // evaluate learning success.
      //
      double              thres = 0.04;
      RegressionValidator v     = new RegressionValidator(net, thres);
      for (Sample s : trainset)
      {
         v.apply(s);
      }
      System.out.println("regression validation trainset result: " + (v.ratio() * 100) + "%.");
      v = new RegressionValidator(net, thres);
      for (Sample s : testset)
      {
         v.apply(s);
      }
      System.out.println("regression validator testset result: " + (v.ratio() * 100) + "%.");
      thres = 0.1;
      ClassificationValidator f = new ClassificationValidator(net, thres);
      for (int i = 0; i < trainset.size(); i++)
      {
         Sample s = trainset.get(i);
         f.apply(s);
      }
      double ratio = f.ratio();
      System.out.println("classification validator trainset result: " + ratio * 100.0 + "%.");
      f = new ClassificationValidator(net, thres);
      for (int i = 0; i < testset.size(); i++)
      {
         Sample s = testset.get(i);
         f.apply(s);
      }
      ratio = f.ratio();
      System.out.println("classification validator testset result: " + ratio * 100.0 + "%.");
   }
}
