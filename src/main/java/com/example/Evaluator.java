package com.example;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Evaluator extends Group{
    private VBox organizerVBox = new VBox(10);

    //Having any buttons in the program cause default css to be applied, so the styles.css file is used to overide it
    private HBox buttonHBox = new HBox(15);
    private Button addInputButton = new Button();
    private Button removeInputButton = new Button();
    private Button MenuButton = new Button("Menu");
    private Button playButton = new Button("");
    private Button playTextButton = new Button("play");

    private ArrayList<WireNode> inputs = new ArrayList<WireNode>();
    //private GridPane inputBox = new GridPane();
    private GridPane dataView = new GridPane();

    Evaluator() {
        ImageView addInputView = new ImageView(new Image(getClass().getResourceAsStream("addbutton_no_bg.png")));
        addInputView.setFitHeight(45);
        addInputView.setPreserveRatio(true);
        addInputButton.setGraphic(addInputView);
        addInputButton.setPadding(Insets.EMPTY);

        ImageView removeInputView = new ImageView(new Image(getClass().getResourceAsStream("minusbutton_no_bg.png")));
        removeInputView.setFitHeight(45);
        removeInputView.setPreserveRatio(true);
        removeInputButton.setGraphic(removeInputView);
        removeInputButton.setPadding(Insets.EMPTY);

        ImageView playView = new ImageView(new Image(getClass().getResourceAsStream("startbutton_no_bg.png")));
        playView.setFitHeight(45);
        playView.setPreserveRatio(true);
        playButton.setGraphic(playView);
        playButton.setPadding(Insets.EMPTY);

        setupInputAdder(addInputButton);
        setupInputRemover(removeInputButton);
        setupPlayButton(playButton);
        setupMenuButton(MenuButton);
        buttonHBox.setAlignment(Pos.TOP_RIGHT);
        buttonHBox.setPadding(new Insets(10,10,0,0));

        organizerVBox.setAlignment(Pos.TOP_RIGHT);

        buttonHBox.getChildren().addAll(MenuButton, addInputButton, removeInputButton, playButton);
        organizerVBox.getChildren().addAll(buttonHBox, dataView);
        this.getChildren().add(organizerVBox);

        setupWirePreviewOverEvaluator(this);
    }

  //------------------------------------------------
    Evaluator(int ThisHoldsNothingJustUsedForSelection) {	//for level mode (temporary solution)
      
        setupPlayButton(playTextButton);
        setupMenuButton(MenuButton);
        
        buttonHBox.setAlignment(Pos.TOP_RIGHT);
        buttonHBox.setPadding(new Insets(10,10,0,0));

        organizerVBox.setAlignment(Pos.TOP_RIGHT);

        buttonHBox.getChildren().addAll(MenuButton,playTextButton);		//spawns buttons
        organizerVBox.getChildren().addAll(buttonHBox, dataView);
        this.getChildren().add(organizerVBox);
        
        
        WireNode wirenode = new WireNode(0,0,"input");	//spawns Output
        inputs.add(wirenode);
        reformat();
        
        setupWirePreviewOverEvaluator(this);
    }
    
    Evaluator(Boolean ThisHoldsNothingJustUsedForSelection) {	//used in ClearScreen 
    	
    }
    //------------------------------------------------    
    
    //Button Handling

    private void setupInputAdder(Button self) {
        self.setOnAction(event -> {
            WireNode wirenode = new WireNode(0,0,"input");
            inputs.add(wirenode);
            for(WireNode node : inputs) {
                if(node.getConnectedNode() != null) {
                    node.getConnectedNode().drawWire(node.getAbsoluteX()-40,node.getAbsoluteY()+30); //The position changes after this executes, so the position change must be added (-20)
                }
            }
            reformat();
            
        });
    }

    private void displayInput(WireNode input, int column) {
        VBox inputBox = new VBox(input, new Rectangle(2,30*(column+1),Color.LIGHTGREEN), new Rectangle(30,2,Color.LIGHTGREEN));
        inputBox.setAlignment(Pos.BOTTOM_CENTER);
        GridPane.setMargin(inputBox,new Insets(10,5,0,5));
        dataView.add(inputBox, column, 0);
    }

    private void setupInputRemover(Button self) {
        self.setOnAction(event -> {
            WireNode wirenode = inputs.get(inputs.size()-1);
            dataView.getChildren().remove(wirenode);
            if(wirenode.getConnectedNode() != null) {
                wirenode.getConnectedNode().drawWire(wirenode.getConnectedNode().getAbsoluteX(),wirenode.getConnectedNode().getAbsoluteY());
                wirenode.getConnectedNode().nullConnectedNode();
            }
            inputs.remove(wirenode);

            for(WireNode node : inputs) {
                if(node.getConnectedNode() != null) {
                    node.getConnectedNode().drawWire(node.getAbsoluteX()+40,node.getAbsoluteY()-30);
                }
            }
            reformat();
        });
    }

    private void setupPlayButton(Button self) {
        self.setOnAction(event -> {
            this.evaluate();
            
            if(WorkSpace.IsItLeveMode == true) {	//checks if in level Mode	-mika
            	WorkSpace.LevelValueMatch(LevelMenu.levelSelected, WorkSpace.LevelOutputEval);	//calls level output checker 
            }
        });
    }

    private void setupMenuButton(Button self) {
        self.setOnAction(event -> {
        	
        	//ClearScreen.All();				//clears screen
        	MainMenuSpace.MainMenuScene();	//loads Main Menu
        });
    }
    
    //Other Stuff

    public void reformat() {
        this.organizerVBox.getChildren().remove(this.dataView);
        this.dataView = new GridPane();
        this.organizerVBox.getChildren().add(this.dataView);

        dataView.setGridLinesVisible(true); //debug visuals
        dataView.setAlignment(Pos.TOP_RIGHT);

        int index = 0;
        for(WireNode input : this.inputs) {
            displayInput(input, index);
            index++;
        }
    }

    public void evaluate() {
        App.tape.reset();
        reformat();

        Insets dataSpacing = new Insets(0,5,0,5);
        WorkSpace.LevelOutputEval = "";			//clears LevelOutputEval //used for Level Value Match check in WorkSpace	-Mika

        for(int i = 0; i < App.tape.getLength(); i++) {
            for(int j = 0; j < inputs.size(); j++) {
                Rectangle rect;
                if(inputs.get(j).getConnectedNode() != null) {
            		rect = new Rectangle(30,30, (inputs.get(j).evaluate()) ? Color.GREEN : Color.BLACK); //Column, row
                    rect.setStroke(Color.LIGHTGREEN);
            	} else {
            		rect = new Rectangle(30,30, (Color.RED));
                    rect.setStroke(Color.PINK);
            	}
                GridPane.setMargin(rect, dataSpacing);
                dataView.add(rect, j, i+1); //Column, row, 1 row is reserved for inputs
                
                WorkSpace.LevelOutputEval = WorkSpace.LevelOutputEval + String.valueOf((inputs.get(j).evaluate()) ? 1 : 0); //get Output eval to be check with LevelOutputEval //used for Level Value Match check in WorkSpace -Mika
            }
            App.tape.next();
        }
    }

    private void setupWirePreviewOverEvaluator(Evaluator self) { //Allows wire previews to render over this node
        self.setOnDragOver(event -> { //Target
            if (event.getDragboard().hasString()) {
                ((WireNode) event.getGestureSource()).drawWire(event.getSceneX(),event.getSceneY());
            }
        });
    }
}
