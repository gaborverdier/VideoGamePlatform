package com;

import javafx.application.Application;
import javafx.stage.Stage;
import com.views.PublisherDashboard;

public class PublisherSimulatorApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        PublisherDashboard dashboard = new PublisherDashboard(primaryStage);
        dashboard.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
