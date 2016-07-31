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

import javafx.application.Application;
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
        primaryStage.setTitle("JCoz Client");
        primaryStage.setScene(PickProcessScene.getPickProcessScene(primaryStage));
        primaryStage.show();
    }
    
    @Override
    public void stop() {
		System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
