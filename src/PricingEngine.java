/**
 * Created by greg boleslavsky on 1/10/18.
 */
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.util.stream.Collectors;
import java.io.InputStream;

public final class PricingEngine {

    static final class SurveyDataItem{
        String productCode;
        String competitor;
        double price;

        SurveyDataItem(String productCode, String competitor, String price){
            this.productCode = productCode;
            this.competitor = competitor;
            this.price = Double.parseDouble(price);
        }

        SurveyDataItem(String strSurveyDataItem){
            this(   strSurveyDataItem.split(" ")[0],
                    strSurveyDataItem.split(" ")[1],
                    strSurveyDataItem.split(" ")[2]);
        }


        public String toString(){
            return this.productCode + " " + this.competitor + " " + this.price;
        }
    }

    private static final String HIGH = "H";
    private static final String LOW = "L";

    private static final class SupplyDemand{
        String supply;
        String demand;
        SupplyDemand(String supply, String demand){
            this.supply = supply;
            this.demand = demand;
        }

        public String value(){
            return this.supply + this.demand;
        }

        public String toString(){
            return value();
        }
    }

    static final class ProductSupplyDemand {
        String productCode;
        SupplyDemand supplyDemand;

        ProductSupplyDemand(String productCode, SupplyDemand supplyDemand) {
            this.productCode = productCode;
            this.supplyDemand = supplyDemand;
        }

        ProductSupplyDemand(String strProductSupplyDemand){
            this(   strProductSupplyDemand.split(" ")[0],
                    new SupplyDemand( strProductSupplyDemand.split(" ")[1],
                                      strProductSupplyDemand.split(" ")[2]));
        }

        public String toString(){
            return this.productCode + " " + this.supplyDemand;
        }
    }

    static final class RawInput{
        public List<ProductSupplyDemand> productSupplyDemands;
        public List<SurveyDataItem> dataItems;
        public Map<String, SupplyDemand> supplyDemandsByProduct;

        RawInput(List<String> strProductSupplyDemands, List<String> strSurveyDataItems) {
            this.productSupplyDemands = new ArrayList<>(strProductSupplyDemands.size());
            for (String spsd : strProductSupplyDemands) {
                this.productSupplyDemands.add(new ProductSupplyDemand(spsd));
            }

            this.supplyDemandsByProduct = new HashMap<>();
            for(ProductSupplyDemand psd : this.productSupplyDemands){
                supplyDemandsByProduct.put(psd.productCode, psd.supplyDemand);
            }

            this.dataItems = new ArrayList<>(strSurveyDataItems.size());
            for (String ssdi : strSurveyDataItems) {
                this.dataItems.add(new SurveyDataItem(ssdi));
            }
        }


        public String toString() {
            String dis = "";
            for (SurveyDataItem di : this.dataItems) {
                dis += di.toString() + "\n";
            }
            String psds = "";
            for (ProductSupplyDemand psd : this.productSupplyDemands) {
                psds += psd.toString() + "\n";
            }
            return dis + "\n" + psds;
        }
    }

    public static RawInput readInput(){
        return readInput(System.in);
    }

    public static RawInput readInput(InputStream is){
        Scanner s = new Scanner(is);
        int numProducts = Integer.parseInt(s.nextLine());
        List<String> strProductSupplyDemands = new ArrayList<>();
        for (int i=0; i<numProducts; i++) {
            strProductSupplyDemands.add(s.nextLine());
        }
        int numSurveyDataItems = Integer.parseInt(s.nextLine());
        List<String> strSurveyDataItems = new ArrayList<>();
        for (int i=0; i<numSurveyDataItems; i++) {
            strSurveyDataItems.add(s.nextLine());
        }
        s.close();
        return new RawInput(strProductSupplyDemands, strSurveyDataItems);
    }


