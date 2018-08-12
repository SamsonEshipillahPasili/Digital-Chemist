package com.jeff.chemist.manager;

import animatefx.animation.FadeIn;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@SpringBootApplication
public class ChemistManagerApplication extends Application{
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;
    private Stage primaryStage;
    private static ChemistManagerApplication instance;


	public static void main(String[] args) {
		launch(args);
	}

	@Override
    public void init() throws IOException {
	    // loead the Spring context
	    springContext = SpringApplication.run(ChemistManagerApplication.class);
	    // create the FXML loader to load the log_in.fxml
	    FXMLLoader fxmlLoader = new FXMLLoader(ChemistManagerApplication.class.getResource("fxml/log_in.fxml"));
	    // set the controller factory to be the ConfigurableApplicationContext. Spring will then
        // instantiate the Controller, rather than JavaFX.
	    fxmlLoader.setControllerFactory(springContext::getBean);
	    // load the root node
	    rootNode = fxmlLoader.load();
    }

	@Override
	public void start(Stage primaryStage){
        // save the primary stage
        this.primaryStage = primaryStage;
	    // set the scene of the primary stage and display it.
        this.primaryStage.setScene(new Scene(rootNode));
        // store this instance once the primary stage is available.
        instance = this;
        this.primaryStage.show();
    }

    public Stage getPrimaryStage(){
	    return this.primaryStage;
    }

    // hide the primary stage
    public void hideStage(){

        this.primaryStage.hide();
    }

    // get an instance of this class.
    public static ChemistManagerApplication getInstance(){
        return instance;
    }


    // get an FXML load as long as you specify the resource path
    // RELATIVE to the ChemistManagerApplication
    public FXMLLoader getFxmlLoader(String resourcePath) throws  Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(ChemistManagerApplication.class.getResource(resourcePath));
        fxmlLoader.setControllerFactory(springContext::getBean);
        return fxmlLoader;
    }


}
