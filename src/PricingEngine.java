/**
 * Created by greg boleslavsky on 1/10/18.
 */
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
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

        public String toString(){
            return this.supply + this.demand;
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

        RawInput(List<String> strProductSupplyDemands, List<String> strSurveyDataItems) {
            this.productSupplyDemands = new ArrayList<>(strProductSupplyDemands.size());
            for (String spsd : strProductSupplyDemands) {
                this.productSupplyDemands.add(new ProductSupplyDemand(spsd));
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
                                    .put(di.price, currentFrequency++);
    }

    public void calculatePriceFrequencies(List<SurveyDataItem> cleanSurveyData){
        for(SurveyDataItem di : cleanSurveyData) {
            recordNewPrice(di);
        }
    }

    public static final SupplyDemand HH = new SupplyDemand(HIGH, HIGH);
    public static final SupplyDemand HL = new SupplyDemand(HIGH, LOW);
    public static final SupplyDemand LH = new SupplyDemand(LOW, HIGH);
    public static final SupplyDemand LL = new SupplyDemand(LOW, LOW);

    Map<SupplyDemand, Double> priceMultiplierBySupplyDemand = new HashMap<>();
    public PricingEngine(){
        /*
        If Supply is High and Demand is High, Product is sold at same price as chosen price.
        If Supply is Low and Demand is Low, Product is sold at 10 % more than chosen price.
        If Supply is Low and Demand is High, Product is sold at 5 % more than chosen price.
        If Supply is High and Demand is Low, Product is sold at 5 % less than chosen price.
         */
        priceMultiplierBySupplyDemand.put(HH, 1.0);
        priceMultiplierBySupplyDemand.put(LL, 1.1);
        priceMultiplierBySupplyDemand.put(LH, 1.05);
        priceMultiplierBySupplyDemand.put(HL, 0.95);
    }

    protected static class priceFrequencyComparator implements Comparator{
        public int compare( Object priceFrequency1, Object priceFrequency2){
            Map.Entry<Double, Integer> pf1 = (Map.Entry<Double, Integer>) priceFrequency1;
            Map.Entry<Double, Integer> pf2 = (Map.Entry<Double, Integer>) priceFrequency1;
            int prelimResult = pf1.getValue() - pf2.getValue();//use frequency first
            if (prelimResult == 0)
                return (int)(pf1.getKey() - pf2.getKey());      //if same frequency, use lowest price
            else
                return prelimResult;
        }
    }


    public static void main(String[] parms){
        PricingEngine pE = new PricingEngine();
        RawInput rawInput = pE.readInput();
        pE.calculatePriceFrequencies(pE.cleanSurveyData(rawInput));
    }


}
