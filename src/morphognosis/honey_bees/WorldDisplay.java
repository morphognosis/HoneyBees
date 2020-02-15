// For conditions of distribution and use, see copyright notice in Main.java

// World display.

package morphognosis.honey_bees;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Checkbox;
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
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import morphognosis.Orientation;

public class WorldDisplay extends JFrame
{
   private static final long serialVersionUID = 0L;

   // World.
   public World world;

   // Dimensions.
   public static final Dimension DISPLAY_SIZE = new Dimension(600, 800);

   // World display.
   public WorldDisplay worldDisplay;

   // Display.
   public Display display;

   // Controls.
   public Controls controls;

   // Bee dashboard.
   public HoneyBeeDashboard beeDashboard;

   // Step frequency (ms).
   static final int MIN_STEP_DELAY = 0;
   static final int MAX_STEP_DELAY = 1000;
   int              stepDelay      = MAX_STEP_DELAY;

   // Quit.
   public boolean quit;

   // Constructor.
   public WorldDisplay(World world)
   {
      this.world   = world;
      worldDisplay = this;

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
                                            (int)((double)DISPLAY_SIZE.height * .7));
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
   public boolean update(int step)
   {
      controls.updateStepCounter(step);
      controls.updateNectarCounter(world.collectedNectar);
      controls.updateDriver(world.driver);
      return(update());
   }


   public boolean update(int step, int steps)
   {
      controls.updateStepCounter(step, steps);
      controls.updateNectarCounter(world.collectedNectar);
      controls.updateDriver(world.driver);
      return(update());
   }