    public static final List<SurveyDataItem> cleanSurveyData(RawInput rawInput){
        //Prices less than 50% of average price are treated as promotion and not considered.
        //Prices more than 150% of average price are treated as data errors and not considered.
        List<SurveyDataItem> rawSurveyData = rawInput.dataItems;
        Map<String, List<Double>> pricesByProduct = new HashMap<>();
        for (SurveyDataItem sdi : rawSurveyData){
            if (pricesByProduct.get(sdi.productCode) == null) {
                pricesByProduct.put(sdi.productCode, new ArrayList<>());
            }
            pricesByProduct.get(sdi.productCode).add(sdi.price);
        }

        Map<String, Double> avgPricesByProduct = new HashMap<>();
        for (Map.Entry<String, List<Double>> productPrices : pricesByProduct.entrySet()){
            Double total = 0d;
            for(int i = 0; i< productPrices.getValue().size(); i++) {
                total += productPrices.getValue().get(i);
            }
            avgPricesByProduct.put(productPrices.getKey(), total/productPrices.getValue().size());
        }

        List<SurveyDataItem> cleanedSurveyData = new ArrayList<>();
        //leave only prices that should be considered: 50%AVG <= price <= 150%AVG
        for(SurveyDataItem di : rawSurveyData){
            Double avgPrice = avgPricesByProduct.get(di.productCode);
            if (0.5 * avgPrice  <=  di.price && di.price <= 1.5 * avgPrice){
                cleanedSurveyData.add(di);
            }
        }
        return cleanedSurveyData;
    }

    private Map<String, Map<Double, Integer>> priceFrequencyByProduct = new HashMap<>();
    public void recordNewPrice(SurveyDataItem di){
        if (priceFrequencyByProduct.get(di.productCode) == null) {
            priceFrequencyByProduct.put(di.productCode, new HashMap<>());
        }
        if (priceFrequencyByProduct.get(di.productCode) //Map<String, Integer>
                                            .get(di.price) == null){
            priceFrequencyByProduct.get(di.productCode).put(di.price, 0);
        }
        Integer currentFrequency = (priceFrequencyByProduct.get(di.productCode)
                                                                .get(di.price));
        priceFrequencyByProduct.get(di.productCode)
                                    .put(di.price, currentFrequency + 1);
    }

    public void calculatePriceFrequencies(List<SurveyDataItem> cleanSurveyData){
        for(SurveyDataItem di : cleanSurveyData) {
            recordNewPrice(di);
        }
    }

    public void recordSupplyDemand(RawInput ri){
        for(ProductSupplyDemand psd : ri.productSupplyDemands){
            supplyDemandByProduct.put(psd.productCode, psd.supplyDemand);
        }
    }

    private Map<String, SupplyDemand> supplyDemandByProduct = new HashMap<>();
    public SupplyDemand supplyDemand(String productCode){
        return supplyDemandByProduct.get(productCode);
    }

    public static final SupplyDemand HH = new SupplyDemand(HIGH, HIGH);
    public static final SupplyDemand HL = new SupplyDemand(HIGH, LOW);
    public static final SupplyDemand LH = new SupplyDemand(LOW, HIGH);
    public static final SupplyDemand LL = new SupplyDemand(LOW, LOW);

    Map<String, Double> priceMultiplierBySupplyDemand = new HashMap<>();
    public PricingEngine(){
        /*
        If Supply is High and Demand is High, Product is sold at same price as chosen price.
        If Supply is Low and Demand is Low, Product is sold at 10 % more than chosen price.
        If Supply is Low and Demand is High, Product is sold at 5 % more than chosen price.
        If Supply is High and Demand is Low, Product is sold at 5 % less than chosen price.
         */
        priceMultiplierBySupplyDemand.put(HH.value(), 1.0);
        priceMultiplierBySupplyDemand.put(LL.value(), 1.1);
        priceMultiplierBySupplyDemand.put(LH.value(), 1.05);
        priceMultiplierBySupplyDemand.put(HL.value(), 0.95);
    }

