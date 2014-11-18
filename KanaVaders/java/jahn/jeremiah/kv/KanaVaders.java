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
    {       
        HIRAGANA(83,12353),
        KATAKANA(89,12449);
        
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
    private static final String[] romanji = new String[]{"A","","I","","U","","E","","O","","KA","GA","KI","GI","KU","GU","KE","GE","KO","GO","SA","ZA","SHI","ZI","SU","ZU","SE","ZE","SO","ZO","TA","DA","CHI","DI","TSU","","DU","TE","DE","TO","DO","NA","NI","NU","NE","NO","HA","BA","PA","HI","BI","PI","FU","BU","PU","HE","BE","PE","HO","BO","PO","MA","MI","MU","ME","MO","YA","YA","YU","YU","YO","YO","RA","RI","RU","RE","RO","WA","WA","WI","WE","WO","N","VU","KA","KE","VA","VI","VE","VO"};
    
    private WritingSystem writingSystem = WritingSystem.HIRAGANA;
    private Text text;
    private int pos = 0;
    private int limit = 1;
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
    @Override
    public void start(Stage stage) {
        try
        {
            this.stage = stage;
            stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
            stage.setFullScreen(true);


            ;
            Rectangle rectangle = new Rectangle(0, 0, (int)Screen.getPrimary().getBounds().getWidth(), (int)Screen.getPrimary().getBounds().getHeight());
            Robot robot = new Robot();
            BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
            screenFile = File.createTempFile("jfx2_screen_capture", ".jpg");
            ImageIO.write(bufferedImage, "jpg", screenFile);


            pos = random.nextInt(limit);
            Group root = new Group();
            Scene scene = new Scene(root, 500, 500, Color.WHEAT);
            preferences = Preferences.userNodeForPackage(KanaVaders.class);
            limit = preferences.getInt("level", limit);
            required = preferences.getInt("required", required);
            lastImageDir = preferences.get("lastImageDir", lastImageDir);
            //textField.set

            text = new Text(Character.toString((char)(writingSystem.unicodeBase+pos)));
            text.setScaleX(3);
            text.setScaleY(3);

            Text charList = new Text("");
            setCharList(charList);
            charList.setLayoutX(475);
            charList.setLayoutY(20);

            Text status = new Text("Status:");
            //status.setScaleX(3);
            //status.setScaleY(3);


            Path path = new Path();
            path.getElements().add(new MoveTo(20,20));

            CubicCurveTo cubicCurveTo1 = new CubicCurveTo(380, 0, 380, 120, 200, 120);
            CubicCurveTo cubicCurveTo2 = new CubicCurveTo(0, 120, 0, 240, 380, 240);
            CubicCurveTo cubicCurveTo3 = new CubicCurveTo(0, 240, 0, 480, 380, 500);
            path.getElements().add(cubicCurveTo1);
            path.getElements().add(cubicCurveTo2);
            path.getElements().add(cubicCurveTo3);
            PathTransition pathTransition = new PathTransition();

            //        scene.heightProperty().addListener(new ChangeListener<Number>()
            //		{
            //
            //			@Override
            //			public void changed(ObservableValue<? extends Number> arg0, Number arg1,Number arg2)
            //			{
            //				
            //			}
            //		});
            pathTransition.setDuration(Duration.millis(14000));
            pathTransition.setPath(path);
            pathTransition.setNode(text);
            //pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            //pathTransition.setCycleCount(Timeline.INDEFINITE);
            //pathTransition.setAutoReverse(true);
            pathTransition.setOnFinished(e -> {if (limit > 1) limit--; totalPoints=0;});



            Image img = new Image(getImageFile());
            ImageView imgView = new ImageView(img);
            imgView.setFitHeight(500);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
            imgView.setCache(true);

            TextField textField = new TextField();
            textField.setLayoutY(450);
            status.setLayoutY(450);
            status.setLayoutX(315);
            textField.setOnKeyTyped(e -> {
                try
                {
                    if(e.getCharacter().equals(" "))
                    {
                        Image imgTmp = new Image(getImageFile());
                        imgView.setImage(imgTmp);
                        e.consume();
                    }
                    else if(e.getCharacter().equals("2"))
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
                    else if(e.getCharacter().equals("1"))
                    {
                       stage.setFullScreen(true);
                       e.consume();
                    }
                    else if(e.getCharacter().equals("-"))
                    {
                        required--;
                        status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                        setCharList(charList);
                        e.consume();
                    }
                    else if(e.getCharacter().equals("="))
                    {
                        required++;
                        status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                        setCharList(charList);
                        e.consume();
                    }
                    else if(e.getCharacter().equals(","))
                    {
                        limit--;
                        status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                        setCharList(charList);
                        e.consume();                
                        preferences.putInt("level", limit);
                        preferences.putInt("required", required);
                        try{ preferences.flush();} catch (Exception e1) {}

                    }
                    else if(e.getCharacter().equals("P"))
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
                    else if(e.getCharacter().equals("."))
                    {
                        limit++;
                        if(limit > writingSystem.limit)
                        {
                            limit--;
                        }
                        status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                        setCharList(charList);
                        e.consume();
                        preferences.putInt("level", limit);
                        preferences.putInt("required", required);
                        try{ preferences.flush();} catch (Exception e1) {}
                    }
                    else if(e.getCharacter().equals("`"))
                    {
                        safe = safe ? false : true;        		
                        pathTransition.pause();
                        Image imgTmp = new Image(getImageFile());
                        imgView.setImage(imgTmp);
                        e.consume();
                        pathTransition.play();
                    }
                    else if((textField.getText()+e.getCharacter()).equalsIgnoreCase(romanji[pos]))
                    {
                        correct++;
                        totalPoints++;
                        if (limit < romanji.length && correct >= required && wrongPoolVector.size() == 0)
                        {
                            correct = -1;
                            limit++;
                            if(limit > writingSystem.limit)
                            {
                                limit--;
                            }
                            Image imgTmp = new Image(getImageFile());
                            imgView.setImage(imgTmp);
                            setCharList(charList);
                            preferences.putInt("level", limit);
                            preferences.putInt("required", required);
                            try{ preferences.flush();} catch (Exception e1) {}

                        }
                        do
                        {
                            if(correct == -1) //when uping the level, always use our new letter.
                            {
                                pos = limit-1;
                                correct = 0;
                            }
                            else
                            {
                                pos = random.nextInt(limit+wrongPoolVector.size());
                            }
                            if(pos >= limit)
                            {
                                pos = wrongPoolVector.remove(0);
                            }
                        } while(romanji[pos].length() == 0);

                        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos)));
                        textField.clear();
                        e.consume();
                        pathTransition.playFromStart();
                        status.setText("Status: cor = "+correct+"/"+required+"/"+wrongPoolVector.size()+" level="+limit+"\n Points = "+totalPoints);
                    }
                    else if (romanji[pos].startsWith((textField.getText()+e.getCharacter()).toUpperCase()))
                    {
                        //do nothing
                    }
                    else //fail
                    {                
                        text.setText(Character.toString((char)(writingSystem.unicodeBase+pos))+" ("+romanji[pos]+")");
                        e.consume();
                        correct--;
                        totalPoints--;
                        status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                        wrongPoolVector.add(pos);
                        required++;
                    }
                } catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
                    );
            root.getChildren().add(imgView);
            root.getChildren().add(text);
            root.getChildren().add(textField);
            root.getChildren().add(charList);
            root.getChildren().add(status);

            scene.heightProperty().addListener((c,n1,n2) -> {
                pathTransition.setDuration(Duration.millis(n2.intValue()*10+10000));
                textField.setLayoutY(n2.intValue()-50);
                status.setLayoutY(n2.intValue()-50);
                cubicCurveTo3.setY(n2.intValue()-100);        	
                imgView.setFitHeight(n2.intValue());
            });
            scene.widthProperty().addListener((c,n1,n2) -> {
                cubicCurveTo1.setX(n2.intValue()-50);
                cubicCurveTo2.setControlX2(n2.intValue()-50);
                cubicCurveTo3.setX(n2.intValue()-100);        	
                status.setLayoutX(n2.intValue()-185);
                charList.setLayoutX(n2.intValue()-25);

            });

            stage.setTitle("Kana-Vaders");

            stage.setScene(scene);
            pathTransition.play();        
            stage.show();
        }
        catch (Exception e2)
        {

            e2.printStackTrace();
        }   
    }
    private void setCharList(Text charList)
	{
    	StringBuffer buffer = new StringBuffer();
    	for(int index = 0; index < limit; index++)
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
	/**
     * @param args
     */
    public static void main(String[] args)
    {
       
        launch(args);
    }


    
}
