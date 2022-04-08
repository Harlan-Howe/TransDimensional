import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TransDimFrame extends JFrame implements ChangeListener, ActionListener {

	private int num_frames;
	private VideoPanel videoPanel;
	private JSlider frameSlider;
	private JButton executeButton;
	private JButton horizontalModeButton, verticalModeButton, diagonalModeButton;
	private JLabel frameNumberLabel;
	private JProgressBar progressBar;
	
	
	public TransDimFrame()
	{
		super("Motion Graph Selector");
		setSize(800,800);
		getContentPane().setLayout(new BorderLayout());
				
		JPanel northPanel = buildNorthPanel();
		Box southPanel = buildSouthPanel();
		videoPanel = new VideoPanel(this);
	
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(videoPanel, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		deactivateControls();
		videoPanel.startLoading();
		
	}
	
	private JPanel buildNorthPanel()
	{
		horizontalModeButton = new JButton("Horizontal");
		verticalModeButton = new JButton("Vertical");
		diagonalModeButton = new JButton("Diagonal");
		
		horizontalModeButton.addActionListener(this);
		verticalModeButton.addActionListener(this);
		diagonalModeButton.addActionListener(this);
		
		horizontalModeButton.setSelected(true);
		
		JPanel northPanel = new JPanel();
		northPanel.add(horizontalModeButton);
		northPanel.add(verticalModeButton);
		northPanel.add(diagonalModeButton);
		return northPanel;
	}
	
	private Box buildSouthPanel()
	{
		
		frameSlider = new JSlider();
		frameSlider.setValue(0);
		frameSlider.addChangeListener(this);
		frameNumberLabel = new JLabel("0");
		executeButton = new JButton("Execute");
		executeButton.addActionListener(this);
		progressBar = new JProgressBar();
		
		Box southPanel = Box.createHorizontalBox();
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(frameNumberLabel);
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(frameSlider);
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(executeButton);
		southPanel.add(Box.createHorizontalStrut(10));
		southPanel.add(progressBar);
		southPanel.add(Box.createHorizontalStrut(10));
		
		return southPanel;
	}
	
	/**
	 * deactivate the controls while we are running a process.
	 */
	public void deactivateControls()
	{
		executeButton.setEnabled(false);
		frameSlider.setEnabled(false);
		
	}
	
	/**
	 * ok, the file is loaded or the image is generated- set the progress bar to full and enable the controls.
	 */
	public void processFinished()
	{
		executeButton.setEnabled(true);
		frameSlider.setEnabled(true);
		updateProgress(100,100);
	}
	
	/**
	 * update the maximum of the slider.
	 * @param num
	 */
	public void setNumFrames(int num)
	{
		num_frames = num;
		frameSlider.setMaximum(num_frames-1);
	}

	@Override
	/**
	 * deal with the fact that the slider has been moved.
	 */
	public void stateChanged(ChangeEvent e) 
	{
		if (videoPanel == null || frameSlider == null)
			return;
		
		videoPanel.moveToFrame(frameSlider.getValue());
		frameNumberLabel.setText(String.format("%3d", frameSlider.getValue()));
	}

	@Override
	/**
	 * deal with one of the buttons being pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == executeButton)
			videoPanel.handleExecuteButtonPressed();
		
		if (e.getSource() == horizontalModeButton)
		{
			horizontalModeButton.setSelected(true);
			verticalModeButton.setSelected(false);
			diagonalModeButton.setSelected(false);
			videoPanel.setMode(VideoPanel.HORIZONTAL_MODE);
		}
		if (e.getSource() == verticalModeButton)
		{
			horizontalModeButton.setSelected(false);
			verticalModeButton.setSelected(true);
			diagonalModeButton.setSelected(false);
			videoPanel.setMode(VideoPanel.VERTICAL_MODE);
		}
		if (e.getSource() == diagonalModeButton)
		{
			horizontalModeButton.setSelected(false);
			verticalModeButton.setSelected(false);
			diagonalModeButton.setSelected(true);
			videoPanel.setMode(VideoPanel.DIAGONAL_MODE);
		}	
	}
	
	/**
	 * revise the state of the progress bar.
	 * @param current the size of the bar...
	 * @param total .. compared to this maximum.
	 */
	public void updateProgress(int current,int total)
	{
		//System.out.println(current+"-"+total);
		progressBar.setMaximum(total);
		progressBar.setValue(current);
		progressBar.repaint();
	}
	
}
