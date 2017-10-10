import java.io.Serializable;
import java.util.Date;

public class StockRecord implements Serializable{
    String name;
    Date date;
    double open, close;

    public StockRecord(String stockName, Date d, double open, double close){
        this.name = stockName;
        this.date = d;
        this.open = open;
        this.close = close;

    }

    @Override
    public String toString(){
        return this.name + " " + this.date.toString() + " " + open + " " + close;
    }
}
