import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.sound.sampled.Line;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;


public class VideoPanel extends JPanel implements MouseListener, MouseMotionListener
{

	public final static int HORIZONTAL_MODE = 0;
	public final static int VERTICAL_MODE = 1;
	public final static int DIAGONAL_MODE = 2;
	
	
	private OpenCVFrameGrabber grabber;
	private Java2DFrameConverter converter;
	private TransDimFrame myParent;
	private boolean isGenerating;
	private ArrayList<BufferedImage> frames;
	private int currentFrame;
	private int num_frames;
	private int selectionMode;
	private int horizontalValue, verticalValue;
	private Color selectionColor = Color.RED;
	private int [] xEndpoints, yEndpoints;
	private int whichEndIsSelected;
	private boolean isLoading;
	
	public VideoPanel(TransDimFrame parent)
	{
		super();
		myParent = parent;
		converter = new Java2DFrameConverter();
		frames = new ArrayList<BufferedImage>();
		
		// select and open the video file.
		JFileChooser dialog = new JFileChooser();
		dialog.setMultiSelectionEnabled(false);
		int result = dialog.showOpenDialog(null);
		if (result == JFileChooser.CANCEL_OPTION)
			throw new RuntimeException("You can't run if you don't have a video file!");
		grabber = new OpenCVFrameGrabber(dialog.getSelectedFile());
		
		
		num_frames = -1;
		currentFrame = 0;
		selectionMode = HORIZONTAL_MODE;
		isLoading = true;
		isGenerating = false;

		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/**
	 * initiate the thread to start loading the video file - this is threaded so that 
	 *   we can see the progress bar as it goes.
	 */
	public void startLoading()
	{
		Thread thread = new Thread(new LoadThread());
		thread.start();
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (isLoading) // if the frames are loading, let's just show a "loading" message.
		{
			g.drawString("Loading",100,100);
			return;
		}
		
		
		g.drawImage(frames.get(currentFrame), 0, 0, null);
		
		if (selectionMode == HORIZONTAL_MODE)
		{
			g.setColor(selectionColor);
			g.drawLine(0, verticalValue, getWidth()-1, verticalValue);
		}
		if (selectionMode == VERTICAL_MODE)
		{
			g.setColor(selectionColor);
			g.drawLine(horizontalValue, 0,  horizontalValue, getHeight()-1);
		}
		if (selectionMode == DIAGONAL_MODE)
		{
			g.setColor(selectionColor);
			for (int i = 0; i<2; i++)
				g.drawOval(xEndpoints[i]-5, yEndpoints[i]-5, 10, 10);
			g.drawLine(xEndpoints[0], yEndpoints[0], xEndpoints[1], yEndpoints[1]);
		}
	}
	
	/**
	 * update which frame should be drawn on screen, most likely in response to the slider.
	 * @param f
	 */
	public void moveToFrame(int f)
	{
		currentFrame = Math.min(num_frames-1,Math.max(0, f));
		repaint();
	}
	
	public void setMode(int m)
	{
		if (m>-1 && m<3)
		{
			selectionMode = m;
			repaint();
		}
	}
	/**
	 * depending on which mode we are in, create and start the thread that will generate & display the new image.
	 */
	public void handleExecuteButtonPressed()
	{
		if (selectionMode == HORIZONTAL_MODE)
		{
			Thread het = new Thread(new HorizontalExecutionThread(verticalValue));
			het.start();
		}	
		if (selectionMode == VERTICAL_MODE)
		{
			Thread vet = new Thread(new VerticalExecutionThread(horizontalValue));
			vet.start();
		}
		if (selectionMode == DIAGONAL_MODE)
		{
			Thread det = new Thread(new DiagonalExecutionThread(xEndpoints,yEndpoints));
			det.start();
		}
		// Note: we will get to this point before the threads finish. 
		
	}
	
	
	
	private void generateMotionGraphFromVerticalLine(int x)
	{
		
		
	}
	
	private void generateMotionGraphFromDiagonalLine(int[] xList, int[] yList)
	{
		
		
	}
	
	
// -------------------------------------------  MOUSE EVENT HANDLERS -------------------------------
	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		if (isLoading || isGenerating)
			return;
		if (selectionMode == VERTICAL_MODE)
			horizontalValue = e.getX();
		if (selectionMode == HORIZONTAL_MODE)
			verticalValue = e.getY();
		if (selectionMode == DIAGONAL_MODE)
		{
			for (int i = 0; i<2; i++)
				if (Math.pow(e.getX()-xEndpoints[i], 2)+Math.pow(e.getY()-yEndpoints[i], 2)<100)
				{
					whichEndIsSelected = i;
					xEndpoints[i] = e.getX();
					yEndpoints[i] = e.getY();
				}
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		whichEndIsSelected = -1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mouseExited(MouseEvent e) {

		whichEndIsSelected = -1;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isLoading || isGenerating)
			return;
		if (selectionMode == VERTICAL_MODE)
			horizontalValue = e.getX();
		if (selectionMode == HORIZONTAL_MODE)
			verticalValue = e.getY();
		if (selectionMode == DIAGONAL_MODE && whichEndIsSelected >-1)
		{
			xEndpoints[whichEndIsSelected] = e.getX();
			yEndpoints[whichEndIsSelected] = e.getY();
		}
		repaint();

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// do nothing
	}
	
	// ----------------------------------------------    LOAD THREAD -------------------------------
	// This thread is called to load the files from the video via "JavaCV." It is in a thread so that 
	//      the window can appear and a progress bar can be displayed showing how the file is loading.
	
	public class LoadThread implements Runnable
	{
		
		
		public LoadThread()
		{
			super();
		}
		@Override
		public void run()
		{
			
			try {
				grabber.start();
				num_frames = grabber.getLengthInFrames(); // a good starting approximation... but we may not get all of them.
				//System.out.println(num_frames);
			} catch (Exception e) {

				e.printStackTrace();
			}
			int i = 0;
			while(grabber.getFrameNumber()<num_frames) // plan on grabbing all the frames the file says it has...
			{
				//System.out.println(i+", "+grabber.getFrameNumber());
				try
				{
				BufferedImage bi = Java2DFrameConverter.cloneBufferedImage(converter.convert(grabber.grab()));
				frames.add(bi);
				} catch (Exception e)
				{
					break;									//.... but bail out if it runs out of frames to get.
				}
				i++;
				myParent.updateProgress(i,num_frames);
				//------------- pause to let the progress bar refresh onscreen.
				try
				{	Thread.sleep(1);
				}
				catch (InterruptedException iExp)  
				{ 
					System.out.println(iExp.getMessage());
					break;
				}
			}
			// ok, let's revise our estimate of how many frames there are.
			num_frames = frames.size();
			myParent.setNumFrames(num_frames);

			// set initial values for the lines we will consider.
			horizontalValue = frames.get(0).getWidth()/2;
			verticalValue = frames.get(0).getHeight()/2;
			xEndpoints = new int[2];
			yEndpoints = new int[2];
			xEndpoints[0] = 20;
			yEndpoints[0] = 20;
			whichEndIsSelected = -1;
			xEndpoints[1] = frames.get(0).getWidth()-20;
			yEndpoints[1] = frames.get(0).getHeight()-20;
			
			// ok, we're done!
			isLoading = false;
			myParent.processFinished();
			repaint();
		}
		
	}
	//--------------------------------------------  HORIZONTAL EXECUTION THREAD ---------------------------------------
	/**
	 * This thread performs the analysis of this video, taking a slice of each frame and accumulating them in a new
	 * BufferedImage that essentially graphs x vs. time for the given y value.
	 */
	public class HorizontalExecutionThread implements Runnable
	{

		private int y;
		public HorizontalExecutionThread(int y)
		{
			super();
			this.y =y;
		}
		
		/**
		 * This is where we will create the new image and display it.
		 */
		public void run()
		{
			// GUI stuff - let the controls reflect that we are in the middle of the generation.
			myParent.deactivateControls();
			isGenerating = true;
			repaint();
			int originalFrame = currentFrame;
			// TODO: you need to create a destination array. I am making one here that is almost certainly the wrong size.
			int[][][] destination = ImageManager.createRGBArrayOfSize(600,300); // makes an array [600][300][3] - the 3 is for RGB.
			for (int f = 0; f<frames.size(); f++)
			{
				int[][][] sourceFrame = ImageManager.RGBArrayFromImage(frames.get(f));
			    //TODO: You write this. You're going to need to copy the color info (the last set of 3 [] values) for each point along the line for this frame into the new picture.
				
				
				
				
				// do this for each frame. Update the parent and take a quick pause to let the screen catch up.
				myParent.updateProgress(f,frames.size());
				currentFrame = f;
				repaint();
				try
				{	Thread.sleep(1); // pause process 1 millisecond. - lets other threads do some work. Avoids the dreaded rainbow pinwheel of death while your computer is executing the loop.
				}
				catch (InterruptedException iExp)
				{
					System.out.println(iExp.getMessage());
					break;
				}
			}
			// convert the destination array into a BufferedImage, which can be drawn and saved.
			BufferedImage destBI = ImageManager.ImageFromArray(destination);

			// show the resulting image in a new window of its own.
			new DisplayWindow(destBI);		

			// ok, we're done. Reset the GUI.
			currentFrame = originalFrame;
			isGenerating = false;
			repaint();
			myParent.processFinished();
			
		}
		
	}
	
	//--------------------------------------------  VERTICAL EXECUTION THREAD ---------------------------------------
		/**
		 * This thread performs the analysis of this video, taking a slice of each frame and accumulating them in a new
		 * BufferedImage that essentially graphs y vs. time for the given x value.
		 */
		public class VerticalExecutionThread implements Runnable
		{

			private int x;
			public VerticalExecutionThread(int x)
			{
				super();
				this.x =x;
			}
			
			public void run()
			{
				//TODO: You write this! It should be very similar to the Horizontal version.
				
			
			}
		}
	//--------------------------------------------  DIAGONAL EXECUTION THREAD ---------------------------------------
		/**
		 * This thread performs the analysis of this video, taking a slice of each frame and accumulating them in a new
		 * BufferedImage that essentially graphs p vs. time for points along the line.
		 */
		public class DiagonalExecutionThread implements Runnable
		{

			private int[] xEndPoints, yEndPoints;
			public DiagonalExecutionThread(int[] xPoints, int[] yPoints)
			{
				super();
				this.xEndPoints = xPoints;
				this.yEndPoints = yPoints;
			}
			
			public void run()
			{
				// TODO: You write this! The big difference on this one is that you will be finding the distance between the endpoints and
				//  finding the color at each step along the line. This will likely involve interpolating the color between points.
				
			
			}
		}
}
