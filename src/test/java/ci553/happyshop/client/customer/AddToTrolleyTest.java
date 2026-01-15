package ci553.happyshop.client.customer;
import ci553.happyshop.catalogue.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class AddToTrolleyTest {

    private CustomerModel customerModel;
    private Product product;

    @BeforeEach //do it before each test
    public void setUp() {
        customerModel = new CustomerModel();//Create a new instance of customer model
        customerModel.cusView = null;//set cusview to null so all the ui doesnt interfere
        product = new Product("0092", "Apple", "test.jpg", 1.50, 2); //add a new test product for tests
        customerModel.theProduct = product; // assign the product to theProduct
    }

    @Test
    public void testAddToTrolley_NewProduct() throws SQLException {//checks it adds a new product
        customerModel.addToTrolley();

        ArrayList<Product> trolley = customerModel.getTrolley();
        assertEquals(1, trolley.size());
        assertEquals("0092", trolley.get(0).getProductId());
        assertEquals(1, trolley.get(0).getOrderedQuantity());
    }

    @Test
    public void testAddToTrolley_ExistingProduct() throws SQLException { //checks if it adds to existing product and sets the order quantity to 2
        customerModel.addToTrolley();
        customerModel.addToTrolley();
        ArrayList<Product> trolley = customerModel.getTrolley();
        assertEquals(1, trolley.size());
        assertEquals(2, trolley.get(0).getOrderedQuantity());
        assertEquals("0092", trolley.get(0).getProductId());
    }
    @Test
    public void testAddToTrolley_ExceedStockCount() throws SQLException { // checks if it goes over stock count you cant add another one
        customerModel.addToTrolley();
        customerModel.addToTrolley();
        ArrayList<Product> trolley = customerModel.getTrolley();
        assertEquals(1, trolley.size());
        assertEquals(2, trolley.get(0).getOrderedQuantity());
        customerModel.addToTrolley();
        trolley = customerModel.getTrolley();
        assertEquals(1, trolley.size());
        assertEquals(2, trolley.get(0).getOrderedQuantity());
    }
    @Test
    public void testAddToTrolley_CancelTrolley() throws SQLException {
        customerModel.addToTrolley();
        ArrayList<Product> trolley = customerModel.getTrolley();
        assertEquals(1, trolley.size());
        assertEquals(1, trolley.get(0).getOrderedQuantity());
        customerModel.cancel();
        trolley = customerModel.getTrolley();
        assertTrue(trolley.isEmpty());
    }
    @Test
    void testUndo_ReducesQuantity() throws SQLException {
        product.setOrderedQuantity(2);
        customerModel.getTrolley().add(product);
        customerModel.setLastAddedProduct("0092");
        customerModel.Undo();
        assertEquals(1, customerModel.getTrolley().size());
        assertEquals(1, customerModel.getTrolley().get(0).getOrderedQuantity());
    }

    @Test
    void testUndo_RemovesProductWhenQuantityZero() throws SQLException {
        product.setOrderedQuantity(1);
        customerModel.getTrolley().add(product);
        customerModel.setLastAddedProduct("0092");
        customerModel.Undo();
        assertTrue(customerModel.getTrolley().isEmpty());
    }
}
