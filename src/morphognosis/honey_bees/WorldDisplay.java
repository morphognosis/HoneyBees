// For conditions of distribution and use, see copyright notice in Main.java

// World display.

package morphognosis.honey_bees;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class WorldDisplay extends JFrame
{
   private static final long serialVersionUID = 0L;

   // World.
   World world;

   // Dimensions.
   public static final Dimension DISPLAY_SIZE = new Dimension(600, 700);

   // Display.
   Display display;

   // Controls.
   Controls controls;
   
   // Bee dashboard.
   HoneyBeeDashboard beeDashboard;

   // Step frequency (ms).
   static final int MIN_STEP_DELAY = 0;
   static final int MAX_STEP_DELAY = 1000;
   int              stepDelay      = MAX_STEP_DELAY;

   // Quit.
   boolean quit;

   // Random numbers.
   SecureRandom random;
   int          randomSeed;

   // Constructor.
   public WorldDisplay(World world, int randomSeed)
   {
      this.world       = world;

      // Random numbers.
      randomSeed      = world.randomSeed;
      random          = new SecureRandom();
      this.randomSeed = randomSeed;
      random.setSeed(randomSeed);

      // Set up display.
      setTitle("Honey bees nectar foraging");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e)
                           {
                              close();
                              quit = true;
                           }
                        }
                        );
      setBounds(0, 0, DISPLAY_SIZE.width, DISPLAY_SIZE.height);
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());

      // Create display.
      Dimension displaySize = new Dimension(DISPLAY_SIZE.width,
                                            (int)((double)DISPLAY_SIZE.height * .8));
      display = new Display(displaySize);
      basePanel.add(display, BorderLayout.NORTH);

      // Create controls.
      controls = new Controls();
      basePanel.add(controls, BorderLayout.SOUTH);

      // Make display visible.
      pack();
      setLocation();
      setVisible(true);
   }


   void setLocation()
   {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int       w   = getSize().width;
      int       h   = getSize().height;
      int       x   = (dim.width - w) / 4;
      int       y   = (dim.height - h) / 2;

      setLocation(x, y);
   }


   // Close.
   void close()
   {
	   if (beeDashboard != null)
	   {
      beeDashboard.close();
      beeDashboard = null;     
      setVisible(false);
	   }
   }


   // Update display.
   public void update(int steps)
   {
      controls.updateStepCounter(steps);
      update();
   }


   private int timer = 0;
   public void update()
   {
      if (quit) { return; }

      // Update bee dashboard.
      if (beeDashboard != null)
      {
      beeDashboard.update();
      }

      // Update display.
      display.update();

      // Timer loop: count down delay by 1ms.
      for (timer = stepDelay; timer > 0 && !quit; )
      {
         try
         {
            Thread.sleep(1);
         }
         catch (InterruptedException e) {
            break;
         }

         display.update();

         if (stepDelay < MAX_STEP_DELAY)
         {
            timer--;
         }
      }
   }


   // Set step delay.
   void setStepDelay(int delay)
   {
      stepDelay = timer = delay;
   }


   // Step.
   void step()
   {
      setStepDelay(MAX_STEP_DELAY);
      controls.speedSlider.setValue(MAX_STEP_DELAY);
      timer = 0;
   }


   // Set message
   void setMessage(String message)
   {
      if (message == null)
      {
         controls.messageText.setText("");
      }
      else
      {
         controls.messageText.setText(message);
      }
   }


   // Display.
   public class Display extends Canvas
   {
      private static final long serialVersionUID = 0L;

      // Image files.
      public static final String BEE_IMAGE_FILENAME = "honeybee.png";
      public static final String FLOWER_IMAGE_FILENAME = "blue_flower.png";
      public static final String NECTAR_IMAGE_FILENAME = "nectar.jpg";
      
      // Colors.
      public final Color HIVE_COLOR = Color.YELLOW;
      public final Color FIELD_COLOR = Color.GREEN;
      public final Color SELECTED_BEE_COLOR = Color.RED;
      
      // Images and graphics.      
      Graphics graphics;
      Image canvasImage;
      Graphics canvasGraphics;
      BufferedImage beeImage;
      Graphics2D beeGraphics;
	  BufferedImage flowerImage;
      Graphics2D flowerGraphics;      
      BufferedImage nectarImage;
      Graphics2D nectarGraphics;
      
      // Font.
      Font font;
      
      // Sizes.
      Dimension canvasSize;      
      int   width, height;
      float cellWidth, cellHeight;
      
      // Constructor.
      public Display(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());

         // Compute sizes.
         width      = Parameters.WORLD_WIDTH;
         height     = Parameters.WORLD_HEIGHT;
         cellWidth  = (float)canvasSize.width / (float)width;
         cellHeight = (float)canvasSize.height / (float)height;        
      }
     

      // Update display.
      void update()
      {
         int x, y, x2, y2;
         int beeWidth, beeHeight;
         int nectarWidth, nectarHeight;

         // Initialize graphics.
         if (graphics == null)
         {
        	 graphics = getGraphics();
             if (graphics == null)
             {
                return;
             }
            canvasImage         = createImage(canvasSize.width, canvasSize.height);
            canvasGraphics = canvasImage.getGraphics();
            initGraphics();
         }

         // Clear display.
         canvasGraphics.setColor(Color.WHITE);
         canvasGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);

         // Draw cells.
         beeWidth = beeImage.getWidth();
         beeHeight = beeImage.getHeight();
         nectarWidth = nectarImage.getWidth();
         nectarHeight = nectarImage.getHeight();
         for (x = x2 = 0; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = canvasSize.height - (int)cellHeight;
                 y < height;
                 y++, y2 = (int)(cellHeight * (double)(height - (y + 1))))
            {
               if (world.cells[x][y].hive)
               {
                   canvasGraphics.setColor(HIVE_COLOR);
               } else {
                   canvasGraphics.setColor(FIELD_COLOR);
               }
               canvasGraphics.fillRect(x2, y2, (int)cellWidth + 1, (int)cellHeight + 1);
               
               // Draw flower and nectar?
               Flower flower = world.cells[x][y].flower;
               if (flower != null)
               {
       		    	flowerGraphics.drawImage(flowerImage, x2, y2, (int)cellWidth + 1, (int)cellHeight + 1, null);
       		    	nectarGraphics.drawImage(nectarImage, x2, y2, (int)cellWidth + 1, (int)cellHeight + 1, null);
                    nectarGraphics.drawString((flower.nectar + ""), x2, y2);
               }
               
               // Draw bee?
               HoneyBee bee = world.cells[x][y].bee;
               if (bee != null)
               {    	   
            	   double angle = 0.0;
                   switch (bee.orientation)
                   {
                   case Compass.NORTH:
                      break;
                   case Compass.NORTHEAST:
                	   angle = Math.toRadians(45.0);
                      break;
                   case Compass.EAST:
                	   angle = Math.toRadians(90.0);
                      break;
                   case Compass.SOUTHEAST:
                	   angle = Math.toRadians(135.0);
                       break;
                   case Compass.SOUTH:
                	   angle = Math.toRadians(180.0);
                       break;
                   case Compass.SOUTHWEST:
                	   angle = Math.toRadians(225.0);
                      break;
                   case Compass.WEST:
                	   angle = Math.toRadians(270.0);
                       break;
                   case Compass.NORTHWEST:
                	   angle = Math.toRadians(315.0);
                      break;
                   }
                   AffineTransform t = beeGraphics.getTransform();
      		    beeGraphics.setTransform(AffineTransform.getRotateInstance(angle, 
      		    		(double)beeWidth / 2.0, (double)beeHeight / 2.0));
       		    beeGraphics.drawImage(beeImage, x2, y2, (int)cellWidth + 1, (int)cellHeight + 1, null); 
       		    beeGraphics.setTransform(t);
               }              
            }
         }
         
         // Draw grid.
         canvasGraphics.setColor(Color.BLACK);
         y2 = canvasSize.height;
         for (x = 1, x2 = (int)cellWidth; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            canvasGraphics.drawLine(x2, 0, x2, y2);
         }
         x2 = canvasSize.width;
         for (y = 1, y2 = (int)cellHeight; y < height;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            canvasGraphics.drawLine(0, y2, x2, y2);
         }

         // Refresh display.
         graphics.drawImage(canvasImage, 0, 0, this);
      }

      // Initialize graphics.
      void initGraphics()
      {
    			// Load source images.
    	  try
    	  {
    			beeImage = ImageIO.read(new File(BEE_IMAGE_FILENAME));
    			flowerImage = ImageIO.read(new File(FLOWER_IMAGE_FILENAME));
       			nectarImage = ImageIO.read(new File(NECTAR_IMAGE_FILENAME));
    	  } catch(IOException e)
    	  {
    		  System.err.println("Cannot load images");
    		  System.exit(1);
    	  }
       			
       			// Get graphics.
       			beeGraphics = (Graphics2D)beeImage.getGraphics();       			
       			flowerGraphics = (Graphics2D)flowerImage.getGraphics();
       			nectarGraphics = (Graphics2D)nectarImage.getGraphics();
       		
       			// Get font.
       		 font = new Font("Serif", Font.PLAIN, 12);
             nectarGraphics.setFont(font);
      }
      
      // Canvas mouse listener.
      class CanvasMouseListener extends MouseAdapter
      {
         // Mouse pressed.
         public void mousePressed(MouseEvent evt)
         {
            int x = (int)((double)evt.getX() / cellWidth);
            int y = height - (int)((double)evt.getY() / cellHeight) - 1;

            if ((x >= 0) && (x < width) &&
                (y >= 0) && (y < height))
            {
            	if (world.cells[x][y].bee != null)          		
            	{
            		if (beeDashboard == null)
            		{
                        beeDashboard = new HoneyBeeDashboard(world.cells[x][y].bee);
                        beeDashboard.open();            			
            		} else {
		                   beeDashboard.close();
            			if (beeDashboard.bee == world.cells[x][y].bee)
            			{
 			                beeDashboard = null;            				
            			} else {
                            beeDashboard = new HoneyBeeDashboard(world.cells[x][y].bee);
                            beeDashboard.open();               				
            			}
            		}
            	} else {
            		if (beeDashboard != null)
            		{
		                   beeDashboard.close(); 
		                beeDashboard = null;
            		}
            	}
            }

            // Refresh display.
            update();
         }
      }
   }

   // Canvas mouse motion listener.
   class CanvasMouseMotionListener extends MouseMotionAdapter
   {
      // Mouse dragged.
      public void mouseDragged(MouseEvent evt)
      {
      }
   }

    // Control panel.
   class Controls extends JPanel implements ActionListener, ChangeListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton    resetButton;
      JLabel     stepCounter;
      JSlider    speedSlider;
      JButton    stepButton;
      JTextField messageText;

      // Constructor.
      Controls()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createRaisedBevelBorder());

         JPanel panel = new JPanel();
         resetButton = new JButton("Reset");
         resetButton.addActionListener(this);
         panel.add(resetButton);
         panel.add(new JLabel("Speed:   Fast", Label.RIGHT));
         speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_STEP_DELAY,
                                   MAX_STEP_DELAY, MAX_STEP_DELAY);
         speedSlider.addChangeListener(this);
         panel.add(speedSlider);
         panel.add(new JLabel("Stop", Label.LEFT));
         stepButton = new JButton("Step");
         stepButton.addActionListener(this);
         panel.add(stepButton);
         stepCounter = new JLabel("");
         panel.add(stepCounter);
         add(panel, BorderLayout.NORTH);
         panel       = new JPanel();
         messageText = new JTextField("Click bee to toggle dashboard", 40);
         messageText.setEditable(false);
         panel.add(messageText);
         add(panel, BorderLayout.SOUTH);
      }


      // Update step counter display
      void updateStepCounter(int steps)
      {
         stepCounter.setText("Steps: " + steps);
      }


      // Speed slider listener.
      public void stateChanged(ChangeEvent evt)
      {
         messageText.setText("");
         setStepDelay(speedSlider.getValue());
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         messageText.setText("");

         // Reset?
         if (evt.getSource() == (Object)resetButton)
         {
            random = new SecureRandom();
            random.setSeed(randomSeed);
            world.reset();
            beeDashboard.update();

            return;
         }

         // Step?
         if (evt.getSource() == (Object)stepButton)
         {
            step();

            return;
         }
      }
   }
}
