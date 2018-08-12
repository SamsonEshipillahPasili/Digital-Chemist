package com.jeff.chemist.manager.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import com.jeff.chemist.manager.dto.DrugDTO;
import com.jeff.chemist.manager.entities.Drug;
import com.jeff.chemist.manager.repositories.DrugRepository;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jeff.chemist.manager.services.DrugService;
import com.jeff.chemist.manager.services.ThemeImageService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class InventoryControlController implements Initializable, BackgroundImageObserver {
    @FXML
    private JFXTextArea codesTextArea;

    @FXML
    private ImageView inventoryControlImageView;

    @FXML
    private ComboBox<String> nameCBox, cirterionFilterCbox;

    @FXML
    private JFXTextField priceField, nameField, dosageField, searchDrugField, buyingPriceField;

    @FXML
    private JFXButton addItemsBtn;

    @FXML
    private JFXSpinner loadingSpinner;

    @Autowired
    private ThemeImageService themeImageService;

    @Autowired
    private ThemeImageRepository themeImageRepository;

    private ObservableList<String> drugNames = FXCollections.observableArrayList();

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private DrugService drugService;

    @FXML
    private TableView<DrugDTO> productTable;

    @FXML
    private TableColumn<DrugDTO, String> codeColumn, nameColumn, priceColumn, dosageColumn, deleteColumn, buyingPriceColumn;


    // function to set the cell value factories for each of the columns
    private void setCellValueFactories(){
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        buyingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("buyingPrice"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("dummy")); // cell value factory needed to render the column. Will contain button instead.


        Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>> cellFactory =
                new Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>>(){
                    @Override
                    public TableCell call(final TableColumn<DrugDTO, String> param) {
                        final TableCell<DrugDTO, String> cell = new TableCell<DrugDTO, String>() {

                            final Button btn = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        DrugDTO drug = getTableView().getItems().get(getIndex());

                                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                        alert.setTitle("Confirm Deletion");
                                        alert.setHeaderText(String.format("%s of id %s will be deleted!", drug.getName(), drug.getId()));
                                        alert.setContentText("This action is irreversible.");
                                        Optional<ButtonType> buttonType = alert.showAndWait();
                                        buttonType.ifPresent(buttonType1 -> {
                                            if(buttonType1 == ButtonType.OK){
                                                drugRepository.deleteById(drug.getId());
                                                getTableView().getItems().remove(drug);
                                            }
                                        });

                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }

                };
        deleteColumn.setCellFactory(cellFactory);
    }


    EventHandler<ActionEvent> addItemsHandler = event -> {
        if (this.codesTextArea.getText().trim().isEmpty() ||
                this.priceField.getText().trim().isEmpty() ||
                this.dosageField.getText().isEmpty() || this.buyingPriceField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Item Error");
            alert.setHeaderText("Empty fields");
            alert.setContentText("Enter item code(s), price and dosage, and buying price!");
            alert.show();
        } else if (!isNumericPrice()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Item Error");
            alert.setHeaderText("The supplied price(s) is not a number.");
            alert.setContentText("Enter a number for the price(s).");
            alert.show();
        } else {
            this.addItems();
        }
    };

    private boolean isNumericPrice() {
        try {
            Integer.parseInt(this.priceField.getText());
            Integer.parseInt(this.buyingPriceField.getText());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public void hideSpinner() {
        new FadeOut(loadingSpinner).play();
        loadingSpinner.setVisible(false);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Operation Successful");
            alert.setHeaderText("Added!");
            alert.setContentText("The drugs were added successfully!");
            alert.show();
        });
    }

    public void showSpinner() {
        this.loadingSpinner.setVisible(true);
        new FadeIn(loadingSpinner).play();
    }

    private void addItems() {
        String itemName = null;
        // store the selected item
        String selectedItem = this.nameCBox.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isEmpty()) {
            // the user selected an item
            itemName = selectedItem;
            if (!this.nameField.getText().isEmpty()) {
                // the name is also set. Give priority to the namefield
                itemName = this.nameField.getText();
            }
        } else {
            itemName = this.nameField.getText();
        }

        if (null == itemName || itemName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Item Error");
            alert.setHeaderText("The drug name is not set.");
            alert.setContentText("Enter or select the drug name and retry.");
            alert.show();
        } else {
            // we cool. now we add the items using the drug service
            this.drugService.addDrug(this.codesTextArea.getText(), priceField.getText(),
                    itemName, this.dosageField.getText(), this.buyingPriceField.getText());
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // add event handler to the button
        addItemsBtn.setOnAction(addItemsHandler);
        // add this class to the list of image observers
        this.themeImageService.getBackgroundImageObservers().add(this);
        // set the background image
        this.setBackgroundImage();
        // update the list of drug names
        this.updateDrugNames();
        // set the cbox's data
        this.nameCBox.setItems(this.drugNames);
        this.nameCBox.setOnAction(e -> {
            this.nameField.setText(this.nameCBox.getSelectionModel().getSelectedItem());
        });
        ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(Arrays.asList("DISPLAY ALL", "CODE", "NAME", "PRICE", "DOSAGE"));
        this.cirterionFilterCbox.setItems(observableList);
        //set the  cell value factories for each of the columns
        this.setCellValueFactories();
        // populate the drug tables like so
        refreshDrugTable();
        // set the onAction handler for the criterion cbox
        this.cirterionFilterCbox.setOnAction(searchHandler);
        this.searchDrugField.setOnKeyReleased(keyEvent -> {
            searchHandler.handle(null);
        });
    }

    // handler to load drugs of a given crieria
    EventHandler<ActionEvent> searchHandler = actionEvent -> {
        String searchText = searchDrugField.getText().trim();
        if(!searchText.isEmpty()){
            String selectedItem = this.cirterionFilterCbox.getSelectionModel().getSelectedItem();
            if(selectedItem == null || selectedItem.isEmpty()){
                // default to the "name" criterion.
                this.productTable.setItems(searchDrugs(searchText, "NAME"));
            }else{
                // get the criterion
                this.productTable.setItems(searchDrugs(searchText, selectedItem));
            }
        }else{
            this.productTable.setItems(searchDrugs("", "DISPLAY ALL"));
        }

    };


    // method to filter the search as per the criterion
    private ObservableList<DrugDTO> searchDrugs(String searchText, String criterion){
        // first load all the drugs
        List<Drug> drugs = this.drugRepository.findAllDrugs(false);
        if(criterion.equalsIgnoreCase("DISPLAY ALL")){
            // return all the drugs, since the user wishes all of them to be displayed.
            return FXCollections.observableArrayList(
                    drugs.stream()
                            .map(DrugDTO::new)
                            .collect(Collectors.toList())
            );
        }else{
            switch (criterion){
                case "CODE":{
                    return FXCollections.observableArrayList(
                            drugs.stream()
                                    .filter(drug -> drug.getId().startsWith(searchText))
                                    .map(DrugDTO::new)
                                    .collect(Collectors.toList())
                    );
                }
                case "NAME":{
                    return FXCollections.observableArrayList(
                            drugs.stream()
                                    .filter(drug -> drug.getName().toLowerCase().startsWith(searchText.toLowerCase()))
                                    .map(DrugDTO::new)
                                    .collect(Collectors.toList())
                    );
                }
                case "DOSAGE":{
                    return FXCollections.observableArrayList(
                            drugs.stream()
                                    .filter(drug -> drug.getDosage().toLowerCase().contains(searchText.toLowerCase()))
                                    .map(DrugDTO::new)
                                    .collect(Collectors.toList())
                    );
                }
                case "PRICE":{
                    return FXCollections.observableArrayList(
                            drugs.stream()
                                    .filter(drug -> String.valueOf(drug.getPrice()).startsWith(searchText))
                                    .map(DrugDTO::new)
                                    .collect(Collectors.toList())
                    );
                }

            }
        }
        return FXCollections.observableArrayList();
    }

    // load all the drugs from the database and update the drug table
    public void refreshDrugTable(){
        List<Drug> allDrugs = this.drugRepository.findAllDrugs(false);
        List<DrugDTO> drugDTOS = allDrugs
                .stream()
                .map(DrugDTO::new)
                .collect(Collectors.toList());
        ObservableList<DrugDTO> tableData = FXCollections.observableArrayList();
        tableData.addAll(drugDTOS);
        this.productTable.setItems(tableData);
    }


    // set the background image
    private void setBackgroundImage() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if (null != imageData && imageData.length != 0) {
                this.inventoryControlImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
            }
        });
    }

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.inventoryControlImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
    }

    public void updateDrugNames() {
        this.drugRepository.findAllDrugs(false)
                .stream()
                .map(drug -> drug.getName().trim())
                .forEach(drugName -> {
                    if (!this.drugNames.contains(drugName)) {
                        this.drugNames.add(drugName);
                    }
                });
    }

    public void clearFields() {
        Platform.runLater(()->{
            this.codesTextArea.clear();
            this.nameCBox.getSelectionModel().clearSelection();
            this.nameField.clear();
            this.priceField.clear();
            this.dosageField.clear();
        });
    }


}
