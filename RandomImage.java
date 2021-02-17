package source;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class RandomImage {

	private static final String dir;
	private static final Path p;
	private static BufferedImage defaultImage;
	private List<String> imageNames;
	private BufferedImage currentImage;
	private static DebugLog dl;
	
	static {
		dir = System.getProperty("user.dir");
		p = Paths.get(dir,"assets");
		defaultImage = new BufferedImage(200,200,IndexColorModel.TRANSLUCENT);
	}
	
	public void printImageNames() {
		imageNames.forEach(System.out::println);
	}
	
	public RandomImage(Random r, DebugLog dl) {
		imageNames = new ArrayList<>();
		RandomImage.dl = dl;
		
		try {
			if(Files.exists(p)) {
				RandomImage.dl.logLn("Trying to pares images from the assets folder");
				Iterator<Path> ip = Files.list(p).collect(Collectors.toList()).iterator();
				while(ip.hasNext()) {
					imageNames.add(ip.next().toString());
				}
				RandomImage.dl.logLn("Successfully created a list of imagesNames with # images: " + imageNames.size());
			} else {
				Files.createDirectory(p);
				dl.logLn(ImageIO.write(defaultImage,"jpeg",p.toFile())+"");
				throw new IOException("path was just created...no files would be found");
			}
		} catch (IOException e) {
			dl.logLn("Something Happened When trying to load the images from:\n" + e.getLocalizedMessage());
		}
		setImage(r);
	}
	
	private void setImage(Random rand) {
		if(rand == null) {
			dl.logLn("Random Object was null :(");
			rand = new Random();
		}
		
		if(imageNames.size() <= 0) {
			dl.logLn("No images were able to be loaded...Default/blank Image will be used");
			currentImage = defaultImage;
		} else if(imageNames.size() == 1) {
			setImage(imageNames.get(0));
		} else {
			setImage(imageNames.get(rand.nextInt( imageNames.size() )));
		}
	}
	
	private void setImage(String name) {
		try {
			currentImage = ImageIO.read(Paths.get(name).toFile());
			dl.logLn("Assigned to image: " + name);
		} catch (IOException e) {
			dl.logLn(e.getMessage());
			currentImage = defaultImage;
		}
	}
	
	public BufferedImage getImage() {
		return currentImage;
	}
}