package com.opencvcamera;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * @author emnaa
 *
 */
public class Camera extends JFrame {

	private int absoluteFaceSize = 0;
	private JLabel label;
	private JFrame frame;
	private JButton b,stop,nearest;
	private ImageIcon icon;
	private VideoCapture capture;
	private Mat image;
	private String emotionRead="";
	private boolean clicked = false;
	private boolean closed = false;
	private boolean near = false;
	public int windowH;
	public int windowW;
	//detection
	CascadeClassifier faceDetector;
	CascadeClassifier eyeDetector;
	CascadeClassifier smileDetector;

	public void readingFromProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			windowH=Integer.valueOf(prop.getProperty("windowHeith"));
			//System.out.println(windowH);
			windowW=Integer.valueOf(prop.getProperty("windowWidth"));
			//System.out.println(windowW);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public Camera() {

		//loadCascade
		String face_cascade_name = "haarcascade_frontalface_alt.xml";
		faceDetector = new CascadeClassifier(face_cascade_name);
		String eye_cascade_name = "haarcascade_eye.xml";
		eyeDetector = new CascadeClassifier(eye_cascade_name);
		String smile_cascade_name = "haarcascade_smile.xml";
		smileDetector = new CascadeClassifier(smile_cascade_name);
		readingFromProperties();
		setLayout(null);//fmch arragement mou3ayn

		label = new JLabel();
		label.setBounds(0, 0, 640, 480);
		add(label);

		//test sur load classifiers

