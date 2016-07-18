package com.vernetperronllc.jcoz.client.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.service.JCozException;
import com.vernetperronllc.jcoz.service.JCozProcessWrapper;
import com.vernetperronllc.jcoz.service.VirtualMachineConnectionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VisualizeProfileScene {
    
	private static VisualizeProfileScene vpScene = null;
	
	private final GridPane grid = new GridPane();
	
	private final Scene scene;

	// Text elements
	private final Text processNameText = new Text();
	
	// Controls
	private final TextField profileOutput = new TextField();
	private final TextField experimentLength = new TextField();
	private final Button viewGraphButton = new Button();
	
	File outputFile = null;
	
	/** Disable constructor */
	private VisualizeProfileScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
        this.grid.setVgap(10);
        this.grid.setPadding(new Insets(25, 25, 25, 25));

        final Text scenetitle = new Text("Profiling process");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        this.grid.add(scenetitle, 0, 0, 2, 1);
        
        /*** Text elements ***/
        final Label processNameLabel = new Label("Process name");
        this.grid.add(processNameLabel, 0, 1);
        this.grid.add(this.processNameText, 1, 1);
        
        /*** Controls ***/
        // Set profile output
        final FileChooser fileChooser = new FileChooser();
        final Button changeOutputButton = new Button("Set profile output file:");
        this.profileOutput.setDisable(true);
        changeOutputButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		fileChooser.setTitle("Profile output");
        		File newFile = fileChooser.showSaveDialog(stage);
        		if (newFile != null) {
        			outputFile = newFile;
        			profileOutput.setText(outputFile.getName());
        		}
        	}
        });
        this.grid.add(changeOutputButton, 0, 4);
        this.grid.add(this.profileOutput, 1, 4);
        
        // Experiment length text element
        final Label lengthLabel = new Label("Experiment length:");
        this.experimentLength.setText("50");
        this.grid.add(lengthLabel, 0, 5);
        this.grid.add(this.experimentLength, 1, 5);

        this.viewGraphButton.setText("View profile output");
        this.viewGraphButton.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
            	System.out.println("Showing results");
            }
        });
        this.grid.add(this.viewGraphButton, 0, 10);
        
        this.scene = new Scene(this.grid, 980, 600);
	}
	
	public Scene getScene() {
		return this.scene;
	}
	
	public void setExperimentLength(int length) {
		this.experimentLength.setText(length + "" );
	}
	
	public int getExperimentLength() {
		return Integer.parseInt(this.experimentLength.getText());
	}
	
	private void setClient(JCozProcessWrapper client) {
		this.processNameText.setText(client.getDescriptor().displayName());
	}
	
	public static Scene getVisualizeProfileScene(JCozProcessWrapper client, Stage stage) {
		if (VisualizeProfileScene.vpScene == null) {
			VisualizeProfileScene.vpScene = new VisualizeProfileScene(stage);
		}
		vpScene.setClient(client);
		return VisualizeProfileScene.vpScene.getScene();
	}
}

