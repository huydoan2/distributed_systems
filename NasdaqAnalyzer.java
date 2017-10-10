import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NasdaqAnalyzer {

    public static void main(String[] args){
        SparkConf conf = new SparkConf();
        conf.setAppName("Nasdaq Stock Analyzer");
        conf.setMaster("local");
        String dir = "/home/asad/distributed-systems/NASDAQ100";
        File directory = new File(dir);
        File [] listOfFiles = directory.listFiles();
        List<String> fileNames = new ArrayList<String>();
        for (File f: listOfFiles){
            if (f.isFile() && f.getName().endsWith("csv")) {
                fileNames.add(f.getName());
            }
        }

        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<StockRecord> allRDDRecords = null;
        for (final String fName: fileNames){
            JavaRDD<String> price_data = sc.textFile(dir + "/" + fName).filter(s -> { return !s.contains("Date");});
            JavaRDD<StockRecord> rdd_records = price_data.map(new Function<String, StockRecord>() {
                public StockRecord call(String s) throws Exception {

                    String[] fields = s.split(",");

                    StockRecord record = new StockRecord(fName.replace(".csv", ""),
                            new SimpleDateFormat("yyyy-MM-dd").parse(fields[0]),
                            Double.valueOf(fields[1]),
                            Double.valueOf(fields[4]));

                    return record;

                }
            });
            if (allRDDRecords == null){
                allRDDRecords = rdd_records;
            }
            else {
                allRDDRecords = sc.union(allRDDRecords, rdd_records);
            }
        }

        JavaPairRDD<String, Integer> stockPerformanceRDD = allRDDRecords
                .mapToPair(s -> {

                    if ((100 * ((s.close-s.open)/s.open)) >= 1){
                        return new Tuple2<>(s.name, 1);
                    }
                    else{
                        return new Tuple2<>(s.name, 0);
                    }});
        JavaPairRDD<String, Integer> stockPerformanceReduced = stockPerformanceRDD.reduceByKey((i1, i2) -> {return i1 + i2;});
        List<Tuple2<String, Integer>> output = stockPerformanceReduced.collect();
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

    }
}
