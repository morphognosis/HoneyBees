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

   // Constructors.
   public Metamorph(Morphognostic morphognostic, int response)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      responseName       = "";
   }


   public Metamorph(Morphognostic morphognostic, int response, String responseName)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      this.responseName  = responseName;
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
      output.flush();
   }


   // Load.
   public static Metamorph load(DataInputStream input) throws IOException
   {
      Morphognostic morphognostic = Morphognostic.load(input);
      int           response      = Utility.loadInt(input);
      String        responseName  = Utility.loadString(input);

      return(new Metamorph(morphognostic, response, responseName));
   }


   // Print.
   public void print()
   {
      System.out.println("Morphognostic:");
      morphognostic.print();
      System.out.println("Response=" + response);
      System.out.println("ResponseName=" + responseName);
   }
}
