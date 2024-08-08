
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DopplerAnimation extends Application {
    DecimalFormat f = new DecimalFormat("#.####");
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 300;
    private static final double SOURCE_A_X = CANVAS_WIDTH -100;
    private static final double SOURCE_A_Y = CANVAS_HEIGHT / 2 + 20;
    private static final double SOURCE_B_Y = CANVAS_HEIGHT / 2 - 20;
    private static final double CLOCK_RADIUS = 20;
    private long clockInterval =  1_000_000_000;
    private double sourceB_velocity = -16; // Adjusted speed for better visualization
    private double sourceA_velocity = 0 ; // Adjusted speed for better visualization
    private double speedOfWave = 20; // Adjusted speed for better visualization

    private int maxNrClicks=5; // we want to detect 10 clicks total

    private double gammaB;
    private double gammaA;

    private double sourceB_X;

    double animationSpeed = 20000000;

    private long startingTime =0;

    private Scenario scenario1;
    private Scenario scenario2;
    private boolean animationStarted;
    static int whichIsEmitter;
    Button runButton;
    Button restartButton;

    VBox root;

    TextField speedFieldA;
    TextField speedFieldB;
    TextField speedOfWaveField;

    TextField animationSpeedField;
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Doppler Effect Simulation");
        runButton = new Button("Start the animation");

        // make drawing smoothe by using a buffer

        restartButton = new Button("Restart");
        restartButton.setOnAction(e ->{
            createScenarios();
        } );

        animationSpeedField = new TextField();
        animationSpeedField.setPromptText("Enter the animation speed");
        animationSpeedField.setText(String.valueOf(animationSpeed));

        animationSpeedField.setOnAction(e -> {
            createScenarios();
        });

        speedFieldA = new TextField();
        speedFieldA.setPromptText("Enter the speed of A");
        speedFieldA.setText(String.valueOf(sourceA_velocity));

        speedFieldA.setOnAction(e -> {
            createScenarios();
        });

        // add a text field to set the speed of B
        speedFieldB = new TextField();
        speedFieldB.setPromptText("Enter the speed of B");
        speedFieldB.setText(String.valueOf(sourceB_velocity));

        speedFieldB.setOnAction(e -> {
            createScenarios();
        });

        // create a text field for the speed of the wave
        speedOfWaveField = new TextField();
        speedOfWaveField.setPromptText("Enter the speed of the wave");
        speedOfWaveField.setText(String.valueOf(speedOfWave));

        speedOfWaveField.setOnAction(e -> {
            createScenarios();
        });


        createScenarios();

        root = new VBox();
        // add the button to the root on the center top
        HBox buttonBox = new HBox();
        // center
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        // hgap
        buttonBox.setSpacing(10);
        buttonBox.getChildren().add(runButton);
        buttonBox.getChildren().add(restartButton);
        // add a label
        buttonBox.getChildren().addAll(new Label("Wave velocity"), speedOfWaveField);
        buttonBox.getChildren().addAll(new Label("Velocity A"), speedFieldA);
        buttonBox.getChildren().addAll(new Label("Velocity B"),speedFieldB);
        buttonBox.getChildren().addAll(new Label("Animation speed"),animationSpeedField);

        root.getChildren().add(buttonBox);
        root.getChildren().add(scenario1.canvas);
        root.getChildren().add(scenario2.canvas);
        // center it
        root.setAlignment(javafx.geometry.Pos.CENTER);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    private void getAnimationSpeed() {
        try {
            // restart the scenario
            animationSpeed = Double.parseDouble(animationSpeedField.getText());
            // it must be positive
            if (animationSpeed <= 0) {
                p("Animation speed must be positive");
                animationSpeed = 0;
                animationSpeedField.setText(String.valueOf(animationSpeed));
            }
            p("New animation speed: " + animationSpeed);
        } catch (NumberFormatException ex) {
            p("Invalid input");
        }
    }

    private void getSpeedA() {
        try {
            // restart the scenario
            sourceA_velocity = Double.parseDouble(speedFieldA.getText());
            // it needs to be less or equal to the speed of the wave
            if (sourceA_velocity >= speedOfWave) {
                p("Speed of A must be less than the speed of the wave");
                sourceA_velocity = speedOfWave;
                speedFieldA.setText(String.valueOf(sourceA_velocity));
            }
            p("New speed of A: " + sourceA_velocity);


        } catch (NumberFormatException ex) {
            p("Invalid input");
        }
    }

    private void getSpeedB() {
        try {
            // restart the scenario
            sourceB_velocity = Double.parseDouble(speedFieldB.getText());
            // it needs to be less or equal to the speed of the wave
            if (sourceB_velocity >= speedOfWave) {
                p("Speed of B must be less than the speed of the wave");
                sourceB_velocity = speedOfWave;
                speedFieldB.setText(String.valueOf(sourceB_velocity));
            }
            p("New speed of B: " + sourceB_velocity);


        } catch (NumberFormatException ex) {
            p("Invalid input");
        }
    }

    private void getSpeedOfWave() {
        try {
            // restart the scenario
            speedOfWave = Double.parseDouble(speedOfWaveField.getText());
            // it must be positive
            if (speedOfWave <= 0) {
                p("Speed of the wave must be positive");
                speedOfWave = 0;
                speedOfWaveField.setText(String.valueOf(speedOfWave));
            }
            p("New speed of the wave: " + speedOfWave);


        } catch (NumberFormatException ex) {
            p("Invalid input");
        }
    }

    private void createScenarios() {
        p("===== Creating scenarios =====");
        startingTime = 0;

        if (animationStarted && scenario1 != null && scenario2 != null) {
            animationStarted=false;
            if (scenario1.animator != null)
                scenario1.animator.stop();
            if (scenario2.animator != null)
                scenario2.animator.stop();
            runButton.setText("Start the animation");
        }
        getSpeedA();
        getSpeedB();
        getSpeedOfWave();
        getAnimationSpeed();

        gammaB = 1.0 / Math.sqrt(1 - Math.pow(sourceB_velocity, 2) / Math.pow(speedOfWave, 2));
        gammaA = 1.0 / Math.sqrt(1 - Math.pow(sourceA_velocity, 2) / Math.pow(speedOfWave, 2));

        double clockIntervalB = clockInterval * gammaB;
        double clockIntervalA = clockInterval * gammaA;

        //  sourceB_X =SOURCE_A_X-sourceB_velocity* clockIntervalB /1_000_000_000*(maxNrClicks/2);
        sourceB_X =SOURCE_A_X;

        boolean AIsEmitter = whichIsEmitter==0;
        int dy = 20;
        Clock clockA = new Clock("A", SOURCE_A_X, SOURCE_A_Y, dy, Color.RED,
                clockIntervalA, sourceA_velocity, maxNrClicks, AIsEmitter);
        Clock clockB = new Clock("B", sourceB_X, SOURCE_B_Y, -dy, Color.BLUE,
                clockIntervalB, sourceB_velocity, maxNrClicks, !AIsEmitter);

        scenario1 = new Scenario(clockA, clockB);

        clockA = new Clock("A", SOURCE_A_X, SOURCE_A_Y, dy, Color.RED,
                clockIntervalA, sourceA_velocity, maxNrClicks, !AIsEmitter);
        clockB = new Clock("B", sourceB_X, SOURCE_B_Y, -dy, Color.BLUE,
                clockIntervalB, sourceB_velocity, maxNrClicks, AIsEmitter);

        scenario2 = new Scenario(clockA, clockB);

        p("sourceB_velocity: " + sourceB_velocity);
        p("sourceA_velocity: " + sourceA_velocity);

        p("gammaA: " + gammaA);
        p("gammaB: " + gammaB);

        p("speedOfWave: " + speedOfWave);

        runButton.setOnAction(e ->{
            // uf the animation is running, stop it
            if (animationStarted) {
                animationStarted=false;
                p("Stopping the animation");
                scenario1.animator.stop();
                scenario2.animator.stop();
                runButton.setText("Start the animation");
                return;
            }
            else {
                animationStarted= true;
                scenario1.animator.start();
                scenario2.animator.start();
                p("Starting the animation");
                // change the text of the button to stop
                runButton.setText("Pause the animation");
            }
        } );

        scenario1.update(0);
        scenario2.update(0);

        // if we had previous scenarios, we need to reset the canvas
        if (root != null && root.getChildren().size()>1) {
            root.getChildren().remove(1);
            root.getChildren().remove(1);
            root.getChildren().add(scenario1.canvas);
            root.getChildren().add(scenario2.canvas);
        }

    }

    private void resetScenarios() {
        // uf the animation is running, stop it

        createScenarios();
    }

    private class Scenario {
        Clock clockA;
        Clock clockB;
        Canvas canvas;
        GraphicsContext gc;

        long animationTime;

        AnimationTimer animator;
        public Scenario(Clock clockA, Clock clockB) {
            this.clockA = clockA;
            this.clockB = clockB;
            canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
            gc = canvas.getGraphicsContext2D();
            // bg is white

            // set white background
            gc.setFill(Color.WHITE);

            animator = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    //    p("drawing "+ now);

                    animationTime+=animationSpeed;
                    update( animationTime);

                    // we stop as soon as either clock has maxNrClicks detected
                    if (done()) {
                        // draw the clocks one more time, then stop
                        update( animationTime);
                        stop();
                        // hide run button

                    }

                }
            };


        }
        public boolean done() {
            boolean done = clockA.nrClicksDetected >= maxNrClicks || clockB.nrClicksDetected >= maxNrClicks;
            // also done if the clock runs out of the canvas on either side, with some margin
            if (!done) {
               if (clockA.x > CANVAS_WIDTH -CLOCK_RADIUS*2 || clockA.x <CLOCK_RADIUS*2) done = true;
            }
            return done;
        }

        public void update(long animationTime) {
            // white background
            gc.setFill(Color.WHITE);
            gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            // fill with white
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            // Draw the time on the top left

            if (startingTime == 0) startingTime = animationTime;
            long globalTimeDiff = (animationTime - startingTime) ; // nano seconds

            clockA.draw(gc,globalTimeDiff);
            clockB.draw(gc,globalTimeDiff);

            if (clockA.emitter) {
                clockA.handleSignals(gc, globalTimeDiff, clockB);
            } else {
                clockB.handleSignals(gc, globalTimeDiff, clockA);
            }

        }

    }
    private class Signal {
        double x, y, radius;
        double nrClicks;
        long emissionTimeNs;
        boolean detected = false;
        public Signal(double x, double y, double radius, double nrClicks, long emissionTimeNs) {
            this.x = x;
            this.y = y;
            this.emissionTimeNs = emissionTimeNs;
            this.radius = radius;
            this.nrClicks = nrClicks;
        }
        public String toString() {
            return "Signal(" + x + ", " + y + ", " + radius + ", "+ (int)nrClicks + ")";
        }
        public void draw(GraphicsContext gc) {
            gc.setFill(Color.GRAY);
            gc.setStroke(Color.GRAY);

            if (detected) {
                gc.setFill(Color.GREEN);
                gc.setStroke(Color.GREEN);
             //   gc.fillText("Detected", x, y);
            }

            gc.strokeOval(x - radius, y - radius, 2 * radius, 2 * radius);
            // also draw the center dot
            gc.fillOval(x - 2, y - 2, 4, 4);
        }
        public void computeRadius(long globalTimeDiff) {
            // the 1_000_000_000 is to convert from nano seconds to seconds
            radius = speedOfWave * (globalTimeDiff - emissionTimeNs) / 1_000_000_000;
        }
        public boolean checkCollision(double targetX, double targetY) {
            double distance = Math.hypot(targetX - x, targetY - y);
            boolean collided= distance <= radius;
            if (collided) {
          //      p("Collision detected at "+ x + ", " + y + " with target at " + targetX + ", " + targetY);
            }
            return collided;
        }

    }



    private class Clock {
        double x, y;
        double nrClicks;

        double nrClicksWhenDetectionStarted;
        double clockInterval;

        String name;
        Color color;
        int nrClicksDetected = 0;
        long firstClickDetectionTime = -1;

        int maxNrClicks;
        long lastEmissionTime;
        int dy;
        List<Signal> signals = new ArrayList<>();

        double clockVelocity;

        boolean emitter;

        double start_x;
        public Clock(String name, double x, double y, int dy, Color color,
                     double clockInterval, double clockVelocity, int maxNrClicks, boolean emitter) {
            this.x = x;
            this.y = y;
            this.dy = dy;
            this.start_x = x;
            this.name = name;
            this.emitter = emitter;
            this.color = color;
            this.clockInterval = clockInterval;
            this.clockVelocity = clockVelocity;
            this.maxNrClicks = maxNrClicks;

        }
        public void checkIfSignalDetected(Signal signal, long globalTimeDiff) {
            if (!signal.detected && signal.checkCollision(x, y)) {
                signal.detected = true;
                signalDetected(globalTimeDiff);
            }
        }
        private void signalDetected(long globalTimeDiff) {
             nrClicksDetected++;

            if (firstClickDetectionTime <0) {
                firstClickDetectionTime = globalTimeDiff;
                nrClicksWhenDetectionStarted =nrClicks;
                // we start the count then
            }
        }
        public void draw(GraphicsContext gc, long globalTimeDiff) {
            gc.setFill(color);
            // compute x position of clock
            x = start_x+clockVelocity * globalTimeDiff / 1_000_000_000;
            gc.fillOval(x - 10, y - 10, CLOCK_RADIUS, CLOCK_RADIUS);
            // Draw the nr of clock ticks on the left side of the screen
            gc.setFill(Color.BLACK);

            nrClicks = ((double)globalTimeDiff  / (double)clockInterval);


            // also add global time diff
         //   gc.fillText("Global time diff: " + f.format(globalTimeDiff), 10, y + dy-20);

            if (emitter) {
                nrClicks  = Math.min(nrClicks, maxNrClicks);
                gc.fillText(name+" clock ticks since emission " + f.format(nrClicks), 10, y+dy);

            }
            else {
                double deltaClicks = 0;
                if (nrClicksWhenDetectionStarted >0) deltaClicks= nrClicks - nrClicksWhenDetectionStarted;
                gc.fillText(name+" clock ticks since detection: " + f.format(deltaClicks), 10, y + dy);
            }

            // also show clock speed
            gc.fillText(name+" clock tick interval: " + clockInterval, 10, y + dy+20);

            // draw the nr of clock ticks detected just below
            String from = name.equals("A") ? "B" : "A";
            if (!emitter) gc.fillText(name+" clock ticks detected (from "+from+"): " + f.format(nrClicksDetected), 10, y + dy+40);

        }

        public  void handleSignals(GraphicsContext gc,  long globalTimeDiff, Clock otherClock) {
            if (lastEmissionTime == 0) lastEmissionTime = globalTimeDiff;

            // we only emit at most maxNrClicks signals

            int signalNr = signals.size()+1;
            if (signalNr<=maxNrClicks &&  globalTimeDiff - lastEmissionTime >= clockInterval || signalNr==1) {
                signals.add(new Signal(x, y, 0, signalNr, globalTimeDiff));
                lastEmissionTime = globalTimeDiff;
            }

            signals.forEach(signal -> {
                signal.computeRadius(globalTimeDiff);
                otherClock.checkIfSignalDetected(signal, globalTimeDiff);
            });

            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            signals.forEach(signal ->  signal.draw(gc));
            signals.removeIf(signal -> signal.radius > CANVAS_WIDTH);
        }
    }


    public static void main(String[] args) {
        // specify which clock, A or B is moving
        whichIsEmitter=1;
        if (args.length>0) {
            whichIsEmitter = Integer.parseInt(args[0]);
            p("whichIsEmitter: "+whichIsEmitter);
        }
        launch(args);
    }
    private static void p(String s) {
        System.out.println(s);
    }
}
