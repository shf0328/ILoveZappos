package me.huafeng.ilovezappos;

/**
 * Created by User on 02/06/2017.
 */

public class BundleData {
    private String query;
    private Result result;
    private ZapposApi myZappoSerice;


    public ZapposApi getMyZappoSerice() {
        return myZappoSerice;
    }

    public void setMyZappoSerice(ZapposApi myZappoSerice) {
        this.myZappoSerice = myZappoSerice;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }




}
