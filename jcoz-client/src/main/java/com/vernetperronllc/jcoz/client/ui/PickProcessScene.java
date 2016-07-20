package com.vernetperronllc.jcoz.client.ui;

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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

public class PickProcessScene {
    
	private static Map<String, VirtualMachineDescriptor> activeJCozVMs;
	
	private static PickProcessScene ppScene = null;
	
	private final GridPane grid = new GridPane();
	
	private final Scene scene;
	
	private final ListView<String> vmList = new ListView<>();
	
	private final TextField klass = new TextField();
	
	private final TextField scope = new TextField();
	
	private final TextField progressPoint = new TextField();

	private final Button profileProcessBtn = new Button();
	
	/** Disable constructor */
	private PickProcessScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
        this.grid.setVgap(10);
        this.grid.setPadding(new Insets(25, 25, 25, 25));

        final Text scenetitle = new Text("Profile a process");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        this.grid.add(scenetitle, 0, 0, 2, 1);

        this.updateVMList();
        this.vmList.setPrefWidth(100);
        this.vmList.setPrefHeight(70);
        Timer vmListUpdateTimer = new Timer("vmList update");
        TimerTask vmListUpdateTimerTask = new TimerTask() {
        	@Override
        	public void run() {
        		updateVMList();
        	}
        };
        vmListUpdateTimer.schedule(vmListUpdateTimerTask, 0, 2000);
        
        Timer buttonEnableTimer = new Timer("buttonEnable");
        TimerTask buttonUpdateTask = new TimerTask() {
        	@Override
        	public void run() {
        		boolean hasProcess = vmList.getSelectionModel().getSelectedItem() != null;
        		boolean hasClass = (klass.getText() != null) && !klass.getText().equals("");
        		boolean hasScope = (scope.getText() != null) && !scope.getText().equals("");
        		boolean hasProgressPoint = (progressPoint != null) && !progressPoint.equals("");
        		profileProcessBtn.setDisable(
        				!hasProcess ||
        				!hasProgressPoint ||
        				!hasScope ||
        				!hasClass);
        	}
        };
        buttonEnableTimer.schedule(buttonUpdateTask, 0, 100);

        this.grid.add(this.vmList, 0, 1, 5, 1);
        
        // Scope text element.
        final Label packageLabel = new Label("Profiling scope (package):");
        this.grid.add(packageLabel, 0, 2);
        this.grid.add(this.scope, 1, 2);
        
        // Scope text element.
        final Label classLabel = new Label("Profiling class:");
        this.grid.add(classLabel, 3, 2);
        this.grid.add(this.klass, 4, 2);
        
        // Progress point element.
        final Label progressPointLabel = new Label("Progress point:");
        this.grid.add(progressPointLabel, 0, 3);
        this.progressPoint.setTextFormatter(
        		new TextFormatter<>(new NumberStringConverter()));
        this.grid.add(this.progressPoint, 1, 3);

        this.profileProcessBtn.setText("Profile process");
        this.profileProcessBtn.setDisable(true);
        this.profileProcessBtn.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
                String chosenProcess = vmList.getSelectionModel().getSelectedItem();
                VirtualMachineDescriptor vmDesc = 
                		PickProcessScene.activeJCozVMs.get(chosenProcess);
                try {
                    JCozProcessWrapper profiledClient =
                            new JCozProcessWrapper(vmDesc);
                    // TODO(david): Switch the scene and allow the user to
                    // control the profiling process from the UI.
                    profiledClient.startProfiling();
                    
                    stage.setScene(VisualizeProfileScene.getVisualizeProfileScene(
                    		profiledClient,stage));
                } catch (VirtualMachineConnectionException e) {
                    System.err.println("There was an issue with the profiled VM");
                    System.err.println(e.getMessage());
                } catch (JCozException e) {
                    System.err.println("A JCoz exception was thrown.");
                    System.err.println(e);
                }
            }
        });
        this.grid.add(this.profileProcessBtn, 0, 10);
        
        this.scene = new Scene(this.grid, 980, 600);
	}
	
	private void updateVMList() {
        final Map<String, VirtualMachineDescriptor> vmDescriptors =
                PickProcessScene.getJCozVMList();
        List<String> vmNameList = new ArrayList<>(vmDescriptors.keySet());
        ObservableList<String> items = FXCollections.observableList(vmNameList);
        this.vmList.setItems(items);
	}
	
	public Scene getScene() {
		return this.scene;
	}
	
	public VirtualMachineDescriptor getChosenVMDescriptor() {
		String chosenProcess = this.vmList.getSelectionModel().getSelectedItem();
		
		return PickProcessScene.activeJCozVMs.get(chosenProcess);
	}
	
	public void setScope(String scope) {
		this.scope.setText(scope);
	}
	
	public String getScope() {
		return this.scope.getText();
	}
	
	public void setProfiledClass(String klass) {
		this.klass.setText(klass);
	}
	
	public String getProfiledClass() {
		return this.klass.getText();
	}
	
	public static Scene getPickProcessScene(final Stage stage) {
		if (ppScene == null) {
			ppScene = new PickProcessScene(stage);
		}
		
		return PickProcessScene.ppScene.getScene();
	}
	
    /**
     * Search through the list of running VMs on the localhost
     * and attach to a JCoz Profiler instance.
     * @return Map<String, VirtualMachineDescriptor> A list of the VirtualMachines that
     *      should be queried for being profilable.
     */
    private static Map<String, VirtualMachineDescriptor> getJCozVMList() {
        PickProcessScene.activeJCozVMs = new HashMap<>();
        List<VirtualMachineDescriptor> vmDescriptions = VirtualMachine.list();
        for(VirtualMachineDescriptor vmDesc : vmDescriptions){
        	activeJCozVMs.put(vmDesc.displayName(), vmDesc);
        }
        
        return activeJCozVMs;
    }
}

