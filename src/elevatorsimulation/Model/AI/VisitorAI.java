package elevatorsimulation.Model.AI;


import elevatorsimulation.Callback.AIRunnable;
import elevatorsimulation.Model.BuildingVisitor;
import elevatorsimulation.Model.ElevatorSimulationGraph;
import elevatorsimulation.Model.Enums.BuildingVisitorEntryPoint;
import elevatorsimulation.Model.Enums.BuildingVisitorState;
import elevatorsimulation.Model.FloorRequest;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Andrew on 2/14/2016.
 */
public class VisitorAI implements AIRunnable, Serializable {

    ArrayList<Timeline> timelines = new ArrayList<>();
    ArrayList<Timeline> waitTimelines = new ArrayList<>();

    private int entranceDelay = 15;

    ElevatorSimulationGraph elevatorSimulationGraph;
    ArrayList<BuildingVisitor> buildingVisitors;
    int numberOfVisitors = 0;

    private static VisitorAI visitorAI = null;


    private VisitorAI() {

        this.elevatorSimulationGraph = ElevatorSimulationGraph.getDefaultGraph();
    }

    public static VisitorAI getAI() {
        if (visitorAI == null) {
            visitorAI = new VisitorAI();
        }

        return visitorAI;
    }

    private void enterBuilding() {

        for (BuildingVisitor buildingVisitor : buildingVisitors) {
            if (buildingVisitor.getBuildingVisitorState() == BuildingVisitorState.OUTSIDE_OF_BUILDING) {
                //startFloorTimer
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(randomNumberGeneration(entranceDelay, 1)), event -> {


                }));

                timeline.setOnFinished(actionEvent -> {
                    buildingVisitor.setBuildingVisitorState(BuildingVisitorState.INSIDE_OF_BUILDING);

                    System.out.println(buildingVisitor.getBuildingVisitorState() + " Floor: " + buildingVisitor.getBuildingVisitorEntryPoint() + " number " + buildingVisitor.getCurrentFloor().getFloorLevel());
                });

                timelines.add(timeline);
            }
        }

        // Iterate through the visitors
        //if the visitor is not on an elevator, visitor must be on floor
    }

    private void playAllTimelines(ArrayList<Timeline> timelines) {
        for (Timeline timeline : timelines) {
            timeline.play();
        }
    }

    private void stopAllTimelines(ArrayList<Timeline> timelines) {
        for (Timeline timeline : timelines) {
            timeline.stop();
        }
    }

    private void waitOnFloor() {

        for(BuildingVisitor visitor : buildingVisitors) {
            waitTimelines.add(new Timeline(new KeyFrame(Duration.seconds(visitor.getAverageTimeOnFloor()), event -> {
                // after we are finished waiting...
                runVisitorLoop();

            })));
        }
        playAllTimelines(waitTimelines);
    }

    //PRIVATE IMPLEMENTATIONS ***********************************************

    private void populateFloorRequests() {
        for (BuildingVisitor buildingVisitor : buildingVisitors) {
            int randomFloor = findRandomFloor();
            if (buildingVisitor.getFloorRequests().isEmpty()) {
                System.out.println("New Visitor Added");
                for (int i = 0; i < randomFloor; i++) {


                    System.out.println("Request: " + i + " out of: " + randomFloor);
                    buildingVisitor.addToRequestQueue();

                }
            }
        }
    }




    private void runVisitorLoop() {

        for(BuildingVisitor visitor : buildingVisitors) {
            if(visitor.isFloorRequestAvailable()) {
             FloorRequest visitorRequest = visitor.getFloorRequests().get(visitor.getFloorRequests().size() -1 );
                visitorRequest.callNextAvailableElevator(findRandomFloor());

                stopAllTimelines(waitTimelines);
                this.waitTimelines.remove(waitTimelines.size() - 1);
                // remove the last time waiting and wait again.

                waitOnFloor();

            } else {
                visitor.addToRequestQueue();
                //ride the elevator back to the entrance of the entrance point
                visitor.getFloorRequests().get(0).callNextAvailableElevator(visitor.getBuildingVisitorEntryPoint().hashCode());

                visitor.exitBuilding();
            }

        }

    }

    public int randomNumberGeneration(int value, int deviation) {
        if (deviation == 0) {
            System.out.println("Division By zero, run visitor entering sequence operation terminated");
            return 0;
        }
        int min = value - value / deviation;
        int max = value + value / deviation;

        return ThreadLocalRandom.current().nextInt((max - min) + 1);
    }

    //SETTERS ***********************************************


    public void setEntranceDelay(int entranceDelay) {
        this.entranceDelay = entranceDelay;
    }

    public void setAmountOfFloorRequests(int amountOfFloorRequests) {

    }


    //GETTERS ***********************************************
    public int getEntranceDelay() {
        return entranceDelay;
    }
    //PUBLIC INTERFACE ***********************************************


    public void addVisitors(ArrayList<BuildingVisitor> buildingVisitors) {
        this.buildingVisitors = buildingVisitors;
    }



    @Override
    public void runAISystems() {
        // Populate floor requests
        populateFloorRequests();
        enterBuilding();
        playAllTimelines(timelines);
        waitOnFloor();
        // have visitors randomly enter the building

        //the visitors will begin making floor requests and move from floor to floor

        // the visitors will stay on a floor for a period of time
        // the visitors will then make another floor request until the 'N'
        //visitors will leave the building when floor requests are empty
        //

    }

    @Override
    public void stopAISystems() {
        stopAllTimelines(timelines);
    }

    public int findRandomFloor() {

        int min = 0;
        int max = elevatorSimulationGraph.getMaxY();

        int randomNum = ThreadLocalRandom.current().nextInt((max - min) + 1);

        return randomNum;

    }


    public void randomAverageTimeInBuilding(int averageTime) {

        for (BuildingVisitor buildingVisitor : buildingVisitors) {
            buildingVisitor.setTimeInBuilding(randomNumberGeneration(averageTime, 4));
        }
    }


    public void randomizeAverageTimeOnFloor(int averageTime, int deviation) {

        for (BuildingVisitor buildingVisitor : buildingVisitors) {
            buildingVisitor.setAverageTimeOnFloor(randomNumberGeneration(averageTime, deviation));
        }
    }

    public void randomizeEntryPoint() {


        for (BuildingVisitor buildingVisitor : buildingVisitors) {
            int randomNum = ThreadLocalRandom.current().nextInt();

            if (randomNum % 2 == 0) {

                buildingVisitor.setBuildingVisitorEntryPoint(BuildingVisitorEntryPoint.GARAGE);
                return;
            }

            buildingVisitor.setBuildingVisitorEntryPoint(BuildingVisitorEntryPoint.LOBBY);
        }
    }

    public void stopVisitorEnteringSquence(int delay, int deviation) {

    }

    public static void runVisitorEnteringSequence(int delay, int deviation) {


    }


}