		if(faceDetector.empty())
		{
			System.out.println("--(!)Error loading face classifier\n");
			return;
		}
		else
		{
			System.out.println("Face classifier loooaaaaaded up");
		}
		if(eyeDetector.empty())
		{
			System.out.println("--(!)Error loading eyes classifier\n");
			return;
		}
		else
		{
			System.out.println("Eyes classifier loooaaaaaded up");
		}
		if(smileDetector.empty())
		{
			System.out.println("--(!)Error loading smile classifier\n");
			return;
		}
		else
		{
			System.out.println("Smile classifier loooaaaaaded up");
		}

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing (WindowEvent e) {
				super.windowClosed(e);
				capture.release();
				image.release();
				closed = true;
				System.out.println("closed");
				System.exit(0);

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				super.windowDeactivated(e);
				System.out.println("closed");
			}

		});

		//initGUI
		setFocusable(false);
		frame = new JFrame("Camera");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(windowH, windowW);

		//button 
		b=new JButton("Analyze Emotions");
		b.setBounds(90,50, 133,35);
		b.setBackground(Color.GREEN);

		b.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				clicked=true;  
			}  
		});  
		frame.getContentPane().add(b);

		//stop button
		stop=new JButton("Stop Analyzing");
		stop.setBounds(253,50, 133,35);
		stop.setBackground(Color.RED);

		stop.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				clicked=false;
			}  
		});  
		frame.getContentPane().add(stop);

		//Nearest Face button
		stop=new JButton("Nearest Face");
		stop.setBounds(416,50, 133,35);
		stop.setBackground(Color.ORANGE);

		stop.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				near=true; 
			}  
		});  
		frame.getContentPane().add(stop);


		label = new JLabel();
		frame.add(label);
		frame.setLocationRelativeTo(null);	
		frame.setVisible(true);	
	}

	public static void main(String... args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("\nRunning FaceDetector");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Camera c = new Camera();
				new Thread(new Runnable() {
					public void run() {
						c.startCamera(args);
					}
				}).start();
			}
		});
	}

	public void startCamera(String[] args) {

		capture = new VideoCapture(0);
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,640);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,480);
		image = new Mat();
		byte[] imageData;
		if( capture.isOpened()) {
			while (true) {
				//read image to matrix
				capture.read(image);

				if( !image.empty() ){  
					int nbFaces=detectAndDrawFace(image);
					detectAndDrawEyes(image,nbFaces);
					//convert matrix to byte
					final MatOfByte buf = new MatOfByte();
					Imgcodecs.imencode(".jpg", image, buf);

					imageData = buf.toArray();

					//add to JLabel
					icon = new ImageIcon(imageData);
					label.setIcon(icon);
					System.out.println(image.cols());

					frame.pack();  //this will resize the window to fit the image

				}  
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}

				if (closed) {
					break;
				}
			}
		}
		else{
			System.out.println("Couldn't open capture.");
		}
	}

	private float Air(Rect rect){
		return rect.height*rect.width;
	}

	private int detectAndDrawFace(Mat image) {
		List<Float> arrayListAirs = new ArrayList<Float>();// Create an ArrayList object
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections, 1.05, 5,0,new Size(150,150),new Size(230,230));
		//Draw a bounding box around each face
		Rect[] facesArray = faceDetections.toArray();
		float bigestAir=0;
		int orderBiggest=0;

		for (int i = 0; i < facesArray.length; i++) {
			String w=String.valueOf(facesArray[i].width);
			System.out.println("Width Face:"+w);
			String h=String.valueOf(facesArray[i].height);
			System.out.println("Height Face:"+h);
			arrayListAirs.add((float)(Air(facesArray[i])));
		}

		if(arrayListAirs.size()>0) {
			for (int i = 0;i<arrayListAirs.size(); i++) {
				if(arrayListAirs.get(i)>bigestAir) {
					bigestAir=arrayListAirs.get(i);
					orderBiggest=i;
				}
			}
		}
		for (int i = 0; i < facesArray.length; i++) {
			if(i==orderBiggest) {
				//DRAW in red
				Imgproc.rectangle(image, new Point(facesArray[i].x, facesArray[i].y), new Point(facesArray[i].x + facesArray[i].width, facesArray[i].y + facesArray[i].height), new Scalar(0, 0, 255),5);		    
			}
			else
			{
				Imgproc.rectangle(image, new Point(facesArray[i].x, facesArray[i].y), new Point(facesArray[i].x + facesArray[i].width, facesArray[i].y + facesArray[i].height), new Scalar(0, 255, 0),5);

			}
		}
		if(clicked==true) {
			for (int i = 0; i < facesArray.length; i++) {
				//n3wd ncherchi ala a9rb wjh
				if(i==orderBiggest){
					//crop
					Mat outFace = image.submat(facesArray[i]);
					// saving cropped face to png form
					Imgcodecs.imwrite("Visage" + i + ".png", outFace);
					//analyzing the captured pic
					ExecutePythonScript();
					emotionRead=ReadFromFile();
					Imgproc.putText(image,emotionRead, new Point(facesArray[i].x, facesArray[i].y + facesArray[i].height+30),Imgproc.FONT_HERSHEY_SIMPLEX,0.9,new Scalar(0, 0, 255),5);
				}
			}
		}


		return facesArray.length;
	}

	private void detectAndDrawEyes(Mat image, int nb) {
		if(nb!=0) {
			MatOfRect EyesDetections = new MatOfRect();
			eyeDetector.detectMultiScale(image, EyesDetections, 1.1, 3,0,new Size(20,30),new Size());
			//Draw a bounding box around each face
			for (Rect rect : EyesDetections.toArray()) {
				Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0),5);
			}
		}
	}

	private String ReadFromFile() {
		try
		{
			// Le fichier d'entrée
			File file = new File("C:/Users/emnaa/Desktop/Emotions.txt");    
			// Créer l'objet File Reader
			FileReader fr = new FileReader(file);  
			// Créer l'objet BufferedReader        
			BufferedReader br = new BufferedReader(fr);  
			StringBuffer sb = new StringBuffer();    
			String line;
			while((line = br.readLine()) != null)
			{
				// ajoute la ligne au buffer
				sb.append(line);      
				sb.append("\n");     
			}
			fr.close();    
			System.out.println("Contenu du fichier: ");
			System.out.println(sb.toString());
			return sb.toString();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return "Operation Failed";
		}

	}

	private static void ExecutePythonScript() { 
		ProcessBuilder pb = new ProcessBuilder("python","Emotions.py");
		pb.directory(new File("C:/Users/emnaa/eclipse-workspace/OpenCv Camera"));
		pb.inheritIO();
		pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
			int exitVal = process.waitFor();
			if (exitVal != 0) {
				System.out.println("Abnormal Behaviour! Something bad happened.");
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Something went wrong. Here are more details\n"+e.getMessage());
		}
	}
}

