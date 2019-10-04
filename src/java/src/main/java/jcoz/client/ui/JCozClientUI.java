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
