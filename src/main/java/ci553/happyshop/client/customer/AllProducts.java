package ci553.happyshop.client.customer;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.utility.ProductListFormatter;
import ci553.happyshop.utility.UIStyle;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;

public class AllProducts{
    private Stage stage;
    private final int WIDTH = UIStyle.customerWinWidth = 610;
    private final int HEIGHT = UIStyle.customerWinHeight = 300;
    public AllProducts(ArrayList<Product> allProducts) {
        stage = new Stage();
        stage.setTitle("All Products-HappyShop");
        Label Title = new Label("All Products");
        Title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        TextArea Products = new TextArea();
        Products.setPrefHeight(HEIGHT-50);
        Products.setEditable(false);
        String allProductsText = ProductListFormatter.buildAllProductsString(allProducts);
        Products.setText(allProductsText);
        VBox box = new VBox(10, Title, Products);
        box.setStyle("-fx-padding: 15px;");
        box.setPrefSize(WIDTH/2, HEIGHT);
        Scene scene = new Scene(box, 610, 300);
        stage.setScene(scene);
        stage.show();
    }
}
