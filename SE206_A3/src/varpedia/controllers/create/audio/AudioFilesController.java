package varpedia.controllers.create.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import varpedia.scene.AppWindow;
import varpedia.controllers.AssociationClass;
import varpedia.processes.BashProcess;
import javafx.scene.control.ListView;

/**
 * Controller for the Audio Files scene,
 * where the user is allowed to merge
 * multiple files together or use a single
 * file for their creation. 
 * 
 * @author Sreeniketh Raghavan
 * 
 */
public class AudioFilesController {

	@FXML
	private ListView<String> existingFiles;
	@FXML
	private ListView<String> filesToMerge;

	@FXML
	private Button playButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button mergeButton;

	@FXML
	private ImageView mergeImage;

	@FXML
	private CheckBox includeBGMusic;

	private ObservableList<String> mergeList = FXCollections.observableArrayList();
	private ObservableList<String> existingList = FXCollections.observableArrayList();


	@FXML
	private void initialize() {

		ObservableList<String> audioFiles = FXCollections.observableArrayList();

		File[] files = new File("./creation_files/temporary_files/audio_files").listFiles();

		// remove audio file extensions (.wav)
		if (files.length != 0) {
			for (File file : files) {
				if (file.isFile()) {
					String name = file.getName();
					name = name.substring(0, name.lastIndexOf("."));
					audioFiles.add(name);
				}
			}
		}

		else {
			// if no audio files exist
			showNoFileAlert();

		}

		// sort alphabetically
		Collections.sort(audioFiles);


		existingFiles.setItems(audioFiles);
		existingList = audioFiles;

		// only one file can be selected at a time 
		existingFiles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		filesToMerge.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		bindPlayAndDeleteButtons();
		bindMergeAddAndRemoveButtons();

	}

	@FXML
	private void deleteFile() {

		String selected = existingFiles.getSelectionModel().getSelectedItem();

		// if nothing is selected from the left list then get the right list selection
		if (selected == null) {
			selected = filesToMerge.getSelectionModel().getSelectedItem();
		}

		final String selectedFile = selected;

		Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
		deleteAlert.setTitle("Deletion Confirmation");
		deleteAlert.setHeaderText("Are you sure that you want to delete the audio file '" + selectedFile + "'?");
		deleteAlert.setContentText("This action CANNOT be undone!");

		deleteAlert.showAndWait().ifPresent(selection -> {

			if(selection == ButtonType.OK) {
				File file = new File("./creation_files/temporary_files/audio_files/"+ selectedFile +".wav");
				file.delete();

				// if the merge list contains the file, remove it from the list after deleting 
				if (mergeList.contains(selectedFile)) {
					mergeList.remove(selectedFile);
					filesToMerge.setItems(mergeList);
				}

				// else remove it from the list of existing files
				else {
					existingList.remove(selectedFile);
					existingFiles.setItems(existingList);
				}

			}
		});


	}


