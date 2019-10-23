package controllers;

import java.io.IOException;

import FXML.AppWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import objects.Level;

/**
 * This is the controller for the learn menu
 * It displays the current level of the user, a well as shows there
 * current level of access to the games.
 * For this prototype, the user will start with a set number of XP points
 * and level, in order to have enough features unlocked to demonstrate.
 * @author Hazel Williams
 *
 */
public class LearnMenuController {

    @FXML
    private ProgressBar xpProgressBar;

    @FXML
    private Text xpText;

    @FXML
    private Text lvlNumTxt;

    @FXML
    private Button backButton;

    @FXML
    private Button btn1;

    @FXML
    private Button btn2;

    @FXML
    private Button btn3;

    @FXML
    private Button btn4;

    @FXML
    private ImageView lock1;

    @FXML
    private ImageView lock2;

    @FXML
    private ImageView lock3;
    
    @FXML
    private ImageView settingImageView;

    @FXML
    private ImageView lock4;

    @FXML
    private AnchorPane settingsPane;

    
    private ImageView[] locks;
    private Button[] gameBtns;
    
    //should have a "level" object that contains max xp, min xp, features unlocked.
    private Level currLevel;
    private int currXP;
    
    

    @FXML
    void exitSettings(ActionEvent event) {
    	settingsPane.setVisible(false);
    }   
    
    @FXML
    void showSettings(ActionEvent event) {
    	settingsPane.setVisible(true);
    }

    @FXML
    void resetXP(ActionEvent event) {
    	AssociationClass.getInstance().increaseXP(-AssociationClass.getInstance().getXP());
    	AssociationClass.getInstance().saveProgess();
    	
    	updateLevel();
    	settingsPane.setVisible(false);

    }
    
    /*
     * If this is called then the game hasn't been unlocked yet.
     */
    private void lockGame(int index) {
    	locks[index].setVisible(true);
    	gameBtns[index].setDisable(true);
    }
    
    /*
     * If this is called then the game has been unlocked.
     */
    private void unlockGame(int index) {
    	locks[index].setVisible(false);
    	gameBtns[index].setDisable(false);
    }
    
    private void updateLevel() {
    	//find out what level we are at
    	//we will first have to get the current XP
    	//eventually this will be stored in the association class
    	//but for now lets have a dummy value
    	currXP = AssociationClass.getInstance().getXP();
    	AssociationClass.getInstance().saveProgess();
    	//then loop through the level enums, til we get to one that is the current level.
    	for (Level level : Level.values()) {
    		//iterate through each level
    		if(level.isThisLevel(currXP)) {
    			currLevel = level;
    		}
    	}
    	
    	if(currLevel == null) {
    		System.out.println("Error: No level was found. Assigning default Lvl 2.");
    		currLevel = Level.L2;
    	}
    	
    	
    	//now that we have that level, we need to update the text.
    	lvlNumTxt.setText(currLevel.getLevelNumber());
    	xpText.setText(currLevel.getXPText(currXP));
    	
    	//here we need to access the level, to find out whats currently available.
    	//and lock off other elements
    	for(int i =0; i < locks.length; i++) {
    		if(currLevel.isUnlocked(i)) {
    			unlockGame(i);
    		} else {
    			lockGame(i);
    		}
    	}
    	
    	//with the currXP we need to update the progress bar
    	xpProgressBar.setProgress(currLevel.getProgress(currXP));
    }
    
    @FXML
	private void initialize() {
    	locks = new ImageView[] {lock1,lock2,lock3,lock4};
    	gameBtns = new Button[] {btn1,btn2,btn3,btn4};
    	settingsPane.setVisible(false);
    	
    	updateLevel();
    	
	}


    @FXML
    void goToMissingWord(ActionEvent event) throws IOException {
    	//while this game isnt implemented, it will just go to the quiz game
    	AppWindow.valueOf("Quiz").setScene(event);
    }

    @FXML
    void goToQuiz(ActionEvent event) throws IOException {
		AppWindow.valueOf("Quiz").setScene(event);
    }

    @FXML
    void returnToMainMenu(ActionEvent event) throws IOException {
		AppWindow.valueOf("MainMenu").setScene(event);
	}

}