   private int timer = 0;
   public boolean update()
   {
      if (quit) { return(false); }

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
      return(!quit);
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

      // Sound.
      public static final String BEE_SOUND_FILENAME = "bees.wav";
      Clip beeSound;

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

         // Initialize sounds.
         initSounds();
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
         BasicStroke dashedLine = new BasicStroke(1, BasicStroke.CAP_BUTT,
                                                  BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
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
                  if (bee.nectarDistanceDisplay != -1)
                  {
                     int maxDist  = Math.max(Parameters.WORLD_WIDTH, Parameters.WORLD_HEIGHT) / 2;
                     int unitDist = maxDist / 2;
                     int d        = 0;
                     if (bee.nectarDistanceDisplay == 0)
                     {
                        d = 1;
                     }
                     int nectarDist = (d * unitDist) + (unitDist / 2);
                     int toX        = bee.x;
                     int toY        = bee.y;
                     switch (bee.orientation)
                     {
                     case Orientation.NORTH:
                        toY += nectarDist;
                        break;

                     case Orientation.NORTHEAST:
                        toX += nectarDist;
                        toY += nectarDist;
                        break;

                     case Orientation.EAST:
                        toX += nectarDist;
                        break;

                     case Orientation.SOUTHEAST:
                        toX += nectarDist;
                        toY -= nectarDist;
                        break;

                     case Orientation.SOUTH:
                        toY -= nectarDist;
                        break;

                     case Orientation.SOUTHWEST:
                        toX -= nectarDist;
                        toY -= nectarDist;
                        break;

                     case Orientation.WEST:
                        toX -= nectarDist;
                        break;

                     case Orientation.NORTHWEST:
                        toX -= nectarDist;
                        toY += nectarDist;
                        break;
                     }
                     canvasGraphics.setColor(Color.BLACK);
                     canvasGraphics.setStroke(dashedLine);
                     int fromX = x2 + (int)(cellWidth / 2.0f);
                     int fromY = y2 + (int)(cellHeight / 2.0f);
                     toX = (int)((float)toX * cellWidth) + (int)(cellWidth / 2.0f);
                     toY = (int)(cellHeight * (float)(height - (toY + 1))) + (int)(cellHeight / 2.0f);
                     canvasGraphics.drawLine(fromX, fromY, toX, toY);
                     canvasGraphics.setColor(Color.WHITE);
                     canvasGraphics.setStroke(thinLine);
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
         String        protocol = this.getClass().getResource("").getProtocol();

         if (Objects.equals(protocol, "jar"))
         {
            try
            {
               beeImage    = ImageIO.read(getClass().getResource(BEE_IMAGE_FILENAME));
               flowerImage = ImageIO.read(getClass().getResource(FLOWER_IMAGE_FILENAME));
               nectarImage = ImageIO.read(getClass().getResource(NECTAR_IMAGE_FILENAME));
            }
            catch (Exception e)
            {
               System.err.println("Cannot load images: " + e.getMessage());
               System.exit(1);
            }
         }
         else
         {
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
               System.err.println("Cannot load images: " + e.getMessage());
               System.exit(1);
            }
         }

         // Set font.
         font = new Font("Serif", Font.PLAIN, 12);
         canvasGraphics.setFont(font);

         // Create oriented bee images.
         beeOrientedImages = new BufferedImage[Orientation.NUM_ORIENTATIONS];
         beeOrientedImages[Orientation.NORTH] = beeImage;
         for (int i = 1; i < Orientation.NUM_ORIENTATIONS; i++)
         {
            double angle = 0.0;
            switch (i)
            {
            case Orientation.NORTHEAST:
               angle = 45.0;
               break;

            case Orientation.EAST:
               angle = 90.0;
               break;

            case Orientation.SOUTHEAST:
               angle = 135.0;
               break;

            case Orientation.SOUTH:
               angle = 180.0;
               break;

            case Orientation.SOUTHWEST:
               angle = 225.0;
               break;

            case Orientation.WEST:
               angle = 270.0;
               break;

            case Orientation.NORTHWEST:
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


      // Initialize sounds.
      void initSounds()
      {
         String protocol = this.getClass().getResource("").getProtocol();

         if (Objects.equals(protocol, "jar"))
         {
            // Running from jar.
            try
            {
               AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResource(BEE_SOUND_FILENAME));
               DataLine.Info    info        = new DataLine.Info(Clip.class, inputStream.getFormat());
               beeSound = (Clip)AudioSystem.getLine(info);
               beeSound.open(inputStream);
            }
            catch (LineUnavailableException | IOException | UnsupportedAudioFileException e)
            {
               System.err.println("Cannot load sound file " + BEE_SOUND_FILENAME + ": " + e.getMessage());
               System.exit(1);
            }
         }
         else
         {
            try
            {
               AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("res/sounds/" + BEE_SOUND_FILENAME));
               DataLine.Info    info        = new DataLine.Info(Clip.class, inputStream.getFormat());
               beeSound = (Clip)AudioSystem.getLine(info);
               beeSound.open(inputStream);
            }
            catch (LineUnavailableException | IOException | UnsupportedAudioFileException e)
            {
               System.err.println("Cannot load sound file " + BEE_SOUND_FILENAME + ": " + e.getMessage());
               System.exit(1);
            }
         }
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
                     beeDashboard = new HoneyBeeDashboard(world.cells[x][y].bee, worldDisplay);
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
                        beeDashboard = new HoneyBeeDashboard(world.cells[x][y].bee, worldDisplay);
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
      JCheckBox  muteCheck;
      JTextField messageText;
      JLabel     nectarCounter;
      JButton    clearMetamorphsButton;
      JButton    writeMetamorphDatasetButton;
      Checkbox   trainNNcheck;

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
         driverChoice.add("metamorphDB");
         driverChoice.add("metamorphNN");
         driverChoice.add("local override");
         driverChoice.select(world.driver);
         driverChoice.addItemListener(this);
         muteCheck = new JCheckBox("Mute", true);
         panel.add(muteCheck);
         muteCheck.addItemListener(this);
         messageText = new JTextField("Click bee for dashboard", 14);
         messageText.setEditable(false);
         panel.add(messageText);
         nectarCounter = new JLabel("Collected nectar: 0");
         panel.add(nectarCounter);
         add(panel, BorderLayout.CENTER);
         panel = new JPanel();
         clearMetamorphsButton = new JButton("Clear metamorphs");
         clearMetamorphsButton.addActionListener(this);
         panel.add(clearMetamorphsButton);
         writeMetamorphDatasetButton = new JButton("Write metamorphs to " + HoneyBee.METAMORPH_DATASET_FILE_BASENAME + ".csv");
         writeMetamorphDatasetButton.addActionListener(this);
         panel.add(writeMetamorphDatasetButton);
         panel.add(new JLabel("Train metamorph neural network:"));
         trainNNcheck = new Checkbox();
         trainNNcheck.setState(false);
         trainNNcheck.addItemListener(this);
         panel.add(trainNNcheck);
         add(panel, BorderLayout.SOUTH);
      }


      // Update step counter display.
      void updateStepCounter(int step)
      {
         stepCounter.setText("Step: " + step);
      }


      void updateStepCounter(int step, int steps)
      {
         stepCounter.setText("Step: " + step + "/" + steps);
      }


      // Update collected nectar counter display.
      void updateNectarCounter(int count)
      {
         nectarCounter.setText("Collected nectar: " + count);
      }


      // Update driver.
      void updateDriver(int count)
      {
         driverChoice.select(world.driver);
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
            world.reset();
            updateNectarCounter(world.collectedNectar);
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

         // Clear metamorphs?
         if ((JButton)evt.getSource() == clearMetamorphsButton)
         {
            world.clearMetamorphs();
            return;
         }

         // Write metamorph dataset?
         if ((JButton)evt.getSource() == writeMetamorphDatasetButton)
         {
            String filename = HoneyBee.METAMORPH_DATASET_FILE_BASENAME + ".csv";
            try
            {
               world.writeMetamorphDataset(filename);
            }
            catch (Exception e)
            {
               controls.messageText.setText("Cannot write metamorph dataset to file " + filename + ": " + e.getMessage());
            }
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
               if (driver != Driver.LOCAL_OVERRIDE)
               {
                  beeDashboard.setDriverChoice(driver);
               }
            }
            return;
         }

         if (source instanceof JCheckBox && ((JCheckBox)source == muteCheck))
         {
            if (evt.getStateChange() == 1)
            {
               display.beeSound.stop();
            }
            else
            {
               display.beeSound.start();
               display.beeSound.loop(Clip.LOOP_CONTINUOUSLY);
            }
            return;
         }

         if (source instanceof Checkbox && ((Checkbox)source == trainNNcheck))
         {
            if (trainNNcheck.getState())
            {
               try
               {
                  controls.messageText.setText("Training metamorph neural network...");
                  paint(getGraphics());
                  world.trainMetamorphNN();
                  controls.messageText.setText("");
               }
               catch (Exception e)
               {
                  controls.messageText.setText("Cannot train metamorph neural network: " + e.getMessage());
               }
               trainNNcheck.setState(false);
            }
            return;
         }
      }
   }
}
