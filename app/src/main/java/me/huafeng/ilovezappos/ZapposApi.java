package me.huafeng.ilovezappos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by User on 02/05/2017.
 */

public interface ZapposApi {
    @GET("/Search")
    Call<ItemBundle> getUser(@Query("term") String term, @Query("key") String key);
}
