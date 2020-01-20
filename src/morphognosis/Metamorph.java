// For conditions of distribution and use, see copyright notice in Morphognosis.java

package morphognosis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Metamorph.
public class Metamorph
{
   // Morphognostic.
   public Morphognostic morphognostic;

   // Response.
   public int    response;
   public String responseName;

   // Neural network trainable?
   public boolean NNtrainable;

   // Constructors.
   public Metamorph(Morphognostic morphognostic, int response)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      responseName       = "";
      NNtrainable        = false;
   }


   public Metamorph(Morphognostic morphognostic, int response, String responseName)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      this.responseName  = responseName;
      NNtrainable        = false;
   }


   // Equality test.
   public boolean equals(Metamorph m)
   {
      if (response != m.response)
      {
         return(false);
      }
      if (morphognostic.compare(m.morphognostic) != 0.0f)
      {
         return(false);
      }
      return(true);
   }


   // Save.
   public void save(DataOutputStream output) throws IOException
   {
      morphognostic.save(output);
      Utility.saveInt(output, response);
      Utility.saveString(output, responseName);
      if (NNtrainable)
      {
         Utility.saveInt(output, 1);
      }
      else
      {
         Utility.saveInt(output, 0);
      }
      output.flush();
   }


   // Load.
   public static Metamorph load(DataInputStream input) throws IOException
   {
      Morphognostic morphognostic = Morphognostic.load(input);
      int           response      = Utility.loadInt(input);
      String        responseName  = Utility.loadString(input);
      int           NNtrainable   = Utility.loadInt(input);
      Metamorph     metamorph     = new Metamorph(morphognostic, response, responseName);

      if (NNtrainable == 1)
      {
         metamorph.NNtrainable = true;
      }
      else
      {
         metamorph.NNtrainable = false;
      }
      return(metamorph);
   }


   // Print.
   public void print()
   {
      System.out.println("Morphognostic:");
      morphognostic.print();
      System.out.println("Response=" + response);
      System.out.println("ResponseName=" + responseName);
      System.out.println("NNtrainable=" + NNtrainable);
   }
}