    public static Comparator<Map.Entry<Double, Integer>> priceFrequencyComparator =
        new Comparator<Map.Entry<Double, Integer>>() {
        // recommend most frequently occurring price.
        // If multiple prices occur frequen
            // tly, the least amongst them is chosen

        public int compare(Map.Entry<Double, Integer> pf1, Map.Entry<Double, Integer> pf2) {
            int prelimResult = pf2.getValue() - pf1.getValue(); //use frequency first
            if (prelimResult == 0)
                return (int)(pf1.getKey() - pf2.getKey());      //if same frequency, use lowest price
            else
                return prelimResult;

        }};

    public Double price(String productCode){
        Set<Map.Entry<Double, Integer>> priceFrequenciesMap = priceFrequencyByProduct.get(productCode).entrySet();
        List<Map.Entry<Double, Integer>> priceFrequencies = priceFrequenciesMap.stream().collect(Collectors.toList());
        Collections.sort(priceFrequencies, priceFrequencyComparator);
        Double rawPrice = priceFrequencies.get(0).getKey();
        return rawPrice * (priceMultiplierBySupplyDemand.get(supplyDemandByProduct.get(productCode).value()));
    }

    public boolean quickTest1Passed(){
        List<String> psds = new ArrayList<>();
        psds.add("flashdrive H H");
        psds.add("ssd L H");
        List<String> surveyData = new ArrayList<>();
        /*
        flashdrive X 1.0
        ssd X 10.0
        flashdrive Y 0.9
        flashdrive Z 1.1
        ssd Y 12.5
        */
        surveyData.add("flashdrive X 1.0");
        surveyData.add("flashdrive X 3.0");
        surveyData.add("ssd X 10.0");
        surveyData.add("flashdrive Y 0.9");
        surveyData.add("flashdrive Y 0.9");
        surveyData.add("flashdrive Z 1.1");
        surveyData.add("flashdrive Z 1.1");
        surveyData.add("ssd Y 12.5");
        surveyData.add("ssd Y 2.5");
        RawInput ri = new RawInput(psds, surveyData);
        processInput(ri);

        System.out.println("ssd"         +"\t\t" +price("ssd"));
        System.out.println("flashdrive"  +"\t\t" +price("flashdrive"));
        return price("ssd")==10.5 && price("flashdrive")==0.9;
    }

    public boolean quickTest2Passed(){
        List<String> psds = new ArrayList<>();
        psds.add("mp3player H H");
        psds.add("ssd L L");
        List<String> surveyData = new ArrayList<>();
        /*
        mp3player H H
        ssd L L
        ssd W 11.0
        ssd X 12.0
        mp3player X 60.0
        mp3player Y 20.0
        mp3player Z 50.0
        ssd V 10.0
        ssd Y 11.0
        ssd Z 12.0
         */
        surveyData.add("ssd W 11.0");
        surveyData.add("ssd X 12.0");
        surveyData.add("mp3player X 60.0");
        surveyData.add("mp3player Y 20.0");
        surveyData.add("mp3player Z 50.0");
        surveyData.add("ssd V 10.0");
        surveyData.add("ssd X 11.0");
        surveyData.add("ssd W 12.0");
        RawInput ri = new RawInput(psds, surveyData);
        processInput(ri);

        System.out.println("ssd"        +"\t\t" +price("ssd"));
        System.out.println("mp3player"  +"\t\t" +price("mp3player"));

        return price("ssd")==12.100000000000001 && price("mp3player")==50.0;
    }

    public void processInput(RawInput input){
        List <SurveyDataItem> csd = cleanSurveyData(input);
        calculatePriceFrequencies(csd);
        recordSupplyDemand(input);

    }


    public static void main(String[] parms){
        //command line app that accepts input as specified in the spec
        if (!(new PricingEngine().quickTest1Passed() && new PricingEngine().quickTest2Passed())){
            try {
                throw new Exception("Pricing Engine has bugs");
            } catch (Exception e) {
                System.exit(1);
            }
        }
        PricingEngine pE = new PricingEngine();
        RawInput rawInput = pE.readInput();
        pE.processInput(rawInput);

        //now pE.price("productCode") will return the price

    }


}
