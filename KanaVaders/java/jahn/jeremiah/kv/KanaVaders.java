package jahn.jeremiah.kv;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.animation.Animation.Status;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * Created on Nov 17, 2014
 */

/**
 * @author jeremiah
 * @version $Id: $
 */
public class KanaVaders extends Application
{
    private enum WritingSystem
    {//       4e00 - 9faf
        HIRAGANA(83,12353),
        KATAKANA(89,12449),
        KANA(89,12449),
        KANJI(2499,19968);
        //KANJI(20911,19968);
        
        int limit = 0;
        int unicodeBase = 0;
        /**
         * 
         */
        private WritingSystem(int limit,int unicodeBase)
        {
            this.limit = limit;
            this.unicodeBase = unicodeBase;
        }
    }
    
    private enum KanjiTest
    {
        onyomi,
        kunyomi,
        meaning;
    }
   
    private static final String[] romanji = new String[]{"A","A","I","I","U","U","E","E","O","O","KA","GA","KI","GI","KU","GU","KE","GE","KO","GO","SA","ZA","SHI","JI","SU","ZU","SE","ZE","SO","ZO","TA","DA","CHI","DI","TSU","TSU","DZU","TE","DE","TO","DO","NA","NI","NU","NE","NO","HA","BA","PA","HI","BI","PI","FU","BU","PU","HE","BE","PE","HO","BO","PO","MA","MI","MU","ME","MO","YA","YA","YU","YU","YO","YO","RA","RI","RU","RE","RO","WA","WA","WI","WE","WO","N","VU","KA","KE","VA","VI","VE","VO"};
    private static final int PIECES = 5;
    private WritingSystem writingSystem = WritingSystem.HIRAGANA;
    private WritingSystem selectedWritingSystem = WritingSystem.HIRAGANA;
    private Text text;
    private int pos = 0;
    private int level = 1;
    private int correct = 0;
    private int totalPoints = 0;
    private Random random = new Random();
    private boolean safe = true;
    private int required = 1;
    private Vector<Integer> wrongPoolVector = new Vector<Integer>();
    private File safetyFile;
    private File screenFile;
    private Stage stage;
    private String lastImageDir = null;
    private Preferences preferences;
    private Path path;
    private boolean crazyMode = false;
    private ImageView bombImageView;
    private Image bombImage = new Image(KanaVaders.class.getResource("explosion.gif").toString());
    private Image bombDoneImage = new Image(KanaVaders.class.getResource("explosion_done.gif").toString());
    private Vector<ImageView> bombImageViewVector = new Vector<ImageView>();
    private Text status;
    private Scene scene;
    private ProgressBar progressBar;
    private Text charList;
    private ImageView imgView;
    private TextField textField;
    private PathTransition pathTransition;
    private Slider levelSlider;
    private Group root;
    private ToggleButton writingSystemToggleButton;
    private ToggleButton arcadeToggleButton;
    private HashMap<String, Element> freqMap;
    private Kanji kanji = null;
    private KanjiTest kanjiTest = KanjiTest.onyomi;
    private Document dictionary = null;
    private XPathFactory xpathFactory = null;
    
    
    @Override
    public void start(Stage stage) {
        try
        {
            
            freqMap = new HashMap<String, Element>();
            dictionary = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(KanaVaders.class.getResource("kanjidic_simplified.xml").toString());
            xpathFactory = XPathFactory.newInstance();
            NodeList freqNodeList = (NodeList) xpathFactory.newXPath().evaluate("//freq", dictionary, XPathConstants.NODESET);
            for(int currentNode = 0; currentNode < freqNodeList.getLength(); currentNode++)
            {
                Element freqNode = (Element) freqNodeList.item(currentNode);
                freqMap.put((Integer.parseInt(freqNode.getTextContent())-1)+"", (Element) freqNode.getParentNode().getParentNode());
                //System.out.println(freqNode.getTextContent());
            }
            
            
            this.stage = stage;
            stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
            
            
            Rectangle rectangle = new Rectangle(0, 0, (int)Screen.getPrimary().getBounds().getWidth(), (int)Screen.getPrimary().getBounds().getHeight());
            Robot robot = new Robot();
            BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
            screenFile = File.createTempFile("jfx2_screen_capture", ".jpg");
            ImageIO.write(bufferedImage, "jpg", screenFile);


            GameKeyEventHandler gameKeyEventHandler = new GameKeyEventHandler();
           
            root = new Group();
            root.addEventHandler(KeyEvent.KEY_TYPED, gameKeyEventHandler);
            scene = new Scene(root, 500, 500, Color.WHEAT);
            preferences = Preferences.userNodeForPackage(KanaVaders.class);
            
            level = preferences.getInt("level", level);
            if(level <= 0) level = 1;
            required = preferences.getInt("required", required);
            lastImageDir = preferences.get("lastImageDir", lastImageDir);
            selectedWritingSystem = WritingSystem.valueOf(preferences.get("selectedWritingSystem", selectedWritingSystem.toString()));
            crazyMode = preferences.getBoolean("arcadeMode", false);
            safe = preferences.getBoolean("safeMode", false);
            if(lastImageDir != null && safe == false)
            {
            	safetyFile = new File(lastImageDir);
            	if(safetyFile.exists() == false)
            	{
            		safetyFile = null;
            	}
            }
            
            text = new Text();
            text.setScaleX(3);
            text.setScaleY(3);
            
            
            

            HBox toggleBox = new HBox();
            toggleBox.setSpacing(3);
            
            
            
            
            ToggleButton fullScreenToggleButton = new ToggleButton("FullScreen");
            fullScreenToggleButton.selectedProperty().addListener(e -> {            	
            	stage.setFullScreen(!stage.isFullScreen());
                preferences.putBoolean("fullScreen", stage.isFullScreen());
                textField.requestFocus();
                try{ preferences.flush();} catch (Exception e1) {}            	
            });
            fullScreenToggleButton.setScaleX(.75d);
            fullScreenToggleButton.setScaleY(.75d);
            fullScreenToggleButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            
            
            arcadeToggleButton = new ToggleButton(crazyMode ? "Arcade" : "Simple");
            arcadeToggleButton.setSelected(crazyMode);
            arcadeToggleButton.selectedProperty().addListener(e -> {            	            	
            	setArcadeMode(arcadeToggleButton.isSelected());
            });
            arcadeToggleButton.setScaleX(.75d);
            arcadeToggleButton.setScaleY(.75d);
            arcadeToggleButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            
            //Level Display stuff
            charList = new Text("");
            
            charList.setLayoutX(475);
            charList.setLayoutY(20);

            
            
            levelSlider = new Slider();
            levelSlider.setRotate(180);
            levelSlider.setMin(1);
            levelSlider.setMax(selectedWritingSystem.limit);
            levelSlider.setValue(level);
            levelSlider.setLayoutX(490);
            levelSlider.setLayoutY(20);
            levelSlider.setMinHeight(480);
            levelSlider.setOrientation(Orientation.VERTICAL);
            levelSlider.setFocusTraversable(false);
            levelSlider.valueProperty().addListener((observableValue,oldValue,newValue) -> {
                    if(oldValue.intValue() < newValue.intValue())
                    {
                        level = newValue.intValue()-1;
                        increaseLevel();                        
                    }
                    else
                    {
                        level = newValue.intValue()+1;
                        decreaseLevel();                        
                    }
                    //if(levelSlider.isValueChanging() == false) //give text field focus when done
                    {
                        textField.requestFocus();
                    }
            });
            
            writingSystemToggleButton = new ToggleButton(selectedWritingSystem.toString());
            writingSystemToggleButton.setSelected(crazyMode);
            writingSystemToggleButton.selectedProperty().addListener(e -> {            	
            	 if(selectedWritingSystem == WritingSystem.HIRAGANA)
                 {                     
            		 writingSystem = WritingSystem.KATAKANA;
                     selectedWritingSystem = writingSystem;                     
                 }
            	 else if (selectedWritingSystem == WritingSystem.KATAKANA)
            	 {
            		 selectedWritingSystem = WritingSystem.KANA;
            		 writingSystem = WritingSystem.KATAKANA;
            	 }
            	 else if (selectedWritingSystem == WritingSystem.KANA)
                 {
                     selectedWritingSystem = WritingSystem.KANJI;
                     writingSystem = WritingSystem.KANJI;
                 }
                 else
                 {
                	 selectedWritingSystem = WritingSystem.HIRAGANA;
                	 writingSystem = selectedWritingSystem;
                 }
            	 if(level >= selectedWritingSystem.limit)
                 {
                     level = selectedWritingSystem.limit-1;
                 }
            	 try
                {
                    setChar();
                }
                catch (Exception e1)
                {
                    
                    e1.printStackTrace();
                }
                 setCharList(charList);
                 levelSlider.setMax(selectedWritingSystem.limit);
                 preferences.put("selectedWritingSystem", selectedWritingSystem+"");
             	try{preferences.flush();} catch (BackingStoreException backingStoreException){}//ignore exception
            });
            writingSystemToggleButton.setScaleX(.75d);
            writingSystemToggleButton.setScaleY(.75d);
            writingSystemToggleButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            setChar();
            setCharList(charList); 
           
            ToggleButton stealthModeToggleButton = new ToggleButton("Stealth");
            stealthModeToggleButton.setSelected(safe);
            stealthModeToggleButton.selectedProperty().addListener(e -> {            	
            	setSafeMode(stealthModeToggleButton.isSelected());            	
            });
            stealthModeToggleButton.setScaleX(.75d);
            stealthModeToggleButton.setScaleY(.75d);
            stealthModeToggleButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            
            
            Button imgBrowseButton = new Button("Img...");            
            imgBrowseButton.pressedProperty().addListener(e -> {            	
            	safetyFile = null;
            	setSafeMode(false);
            });
            imgBrowseButton.setScaleX(.75d);
            imgBrowseButton.setScaleY(.75d);
            imgBrowseButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            
            
            Button exitButton = new Button("EXIT");
            exitButton.pressedProperty().addListener(e -> {            	
            	Platform.exit();
            });
            exitButton.setScaleX(.75d);
            exitButton.setScaleY(.75d);
            exitButton.setStyle("-fx-background-color: rgba(255,255,255,0.5);");
            
            
            //status stuff
            status = new Text();            
            progressBar = new ProgressBar();
            progressBar.setProgress(0);
            setStatusText();
            //END status stuff
            
            //START PATH AND EXPLOSION STUFF
            path = new Path();
            pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.millis(14000));
            pathTransition.setNode(text);            
            pathTransition.setOnFinished(e -> {
                if (level > 1 && pathTransition.isAutoReverse() == false)
                    {                    
                        level--;
                    }
                    totalPoints=0;
                    if(crazyMode)
                    {
                        
                        if(bombImageView != null)
                        {
                            bombImageView.setImage(bombDoneImage);
                        }
                        bombImageView = new ImageView(bombImage);
                        bombImageView.setFitHeight(75);
                        bombImageView.setPreserveRatio(true);
                        bombImageView.setSmooth(true);                   
                        root.getChildren().add(bombImageView);
                        bombImageViewVector.add(bombImageView);
                        bombImageView.setLayoutX(pathTransition.getNode().getBoundsInParent().getMinX());
                        bombImageView.setLayoutY(pathTransition.getNode().getBoundsInParent().getMinY());
                    }
                    buildPath(scene.widthProperty().intValue(), scene.heightProperty().intValue());
                });
            


            //LOAD IMAGE STUFF
            Image img = new Image(getImageFile());
            imgView = new ImageView(img);
            imgView.setFitHeight(500);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
            imgView.setCache(true);

            
            textField = new TextField();
            textField.setLayoutY(470);
            textField.setLayoutX(10);
            status.setLayoutY(450);
            status.setLayoutX(315);
            progressBar.setLayoutX(315);
            progressBar.setLayoutY(470);
            textField.setOnKeyTyped(gameKeyEventHandler);
            //END KEYBOARD STUFF
            
            //BUILD SCENE
            root.getChildren().add(imgView);
            root.getChildren().add(text);
            root.getChildren().add(textField);
            root.getChildren().add(charList);
            root.getChildren().add(levelSlider);
            root.getChildren().add(status);
            root.getChildren().add(progressBar);
            root.getChildren().add(toggleBox);
            toggleBox.getChildren().add(exitButton);
            toggleBox.getChildren().add(fullScreenToggleButton);
            toggleBox.getChildren().add(arcadeToggleButton);
            toggleBox.getChildren().add(writingSystemToggleButton);
            toggleBox.getChildren().add(stealthModeToggleButton);
            toggleBox.getChildren().add(imgBrowseButton);

            
            //RESIZE EVENT PROCESSING
            //height
            scene.heightProperty().addListener((c,n1,n2) -> {
                pathTransition.setDuration(Duration.millis(n2.intValue()*10+10000));
                textField.setLayoutY(n2.intValue()-30);
                status.setLayoutY(n2.intValue()-50);
                levelSlider.setMinHeight(n2.intValue()-50);
                progressBar.setLayoutY(n2.intValue()-30);
                buildPath(scene.widthProperty().intValue(), n2.intValue());
                imgView.setFitHeight(n2.intValue());
                pathTransition.playFromStart();
            });
            
            //width
            scene.widthProperty().addListener((c,n1,n2) -> {                
                buildPath(n2.intValue(),scene.heightProperty().intValue());
                status.setLayoutX(n2.intValue()-(status.getBoundsInParent().getWidth()+20));                
                progressBar.setLayoutX(n2.intValue()-(status.getBoundsInParent().getWidth()+20));
                progressBar.setMinWidth(n2.intValue()-progressBar.getLayoutX()-10);                
                charList.setLayoutX(n2.intValue()-30);
                levelSlider.setLayoutX(n2.intValue()-15);                
                
                pathTransition.playFromStart();
            });
            //END RESIZE CODE 
            
            //INITIAL STARTUP STUFF
            stage.setTitle("Kana-Vaders");
            stage.setScene(scene);
            stage.setFullScreen(preferences.getBoolean("fullScreen", false));
            if(stage.isFullScreen())
            {
                buildPath((int)Screen.getPrimary().getBounds().getWidth(), (int)Screen.getPrimary().getBounds().getHeight());
            }
            else
            {
                buildPath(scene.widthProperty().intValue(), scene.heightProperty().intValue());
            }
            pathTransition.setPath(path);
            pathTransition.play();
            textField.requestFocus();
            stage.show();
        }
        catch (Exception e2)
        {

            e2.printStackTrace();
        }   
    }
    private void setChar()
	{
        text.setFill(Color.BLACK);
//    	//textField.set
//        do
//        {
//            pos = random.nextInt(level);
//        } while(romanji[pos].length() == 0);
//        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos)));
		
        do
        {
        	if(selectedWritingSystem == WritingSystem.KANA)
            {
            	writingSystem = WritingSystem.values()[random.nextInt(WritingSystem.KANA.ordinal())];
            	setCharList(charList);
            }
        	
            if(correct == -1) //when uping the level, always use our new letter.
            {
                pos = level-1;
                correct = 0;
            }
            else
            {
                pos = random.nextInt(level+wrongPoolVector.size());
            }
            if(pos >= level) // looks like we want a number that's in our wrong pool
            {
                pos = wrongPoolVector.get(pos-level);
            }
            //clean out the wrong pool, to finish the level
            else if(correct > required && wrongPoolVector.size() > 0)
            {
                pos = wrongPoolVector.remove(0);
            }
            if(selectedWritingSystem == WritingSystem.KANJI)
            {
                break;
            }
        } while(romanji[pos].length() == 0);
        
        if(selectedWritingSystem == WritingSystem.KANJI)
        {
            try
            {
                kanji = new Kanji(freqMap.get(pos+""));
                text.setText(kanji.kanji);
                switch (kanjiTest)
                {
                    case kunyomi:
                        text.setFill(Color.DARKBLUE);
                        break;
                    case onyomi:
                        text.setFill(Color.DARKGREEN);
                        break;
                    default:
                        text.setFill(Color.BLACK);
                        break;
                }
            }
            catch (Exception e)
            {
                
                e.printStackTrace();
            }
            
        }
        else
        {
            text.setText(Character.toString((char)(writingSystem.unicodeBase+pos)));
        }
        
	}
	private void setArcadeMode(boolean arcadeMode)
	{
    	this.crazyMode = arcadeMode;
    	if(crazyMode == false)
        {
            root.getChildren().removeAll(bombImageViewVector);
            bombImageViewVector.clear();
            bombImageView = null;
        }
    	preferences.put("arcadeMode", crazyMode+"");
    	try
		{
			preferences.flush();
		} catch (BackingStoreException e){}
    	arcadeToggleButton.setText(crazyMode ? "Arcade" : "Simple");
    	textField.requestFocus();
	}
	/**
     * 
     */
    private void success() throws Exception
    {        
        boolean actualSuccess = true;
        buildPath(scene.widthProperty().intValue(), scene.heightProperty().intValue());
        if(selectedWritingSystem == WritingSystem.KANJI)
        {
            if(kanjiTest.ordinal() == KanjiTest.values().length-1)
            {
                kanjiTest = KanjiTest.values()[0];
            }
            else
            {
                kanjiTest = KanjiTest.values()[kanjiTest.ordinal()+1];
                text.setText(kanji.kanji);
                switch (kanjiTest)
                {
                    case kunyomi:
                        text.setFill(Color.DARKBLUE);
                        break;
                    case onyomi:
                        text.setFill(Color.DARKGREEN);
                        break;
                    default:
                        text.setFill(Color.BLACK);
                        break;
                }
                actualSuccess = false;
            }
            
        }
        if(actualSuccess)
        {
            correct++;
            totalPoints++;
            if (level < romanji.length && correct >= required && wrongPoolVector.size() == 0)
            {
                correct = -1;
                increaseLevel();
                Image imgTmp = new Image(getImageFile());
                imgView.setImage(imgTmp);
                setCharList(charList);
                if(required > 100)
                {
                    required = 100;
                }
                preferences.putInt("level", level);
                preferences.putInt("required", required);
                try{ preferences.flush();} catch (Exception e1) {}

            }
        
            setChar();
        }
        textField.clear();        
        pathTransition.playFromStart();
        setStatusText();
    }
    /**
     * 
     */
    private void decreaseLevel()
    {
        level--;
        setStatusText();
        setCharList(charList);          
        preferences.putInt("level", level);
        preferences.putInt("required", required);
        try{ preferences.flush();} catch (Exception e1) {}
        
    }
    /**
     * 
     */
    private void increaseLevel()
    {
        level++;
        if(level > selectedWritingSystem.limit)
        {
            level--;
        }
        setStatusText();
        setCharList(charList);
        levelSlider.setValue(level);
        preferences.putInt("level", level);
        preferences.putInt("required", required);
        try{ preferences.flush();} catch (Exception e1) {}
        
    }
    private void setCharList(Text charList)
	{
    	StringBuffer buffer = new StringBuffer();    	
    	for(int index = 0; index < level; index++)
    	{
    	    if(selectedWritingSystem != WritingSystem.KANJI)
    	    {
    	        if(romanji[index].length() == 0)
    	        {
    	            continue;
    	        }
    	        buffer.append("\n");
                buffer.append(Character.toString((char)(writingSystem.unicodeBase+index)));
    	    }
    	    else
    	    {
    	        buffer.append("\n");
                try
                {                    
                    buffer.append(freqMap.get(index+"").getElementsByTagName("literal").item(0).getTextContent());
                }
                catch (Exception e)
                {
                    
                    e.printStackTrace();
                }
    	    }
    		
    	}
		charList.setText(buffer.toString());
		
		writingSystemToggleButton.setText(selectedWritingSystem.toString());
		
	}
    
    private void setSafeMode(boolean safeMode)
    {
        this.safe = safeMode;
        preferences.putBoolean("safeMode", safeMode);
        try{ preferences.flush();} catch (Exception e1) {}
        pathTransition.pause();
        try{
        	Image imgTmp = new Image(getImageFile());
        	imgView.setImage(imgTmp); 
        } catch (Exception e1) {}
               
        pathTransition.play();
        textField.requestFocus();
    }
    
	private String getImageFile() throws Exception
	{
		
		if(safe == true)
		{
		    if(screenFile == null || screenFile.exists() == false || screenFile.isDirectory())
		    {
		        return KanaVaders.class.getResource("safe.jpg").toString();    
		    }
		    else
		    {
		        return screenFile.toURI().toURL().toString();
		    }
			
		}
		while(safetyFile == null)
        {
		    
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Choose Image/Image Directory");
            
            if(lastImageDir != null)
            {
                
                File lastImageDirFile = new File(lastImageDir);
                if (lastImageDirFile.isDirectory())
                {
                    fileChooser.setInitialDirectory(lastImageDirFile);
                    //fileChooser.setInitialFileName(null);
                }
                else
                {
                    fileChooser.setInitialDirectory(lastImageDirFile.getParentFile());
                    //fileChooser.setInitialFileName("");
                    //fileChooser.setInitialFileName(lastImageDir);
                }
            }
            
            safetyFile = fileChooser.showDialog(stage);    
            
            
            if(safetyFile == null)
            {
                continue;
                //return KanaVaders.class.getResource("safe.jpg").toString();
            }
            if(safetyFile.isDirectory() == false)
            {
            	return safetyFile.toURI().toURL().toString();
            }
        }
        File file = safetyFile.getParentFile();
        if(safetyFile.isDirectory())
        {
            file = safetyFile;
            lastImageDir = safetyFile.getAbsolutePath();
        }        
        preferences.put("lastImageDir", safetyFile.getAbsolutePath());
        try{ preferences.flush();} catch (Exception e1) {}
        
        File[] fileList = file.listFiles();
		return fileList[random.nextInt(fileList.length)].toURI().toURL().toString();
	}
	
	
	private void buildPath(int width, int height)
	{
	    int startX = 20;
	    int startY = 20;
	    path.getElements().clear();
	    path.getElements().add(new MoveTo(startX+(crazyMode ? random.nextInt(width) : 0),startY));
	    int xSize = width/PIECES;
	    int ySize = height/PIECES;
	    for(int currentPiece = 0; currentPiece < PIECES; currentPiece++)
	    {
	        
	        
	        if(currentPiece == PIECES-1)
	        {
	            if(crazyMode == true)
	            {
	                if (width < 75)
	                {
	                    width = 76;
	                }
	            }
	            startX = (crazyMode ? random.nextInt(width-75) : -75);
	            xSize = (crazyMode ? 0 : xSize);    
	            startY = -75;  
	        }
	        CubicCurveTo cubicCurveTo = new CubicCurveTo(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height), startX+(xSize*(currentPiece+1)), startY+(ySize*(currentPiece+1)));
	        path.getElements().add(cubicCurveTo);
	    }
	}
	
	private void setStatusText()
	{
	    if(selectedWritingSystem == WritingSystem.KANJI)
	    {
	        status.setText(kanjiTest.toString()+"? "+correct+"/"+required+"+"+wrongPoolVector.size()+" Level="+level+"\nPoints = "+totalPoints);
	        status.setLayoutX(scene.widthProperty().intValue()-(status.getBoundsInParent().getWidth()+30));
	    }
	    else
	    {
	        status.setText(correct+"/"+required+"+"+wrongPoolVector.size()+" Level="+level+"\nPoints = "+totalPoints);
	        status.setLayoutX(scene.widthProperty().intValue()-(status.getBoundsInParent().getWidth()+20));
	    }
	    progressBar.setProgress((double)correct/(double)(required+wrongPoolVector.size()));
	}
	
	/**
     * @param args
     */
    public static void main(String[] args)
    {
       
        launch(args);
    }

    private class GameKeyEventHandler implements EventHandler<KeyEvent>
    {
        
        @Override
        public void handle(KeyEvent e)
        {
            try
            {
                if(textField.isFocused() == false)
                {
                    textField.requestFocus();
                }
                
                if(e.getCharacter() == null || e.getCharacter().length() == 0 || e.getCharacter().codePointAt(0) == 27) //consume escape key
                {                        
                    e.consume();
                }
                else if(pathTransition.getStatus() == Status.STOPPED) //resume on AnyKey
                {
                    pathTransition.play();
                    e.consume();
                }
                else if(e.getCharacter().equals(" ")) //change image
                {
                    Image imgTmp = new Image(getImageFile());
                    imgView.setImage(imgTmp);
                    e.consume();
                }
                else if(e.getCharacter().equals("2")) //tottlge writing system
                {
                  if(writingSystem == WritingSystem.HIRAGANA)
                  {
                      writingSystem = WritingSystem.KATAKANA;
                  }
                  else
                  {
                      writingSystem = WritingSystem.HIRAGANA;
                  }
                  setCharList(charList);
                  e.consume();
                }
                else if(e.getCharacter().equals("1")) //full screen 
                {
                   stage.setFullScreen(!stage.isFullScreen());
                   preferences.putBoolean("fullScreen", stage.isFullScreen());
                   try{ preferences.flush();} catch (Exception e1) {}
                   e.consume();
                }                    
                else if(e.getCharacter().equals("3")) //arcade mode
                {
                    crazyMode = !crazyMode;
                    setArcadeMode(crazyMode);
                    
                    e.consume();
                }                
                else if(e.getCharacter().equals("=")) //increase required
                {
                    required++;
                    setStatusText();
                    setCharList(charList);
                    e.consume();
                }
               
                else if(e.getCharacter().equals("]")) //increase speed
                {                        
                    if(pathTransition.getDuration().greaterThanOrEqualTo(Duration.millis(1000)))
                    {
                        pathTransition.setDuration(pathTransition.getDuration().subtract(Duration.millis(1000)));
                    }
                    pathTransition.playFromStart();
                    e.consume();
                }
                else if(e.getCharacter().equals("[")) //decrease speed
                {
                    pathTransition.setDuration(pathTransition.getDuration().add(Duration.millis(1000)));
                    pathTransition.playFromStart();
                    e.consume();
                }
                else if(e.getCharacter().equals(",")) //decrease level
                {
                    decreaseLevel();
                    e.consume();

                }
                
                else if(e.getCharacter().equals("P")) //pause
                {
                    if(pathTransition.getStatus() == Status.PAUSED)
                    {
                        pathTransition.play();
                    }
                    else
                    {
                        pathTransition.pause();
                    }
                    e.consume();
                }
                
                else if(e.getCharacter().equals("`")) //toggle safe mode
                {
                	safe = safe ? false : true;
                	setSafeMode(safe);
                    e.consume();
                }
                else if(e.getCharacter().equals("~")) //toggle safe mode
                {
                    safetyFile = null;
                    setSafeMode(false);
                    e.consume();
                }
                else if(selectedWritingSystem != WritingSystem.KANJI && (textField.getText()+e.getCharacter()).equalsIgnoreCase(romanji[pos])) //correct romanji/ success
                {
                    
                    success();
                    e.consume();
                }
                else if(selectedWritingSystem == WritingSystem.KANJI && (isKanjiTestCorrect(textField.getText()+e.getCharacter()))) //correct romanji/ success
                {
                    //KANJI MEANING and ROUMAJI TEST 
                    success();
                    e.consume();
                }
                else if (selectedWritingSystem != WritingSystem.KANJI && textField.getText().endsWith(e.getCharacter())) //ignore double type
                {
                    e.consume();
                }
                else if (selectedWritingSystem != WritingSystem.KANJI && romanji[pos].startsWith((textField.getText()+e.getCharacter()).toUpperCase()))
                {
                   
                    //do nothing
                }
                else if (selectedWritingSystem == WritingSystem.KANJI && doesKanjiStartWith((textField.getText()+e.getCharacter())))
                {
                  //KANJI MEANING and ROUMAJI STARTS WITH TEST 
                    //do nothing
                }
                else if(e.getCharacter().equals(".")) //must be careful, because this can be used in Kanji tests
                {
                    increaseLevel();
                    e.consume();
                }
                else if(e.getCharacter().equals("-")) //decrease required
                { //must be careful, because this can be used in Kanji tests
                    required--;
                    setStatusText();                        
                    setCharList(charList);
                    e.consume();
                }
                else //fail
                {
                    //ADD KANJI Reading than meaning update
                    if(selectedWritingSystem != WritingSystem.KANJI)
                    {
                        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos))+" ("+romanji[pos]+")");
                    }
                    else
                    {
                        switch (kanjiTest)
                        {
                            case onyomi:
                                text.setText(kanji.kanji+"   "+Arrays.toString(kanji.onYomis).replaceAll("[\\[\\]]", "")+"  ");
                                break;
                            case kunyomi:
                                text.setText(kanji.kanji+"   "+Arrays.toString(kanji.kunYomis).replaceAll("[\\[\\]]", "")+"  ");
                                break;
                            case meaning:
                                text.setText(kanji.kanji+"   "+Arrays.toString(kanji.meanings).replaceAll("[\\[\\]]", "")+"  ");
                                break;
                            default:
                                
                        }
                       
                    }
                    e.consume();
                    //correct--;
                    totalPoints--;
                    setStatusText();
                    wrongPoolVector.add(pos);
                    required++;
                }
            } catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * @param string
         * @return
         */
        private boolean doesKanjiStartWith(String currentInput)
        {
            currentInput = currentInput.toLowerCase();
            switch (kanjiTest)
            {
                case onyomi:
                    for (String onYomiRoumaji : kanji.onYomisRoumajis)
                    {
                        if(onYomiRoumaji.toLowerCase().startsWith(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                case kunyomi:
                    for (String kunYomiRoumaji : kanji.kunYomisRoumajis)
                    {
                        if(kunYomiRoumaji.toLowerCase().startsWith(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                case meaning:
                    for (String meaning : kanji.meanings)
                    {
                        if(meaning.toLowerCase().startsWith(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return false;
        }

        /**
         * @param string
         * @return
         */
        private boolean isKanjiTestCorrect(String currentInput)
        {
            switch (kanjiTest)
            {
                case onyomi:
                    for (String onYomiRoumaji : kanji.onYomisRoumajis)
                    {
                        if(onYomiRoumaji.equalsIgnoreCase(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                case kunyomi:
                    for (String kunYomiRoumaji : kanji.kunYomisRoumajis)
                    {
                        if(kunYomiRoumaji.equalsIgnoreCase(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                case meaning:
                    for (String meaning : kanji.meanings)
                    {
                        if(meaning.equalsIgnoreCase(currentInput))
                        {
                            return true;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return false;
        }
    };
   
    
    private class Kanji
    {
        
        String kanji = null;
        private String[] onYomis;
        private String[] onYomisRoumajis;
        private String[] kunYomis;
        private String[] kunYomisRoumajis;
        private String[] meanings;
        /**
         * 
         */
        public Kanji(Element character) throws Exception
        {
         // Element element = (Element) xpathFactory.newXPath().evaluate("/kanjidic2/character/misc/freq", dictionary, XPathConstants.NODESET); 
            
            kanji = (String) xpathFactory.newXPath().evaluate("./literal/text()",character , XPathConstants.STRING);
            //System.out.println("Kanji = "+kanji+" freq = "+pos);
            NodeList meaningNodeList = (NodeList) xpathFactory.newXPath().evaluate("./reading_meaning/rmgroup/meaning", character, XPathConstants.NODESET);
            meanings = new String[meaningNodeList.getLength()];
            for(int index = 0; index < meanings.length; index++)
            {
                meanings[index] = meaningNodeList.item(index).getTextContent();
            }
           System.out.println("Meanings = "+Arrays.toString(meanings));
            
            NodeList onYomiNodeList = (NodeList) xpathFactory.newXPath().evaluate("./reading_meaning/rmgroup/reading[@r_type = 'ja_on']", character, XPathConstants.NODESET);
            onYomis = new String[onYomiNodeList.getLength()];
            onYomisRoumajis = new String[onYomiNodeList.getLength()];
            for(int index = 0; index < onYomis.length; index++)
            {
                onYomis[index] = onYomiNodeList.item(index).getTextContent();
                StringBuffer buffer = new StringBuffer();
                for(int currentChar = 0; currentChar < onYomis[index].length(); currentChar++)
                {
                    int codePoint = onYomis[index].codePointAt(currentChar);
                    if(codePoint >= WritingSystem.KATAKANA.unicodeBase)
                    {
                        buffer.append(romanji[codePoint - WritingSystem.KATAKANA.unicodeBase]);
                    }
                    else if (codePoint >= WritingSystem.HIRAGANA.unicodeBase)
                    {
                        buffer.append(romanji[codePoint - WritingSystem.HIRAGANA.unicodeBase]);
                    }
                    else
                    {
                        buffer.append(onYomis[index].charAt(currentChar));
                    }
                }
                onYomisRoumajis[index] = buffer.toString();
            }
            System.out.println("on'Yomi = "+Arrays.toString(onYomis));
            System.out.println("on'Romi = "+Arrays.toString(onYomisRoumajis));
            
            NodeList kunYomiNodeList = (NodeList) xpathFactory.newXPath().evaluate("./reading_meaning/rmgroup/reading[@r_type = 'ja_kun']", character, XPathConstants.NODESET);
            kunYomis = new String[kunYomiNodeList.getLength()];
            kunYomisRoumajis = new String[kunYomiNodeList.getLength()];
            for(int index = 0; index < kunYomis.length; index++)
            {
                kunYomis[index] = kunYomiNodeList.item(index).getTextContent();
                StringBuffer buffer = new StringBuffer();
                for(int currentChar = 0; currentChar < kunYomis[index].length(); currentChar++)
                {
                    int codePoint = kunYomis[index].codePointAt(currentChar);
                    if(codePoint >= WritingSystem.KATAKANA.unicodeBase)
                    {
                        buffer.append(romanji[codePoint - WritingSystem.KATAKANA.unicodeBase]);
                    }
                    else if (codePoint >= WritingSystem.HIRAGANA.unicodeBase)
                    {
                        buffer.append(romanji[codePoint - WritingSystem.HIRAGANA.unicodeBase]);
                    }
                    else
                    {
                        buffer.append(kunYomis[index].charAt(currentChar));
                    }
                }
                kunYomisRoumajis[index] = buffer.toString();
            }
            System.out.println("kun'Yomi = "+Arrays.toString(kunYomis));
            System.out.println("kun'Romi = "+Arrays.toString(kunYomisRoumajis));
        
        }
    }
    
}
