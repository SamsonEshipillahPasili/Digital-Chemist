package com.jeff.chemist.manager.controllers;

import com.jeff.chemist.manager.entities.Drug;
import com.jeff.chemist.manager.entities.Sale;
import com.jeff.chemist.manager.repositories.DrugRepository;
import com.jeff.chemist.manager.repositories.SaleRepository;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import com.jeff.chemist.manager.services.BackgroundImageObserver;
import com.jeff.chemist.manager.services.ThemeImageService;
import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ReportsController implements Initializable, BackgroundImageObserver {
    @FXML
    private ComboBox<String> fromDateCBox, toDateCBox;

    @FXML
    private JFXButton generateSalesReportRange, generateInventoryReport;

    @FXML
    private ImageView backgroundImageView;

    @Autowired
    private ThemeImageRepository themeImageRepository;

    @Autowired
    private ThemeImageService themeImageService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private DrugRepository drugRepository;

    private List<Sale> salesData;

    //generateInventoryReport

    EventHandler<ActionEvent> generateSalesReportHandler = actionEvent -> {
        this.generateSalesReport();
    };

    EventHandler<ActionEvent> generateInventroyReportHandler = actionEvent -> {
        this.generateInventoryReport();
    };

    EventHandler<ActionEvent> toCBoxHandler = actionEvent -> {
        // get the date from the from CBOX
        String fromCboxDate = this.fromDateCBox.getSelectionModel().getSelectedItem();
        String toCboxDate = this.toDateCBox.getSelectionModel().getSelectedItem();

        if(fromCboxDate != null && !fromCboxDate.trim().isEmpty()  && !toCboxDate.isEmpty()){
            // get the sales from given the from and to dates.
            // generate the report from the obtained sales
            List<Sale> sales = getSales(fromCboxDate, toCboxDate);
            salesData = sales;
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Date error");
            alert.setHeaderText("Date Error");
            alert.setContentText("Select the 'from'  and 'to' dates!");
            alert.show();
        }
    };


    // generate inventory report
    private void generateInventoryReport(){
        List<Drug> allDrugs = this.drugRepository.findAllDrugs(false);
        if(allDrugs.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Report Error");
            alert.setHeaderText("Report Error");
            alert.setContentText("No report data found!");
            alert.show();
        }else{
            // heading
            String heading = ",,,DIGITAL CHEMIST INVENTORY REPORT\n\n";
            // drugs heading
            String drugsHeading = "CODE,NAME,B.P,S.P,DOSAGE\n";
            // drugs
            String drugsString = allDrugs.stream()
                    .map(drug -> String.format("%s,%s,%d,%d,%s\n", drug.getId(), drug.getName(),
                            drug.getBuyingPrice(), drug.getPrice(), drug.getDosage()))
                    .reduce((a,b) -> a + b).get();
            // total buying price.
            int totalBP = allDrugs.stream()
                    .mapToInt(Drug::getBuyingPrice)
                    .sum();
            String bpString = String.format("\nTotal Buying Price,,,,%d", totalBP);
            // total selling price
            int totalSP = allDrugs.stream()
                    .mapToInt(Drug::getPrice)
                    .sum();
            String spString = String.format("\nTotal Selling Price,,,,%d", totalSP);
            // expected profit
            int expectedProfit = totalSP - totalBP;
            String exProfit = String.format("\nExpected Profits,,,,%d", expectedProfit);

            String reportString = Stream.of(heading,drugsHeading,drugsString, bpString, spString, exProfit).reduce((a, b)-> a + b).get();

            try {
                String reportName = "Inventory Report_" + new Date() + ".csv";
                FileOutputStream fileOutputStream = new FileOutputStream(reportName);
                fileOutputStream.write(reportString.getBytes());
                fileOutputStream.flush();


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Generate Report");
                alert.setHeaderText("Report Generated!");
                alert.setContentText(reportName + " was generated successfully!");
                alert.show();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Report Error");
                alert.setHeaderText("Report Error");
                alert.setContentText("The report could not be generated.");
                alert.show();

                e.printStackTrace();
            }

        }

    }

    // generate report given the date
    private void generateSalesReport(){
        if(this.salesData == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Report Error");
            alert.setHeaderText("Report Error");
            alert.setContentText("No Report Data Found!");
            alert.show();
        }else{
            // heading
            String heading = ",,,DIGITAL CHEMIST SALES REPORT\n\n";
            // sales heading
            String salesHeading = "CODE,NAME,B.P,S.P,DOSAGE,DATE\n";
            // sales
            String saleString = this.salesData.stream()
                    .map(sale -> sale.getDrugsWrapper().getDrugs().stream()
                            .map(drug -> String.format("%s,%s,%d,%d,%s,%s\n",
                                    drug.getId(), drug.getName(), drug.getBuyingPrice(),
                                    drug.getPrice(), drug.getDosage(), sale.getDate().toString())
                            ).reduce(String::concat).get()).reduce(String::concat).get();
            // total buying price
            int totalBuyingPrice = this.salesData.stream().mapToInt(sale -> sale.getDrugsWrapper().getDrugs().stream()
                    .mapToInt(Drug::getBuyingPrice)
                    .sum()
            ).sum();

            String buyingPriceString = String.format("\nTotal Buying Price,,,,,%d", totalBuyingPrice);
            // total selling price
            int totalSellingPrice = this.salesData.stream().mapToInt(sale -> sale.getDrugsWrapper().getDrugs().stream()
                    .mapToInt(Drug::getPrice)
                    .sum()
            ).sum();
            String sellingPriceString = String.format("\nTotal Selling Price,,,,,%d", totalSellingPrice);

            // profit
            int profit = totalSellingPrice - totalBuyingPrice;

            String profitString = String.format("\nTotal Profit,,,,,%d", profit);

            String salesReportString = Stream.of(heading, salesHeading, saleString, buyingPriceString, sellingPriceString, profitString)
                    .reduce(String::concat).get();

            String reportName = "Report_" + new Date().getTime() + ".csv";
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(reportName);
                fileOutputStream.write(salesReportString.getBytes());
                fileOutputStream.flush();


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Generated!");
                alert.setHeaderText("Report Generated!");
                alert.setContentText("The report " + reportName + " has been generated!");
                alert.show();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Report Error");
                alert.setHeaderText("Report Error");
                alert.setContentText("There was an error generating the report.");
                alert.show();

                e.printStackTrace();
            }
        }

    }



    private List<Sale> getSales(String fromDate, String toDate){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        if(fromDate.equals(toDate)){
            // load sales with the current date.
            List<Sale> allSales = new ArrayList<>();
            this.saleRepository.findAll().forEach(allSales::add);
            return allSales.stream()
                    .filter(sale -> df.format(sale.getDate()).startsWith(fromDate))
                    .collect(Collectors.toList());

        }else{
            // get all dates with dates that are between this range
            List<Sale> allSales = new ArrayList<>();
            this.saleRepository.findAll().forEach(allSales::add);
            return allSales.stream()
                    .filter(sale -> {
                        boolean firstCheck = fromDate.equals(df.format(sale.getDate()));
                        boolean secondCheck = toDate.equals(df.format(sale.getDate()));
                        boolean thirdCheck = false;
                        boolean fourthCheck = false;

                        try {
                            thirdCheck = sale.getDate().after(df.parse(fromDate));
                            fourthCheck = sale.getDate().before(df.parse(toDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return Stream.of(firstCheck, secondCheck, thirdCheck, fourthCheck).reduce((a, b) -> a || b).get();
                    })
                    .collect(Collectors.toList());

        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.themeImageService.getBackgroundImageObservers().add(this);
        // initialize fromCBox and toCBox with unique dates
        this.initializeDates();
        this.toDateCBox.setOnAction(this.toCBoxHandler);
        this.generateSalesReportRange.setOnAction(generateSalesReportHandler);
        this.generateInventoryReport.setOnAction(generateInventroyReportHandler);
        setBackgroundImage();
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

    // initialize fromCBox and toCBox with unique dates
    public void initializeDates() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        // get list of sales
        List<Sale> sales = new ArrayList<>();
        this.saleRepository.findAll().forEach(sales::add);

        // get a list of unique dates
        ObservableList<String> uniqueDates = FXCollections.observableArrayList();
        List<String> dateStrings = sales.stream()
                .map(Sale::getDate)
                .map(df::format)
                .distinct()
                .collect(Collectors.toList());
        uniqueDates.addAll(dateStrings);

        this.fromDateCBox.setItems(uniqueDates);
        this.toDateCBox.setItems(uniqueDates);
    }

    @Override
    public void update() {
        this.themeImageRepository.findById("currentImage").ifPresent(image -> {
            byte[] imageData = image.getImageBytes();
            this.backgroundImageView.setImage(new Image(new ByteArrayInputStream(imageData)));
        });
    }
}