package com.example;

import com.example.App.GateType;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

//controls Gate Cards

public class GateCard extends ImageView{ //LogicGate extends Group
    public GateType type;
    
     GateCard(GateType gateType) {	//the gate Card control
        try {
            this.type = gateType;
            String fileName = "";
            switch(gateType) {
                case OR:
                    fileName = "orcard.png";
                    break;
                case AND:
                    fileName = "andcard.png";
                    break;
                case NOT:
                    fileName = "notcard.png";
                    break;
                case SPLITTER:
                    fileName = "splitter.png";
                    break;
                case NOR:
                    fileName = "norcard.png";
                    break;
                case NAND:
                    fileName = "nandcard.png";
                    break;
                case XOR:
                    fileName = "xorcard.png";
                    break;
                default:
                    fileName = "andcard.png";
                    System.out.println("How did you get here?");
                    break;
            }

            this.setImage(new Image(getClass().getResourceAsStream(fileName)));
            this.setX(0);
            this.setY(0);
            this.setFitHeight(225);	//will fix + standardize latter, this is for testing
            this.setFitWidth(150);
            //setupDrag(this.image, this); //Allows clicking and dragging to translate (change the tranlsation x and y, which apply after other positioning) to the group
            setupClickSpawn(this);	//Allows click to spawn Gates from cards
            setupWirePreviewOverCard(this);	//Works now
            
        } catch(Exception e) {
            System.out.println("Error: Invalid filename for GateCard (or another error in this code block): " + type);
        }
    }

    private void setupWirePreviewOverCard(GateCard self) { //Allows wire previews to render over this node
        self.setOnDragOver(new EventHandler<DragEvent>() { public void handle(DragEvent event) { //Target
            if (event.getDragboard().hasString()) {
                ((WireNode) event.getGestureSource()).drawWire(event.getSceneX(),event.getSceneY());
            }
        }});
    }

     private void setupClickSpawn(GateCard self) {
         this.setOnMousePressed(new EventHandler<MouseEvent>() {@Override public void handle(MouseEvent event) {
             App.SpawnGate(self.type);
         }});
     }
}