/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package com.vernetperronllc.jcoz.client.ui;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.vernetperronllc.jcoz.service.JCozClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Main class where the Profiler UI is launched and managed.
 * TODO(david): Add scenes for the various stages of use of the UI (need to
 * make some decisions with matt on this) and put the various scenes into their
 * own classes.
 * @author David
 */
public class JCozClientUI extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        final Text scenetitle = new Text("Profile a process");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        // List of running processes
        final Map<String, VirtualMachineDescriptor> vmDescriptors = JCozClient.getJCozVMList();
        final ListView<String> vmList = new ListView<>();
        List<String> vmNameList = new ArrayList<>(vmDescriptors.keySet());
        ObservableList<String> items = FXCollections.observableList(vmNameList);
        vmList.setItems(items);
        vmList.setPrefWidth(100);
        vmList.setPrefHeight(70);
        grid.add(vmList, 0, 1, 2, 1);
        
        // Scope text element
        final Label packageLabel = new Label("Profiling scope (package):");
        grid.add(packageLabel, 0, 2);
        final TextField packageValue = new TextField();
//        packageValue.setPromptText("Enter the package to profile...");
        grid.add(packageValue, 1, 2);

        Button btn = new Button();
        btn.setText("Profile process");
        btn.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
                String chosenProcess = vmList.getSelectionModel().getSelectedItem();
                VirtualMachineDescriptor vmDesc = vmDescriptors.get(chosenProcess);
                try {
                    JCozClient profiledClient = new JCozClient(vmDesc);
                    // TODO(david): Switch the scene and allow the user to
                    // control the profiling process from the UI.
                    profiledClient.startProfiling();
                } catch (AttachNotSupportedException e) {
                    System.err.println("Unable to attach to VM");
                    System.err.println(e.getMessage());
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                } catch (AgentLoadException e) {
                    System.err.println("Unable to load Agent");
                    System.err.println(e.getMessage());
                } catch (AgentInitializationException e) {
                    System.err.println("Unable to initialize Agent");
                    System.err.println(e.getMessage());
                }
            }
        });
        grid.add(btn, 0, 3);
        
        Scene scene = new Scene(grid, 600, 400);
        
        primaryStage.setTitle("JCoz Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
