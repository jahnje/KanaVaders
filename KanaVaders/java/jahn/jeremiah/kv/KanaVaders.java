package jahn.jeremiah.kv;

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
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * Created on Nov 17, 2014
 */

/**
 * @author jeremiah
 * @version $Id: $
 */
public class KanaVaders extends Application
{
    private static final String[] romanji = new String[]{"A","","I","","U","","E","","O","","KA","GA","KI","GI","KU","GU","KE","GE","KO","GO","SA","ZA","SI","ZI","SU","ZU","SE","ZE","SO","ZO","TA","DA","TI","DI","TU","TU","DU","TE","DE","TO","DO","NA","NI","NU","NE","NO","HA","BA","PA","HI","BI","PI","HU","BU","PU","HE","BE","PE","HO","BO","PO","MA","MI","MU","ME","MO","YA","YA","YU","YU","YO","YO","RA","RI","RU","RE","RO","WA","WA","WI","WE","WO","N","VU"};
    private boolean verifyHeight = false;
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
	private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        pos = random.nextInt(limit);
        Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHEAT);
        Preferences preferences = Preferences.userNodeForPackage(KanaVaders.class);
        limit = preferences.getInt("level", limit);
        required = preferences.getInt("required", required);
        
        //textField.set
        
        text = new Text(Character.toString((char)(12353+pos)));
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
        	if(e.getCharacter().equals(" "))
        	{
        		Image imgTmp = new Image(getImageFile());
                imgView.setImage(imgTmp);
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
        		 Image imgTmp = new Image(getImageFile());
                 imgView.setImage(imgTmp);
                e.consume();
        	}
        	else if((textField.getText()+e.getCharacter()).equalsIgnoreCase(romanji[pos]))
            {
                correct++;
                totalPoints++;
                if (limit < romanji.length && correct >= required && wrongPoolVector.size() == 0)
                {
                    correct = -1;
                    limit++;
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

                text.setText(Character.toString((char)(12353+pos)));
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
                text.setText(Character.toString((char)(12353+pos))+" ("+romanji[pos]+")");
                e.consume();
                correct--;
                totalPoints--;
                status.setText("Status: cor = "+correct+"/"+required+" level="+limit+"\n Points = "+totalPoints);
                wrongPoolVector.add(pos);
                required++;
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
    		buffer.append(Character.toString((char)(12353+index)));
    	}
		charList.setText(buffer.toString());
		
	}
	private String getImageFile()
	{
		while(safetyFile == null)
		{
			FileChooser fileChooser = new FileChooser();
			safetyFile = fileChooser.showOpenDialog(stage);
		}
		File file = safetyFile.getParentFile();
		File[] fileList = file.listFiles();
		if(safe == true)
		{
			return "file://"+safetyFile.getAbsolutePath();
		}
		return "file://"+fileList[random.nextInt(fileList.length)].getAbsolutePath();
	}
	/**
     * @param args
     */
    public static void main(String[] args)
    {
        launch(args);
    }


    
}
