import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;

public class DisplayWindow extends JFrame implements ActionListener {
	
	public DisplayPanel myPanel;
	
	public DisplayWindow(BufferedImage img)
	{
		super("Result");
		setSize((int)img.getWidth(),(int)img.getHeight()+36);
		getContentPane().setLayout(new BorderLayout());
		myPanel = new DisplayPanel(img);
		getContentPane().add(myPanel, BorderLayout.CENTER);
		JButton saveButton = new JButton("Save");
		getContentPane().add(saveButton, BorderLayout.SOUTH);
		saveButton.addActionListener(this);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		myPanel.save();
	}
	
	
}
