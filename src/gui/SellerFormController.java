package gui;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exception.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable{

	private Seller seller;
	
	private SellerService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private Label labelErrorName;
	@FXML
	private TextField txtEmail;
	@FXML
	private Label labelErrorEmail;
	@FXML
	private DatePicker dtBirthDate;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private TextField txtBaseSalary;
	@FXML
	private Label labelErrorBaseSalary;
	
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;
	
	public void setSeller(Seller seller) {
		this.seller = seller;
	}
	
	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	public void subcribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if(seller == null) {
			throw new IllegalStateException("Seller was null");
		}
		if(service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			seller = getFormData();
			service.saveOrUpdate(seller);
			notifyDataChangeListener();
			Utils.currentStage(event).close();
		}
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}
		catch(DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	private void notifyDataChangeListener() {
		for(DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
		
	}

	private Seller getFormData() {
		Seller dep = new Seller();
		
		ValidationException exception = new ValidationException("Validation error!");
		
		dep.setId(Utils.tryParseToInt(txtId.getText()));
		
		if(txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addErrors("name", "Field can't be empty");
		}
		dep.setName(txtName.getText());
		if(exception.getErrors().size() > 0) {
			throw exception;
		}
		return dep;
	}
	
	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	public void updateDataForm() {
		
		if(seller == null) {
			throw new IllegalStateException("seller was null");
		}
		txtId.setText(String.valueOf(seller.getId()));
		txtName.setText(seller.getName());
		txtEmail.setText(seller.getEmail());
		if(seller.getBirthDate() != null) {
			dtBirthDate.setValue(LocalDate.ofInstant(seller.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		txtBaseSalary.setText(String.format("%.2f", seller.getBaseSalary()));
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		initializableNodes();
	}	
	
	public void initializableNodes() {
		
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Utils.formatDatePicker(dtBirthDate, "dd/MM/yyyy");
	}
	
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
			labelErrorName.setText(errors.get("name"));
		}
	}
}
