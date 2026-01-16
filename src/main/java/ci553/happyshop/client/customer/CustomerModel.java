package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.
    private String lastAddedProduct;
    public Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley
    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {
        String productId = cusView.tfId.getText().trim(); //gets the product ID, turns it to text and gets rid of whitespaces
        String productName = cusView.tfName.getText().trim(); //gets the product Name, turns it to text and gets rid of whitespaces
        if (!productId.isEmpty() == !productName.isEmpty()) { //compares if both are filled in and if neither are filled in
            theProduct = null;
            displayLaSearchResult = "please enter either a product id OR a product name";
            updateView();
            return;
        }
        if  (!productId.isEmpty()) { //checks if its filled in to search by the product ID if true and sets it to the product
            theProduct = databaseRW.searchByProductId(productId);
            System.out.println("Found by id");
        }
        else{ //else searches by the product name and sets it to the product
            theProduct = databaseRW.searchByProductName(productName);
            System.out.println("Found by name");
        }
        if(theProduct != null && theProduct.getStockQuantity()>=0) { //checks if theres a product to search
            double unitPrice = theProduct.getUnitPrice();
            String description = theProduct.getProductDescription();
            int stock = theProduct.getStockQuantity(); //gets product info
            for (Product p : trolley) {//Goes through each product in the trolley
                if (p.getProductId().equals(theProduct.getProductId())) {//checks if its already in the trolley
                    stock -= p.getOrderedQuantity();//stock deduction only for ui
                    System.out.println("stock updated");
                }
            }

                String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", theProduct.getProductId(), description, unitPrice);
            //needed to get the product id as if searched by name it didnt get the product ID and displayed nothing
                String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
                displayLaSearchResult = baseInfo + quantityInfo;
                System.out.println(displayLaSearchResult);
            }
            else{
                theProduct=null;
                displayLaSearchResult = "No Product was found";
                System.out.println("No Product was found");
            }
        updateView();
    }

    void addToTrolley() throws SQLException {
        if(theProduct!= null){

            // trolley.add(theProduct) — Product is appended to the end of the trolley.
            // To keep the trolley organized, add code here or call a method that:
            //TODO
            // 1. Merges items with the same product ID (combining their quantities).
            // 2. Sorts the products in the trolley by product ID.
            if(theProduct.getStockQuantity() <= 0){ //checks the stock so you cant order when 0 or less
                displayLaSearchResult = "not enough stock";
                if (cusView != null) updateView();
                return;
            }
            for (Product p: trolley){ //checks each product in the trolley
                if (p.getProductId().equals(theProduct.getProductId())){ //checks if the product its checking in the trolley is equal to the product id your adding
                    if (p.getOrderedQuantity() + 1 > theProduct.getStockQuantity()){ //checks if its got enough stock to add it
                        displayLaSearchResult = "Not enough stock left";
                        if (cusView != null) updateView();
                        return;
                    }

                    else {
                        p.setOrderedQuantity(p.getOrderedQuantity() + 1); //adds one to ordered
                        lastAddedProduct = p.getProductId();//makes the variable the last one added for the undo button
                        trolley.sort((a, b) -> a.getProductId().compareTo(b.getProductId()));//sorts the trolley by product ID
                        displayTaTrolley = ProductListFormatter.buildString(trolley);
                        if (cusView != null) search();
                        return;
                    }
                }
            }
            theProduct.setOrderedQuantity(1);//if its not already in the trolley it sets the quantity to 1
            trolley.add(theProduct);//adds it to the trolley
            lastAddedProduct = theProduct.getProductId();//makes the variable the last one added for the undo button
            trolley.sort((a,b)->a.getProductId().compareTo(b.getProductId()));//sorts the list in ID order
            displayTaTrolley = ProductListFormatter.buildString(trolley);
            if (cusView != null) {
                try {
                    search();
                } catch (SQLException e) {
                    updateView();
                }
            }
        }
        else{
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        if (cusView != null) updateView();
    }

    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
            ArrayList<Product> groupedTrolley= groupProductsById(trolley);
            ArrayList<Product> insufficientProducts= databaseRW.purchaseStocks(groupedTrolley);

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley);
                trolley.clear();
                displayTaTrolley ="";
                if (theProduct != null){
                    theProduct = databaseRW.searchByProductId(theProduct.getProductId());
                }
                if (theProduct != null && theProduct.getStockQuantity() == 0){
                    theProduct = null;
                }
                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(displayTaReceipt);
            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
             for (int i = trolley.size() - 1; i>=0; i--){
                 Product trolleyProduct = trolley.get(i);
                 for (Product p : insufficientProducts){
                     if (trolleyProduct.getProductId().equals(p.getProductId())) {
                     trolley.remove(i);
                     break;
                     }
                 }
             }
             theProduct=null;
                //TODO
                // Add the following logic here:
                // 1. Remove products with insufficient stock from the trolley.
                // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)
                RemoveProductNotifier notifier = new RemoveProductNotifier();
                notifier.showRemovalMsg("CheckOut Failed" + errorMsg.toString());
                notifier.closeNotifierWindow();
                System.out.println("CheckOut Failed");
            }
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                Product copy = new Product(
                        p.getProductId(),
                        p.getProductDescription(),
                        p.getProductImageName(),
                        p.getUnitPrice(),
                        p.getStockQuantity()
                );
                copy.setOrderedQuantity(p.getOrderedQuantity());
                grouped.put(id, copy);
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        System.out.println("cancel");
        if (cusView != null) updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }
    void Undo() throws SQLException {
        if (trolley.isEmpty()|| lastAddedProduct == null){//checks if the trolley has anything in to undo
            System.out.println("No products");
            return;
        }
        for (int i = trolley.size() - 1; i>=0; i--){ // gets the size of the trolley and goes through each one going backwards
            Product p = trolley.get(i);//gets the current product
            if (p.getProductId().equals(lastAddedProduct)) {//checks if the current product in trolley is the same as the last one added
                int NewQ = p.getOrderedQuantity() - 1; //sets new quantity to -1 of the current ordered quantity
                System.out.println("changed quantity");
                if (NewQ <= 0){//checks if when its been taken away the ordered quantity has reached 0
                    trolley.remove(i);//it removes the item
                    System.out.println("removed item");
                }
                else{
                    p.setOrderedQuantity(NewQ);//otherwise sets it to the new quantity after the undo
                }
                break;
            }
        }
        displayTaTrolley=ProductListFormatter.buildString(trolley);//rebuilds the trolley to display changes
        if (cusView != null)search();//calls searched for the stock ui changes
    }
    void setLastAddedProduct(String productId) {
        this.lastAddedProduct = productId;
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object
     public ArrayList<Product> getAllProducts(){
         return databaseRW.getAllProducts();
     }
    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
