// For conditions of distribution and use, see copyright notice in Main.java

// Metamorph machine learning.

package morphognosis.honey_bees;

import java.security.SecureRandom;
import java.util.ArrayList;
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MetamorphML
{
   // ML type.
   public static final int NEURAL_NETWORK = 0;
   public static final int DECISION_TREE  = 1;
   public int              type;

   // Dataset.
   public Instances metamorphInstances;
   public int       numAttributes;

   // Neural network model.
   public MultilayerPerceptron metamorphNN;

   // Decision tree model.
   public J48 metamorphDT;

   // Random numbers.
   public SecureRandom random;

   // Constructor.
   public MetamorphML(int type, SecureRandom random)
   {
      this.type   = type;
      this.random = random;
   }


   // Train metamorphs.
   public void train(ArrayList<Metamorph> metamorphs)
   {
      if (metamorphs.size() == 0) { return; }

      // Create metamorph training dataset.
      Morphognostic        morphognostic  = metamorphs.get(0).morphognostic;
      ArrayList<Attribute> attributeNames = new ArrayList<Attribute>();
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
      metamorphInstances = new Instances("metamorphs", attributeNames, 0);
      numAttributes      = attributeNames.size();
      for (Metamorph metamorph : metamorphs)
      {
         metamorphInstances.add(createInstance(metamorph.morphognostic, metamorph.response));
      }
      metamorphInstances.setClassIndex(numAttributes - 1);

      // Neural network?
      if (type == NEURAL_NETWORK)
      {
         trainNN(metamorphInstances);
      }
      else
      {
         trainDT(metamorphInstances);
      }
   }


   // Create instance.
   public Instance createInstance(Morphognostic morphognostic, int response)
   {
      double[]  attrValues = new double[numAttributes];
      int a = 0;
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
      attrValues[a] = metamorphInstances.attribute(a).indexOfValue(response + "");
      a++;
      return(new DenseInstance(1.0, attrValues));
   }


   // Train neural network.
   public void trainNN(Instances metamorphInstances)
   {
      // Create model.
      metamorphNN = new MultilayerPerceptron();
      metamorphNN.setLearningRate(0.1);
      metamorphNN.setMomentum(0.2);
      metamorphNN.setTrainingTime(2000);
      metamorphNN.setHiddenLayers("5");

      /*
       * try
       * {
       * metamorphNN.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 5"));
       * }
       * catch (Exception e)
       * {
       * System.err.println("Cannot create neural network model: " + e.getMessage());
       * e.printStackTrace();
       * }
       */

      // Train model.
      try
      {
         metamorphNN.buildClassifier(metamorphInstances);
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
         eval.evaluateModel(metamorphNN, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
      catch (Exception e)
      {
         System.err.println("Cannot evaluate neural network model: " + e.getMessage());
         e.printStackTrace();
      }
   }


   // Train decision tree.
   public void trainDT(Instances metamorphInstances)
   {
      metamorphDT = new J48();
      try
      {
         metamorphDT.buildClassifier(metamorphInstances);
      }
      catch (Exception e)
      {
         System.err.println("Cannot train decision tree model: " + e.getMessage());
         e.printStackTrace();
      }

      // Evaluate model.
      try
      {
         Evaluation eval = new Evaluation(metamorphInstances);
         eval.evaluateModel(metamorphDT, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
      catch (Exception e)
      {
         System.err.println("Cannot evaluate decision tree model: " + e.getMessage());
         e.printStackTrace();
      }
   }


   // Respond.
   public int respond(Morphognostic morphognostic)
   {
      return(respond(morphognostic, false));
   }


   public int respond(Morphognostic morphognostic, boolean probabilistic)
   {
      if (type == NEURAL_NETWORK)
      {
         return(respondNN(morphognostic, probabilistic));
      }
      else
      {
         return(respondDT(morphognostic, probabilistic));
      }
   }


   // Neural network response.
   public int respondNN(Morphognostic morphognostic, boolean probabilistic)
   {
      Instance morphognosticInstance = createInstance(morphognostic, 0);
      int      response = 0;

      try
      {
         if (probabilistic)
         {
            return(respondProbabilistic(metamorphNN.distributionForInstance(morphognosticInstance)));
         }
         else
         {
            int    predictionIndex     = (int)metamorphNN.classifyInstance(morphognosticInstance);
            String predictedClassLabel = metamorphInstances.classAttribute().value(predictionIndex);
            response = Integer.parseInt(predictedClassLabel);
         }
      }
      catch (Exception e)
      {
         System.err.println("Cannot get response from neural network: " + e.getMessage());
         e.printStackTrace();
      }
      return(response);
   }


   // Decision tree response.
   public int respondDT(Morphognostic morphognostic, boolean probabilistic)
   {
      Instance morphognosticInstance = createInstance(morphognostic, 0);

      morphognosticInstance.setDataset(metamorphInstances);
      int response = 0;

      try
      {
         if (probabilistic)
         {
            return(respondProbabilistic(metamorphDT.distributionForInstance(morphognosticInstance)));
         }
         else
         {
            int    predictionIndex     = (int)metamorphDT.classifyInstance(morphognosticInstance);
            String predictedClassLabel = metamorphInstances.classAttribute().value(predictionIndex);
            response = Integer.parseInt(predictedClassLabel);
         }
      }
      catch (Exception e)
      {
         System.err.println("Cannot get response from decision tree: " + e.getMessage());
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
}
