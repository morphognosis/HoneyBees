// For conditions of distribution and use, see copyright notice in Main.java

// World display.

package morphognosis.honey_bees;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
      this.world = world;

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
      controls.updateNectarCounter(world.collectedNectar);
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
      public static final String BEE_IMAGE_FILENAME    = "honeybee.png";
      public static final String FLOWER_IMAGE_FILENAME = "flower.png";
      public static final String NECTAR_IMAGE_FILENAME = "nectar.png";

      // Colors.
      public final Color HIVE_COLOR  = Color.YELLOW;
      public final Color WORLD_COLOR = Color.GREEN;
      public final Color SELECTED_BEE_HIGHLIGHT_COLOR = Color.RED;

      // Images and graphics.
      Graphics      graphics;
      BufferedImage canvasImage;
      Graphics2D    canvasGraphics;
      BufferedImage[] beeOrientedImages;
      BufferedImage flowerImage;
      BufferedImage nectarImage;

      // Font.
      Font font;

      // Sizes.
      Dimension canvasSize;
      int       width, height;
      float     cellWidth, cellHeight;

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

         // Initialize graphics.
         if (graphics == null)
         {
            graphics = getGraphics();
            if (graphics == null)
            {
               return;
            }
            canvasImage    = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
            canvasGraphics = canvasImage.createGraphics();
            initGraphics();
         }

         // Clear display.
         canvasGraphics.setColor(Color.WHITE);
         canvasGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);

         // Draw cells.
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
               }
               else
               {
                  canvasGraphics.setColor(WORLD_COLOR);
               }
               canvasGraphics.fillRect(x2, y2, (int)cellWidth + 1, (int)cellHeight + 1);
               canvasGraphics.setColor(Color.WHITE);
            }
         }

         // Draw objects.
         BasicStroke thickLine  = new BasicStroke(3);
         BasicStroke dashedLine = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
         BasicStroke thinLine   = new BasicStroke(1);
         int         nectarYoff = (int)(cellHeight / 2.0f);
         for (x = x2 = 0; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = canvasSize.height - (int)cellHeight;
                 y < height;
                 y++, y2 = (int)(cellHeight * (double)(height - (y + 1))))
            {
               // Draw flower and nectar?
               Flower flower = world.cells[x][y].flower;
               if (flower != null)
               {
                  canvasGraphics.drawImage(flowerImage, x2, y2,
                                           (int)cellWidth + 1, (int)cellHeight + 1, null);
                  if (flower.nectar > 0)
                  {
                     canvasGraphics.drawImage(nectarImage, x2, y2 + nectarYoff,
                                              (int)(cellWidth / 2.0f), (int)(cellHeight / 2.0f), null);
                  }
               }

               // Draw bee?
               HoneyBee bee = world.cells[x][y].bee;
               if (bee != null)
               {
                  canvasGraphics.drawImage(beeOrientedImages[bee.orientation], x2, y2,
                                           (int)cellWidth + 1, (int)cellHeight + 1, null);

                  // Carrying nectar?
                  if (bee.nectarCarry)
                  {
                     canvasGraphics.drawImage(nectarImage, x2, y2,
                                              (int)(cellWidth / 2.0f), (int)(cellHeight / 2.0f), null);
                  }

                  // Bee displaying distance to nectar?
                  if (world.cells[x][y].hive && bee.nectarCarry)
                  {
                     if (bee.response == HoneyBee.DISPLAY_NECTAR_DISTANCE)
                     {
                        int maxDist    = Math.max(Parameters.WORLD_WIDTH, Parameters.WORLD_HEIGHT) / 2;
                        int unitDist   = maxDist / Parameters.BEE_NUM_DISTANCE_VALUES;
                        int nectarDist = (bee.nectarDistanceDisplay * unitDist) + (unitDist / 2);
                        int toX        = bee.x;
                        int toY        = bee.y;
                        switch (bee.orientation)
                        {
                        case Compass.NORTH:
                           toY += nectarDist;
                           break;

                        case Compass.NORTHEAST:
                           toX += nectarDist;
                           toY += nectarDist;
                           break;

                        case Compass.EAST:
                           toX += nectarDist;
                           break;

                        case Compass.SOUTHEAST:
                           toX += nectarDist;
                           toY -= nectarDist;
                           break;

                        case Compass.SOUTH:
                           toY -= nectarDist;
                           break;

                        case Compass.SOUTHWEST:
                           toX -= nectarDist;
                           toY -= nectarDist;
                           break;

                        case Compass.WEST:
                           toX -= nectarDist;
                           break;

                        case Compass.NORTHWEST:
                           toX -= nectarDist;
                           toY += nectarDist;
                           break;
                        }
                        canvasGraphics.setColor(Color.BLACK);
                        canvasGraphics.setStroke(dashedLine);
                        int fromX = x2 + (int)(cellWidth / 2.0f);
                        int fromY = y2 + (int)(cellHeight / 2.0f);
                        toX = (int)((float)toX * cellWidth) + (int)(cellWidth / 2.0f);
                        toY = (int)(cellHeight * (double)(height - (toY + 1))) + (int)(cellHeight / 2.0f);
                        canvasGraphics.drawLine(fromX, fromY, toX, toY);
                        canvasGraphics.setColor(Color.WHITE);
                        canvasGraphics.setStroke(thinLine);
                     }
                  }

                  // Highlight selected bee?
                  if ((beeDashboard != null) && (beeDashboard.bee == bee))
                  {
                     canvasGraphics.setColor(SELECTED_BEE_HIGHLIGHT_COLOR);
                     canvasGraphics.setStroke(thickLine);
                     canvasGraphics.drawRect(x2 + 1, y2 + 1, (int)cellWidth - 1, (int)cellHeight - 1);
                     canvasGraphics.setColor(Color.WHITE);
                     canvasGraphics.setStroke(thinLine);
                  }
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
         BufferedImage beeImage = null;

         try
         {
            beeImage    = ImageIO.read(getClass().getResource(BEE_IMAGE_FILENAME));
            flowerImage = ImageIO.read(getClass().getResource(FLOWER_IMAGE_FILENAME));
            nectarImage = ImageIO.read(getClass().getResource(NECTAR_IMAGE_FILENAME));
         }
         catch (Exception e) {}
         try
         {
            if (beeImage == null)
            {
               beeImage = ImageIO.read(new File("res/images/" + BEE_IMAGE_FILENAME));
            }
            if (flowerImage == null)
            {
               flowerImage = ImageIO.read(new File("res/images/" + FLOWER_IMAGE_FILENAME));
            }
            if (nectarImage == null)
            {
               nectarImage = ImageIO.read(new File("res/images/" + NECTAR_IMAGE_FILENAME));
            }
         }
         catch (Exception e)
         {
            System.err.println("Cannot load images");
            System.exit(1);
         }

         // Set font.
         font = new Font("Serif", Font.PLAIN, 12);
         canvasGraphics.setFont(font);

         // Create oriented bee images.
         beeOrientedImages = new BufferedImage[Compass.NUM_POINTS];
         beeOrientedImages[Compass.NORTH] = beeImage;
         for (int i = 1; i < Compass.NUM_POINTS; i++)
         {
            double angle = 0.0;
            switch (i)
            {
            case Compass.NORTHEAST:
               angle = 45.0;
               break;

            case Compass.EAST:
               angle = 90.0;
               break;

            case Compass.SOUTHEAST:
               angle = 135.0;
               break;

            case Compass.SOUTH:
               angle = 180.0;
               break;

            case Compass.SOUTHWEST:
               angle = 225.0;
               break;

            case Compass.WEST:
               angle = 270.0;
               break;

            case Compass.NORTHWEST:
               angle = 315.0;
               break;
            }
            beeOrientedImages[i] = createRotatedImage(beeImage, angle);
         }
      }


      // Create rotated image.
      public BufferedImage createRotatedImage(BufferedImage bimg, double angle)
      {
         int w = bimg.getWidth();
         int h = bimg.getHeight();

         BufferedImage rotated = new BufferedImage(w, h, bimg.getType());
         Graphics2D    graphic = rotated.createGraphics();

         graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
         graphic.drawImage(bimg, null, 0, 0);
         graphic.dispose();
         return(rotated);
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
                  }
                  else
                  {
                     beeDashboard.close();
                     if (beeDashboard.bee == world.cells[x][y].bee)
                     {
                        beeDashboard = null;
                     }
                     else
                     {
                        beeDashboard = new HoneyBeeDashboard(world.cells[x][y].bee);
                        beeDashboard.open();
                     }
                  }
               }
               else
               {
                  if (beeDashboard != null)
                  {
                     beeDashboard.close();
                     beeDashboard = null;
                  }
               }
            }
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
   class Controls extends JPanel implements ActionListener, ChangeListener, ItemListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton    resetButton;
      JLabel     stepCounter;
      JSlider    speedSlider;
      JButton    stepButton;
      Choice     driverChoice;
      JTextField messageText;
      JLabel     nectarCounter;

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
         panel = new JPanel();
         panel.add(new JLabel("Driver:"));
         driverChoice = new Choice();
         panel.add(driverChoice);
         driverChoice.add("autopilot");
         driverChoice.add("metamorphs");
         driverChoice.add("variable");
         driverChoice.addItemListener(this);
         messageText = new JTextField("Click bee to toggle dashboard", 40);
         messageText.setEditable(false);
         panel.add(messageText);
         nectarCounter = new JLabel("Collected nectar: 0");
         panel.add(nectarCounter);
         add(panel, BorderLayout.SOUTH);
      }


      // Update step counter display.
      void updateStepCounter(int steps)
      {
         stepCounter.setText("Steps: " + steps);
      }


      // Update collected nectar counter display.
      void updateNectarCounter(int count)
      {
         nectarCounter.setText("Collected nectar: " + count);
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
            if (beeDashboard != null)
            {
               beeDashboard.update();
            }

            return;
         }

         // Step?
         if (evt.getSource() == (Object)stepButton)
         {
            step();

            return;
         }
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Choice && ((Choice)source == driverChoice))
         {
            int driver = driverChoice.getSelectedIndex();
            world.setDriver(driver);
            if (beeDashboard != null)
            {
               if (driver != World.DRIVER_TYPE.VARIABLE.getValue())
               {
                  beeDashboard.setDriverChoice(driver);
               }
            }
            return;
         }
      }
   }
}
