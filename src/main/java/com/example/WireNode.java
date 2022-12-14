package com.example;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.geometry.Bounds;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class WireNode extends Circle{

    public static final int APPWIDTH = 1920;
    public static final int APPHEIGHT = 1080; //Not sure why these don't work from App
    public static final int RADIUS = 6;

    private String type; //Either "input" or "output"
    private CubicCurve wire = new CubicCurve();
    private Boolean wireIsVisible = false;
    private WireNode connectedNode = null;
    
    WireNode(double x, double y, String type) {

        this.type = type;

        this.setLayoutX(x);
        this.setLayoutY(y);
        this.setRadius(RADIUS);
        this.setFill(Color.GREEN);

        //wire drawing line
        this.wire.setFill(Color.TRANSPARENT);
        this.wire.setStroke(Color.GREEN);
        //this.wire.setStroke(Color.GRAY);
        this.wire.setStrokeWidth(2);
        this.wire.getStrokeDashArray().addAll(5d, 5d);

        this.makeWirable(this);
        
    }

    public void setupWire() { //Seperate function so it can run after it becomes a child, so the parent's translation can be inherited
        double startX = this.getLayoutX() + this.getParent().getTranslateX();
        double startY = this.getLayoutY() + this.getParent().getTranslateY();
        this.wire.setStartX(startX);
        this.wire.setStartY(startY);
        this.wire.setControlX1(startX);
        this.wire.setControlY1(startY);
        this.wire.setControlX2(startX);
        this.wire.setControlY2(startY);
        this.wire.setEndX(this.getLayoutX() + this.getParent().getTranslateX());
        this.wire.setEndY(this.getLayoutY() + this.getParent().getTranslateY());
    }

    public void drawWire(double x, double y) { //X and Y are the end coords
        if(!wireIsVisible) {
            App.root.getChildren().add(wire);
            wire.toBack();
            wireIsVisible = true;
        }

        double startX = this.getAbsoluteX();
        double startY = this.getAbsoluteY();
        double endX = x;
        double endY = y;

        wire.setStartX(startX);
        wire.setStartY(startY);

        wire.setEndX(endX);
        wire.setEndY(endY);

        //Java handles dividing by zero with no complaints
        //It literally ouputs "Infinity", and diving by Infinity gives zero
        //So the following code does in fact work
        wire.setControlX1(endX + ((startX - endX) / (APPWIDTH / Math.abs(startX - endX)))); 
        wire.setControlY1(startY);
        
        wire.setControlX2(startX - ((startX - endX) / (APPWIDTH / Math.abs(startX - endX))));
        wire.setControlY2(endY);
    }

    public void clearWire() {
        App.root.getChildren().remove(this.wire);
        this.wireIsVisible = false;
        //Remove reference in LogicGate
        this.nullConnectedNode();
    }

    public double getX() {
        return(this.getLayoutX() + this.getParent().getTranslateX());
    }
    public double getY() {
        return(this.getLayoutY() + this.getParent().getTranslateY());
    }

    //This is a fix for the start and end nodes not having the correct position according to the above functions
    public double getAbsoluteX() {
        Bounds bounds = this.localToScene(this.getBoundsInLocal());
        return bounds.getCenterX();
    }
    public double getAbsoluteY() {
        Bounds bounds = this.localToScene(this.getBoundsInLocal());
        return bounds.getCenterY();
    }

    public void setWireEndPosition(double x, double y) {
        this.wire.setEndX(x);
        this.wire.setEndY(y);
    }
    public void setWireStartPosition(double x, double y) {
        this.wire.setStartX(x);
        this.wire.setStartY(y);
    }

    //Each node points to the node it's wired to, these connections run both ways
    public WireNode getConnectedNode() {
        return connectedNode;
    }
    public void setConnectedNode(WireNode node) {
        this.connectedNode = node;
    }
    public void nullConnectedNode() {
        this.connectedNode = null;
    }

    public Boolean evaluate() {
        if(this.type == "input") {
            return this.getConnectedNode().evaluate();
        } else {
            if(this.getParent() instanceof LogicGate) {
                return ((LogicGate)this.getParent()).evaluate();
            } else if(this.getParent().getParent().getParent() instanceof Tape) { //You need to ascend the hierarchy of UI elements to get to Tape. There's probably a better way
                return ((Tape)this.getParent().getParent().getParent()).evaluate(this);
            } else {
                System.out.println("Error: wirenode in invalid object" + this.getParent());
                return true;
            }
        }
    }

    private void makeWirable(WireNode self) {

        self.setOnMouseEntered(event -> {
            self.setFill(Color.LIGHTGREEN);
        });

        self.setOnMouseExited(event -> {
            self.setFill(Color.GREEN);
        });

        self.setOnDragDetected(event -> { //Drag started
            //Communicates whether the initiating node is an input or output node with dragboard
            Dragboard db = self.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type);
            db.setContent(content);
            //Disconnects wire
            if(connectedNode != null) {
                connectedNode.clearWire();
                connectedNode.nullConnectedNode();
            }
            self.clearWire();
            event.consume();
        });

        //This performs the import task of communicating to the drag gestrure that the node can be dragged onto
        self.setOnDragOver(event -> { //Target (This executes on the target node rather than the originating node)
            if (event.getGestureSource() != self && 
                ((WireNode) event.getGestureSource()).getParent() != self.getParent() && 
                event.getDragboard().hasString() &&
                self.connectedNode == null) {

                if(event.getDragboard().getString() == "output" && self.type != "output" || event.getDragboard().getString() != "output" && self.type == "output") { //Check for heterosexuality
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            if (event.getDragboard().hasString()) { //Allows wire preview over node
                ((WireNode) event.getGestureSource()).drawWire(event.getSceneX(),event.getSceneY());
            }

            event.consume();
        });

        //Visual feedback
        self.setOnDragEntered(event -> { //Target
            if (event.getGestureSource() != self && event.getDragboard().hasString()) {
                if(event.getDragboard().getString() == "output" && self.type != "output" || event.getDragboard().getString() != "output" && self.type == "output") {
                    self.setFill(Color.LIGHTGREEN);
                }
            }
            event.consume();
        });

        self.setOnDragExited(event -> { //Target
            self.setFill(Color.GREEN);
            event.consume();
        });

        //Drag completion
        self.setOnDragDropped(event -> { //Target
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                success = true;
                ((WireNode) event.getGestureSource()).setConnectedNode(self);
                self.setConnectedNode((WireNode) event.getGestureSource());
                
                //Refresh wire so it snaps to node
                if(db.getString() == "output") {
                    ((WireNode) event.getGestureSource()).setWireEndPosition(self.getAbsoluteX(), self.getAbsoluteY());
                } else {
                    self.setWireStartPosition(self.getAbsoluteX(), self.getAbsoluteY());
                    self.drawWire(((WireNode) event.getGestureSource()).getAbsoluteX(),((WireNode) event.getGestureSource()).getAbsoluteY());
                }
            }
            event.setDropCompleted(success); //lets the source know whether the string was successfully transferred and used
            event.consume();
        });

        self.setOnDragDone(event -> { //Source
            if (event.getTransferMode() == TransferMode.MOVE) { //Checks if the drop was successful

            } else {
                //Remove wire preview on failure to connect
                if(connectedNode != null) {
                    connectedNode.clearWire();
                    connectedNode.nullConnectedNode();
                }
                self.clearWire();
            }
            if(self.type == "input") {
                App.root.getChildren().remove(self.wire);
                self.wireIsVisible = false;
            }
            event.consume();
        });
    }
}
