/*
 * This file is part of JCoz.
 *
 * JCoz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JCoz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JCoz.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package jcoz.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jcoz.JCozVMDescriptor;
import jcoz.client.cli.LocalProcessWrapper;
import jcoz.client.cli.RemoteServiceWrapper;
import jcoz.client.cli.TargetProcessInterface;
import jcoz.service.JCozException;
import jcoz.service.VirtualMachineConnectionException;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

public class PickProcessScene {

	private static Map<String, VirtualMachineDescriptor> activeLocalJCozVMs;

	private static PickProcessScene ppScene = null;

	private final GridPane grid = new GridPane();

	private final Scene scene;

	/* LOCAL / REMOTE */
	private final ToggleGroup localRemoteGroup = new ToggleGroup();
	private final RadioButton localRadio = new RadioButton("Local Process");
	private final RadioButton remoteRadio = new RadioButton("Remote Process");
	private final TextField remoteHostName = new TextField();
	private final TextField remotePort = new TextField();
	private RemoteServiceWrapper remoteService = null;

	/* PROCESS SPECIFIC */
	private Timeline vmListUpdateTimeline;
	private final ListView<String> vmList = new ListView<>();
	private final TextField klass = new TextField();
	private final TextField scope = new TextField();
	private final TextField lineNumber = new TextField();

	/* BUTTON */
	private Timeline profileButtonEnableTimeline;
	private final Button profileProcessBtn = new Button();

	/** Disable constructor */
	private PickProcessScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setPadding(new Insets(25, 25, 25, 25));

		// TITLE
		final Text scenetitle = new Text("Profile a process");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		this.grid.add(scenetitle, 0, 0, 2, 1);
		int currRow = 1;

		// LOCAL OR REMOTE
		currRow = this.setUpLocalOrRemoteSection(currRow);

		// VM LIST
		currRow = this.setUpVMListSection(currRow);

		// PROFILE CONFIGURATION
		currRow = this.setUpProfileConfigurationSection(currRow);

		// START PROFILING PROCESS
		this.setUpStartProfilingSection(currRow, stage);


		this.scene = new Scene(this.grid, 980, 600);
	}

	private final int setUpLocalOrRemoteSection(int currRow) {
		localRadio.setToggleGroup(this.localRemoteGroup);
		remoteRadio.setToggleGroup(this.localRemoteGroup);
		localRadio.setSelected(true);
		this.grid.add(localRadio, 0, currRow++);
		this.grid.add(remoteRadio, 0, currRow++);
		this.localRemoteGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(
					ObservableValue<? extends Toggle> observable,
					Toggle oldValue,
					Toggle newValue) {
				boolean localSelected = newValue.equals(localRadio);
				remoteHostName.setDisable(localSelected);
				remotePort.setDisable(localSelected);
			}
		});
		final Label hostNameLabel = new Label("Hostname:");
		this.grid.add(hostNameLabel, 0, currRow);
		this.grid.add(this.remoteHostName, 1, currRow);
		this.remoteHostName.setDisable(true);
		currRow++;
		final Label portLabel = new Label("Port:");
		this.grid.add(portLabel, 0, currRow);
		this.grid.add(this.remotePort, 1, currRow);
		this.remotePort.setDisable(true);
		currRow++;
		final Button connectToServiceButton = new Button("Connect to remote host");
		this.grid.add(connectToServiceButton, 0, currRow);
		currRow++;
		connectToServiceButton.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				if (remoteRadio.isSelected()) {
					String host = remoteHostName.getText();
					if (host.length() == 0) return;
					boolean makeNewService = (remoteService == null) ||
							!remoteService.getHost().equals(host);

					if (makeNewService) {
						try {
							remoteService = new RemoteServiceWrapper(host);
							System.out.println("Connected to remote host");
						} catch (JCozException e) {
							// TODO(david): Display error state when a connection isn't made.
							System.err.println("Unable to create connection to remote host " + host);
							e.printStackTrace();
						}
					}
				}
			}
		});

		return currRow;
	}

	/**
	 * Set up the section where the list of available VMs are displayed.
	 * @param currRow The row where the list should be placed.
	 * @return Next available row after setting scene section.
	 */
	private int setUpVMListSection(int currRow) {
		vmListUpdateTimeline = new Timeline(new KeyFrame(
				Duration.millis(1000),
				new EventHandler<ActionEvent>() { 
					@Override
					public void handle(ActionEvent event) {
						if (localRadio.isSelected()) {
							updateLocalVMList();
						} else {
							try {
								updateRemoteVMList(remoteService);
							} catch (JCozException e) {
								System.err.println("Unable to update the remote VM list");
								e.printStackTrace();
							}
						}
					}
				}));
		vmListUpdateTimeline.setCycleCount(Animation.INDEFINITE);

		this.updateLocalVMList();
		this.vmList.setPrefWidth(100);
		this.vmList.setPrefHeight(70);

		this.grid.add(this.vmList, 0, currRow, 5, 1);
		currRow++;

		return currRow;
	}

	/**
	 * Set up the section where the list of jcoz.profile configuration options
	 * are placed.
	 * @param currRow The row where the configuration options should start.
	 * @return Next available row after setting scene section.
	 */
	private int setUpProfileConfigurationSection(int currRow) {
		// Scope text element.
		final Label packageLabel = new Label("Profiling scope (package):");
		this.grid.add(packageLabel, 0, currRow);
		this.grid.add(this.scope, 1, currRow);

		// Class text element.
		final Label classLabel = new Label("Profiling class:");
		this.grid.add(classLabel, 3, currRow);
		this.grid.add(this.klass, 4, currRow);
		currRow++;

		// Progress point element.
		final Label lineNumberLabel = new Label("Line number:");
		this.grid.add(lineNumberLabel, 0, currRow);
		this.lineNumber.setTextFormatter(
				new TextFormatter<>(new NumberStringConverter()));
		this.grid.add(this.lineNumber, 1, currRow);
		currRow++;

		return currRow;
	}

	/**
	 * Set up the section where the start profiling button is displayed. This
	 * also includes the logic for what should get fired when the start profiling
	 * button is fired.
	 * @param currRow The row where the start profiling button should be placed.
	 * @return Next available row after setting scene section.
	 */
	private int setUpStartProfilingSection(int currRow, final Stage stage) {
		// Listener for enabling profile process button
		profileButtonEnableTimeline = new Timeline(new KeyFrame(
				Duration.millis(100),
				new EventHandler<ActionEvent>() { 
					@Override
					public void handle(ActionEvent event) {
						String selectedItem = vmList.getSelectionModel().getSelectedItem();
						boolean hasProcess = (selectedItem != null) && (!selectedItem.equals(""));
						boolean hasClass = (klass.getText() != null) && !klass.getText().equals("");
						boolean hasScope = (scope.getText() != null) && !scope.getText().equals("");
						boolean hasLineNumber = (lineNumber != null) && !lineNumber.equals("");
						profileProcessBtn.setDisable(
								!hasProcess ||
								!hasLineNumber ||
								!hasScope ||
								!hasClass);
					}
				}));
		profileButtonEnableTimeline.setCycleCount(Animation.INDEFINITE);

		this.profileProcessBtn.setText("Profile process");
		this.profileProcessBtn.setDisable(true);
		this.profileProcessBtn.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				String chosenProcess = vmList.getSelectionModel().getSelectedItem();
				try {
					TargetProcessInterface profiledClient;
					if (localRadio.isSelected()) {
						VirtualMachineDescriptor descriptor = activeLocalJCozVMs.get(chosenProcess);
						profiledClient = new LocalProcessWrapper(descriptor);
					} else {
						int remotePid = getPidFromVMStringString(chosenProcess);
						chosenProcess = getProcessNameFromVMString(chosenProcess);
						profiledClient = remoteService.attachToProcess(remotePid);
					}

					setClientParameters(profiledClient);
					vmListUpdateTimeline.stop();
					profileButtonEnableTimeline.stop();
					profiledClient.startProfiling();

					stage.setScene(VisualizeProfileScene.getVisualizeProfileScene(
							profiledClient, stage, chosenProcess));
				} catch (JCozException | VirtualMachineConnectionException e) {
					System.err.println("Unable to connect to target process.");
					e.printStackTrace();
				}
			}
		});
		this.grid.add(this.profileProcessBtn, 0, 10);

		return currRow;
	}

	/**
	 * Get the PID from a string of the form PID: <pid> - Name: <name>.
	 */
	private int getPidFromVMStringString(String vmString) {
		String pidStart = vmString.substring(5, vmString.length());
		Scanner pidScanner = new Scanner(pidStart);

		int pid = pidScanner.nextInt();
		pidScanner.close();

		return pid;
	}

	/**
	 * Get the process name from a string of the form PID: <pid> - Name: <name>.
	 */
	private String getProcessNameFromVMString(String vmString) {
		String[] splitVMStrings = vmString.split(" ");
		for (int i = 0; i < splitVMStrings.length - 1; i++) {
			String curr = splitVMStrings[i];
			if (curr.indexOf("Name:") >= 0) {
				return splitVMStrings[i + 1];
			}
		}
		
		return "";
	}

	private void setClientParameters(TargetProcessInterface profiledClient) throws JCozException {
		String className = klass.getText();
		int lineNo = Integer.parseInt(lineNumber.getText());

		profiledClient.setProgressPoint(className, lineNo);
		profiledClient.setScope(this.scope.getText());
	}

	/**
	 * Update the VM list to display the current list of locally running VMs. 
	 */
	private void updateLocalVMList() {
		final Map<String, VirtualMachineDescriptor> vmDesciptors = 
				PickProcessScene.getLocalJCozVMList();

		List<String> vmNameList = new ArrayList<>(vmDesciptors.keySet());
		ObservableList<String> items = FXCollections.observableList(vmNameList);
		this.vmList.setItems(items);
	}

	/**
	 * Update the VM list from a remote service connection.
	 * @param service
	 * @throws JCozException
	 */
	private void updateRemoteVMList(RemoteServiceWrapper service) throws JCozException {
		// If we haven't made a service connection, empty the list and return.
		if (service == null) {
			this.vmList.setItems(null);
			return;
		}

		final List<JCozVMDescriptor> vmDescriptors = 
				service.listRemoteVirtualMachines();

		List<String> vmNameList = new ArrayList<>();
		// PID: <pid> - Name: <name>
		for (JCozVMDescriptor descriptor : vmDescriptors) {
			vmNameList.add("PID: " + descriptor.getPid() +
					" - Name: " + descriptor.getDisplayName());
		}

		ObservableList<String> items = FXCollections.observableList(vmNameList);
		this.vmList.setItems(items);
	}

	public Scene getScene() {
		return this.scene;
	}

	public VirtualMachineDescriptor getChosenVMDescriptor() {
		String chosenProcess = this.vmList.getSelectionModel().getSelectedItem();

		return PickProcessScene.activeLocalJCozVMs.get(chosenProcess);
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

		ppScene.vmListUpdateTimeline.play();
		ppScene.profileButtonEnableTimeline.play();
		return PickProcessScene.ppScene.getScene();
	}

	/**
	 * Search through the list of running VMs on the localhost
	 * and attach to a JCoz Profiler instance.
	 * @return Map<String, VirtualMachineDescriptor> A list of the VirtualMachines that
	 *      should be queried for being profilable.
	 */
	private static Map<String, VirtualMachineDescriptor> getLocalJCozVMList() {
		PickProcessScene.activeLocalJCozVMs = new HashMap<>();
		List<VirtualMachineDescriptor> vmDescriptions = VirtualMachine.list();
		for(VirtualMachineDescriptor vmDesc : vmDescriptions){
			activeLocalJCozVMs.put(vmDesc.displayName(), vmDesc);
		}

		return activeLocalJCozVMs;
	}
}

