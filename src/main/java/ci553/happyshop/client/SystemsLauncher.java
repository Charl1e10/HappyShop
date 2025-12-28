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


public class SystemsLauncher extends Application{
    public TextField textCustomers;
    public TextField textWorkers;
    public TextField textManagers;
    public Button bStart;
    public Label info;
    public Label info2;

    @Override
    public void start(Stage stage) {
        textCustomers = new TextField("1");
        textWorkers = new TextField("1");
        textManagers = new TextField("1");
        info = new Label("");
        info2 = new Label("");
        bStart = new Button("Start System");
        bStart.setOnAction(e -> startSystem());
        VBox main = new VBox(10);
        main.getChildren().addAll(
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
        stage.setScene(new Scene(main, 260, 300));
        stage.setTitle("HappyShop Options Launcher");
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
        if (customers > 0 && workers == 0){
            info2.setText("No workers");
        }
        if (workers > 0 && customers == 0){
            info2.setText("No Customer Tills");
        }
        if((textCustomers != null && textWorkers != null && textManagers != null) && (workers > 0 && managers > 0) && customers != 0){
            for (int i = 0; i < customers; i++){
                Main.startCustomerClient();
            }
            for (int i = 0; i < workers; i++){
                Main.startPickerClient();
                Main.startOrderTracker();
                Main.initializeOrderMap();
            }
            for (int i = 0; i < managers; i++){
                Main.startWarehouseClient();
            }
            Main.startEmergencyExit();
            Stage stage = (Stage) bStart.getScene().getWindow();
            stage.close();
        }
    }
}


