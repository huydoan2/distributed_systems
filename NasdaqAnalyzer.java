import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class NasdaqAnalyzer {

    public static void main(String[] args){
        SparkConf conf = new SparkConf();
        conf.setAppName("Nasdaq Stock Analyzer");
        conf.setMaster("local");
        String dir = System.getProperty("user.dir") + "/NASDAQ100";
        System.out.println(dir);
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

        // Part A *******************************************************
        JavaPairRDD<String, Integer> stockPerformanceRDD = allRDDRecords
                .mapToPair(s -> {

                    if ((100 * ((s.close-s.open)/s.open)) >= 1){
                        return new Tuple2<>(s.name, 1);
                    }
                    else{
                        return new Tuple2<>(s.name, 0);
                    }});
        JavaPairRDD<String, Integer> stockPerformanceReduced = stockPerformanceRDD.reduceByKey((i1, i2) -> {return i1 + i2;});

        List<Tuple2<String, Integer>> output = stockPerformanceReduced.sortByKey().collect();

        try{
            PrintWriter writer = new PrintWriter("output_1.txt", "UTF-8");
            for (Tuple2<?,?> tuple : output) {
                writer.println(tuple._1() + "," + tuple._2());
            }
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        // Part A *******************************************************

        // Part B *******************************************************
        JavaPairRDD<Date, List<Tuple2<String, Double>>> dailyPerformance = allRDDRecords
                .mapToPair(s -> {
                    List<Tuple2<String, Double>> list = new ArrayList<>();
                    list.add(new Tuple2<>(s.name, (100 * ((s.close-s.open)/s.open))));
                    return new Tuple2<>(s.date, list);
                    });
        // Part B *******************************************************

        JavaPairRDD<Date, List<Tuple2<String, Double>>> dailyPerformanceReduced =
                dailyPerformance.reduceByKey((i1, i2) ->
                {
                    i1.addAll(i2);
                    i1.sort(new Comparator<Tuple2<String, Double>>() {
                            @Override
                            public int compare(Tuple2<String, Double> stringDoubleTuple2, Tuple2<String, Double> t1) {
                                if (stringDoubleTuple2._2() > t1._2())
                                    return -1;
                                if (stringDoubleTuple2._2() < t1._2())
                                    return 1;
                                return 0;
                            }});
                    if (i1.size() > 5){
                        return new ArrayList<Tuple2<String, Double>>(i1.subList(0, 5));
                    }
                    return i1;
                });



        JavaPairRDD<Date, List<String>> topPerformersDaily = dailyPerformanceReduced.mapValues(new Function<List<Tuple2<String, Double>>, List<String>>() {
            @Override
            public List<String> call(List<Tuple2<String, Double>> tuple2s) throws Exception {
                ArrayList<String> clean_list = new ArrayList<>();
                for (Tuple2<String, Double> t: tuple2s){
                    clean_list.add(t._1());
                }
                return clean_list;
            }
        });

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            PrintWriter writer = new PrintWriter("output_2.txt", "UTF-8");
            for (Tuple2<Date,List<String>> tuple : topPerformersDaily.sortByKey().collect()) {
                writer.println(sdf.format(tuple._1()) + "," + tuple._2().toString().replace(" ", ""));
            }
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
