import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DisplayPanel extends JPanel {

	private Image myImage;
	
	public DisplayPanel(Image img)
	{
		super();
		setMyImage(img);
	}
	
	public void setMyImage(Image img)
	{
		myImage = img;
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (myImage != null)
			g.drawImage(myImage,0,0,null);
	}
	
	public void save()
	{
		JFileChooser dialog = new JFileChooser();
		dialog.addChoosableFileFilter(new FileNameExtensionFilter("png files","png"));
		int result = dialog.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File theFileLoc = dialog.getSelectedFile();
			try {
				ImageIO.write((RenderedImage)myImage, "png", theFileLoc);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		
	}
}