	@FXML
	private void playFile() {

		playButton.disableProperty().unbind();
		deleteButton.disableProperty().unbind();

		playButton.setDisable(true);
		deleteButton.setDisable(true);

		// Get the file which needs to be played
		String selected = existingFiles.getSelectionModel().getSelectedItem();

		// if a file is not chosen from the list of existing files, get the selected file from the list of files to merge
		if (selected == null) {
			selected = filesToMerge.getSelectionModel().getSelectedItem();
		}

		final String selectedFile = selected;

		// play the file on a different thread
		Task<Void> task = new Task<Void>() {
			@Override protected Void call() throws Exception {

				BashProcess playAudio = new BashProcess();
				String command = "aplay \"./creation_files/temporary_files/audio_files/" + selectedFile + ".wav\" 2> /dev/null";
				playAudio.runCommand(command);

				return null;
			}

			@Override protected void done() {

				Platform.runLater(() -> {
					playButton.setDisable(false);
					deleteButton.setDisable(false);

					bindPlayAndDeleteButtons();

				});

			}
		};

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Add a file to the right side view 
	 * for every file the user wishes to
	 * add to the merge list. 
	 */
	@FXML
	private void addToMergeList() {

		String selection = existingFiles.getSelectionModel().getSelectedItem();

		mergeList.add(selection);
		existingList.remove(selection);

		existingFiles.setItems(existingList);
		filesToMerge.setItems(mergeList);

		// if the merge list has more than 1 file then change the 
		// button image and text from 'use selected file' to 'merge files'
		if (mergeList.size() > 1) {
			mergeButton.setText("Merge Audio Files");

			Image image = new Image("/varpedia/images/merge.jpg");
			mergeImage.setImage(image);
		}
	}

	/**
	 * Remove a file from the right side view 
	 * and add it back to the list of 
	 * existing files
	 */
	@FXML
	private void removeFromMergeList() {

		String selection = filesToMerge.getSelectionModel().getSelectedItem();

		if (selection != null) {

			mergeList.remove(selection);
			existingList.add(selection);

			// sort alphabetically
			Collections.sort(existingList);

			existingFiles.setItems(existingList);
			filesToMerge.setItems(mergeList);

			// if the merge list has 1 or no files then change the 
			// button image and text from 'merge files' to 'use selected file'
			if (mergeList.size() <= 1) {
				mergeButton.setText("Use Audio File");

				Image image = new Image("/varpedia/images/audioNote.png");
				mergeImage.setImage(image);
			}

		}

	}

	/**
	 * Method which stores the name of the audio file
	 * if a single file is chosen, else it combines 
	 * multiple audio files and uses the combined
	 * output. It also takes into account if the user
	 * wants background music or not. 
	 */
	@FXML
	private void mergeFiles(ActionEvent e) throws Exception {

		if(includeBGMusic.isSelected()) {
			AssociationClass.getInstance().setBGMusic(true);
		}

		else {
			AssociationClass.getInstance().setBGMusic(false);
		}

		// If a single audio file is selected by the user for their creation
		if (mergeButton.getText().equals("Use Audio File")) {
			String selected = mergeList.get(0);
			AssociationClass.getInstance().storeAudioFile(selected);

			AppWindow.valueOf("SelectImages").setScene(e);
			return;
		}

		else {

			// if multiple files need to be combined
			List<String> filesToMerge = new ArrayList<String>();

			for (String file : mergeList) {
				filesToMerge.add(file);
			}

			AssociationClass.getInstance().storeFilesToMerge(filesToMerge);
			AppWindow.valueOf("MergeName").setScene(e);
			return;
		}
	}

	@FXML
	private void bindPlayAndDeleteButtons() {

		// bind the play and delete buttons to both the lists so that they are 
		// disabled if nothing is selected
		playButton.disableProperty().bind(
				Bindings.isNull(existingFiles.getSelectionModel().selectedItemProperty())
				.and(Bindings.isNull(filesToMerge.getSelectionModel().selectedItemProperty())));

		deleteButton.disableProperty().bind(
				Bindings.isNull(existingFiles.getSelectionModel().selectedItemProperty())
				.and(Bindings.isNull(filesToMerge.getSelectionModel().selectedItemProperty())));
	}

	@FXML
	private void showNoFileAlert() {

		Alert noAudioFiles = new Alert(Alert.AlertType.INFORMATION);
		noAudioFiles.setTitle("No Existing Audio Files");
		noAudioFiles.setHeaderText("There are currently no existing audio files to display.");
		noAudioFiles.setContentText("Kindly go back and create an audio file to view it here. ");
		noAudioFiles.showAndWait();
	}

	@FXML
	private void returnToSelectSentences(ActionEvent e) throws IOException {

		AppWindow.valueOf("SelectSentences").setScene(e);
		return;
	}

	@FXML
	private void bindMergeAddAndRemoveButtons() {

		// disable the merge button if the merge list is empty
		mergeButton.disableProperty().bind(
				Bindings.size(mergeList).isEqualTo(0));

		// disable the add button if nothing is selected in the left list
		addButton.disableProperty().bind(
				Bindings.isNull(existingFiles.getSelectionModel().selectedItemProperty()));

		// disable the remove button if nothing is selected in the right list
		removeButton.disableProperty().bind(
				Bindings.isNull(filesToMerge.getSelectionModel().selectedItemProperty()));
	}

	@FXML
	private void deselectExistingList() {
		existingFiles.getSelectionModel().clearSelection();
	}

	@FXML
	private void deselectMergeList() {
		filesToMerge.getSelectionModel().clearSelection();
	}
}
