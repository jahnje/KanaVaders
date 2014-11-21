package jahn.jeremiah.kv;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.Vector;
import java.util.prefs.Preferences;

import javafx.animation.Animation.Status;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;

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
        KANJI(20911,19968);
        
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
   
    private static final String[] romanji = new String[]{"A","","I","","U","","E","","O","","KA","GA","KI","GI","KU","GU","KE","GE","KO","GO","SA","ZA","SHI","JI","SU","ZU","SE","ZE","SO","ZO","TA","DA","CHI","DI","TSU","","DZU","TE","DE","TO","DO","NA","NI","NU","NE","NO","HA","BA","PA","HI","BI","PI","FU","BU","PU","HE","BE","PE","HO","BO","PO","MA","MI","MU","ME","MO","YA","YA","YU","YU","YO","YO","RA","RI","RU","RE","RO","WA","WA","WI","WE","WO","N","VU","KA","KE","VA","VI","VE","VO"};
    private static final int PIECES = 5;
    private WritingSystem writingSystem = WritingSystem.HIRAGANA;
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
    @Override
    public void start(Stage stage) {
        try
        {
            this.stage = stage;
            stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
            //stage.setFullScreen(true);
            
            Rectangle rectangle = new Rectangle(0, 0, (int)Screen.getPrimary().getBounds().getWidth(), (int)Screen.getPrimary().getBounds().getHeight());
            Robot robot = new Robot();
            BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
            screenFile = File.createTempFile("jfx2_screen_capture", ".jpg");
            ImageIO.write(bufferedImage, "jpg", screenFile);


           
            Group root = new Group();
            scene = new Scene(root, 500, 500, Color.WHEAT);
            preferences = Preferences.userNodeForPackage(KanaVaders.class);
            level = preferences.getInt("level", level);
            required = preferences.getInt("required", required);
            lastImageDir = preferences.get("lastImageDir", lastImageDir);
           
            //textField.set
            do
            {
                pos = random.nextInt(level);
            } while(romanji[pos].length() == 0);
            
            text = new Text(Character.toString((char)(writingSystem.unicodeBase+pos)));
            text.setScaleX(3);
            text.setScaleY(3);

            
            
            charList = new Text("");
            setCharList(charList);
            charList.setLayoutX(475);
            charList.setLayoutY(20);

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
            textField.setLayoutY(450);
            status.setLayoutY(450);
            status.setLayoutX(315);
            progressBar.setLayoutX(315);
            progressBar.setLayoutY(480);
            textField.setOnKeyTyped(e -> {
                try
                {
                   
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
                       stage.setFullScreen(true);
                       e.consume();
                    }                    
                    else if(e.getCharacter().equals("3")) //arcade mode
                    {
                        crazyMode = !crazyMode;
                        if(crazyMode == false)
                        {
                            root.getChildren().removeAll(bombImageViewVector);
                            bombImageViewVector.clear();
                            bombImageView = null;
                        }
                        e.consume();
                    }
                    else if(e.getCharacter().equals("-")) //decrease required
                    {
                        required--;
                        setStatusText();                        
                        setCharList(charList);
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
                    else if(e.getCharacter().equals("."))
                    {
                        increaseLevel();
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
                        pathTransition.pause();
                        Image imgTmp = new Image(getImageFile());
                        imgView.setImage(imgTmp);
                        e.consume();
                        pathTransition.play();
                    }
                    else if((textField.getText()+e.getCharacter()).equalsIgnoreCase(romanji[pos])) //correct romanji/ success
                    {
                        success();
                        e.consume();
                    }
                    else if (romanji[pos].startsWith((textField.getText()+e.getCharacter()).toUpperCase()))
                    {
                        //do nothing
                    }
                    else //fail
                    {                
                        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos))+" ("+romanji[pos]+")");
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
            });
            //END KEYBOARD STUFF
            
            //BUILD SCENE
            root.getChildren().add(imgView);
            root.getChildren().add(text);
            root.getChildren().add(textField);
            root.getChildren().add(charList);
            root.getChildren().add(status);
            root.getChildren().add(progressBar);

            
            //RESIZE EVENT PROCESSING
            //height
            scene.heightProperty().addListener((c,n1,n2) -> {
                pathTransition.setDuration(Duration.millis(n2.intValue()*10+10000));
                textField.setLayoutY(n2.intValue()-50);
                status.setLayoutY(n2.intValue()-50);
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
                charList.setLayoutX(n2.intValue()-25);
                pathTransition.playFromStart();
            });
            //END RESIZE CODE 
            
            //INITIAL STARTUP STUFF
            stage.setTitle("Kana-Vaders");
            stage.setScene(scene);
            
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
            stage.show();
        }
        catch (Exception e2)
        {

            e2.printStackTrace();
        }   
    }
    /**
     * 
     */
    private void success() throws Exception
    {
        buildPath(scene.widthProperty().intValue(), scene.heightProperty().intValue());
        correct++;
        totalPoints++;
        if (level < romanji.length && correct >= required && wrongPoolVector.size() == 0)
        {
            correct = -1;
            level++;
            if(level > writingSystem.limit)
            {
                level--;
            }
            Image imgTmp = new Image(getImageFile());
            imgView.setImage(imgTmp);
            setCharList(charList);
            preferences.putInt("level", level);
            preferences.putInt("required", required);
            try{ preferences.flush();} catch (Exception e1) {}

        }
        do
        {
            if(correct == -1) //when uping the level, always use our new letter.
            {
                pos = level-1;
                correct = 0;
            }
            else
            {
                pos = random.nextInt(level+wrongPoolVector.size());
            }
            if(pos >= level || (correct > required && wrongPoolVector.size() > 0))
            {
                pos = wrongPoolVector.remove(0);
            }
        } while(romanji[pos].length() == 0);

        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos)));
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
        if(level > writingSystem.limit)
        {
            level--;
        }
        setStatusText();
        setCharList(charList);
       
        preferences.putInt("level", level);
        preferences.putInt("required", required);
        try{ preferences.flush();} catch (Exception e1) {}
        
    }
    private void setCharList(Text charList)
	{
    	StringBuffer buffer = new StringBuffer();
    	for(int index = 0; index < level; index++)
    	{
    		if(romanji[index].length() == 0)
    		{
    			continue;
    		}
    		buffer.append("\n");
    		buffer.append(Character.toString((char)(writingSystem.unicodeBase+index)));
    	}
		charList.setText(buffer.toString());
		
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
		    
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Image/Image Directory");
            if(lastImageDir != null)
            {
                
                File lastImageDirFile = new File(lastImageDir);
                if (lastImageDirFile.isDirectory())
                {
                    fileChooser.setInitialDirectory(lastImageDirFile);
                }
                else
                {
                    fileChooser.setInitialDirectory(lastImageDirFile.getParentFile());
                    fileChooser.setInitialFileName(lastImageDir);
                }
            }
            safetyFile = fileChooser.showOpenDialog(stage);
            if(safetyFile == null)
            {
                return KanaVaders.class.getResource("safe.jpg").toString();
            }
        }
        File file = safetyFile.getParentFile();
        if(safetyFile.isDirectory())
        {
            file = safetyFile;
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
	    status.setText(correct+"/"+(required+wrongPoolVector.size())+" Level="+level+"\nPoints = "+totalPoints);
	    status.setLayoutX(scene.widthProperty().intValue()-(status.getBoundsInParent().getWidth()+20));
	    progressBar.setProgress((double)correct/(double)(required+wrongPoolVector.size()));
	}
	
	/**
     * @param args
     */
    public static void main(String[] args)
    {
       
        launch(args);
    }


    
}
