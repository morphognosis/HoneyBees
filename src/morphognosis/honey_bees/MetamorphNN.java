// For conditions of distribution and use, see copyright notice in Main.java

// Metamorph neural network.

package morphognosis.honey_bees;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MetamorphNN
{
   // Neural network model.
   public MultilayerPerceptron mlp;
   public int numAttributes;

   // Random numbers.
   public SecureRandom random;

   // Constructor.
   public MetamorphNN(SecureRandom random)
   {
      this.random = random;
   }


   // Train metamorphs.
   public void train(ArrayList<Metamorph> metamorphs)
   {
      mlp = null;
      if (metamorphs.size() == 0) { return; }

      // Create metamorph training dataset.
      Morphognostic        morphognostic  = metamorphs.get(0).morphognostic;
      ArrayList<Attribute> attributeNames = new ArrayList<Attribute>();
      for (int i = 0; i < morphognostic.numImmediateEvents; i++)
      {
         attributeNames.add(new Attribute(i + ""));
      }
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
                  {
                     attributeNames.add(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
                  }
               }
            }
         }
      }
      ArrayList<String> responseVals = new ArrayList<String>();
      for (int i = 0; i < HoneyBee.NUM_RESPONSES; i++)
      {
         responseVals.add(i + "");
      }
      attributeNames.add(new Attribute("response", responseVals));
      Instances metamorphInstances = new Instances("metamorphs", attributeNames, 0);
      numAttributes = attributeNames.size();
      for (Metamorph metamorph : metamorphs)
      {
         metamorphInstances.add(createInstance(metamorph.morphognostic, metamorph.response));
      }
      metamorphInstances.setClassIndex(numAttributes - 1);

      // Create model.
      mlp = new MultilayerPerceptron();
      mlp.setLearningRate(Parameters.NN_LEARNING_RATE);
      mlp.setMomentum(Parameters.NN_MOMENTUM);
      mlp.setHiddenLayers(Parameters.NN_HIDDEN_LAYERS);
      mlp.setTrainingTime(Parameters.NN_TRAINING_TIME);

      // Train model.
      try
      {
         mlp.buildClassifier(metamorphInstances);
      }
      catch (Exception e)
      {
         System.err.println("Cannot train neural network model: " + e.getMessage());
         e.printStackTrace();
      }

      // Evaluate model.
      try
      {
         Evaluation eval = new Evaluation(metamorphInstances);
         eval.evaluateModel(mlp, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
      catch (Exception e)
      {
         System.err.println("Cannot evaluate neural network model: " + e.getMessage());
         e.printStackTrace();
      }
   }


   // Create instance.
   public Instance createInstance(Morphognostic morphognostic, int response)
   {
      double[]  attrValues = new double[numAttributes];
      int a = 0;
      for (int i = 0; i < morphognostic.numImmediateEvents; i++)
      {
         attrValues[a] = morphognostic.immediateEvents[i];
         a++;
      }
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               Morphognostic.Neighborhood.Sector s = morphognostic.neighborhoods.get(i).sectors[x][y];
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < s.typeDensities[d].length; j++)
                  {
                     attrValues[a] = s.typeDensities[d][j];
                     a++;
                  }
               }
            }
         }
      }
      attrValues[a] = (double)response;
      a++;
      return(new DenseInstance(1.0, attrValues));
   }


   // Respond.
   public int respond(Morphognostic morphognostic)
   {
      return(respond(morphognostic, false));
   }


   public int respond(Morphognostic morphognostic, boolean probabilistic)
   {
      if (mlp == null)
      {
         System.err.println("Warning: cannot get metamorph neural network response: model is null");
         return(HoneyBee.WAIT);
      }
      Instance morphognosticInstance = createInstance(morphognostic, 0);
      int      response = HoneyBee.WAIT;

      try
      {
         if (probabilistic)
         {
            return(respondProbabilistic(mlp.distributionForInstance(morphognosticInstance)));
         }
         else
         {
            response = (int)mlp.classifyInstance(morphognosticInstance);
         }
      }
      catch (Exception e)
      {
         System.err.println("Cannot get response from neural network: " + e.getMessage());
         e.printStackTrace();
      }
      return(response);
   }


   // Respond probabilistically.
   public int respondProbabilistic(double[] probabilities)
   {
      double d = random.nextDouble();
      double c = 0.0;

      for (int response = 0, i = probabilities.length; response < i; response++)
      {
         if (d < (c + probabilities[response]))
         {
            return(response);
         }
         c += probabilities[response];
      }
      return(HoneyBee.WAIT);
   }


   // Save model to file.
   public boolean saveModel(String filename)
   {
      if (mlp != null)
      {
         try
         {
            Debug.saveToFile(filename, mlp);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save metamorph neural network to file " + filename + ": " + e.getMessage());
            return(false);
         }
      }
      else
      {
         System.err.println("Cannot save null metamorph neural network to file " + filename);
         return(false);
      }
      return(true);
   }


   // Load model from file.
   public boolean loadModel(String filename)
   {
      mlp = null;
      try
      {
         SerializedClassifier classifier = new SerializedClassifier();
         classifier.setModelFile(new File(filename));
         mlp = (MultilayerPerceptron)classifier.getCurrentModel();
      }
      catch (Exception e)
      {
         System.err.println("Cannot load metamorph neural network from file " + filename + ": " + e.getMessage());
         return(false);
      }
      return(true);
   }
}
