package ci553.happyshop.client;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ci553.happyshop.client.Main;
import org.w3c.dom.Text;


public class SystemsLauncher extends Application{ //initialises all UI
    public TextField textCustomers;
    public TextField textWorkers;
    public TextField textManagers;
    public Button bStart;
    public Label info;
    public Label info2;

    @Override
    public void start(Stage stage){ //Sets the base value for the UI
        textCustomers = new TextField("1");
        textWorkers = new TextField("1");
        textManagers = new TextField("1");
        info = new Label("");
        info2 = new Label("");
        bStart = new Button("Start System");
        bStart.setOnAction(e -> startSystem());//links the button to startSystem
        VBox main = new VBox(10);
        main.getChildren().addAll( //adds all UI in that order in a list
                new Label("Number Of Customer Tills"),
                textCustomers,
                new Label("Number Of Workers Tills"),
                textWorkers,
                new Label("Number Of Managers"),
                textManagers,
                bStart,
                info,
                info2
        );
        main.setStyle("-fx-padding: 20;");
        stage.setScene(new Scene(main, 260, 300)); //creates a new scene with main and all the UI within main
        stage.setTitle("HappyShop Setup Launcher");
        stage.show();
    }
    private void startSystem() {
        //uses regex to check that its only numbers that are inputted and makes sure its not null takes out all wrong cases
        if (textCustomers == null || !textCustomers.getText().matches("\\d+") || textWorkers == null || !textWorkers.getText().matches("\\d+") || textManagers == null || !textManagers.getText().matches("\\d+")){
            info.setText("Enter a valid number");
        }
        else{
            info.setText("");
        }


        int customers = Integer.parseInt(textCustomers.getText());
        int workers = Integer.parseInt(textWorkers.getText());
        int managers = Integer.parseInt(textManagers.getText());
        if (customers > 0 && workers == 0){ //checks if theres any workers available for customers
            info2.setText("No workers");
        }
        if (workers > 0 && customers == 0){//checks if theres customer tills open
            info2.setText("No Customer Tills");
        }
        if((textCustomers != null && textWorkers != null && textManagers != null) && (workers > 0 && managers > 0) && customers != 0){
            //checks if all the text fields are filled out and if managers and workers are more than 0 and that theres not 0 customer tills open
            for (int i = 0; i < customers; i++){ // starts up the customer client for each customer till thats open
                Main.startCustomerClient();
            }
            for (int i = 0; i < workers; i++){//starts up picker and order for each worker thats on - then initializes the order map so the order tracker works
                Main.startPickerClient();
                Main.startOrderTracker();
                Main.initializeOrderMap();
            }
            for (int i = 0; i < managers; i++){//starts up the warehouse for each manager thats on
                Main.startWarehouseClient();
            }
            Main.startEmergencyExit(); // always produces an emergency exit to quit
            Stage stage = (Stage) bStart.getScene().getWindow();//gets the stage that the start buttons on
            stage.close();//closes the stage that the start buttons on so the application can run
        }
    }
}


