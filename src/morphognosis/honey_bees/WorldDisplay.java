// For conditions of distribution and use, see copyright notice in Main.java

// World display.

package morphognosis.honey_bees;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.security.SecureRandom;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
   World world;
   
   // Honey bees.
   HoneyBee[] bees;

   // Dashboard.
   Dashboard dashboard;

   // Dimensions.
   public static final Dimension DISPLAY_SIZE = new Dimension(600, 700);

   // Pufferfish display.
   PufferfishDisplay display;

   // Controls.
   PufferfishControls controls;

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
   public WorldDisplay(World world, HoneyBee[] bees, int randomSeed)
   {
      this.world       = world;
      this.bees = bees;

      // Random numbers.
      randomSeed      = pufferfish.randomSeed;
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
      display = new PufferfishDisplay(displaySize);
      basePanel.add(display, BorderLayout.NORTH);

      // Create controls.
      controls = new PufferfishControls();
      basePanel.add(controls, BorderLayout.SOUTH);

      // Make display visible.
      pack();
      setLocation();
      setVisible(true);

      // Create pufferfish dashboard.
      pufferfishDashboard = new PufferfishDashboard(pufferfish, this);
      pufferfishDashboard.setVisible(true);
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
      pufferfishDashboard.close();
      setVisible(false);
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

      // Update pufferfish dashboard.
      pufferfishDashboard.update();

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


   // Pufferfish display.
   public class PufferfishDisplay extends Canvas
   {
      private static final long serialVersionUID = 0L;

      final Color PUFFERFISH_COLOR = Color.RED;

      // Buffered display.
      Dimension canvasSize;
      Graphics  graphics;
      Image     image;
      Graphics  imageGraphics;

      // Sizes.
      int   width, height;
      float cellWidth, cellHeight;

      // Constructor.
      public PufferfishDisplay(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());

         // Compute sizes.
         width      = nest.size.width;
         height     = nest.size.height;
         cellWidth  = (float)canvasSize.width / (float)width;
         cellHeight = (float)canvasSize.height / (float)height;
      }


      // Update display.
      void update()
      {
         int x, y, x2, y2, cx, cy;

         int[] sx, sy, px, py;

         if (graphics == null)
         {
            graphics = getGraphics();
            if (graphics == null)
            {
               return;
            }
            image         = createImage(canvasSize.width, canvasSize.height);
            imageGraphics = image.getGraphics();
         }

         // Clear display.
         imageGraphics.setColor(Color.white);
         imageGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);

         // Draw cells.
         int n = Nest.MAX_ELEVATION + 1;
         Color[] colors = new Color[n];
         for (int i = 0; i < n; i++)
         {
            float s = (float)(n - (i + 1)) / (float)(n - 1);
            int   r = 255 - (int)(255.0f * s);
            int   g = 255 - (int)(255.0f * s);
            int   b = 255 - (int)(255.0f * s);
            colors[i] = new Color(r, g, b);
         }
         for (x = x2 = 0; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = canvasSize.height - (int)cellHeight;
                 y < height;
                 y++, y2 = (int)(cellHeight * (double)(height - (y + 1))))
            {
               imageGraphics.setColor(colors[nest.cells[x][y][Nest.ELEVATION_CELL_INDEX]]);
               imageGraphics.fillRect(x2, y2, (int)cellWidth + 1, (int)cellHeight + 1);
            }
         }

         // Draw grid.
         imageGraphics.setColor(Color.black);
         y2 = canvasSize.height;
         for (x = 1, x2 = (int)cellWidth; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            imageGraphics.drawLine(x2, 0, x2, y2);
         }
         x2 = canvasSize.width;
         for (y = 1, y2 = (int)cellHeight; y < height;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            imageGraphics.drawLine(0, y2, x2, y2);
         }
         imageGraphics.setColor(Color.black);

         // Draw pufferfish sensor locations.
         imageGraphics.setColor(PUFFERFISH_COLOR);
         sx = new int[4];
         sy = new int[4];
         cx = pufferfish.x;
         cy = pufferfish.y;
         switch (pufferfish.orientation)
         {
         case Orientation.NORTH:
            for (int i = 0; i < 3; i++)
            {
               x = cx;
               y = cy;
               switch (i)
               {
               case 0:
                  x--;
                  if (x < 0) { x += width; }
                  y = ((y + 1) % height);
                  break;

               case 1:
                  y = ((y + 1) % height);
                  break;

               case 2:
                  x = ((x + 1) % width);
                  y = ((y + 1) % height);
                  break;
               }
               x2    = (int)(cellWidth * (double)x);
               y2    = (int)(cellHeight * (double)(height - (y + 1)));
               sx[0] = x2;
               sy[0] = y2;
               sx[1] = x2 + (int)cellWidth;
               sy[1] = y2;
               sx[2] = x2 + (int)cellWidth;
               sy[2] = y2 + (int)cellHeight;
               sx[3] = x2;
               sy[3] = y2 + (int)cellHeight;
               imageGraphics.drawPolygon(sx, sy, 4);
            }
            break;

         case Orientation.EAST:
            for (int i = 0; i < 3; i++)
            {
               x = cx;
               y = cy;
               switch (i)
               {
               case 0:
                  x = ((x + 1) % width);
                  y = ((y + 1) % height);
                  break;

               case 1:
                  x = ((x + 1) % width);
                  break;

               case 2:
                  x = ((x + 1) % width);
                  y--;
                  if (y < 0) { y += height; }
                  break;
               }
               x2    = (int)(cellWidth * (double)x);
               y2    = (int)(cellHeight * (double)(height - (y + 1)));
               sx[0] = x2;
               sy[0] = y2;
               sx[1] = x2 + (int)cellWidth;
               sy[1] = y2;
               sx[2] = x2 + (int)cellWidth;
               sy[2] = y2 + (int)cellHeight;
               sx[3] = x2;
               sy[3] = y2 + (int)cellHeight;
               imageGraphics.drawPolygon(sx, sy, 4);
            }
            break;

         case Orientation.SOUTH:
            for (int i = 0; i < 3; i++)
            {
               x = cx;
               y = cy;
               switch (i)
               {
               case 0:
                  x = ((x + 1) % width);
                  y--;
                  if (y < 0) { y += height; }
                  break;

               case 1:
                  y--;
                  if (y < 0) { y += height; }
                  break;

               case 2:
                  x--;
                  if (x < 0) { x += width; }
                  y--;
                  if (y < 0) { y += height; }
                  break;
               }
               x2    = (int)(cellWidth * (double)x);
               y2    = (int)(cellHeight * (double)(height - (y + 1)));
               sx[0] = x2;
               sy[0] = y2;
               sx[1] = x2 + (int)cellWidth;
               sy[1] = y2;
               sx[2] = x2 + (int)cellWidth;
               sy[2] = y2 + (int)cellHeight;
               sx[3] = x2;
               sy[3] = y2 + (int)cellHeight;
               imageGraphics.drawPolygon(sx, sy, 4);
            }
            break;

         case Orientation.WEST:
            for (int i = 0; i < 3; i++)
            {
               x = cx;
               y = cy;
               switch (i)
               {
               case 0:
                  x--;
                  if (x < 0) { x += width; }
                  y--;
                  if (y < 0) { y += height; }
                  break;

               case 1:
                  x--;
                  if (x < 0) { x += width; }
                  break;

               case 2:
                  x--;
                  if (x < 0) { x += width; }
                  y = ((y + 1) % height);
                  break;
               }
               x2    = (int)(cellWidth * (double)x);
               y2    = (int)(cellHeight * (double)(height - (y + 1)));
               sx[0] = x2;
               sy[0] = y2;
               sx[1] = x2 + (int)cellWidth;
               sy[1] = y2;
               sx[2] = x2 + (int)cellWidth;
               sy[2] = y2 + (int)cellHeight;
               sx[3] = x2;
               sy[3] = y2 + (int)cellHeight;
               imageGraphics.drawPolygon(sx, sy, 4);
            }
            break;
         }

         // Draw pufferfish.
         px = new int[3];
         py = new int[3];
         x2 = (int)(cellWidth * (double)pufferfish.x);
         y2 = (int)(cellHeight * (double)(height - (pufferfish.y + 1)));
         switch (pufferfish.orientation)
         {
         case Orientation.NORTH:
            px[0] = x2 + (int)(cellWidth * 0.5f);
            py[0] = y2;
            px[1] = x2;
            py[1] = y2 + (int)cellHeight;
            px[2] = x2 + (int)cellWidth;
            py[2] = y2 + (int)cellHeight;
            break;

         case Orientation.EAST:
            px[0] = x2 + (int)(cellWidth);
            py[0] = y2 + (int)(cellHeight * 0.5f);
            px[1] = x2;
            py[1] = y2;
            px[2] = x2;
            py[2] = y2 + (int)cellHeight;
            break;

         case Orientation.SOUTH:
            px[0] = x2 + (int)(cellWidth * 0.5f);
            py[0] = y2 + (int)cellHeight;
            px[1] = x2;
            py[1] = y2;
            px[2] = x2 + (int)cellWidth;
            py[2] = y2;
            break;

         case Orientation.WEST:
            px[0] = x2;
            py[0] = y2 + (int)(cellHeight * 0.5f);
            px[1] = x2 + (int)cellWidth;
            py[1] = y2;
            px[2] = x2 + (int)cellWidth;
            py[2] = y2 + (int)cellHeight;
            break;
         }
         imageGraphics.fillPolygon(px, py, 3);

         // Refresh display.
         graphics.drawImage(image, 0, 0, this);
      }


      // Canvas mouse listener.
      class CanvasMouseListener extends MouseAdapter
      {
         // Mouse pressed.
         public void mousePressed(MouseEvent evt)
         {
            int x, y, x2, y2, cx, cy;

            // Selecting pufferfish?
            x = (int)((double)evt.getX() / cellWidth);
            y = height - (int)((double)evt.getY() / cellHeight) - 1;

            if ((x >= 0) && (x < width) &&
                (y >= 0) && (y < height))
            {
               cx = pufferfish.x;
               cy = pufferfish.y;
               for (int i = 0; i < 9; i++)
               {
                  x2 = cx;
                  y2 = cy;
                  switch (i)
                  {
                  case 0:
                     x2--;
                     if (x2 < 0) { x2 += width; }
                     y2 = ((y2 + 1) % height);
                     break;

                  case 1:
                     y2 = ((y2 + 1) % height);
                     break;

                  case 2:
                     x2 = ((x2 + 1) % width);
                     y2 = ((y2 + 1) % height);
                     break;

                  case 3:
                     x2--;
                     if (x2 < 0) { x2 += width; }
                     break;

                  case 4:
                     break;

                  case 5:
                     x2 = ((x2 + 1) % width);
                     break;

                  case 6:
                     x2--;
                     if (x2 < 0) { x2 += width; }
                     y2--;
                     if (y2 < 0) { y2 += height; }
                     break;

                  case 7:
                     y2--;
                     if (y2 < 0) { y2 += height; }
                     break;

                  case 8:
                     x2 = ((x2 + 1) % width);
                     y2--;
                     if (y2 < 0) { y2 += height; }
                     break;
                  }
                  if ((x2 == x) && (y2 == y))
                  {
                     if (pufferfishDashboard.isVisible())
                     {
                        pufferfishDashboard.close();
                     }
                     else
                     {
                        pufferfishDashboard.open();
                     }
                     break;
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
   class PufferfishControls extends JPanel implements ActionListener, ChangeListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton    resetButton;
      JButton    scrambleButton;
      JLabel     stepCounter;
      JSlider    speedSlider;
      JButton    stepButton;
      JTextField messageText;

      // Constructor.
      PufferfishControls()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createRaisedBevelBorder());

         JPanel panel = new JPanel();
         resetButton = new JButton("Reset");
         resetButton.addActionListener(this);
         panel.add(resetButton);
         scrambleButton = new JButton("Scramble");
         scrambleButton.addActionListener(this);
         panel.add(scrambleButton);
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
         messageText = new JTextField("Click pufferfish to toggle dashboard", 40);
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
            nest.restore();
            pufferfish.reset();
            pufferfishDashboard.update();

            return;
         }

         // Scramble surface?
         if (evt.getSource() == (Object)scrambleButton)
         {
            nest.restore();
            int w = nest.size.width;
            int h = nest.size.height;
            for (int i = 0, j = (w * h * 10); i < j; i++)
            {
               int x  = random.nextInt(w);
               int x2 = random.nextInt(w);
               int y  = random.nextInt(h);
               int y2 = random.nextInt(h);
               int c  = nest.cells[x][y][Nest.ELEVATION_CELL_INDEX];
               nest.cells[x][y][Nest.ELEVATION_CELL_INDEX] =
                  nest.cells[x2][y2][Nest.ELEVATION_CELL_INDEX];
               nest.cells[x2][y2][Nest.ELEVATION_CELL_INDEX] = c;
            }
            pufferfish.reset();
            pufferfishDashboard.update();

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
