package com.jeff.chemist.manager.controllers;

import com.jeff.chemist.manager.dto.DrugDTO;
import com.jeff.chemist.manager.dto.DrugsWrapper;
import com.jeff.chemist.manager.entities.Drug;
import com.jeff.chemist.manager.entities.Sale;
import com.jeff.chemist.manager.repositories.DrugRepository;
import com.jeff.chemist.manager.repositories.SaleRepository;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jeff.chemist.manager.services.ThemeImageService;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class PointOfSaleController implements Initializable, BackgroundImageObserver{
    @FXML
    private JFXTextField itemCodeSearch;

    @FXML
    private TableView<DrugDTO> drugTable, saleTable;

    @FXML
    private TableColumn<DrugDTO, String> codeColumn, nameColumn, priceColumn,
            dosageColumn, selectItemColumn, saleIDColumn, saleNameColumn, salePriceColumn, saleRemoveColumn;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ThemeImageRepository themeImageRepository;

    @FXML
    private ImageView backgroundImageView;

    @Autowired
    private ThemeImageService themeImageService;

    @FXML
    private TextField totalSaleField, cashTenderedField, changeField;

    @FXML
    private Button cancelSaleBtn, completeSaleBtn;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private ReportsController reportsController;

    @Autowired
    private InventoryControlController inventoryControlController;

    // set the cell value factories for the table columns
    private void setCellValueFactories(){
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        selectItemColumn.setCellValueFactory(new PropertyValueFactory<>("dummy"));

        // saleIDColumn, saleNameColumn, salePriceColumn, saleRemoveColumn
        saleIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        saleNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        salePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        saleRemoveColumn.setCellValueFactory(new PropertyValueFactory<>("dummy"));

        // call back for picking a value from the drug table.
        Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>> cellFactory =
                new Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>>(){
                    @Override
                    public TableCell call(final TableColumn<DrugDTO, String> param) {
                        final TableCell<DrugDTO, String> cell = new TableCell<DrugDTO, String>() {

                            final Button btn = new Button("Select");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        DrugDTO drug = getTableView().getItems().get(getIndex());
                                        drugTable.getItems().remove(drug);
                                        saleTable.getItems().add(drug);
                                        updateTotalSales();
                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }

                };


        Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>> removeItemFromSaleCellFactory =
                new Callback<TableColumn<DrugDTO, String>, TableCell<DrugDTO, String>>(){
                    @Override
                    public TableCell call(final TableColumn<DrugDTO, String> param) {
                        final TableCell<DrugDTO, String> cell = new TableCell<DrugDTO, String>() {

                            final Button btn = new Button("Remove");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        DrugDTO drug = getTableView().getItems().get(getIndex());
                                        saleTable.getItems().remove(drug);
                                        drugTable.getItems().add(drug);
                                        updateTotalSales();

                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }

                };

        selectItemColumn.setCellFactory(cellFactory);
        saleRemoveColumn.setCellFactory(removeItemFromSaleCellFactory);
    }

    private  void updateTotalSales(){
        totalSaleField.setText(String.valueOf(this.saleTable.
                getItems()
                .stream()
                .mapToInt(drugDTO -> drugDTO.getPrice())
                .sum()
        ));
    }

    // event handler to fetch items with the specified code
    EventHandler<KeyEvent> searchEventHandler = keyEvent -> {

        // fetch all the drugs from the db
        List<Drug> drugList = this.drugRepository.findAllDrugs(false);
        this.drugTable.setItems(FXCollections.observableArrayList(
                drugList.stream()
                        .map(drug -> new DrugDTO(drug))
                        .filter(drugDTO -> drugDTO.getId().startsWith(itemCodeSearch.getText()))
                        .collect(Collectors.toList())
        ));
    };

    // event handler for cash tendered
    EventHandler<KeyEvent> cashTenderedHandler = keyEvent -> {
        try{
            int cashTendered = Integer.parseInt(cashTenderedField.getText());
            int totalSales = Integer.parseInt(totalSaleField.getText());
            changeField.setText(String.valueOf(cashTendered - totalSales));
        }catch (NumberFormatException nfe){
            // ignore non number values
            changeField.clear();
        }
    };

    // event handler to complete sale
    EventHandler<ActionEvent> completeSaleEventHandler = actionEvent -> {
        if(changeField.getText().isEmpty()){
            // invalid cash was tendered
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Cash!");
            alert.setHeaderText("Invalid cash was tendered.");
            alert.setContentText("Invalid cash was supplied.");
            alert.show();
        }else{
            try{
                if(Integer.parseInt(changeField.getText()) < 0){
                    // insufficient cash tendered.
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Insufficient Cash!");
                    alert.setHeaderText("Insufficient cash was tendered.");
                    alert.setContentText("Insufficient cash was supplied.");
                    alert.show();
                }else{
                    // we good to complete tx.
                    // create a sale
                    Sale sale = new Sale();
                    // set the date of the sale
                    sale.setDate(new Date());
                    // collect the drugs from the 'sale table'
                    List<Drug> drugs = saleTable.getItems().stream().map(drugDTO -> {
                        Drug drug = new Drug();
                        drug.setId(drugDTO.getId());
                        drug.setName(drugDTO.getName());
                        drug.setDosage(drugDTO.getDosage());
                        drug.setPrice(drugDTO.getPrice());
                        drug.setBuyingPrice(drugDTO.getBuyingPrice());
                        return drug;
                    }).collect(Collectors.toList());

                    // mark the sold drugs as 'sold'
                    drugs.forEach(drug -> drug.setSold(true));

                    // update the drug state in the DB
                    this.drugRepository.saveAll(drugs);

                    //sale.setDrugs(drugs);
                    sale.setDrugsWrapper(new DrugsWrapper(drugs));

                    int cashTendered = 0;
                    try{
                        cashTendered = Integer.parseInt(cashTenderedField.getText());
                    }catch (NumberFormatException nfe){
                        // do nothing.
                    }

                    sale.setCashTendered(cashTendered);

                    // store the sale in the repository
                    this.saleRepository.save(sale);

                    // update the sale dates
                    reportsController.initializeDates();

                    // remove the drugs from the UI.
                    saleTable.getItems().clear();
                    cashTenderedField.clear();
                    totalSaleField.clear();
                    changeField.clear();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sale completed");
                    alert.setHeaderText("Sale completed");
                    alert.setContentText("The sale was completed.");
                    alert.show();

                    this.inventoryControlController.refreshDrugTable();

                    // generate the receipt, which is a good old CSV file.
                    this.generateReceipt(sale);
                }
            }catch (NumberFormatException nfe){
                // ignore the nfe
            }
        }
    };

    // receipt generator
    private  void generateReceipt(Sale sale){
        // compute some values for this sale
        int totalSales = sale.getDrugsWrapper().getDrugs().stream().mapToInt(Drug::getPrice).sum();

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm::ss");
        // header
        String header = ",,DIGITAL CHEMIST,,\n\n";
        // date generated
        String date = String.format(",Date Generated, %s,,\n\n", df.format(sale.getDate()));
        String drugHeader = ",,DRUGS,,\nID,NAME,DOSAGE,PRICE\n";
        // drugs
        String drugs = sale.getDrugsWrapper().getDrugs()
                .stream()
                .map(drug -> String.format("%s,%s,%s,%s,\n", drug.getId(), drug.getName(), drug.getDosage(), drug.getPrice()))
                .reduce(String::concat).get().concat("\n\n");

        String salesHeader = ",,SALE SUMMARY,,\n";

        // total sales
        String sales = String.format("Total Sales,,,%d\n", totalSales);
        // cash tendered
        String cashTendered = String.format("Cash Tendered,,,%d\n", sale.getCashTendered());
        // change
        String change = String.format("Change,,,%d\n", (sale.getCashTendered() - totalSales));

        // sale string
        String saleString = Stream.of(header, date, drugHeader, drugs, salesHeader, sales, cashTendered, change)
                .reduce(String::concat).get();

        try {
            String fileName = String.format("Receipt_%d.csv", new Date().getTime());
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(saleString.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // set the background image
    private  void setBackgroundImage(){
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            if(null != imageData && imageData.length != 0){
                this.backgroundImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
            }
        });
    }

    // event handler for the cancel sale button
    EventHandler<ActionEvent> cancelSaleEventHandler = actionEvent -> {
        // return all the items to the drug table
        this.drugTable.getItems().addAll(this.saleTable.getItems());
        this.saleTable.getItems().clear();
        Arrays.asList(this.totalSaleField, cashTenderedField, changeField).forEach(field -> field.clear());
    };

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.backgroundImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // set the cell value factories for the drugTable.
        setCellValueFactories();
        // when the user types a code, load items which start with the type code.
        itemCodeSearch.setOnKeyReleased(searchEventHandler);
        // add this observer to be notified of any background image updates
        this.themeImageService.getBackgroundImageObservers().add(this);
        // add empty values to the sale table
        this.saleTable.setItems(FXCollections.observableArrayList());
        // set the handler for cash tendered
        cashTenderedField.setOnKeyReleased(cashTenderedHandler);
        // set the cancel sale button event handler
        cancelSaleBtn.setOnAction(cancelSaleEventHandler);
        // set the complete sale event handler
        completeSaleBtn.setOnAction(completeSaleEventHandler);
        // set the background image
        setBackgroundImage();
    }
}
