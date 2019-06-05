/**
 * Sample Skeleton for 'Ufo.fxml' Controller Class
 */

package it.polito.tdp.ufo;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.ufo.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class UfoController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="boxAnno"
    private ComboBox<Integer> boxAnno; // Value injected by FXMLLoader

    @FXML // fx:id="boxStato"
    private ComboBox<String> boxStato; // Value injected by FXMLLoader

    @FXML // fx:id="txtResult"
    private TextArea txtResult; // Value injected by FXMLLoader
    
    private Model model;

    @FXML
    void handleAnalizza(ActionEvent event) 
    {
    	model.analizza(boxStato.getSelectionModel().getSelectedItem());

    }

    @FXML
    void handleAvvistamenti(ActionEvent event) 
    {
    	boxStato.getItems().addAll(model.creaGrafo(boxAnno.getSelectionModel().getSelectedItem()));
    }

    @FXML
    void handleSequenza(ActionEvent event) 
    {
    	model.calcolaSequenza(boxStato.getSelectionModel().getSelectedItem());
    }
    
    //forse dovrei metter tutto in cmb
    @FXML
    void handleMostraAvvistamentiAnno(ActionEvent event) 
    {
    	if (boxAnno.getSelectionModel().getSelectedIndex() != -1)
    	{
    		int anno = boxAnno.getSelectionModel().getSelectedItem();
    		txtResult.appendText(String.format("Avvenimenti nel %d : %d\n",anno , model.getNumeroAvvistamenti(anno)));
    	}

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert boxAnno != null : "fx:id=\"boxAnno\" was not injected: check your FXML file 'Ufo.fxml'.";
        assert boxStato != null : "fx:id=\"boxStato\" was not injected: check your FXML file 'Ufo.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Ufo.fxml'.";

    }

	public void setModel(Model model)
	{
		this.model = model;
		List<Integer> anniAvvistamenti = model.getAnniAvvistamenti();
		boxAnno.getItems().addAll(anniAvvistamenti);
	}
}
